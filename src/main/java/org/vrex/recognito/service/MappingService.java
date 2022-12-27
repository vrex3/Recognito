package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.ResourceAppMap;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.dto.ApplicationDTO;
import org.vrex.recognito.model.dto.RoleResourceMap;
import org.vrex.recognito.model.dto.RoleResourceMapping;
import org.vrex.recognito.repository.ApplicationRepository;
import org.vrex.recognito.repository.MappingRepository;
import org.vrex.recognito.repository.UserRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class MappingService {

    private static final String LOG_TEXT = "Mapping-Service : ";
    private static final String LOG_TEXT_ERROR = "Mapping-Service - Encountered Exception - ";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MappingRepository mappingRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Adds role-resource mappings for appUUID mentioned in request
     * Intended to be used only from stateless mapping controller
     * Request is presumed to be validated by controller
     *
     * @param request
     * @return
     */
    public ApplicationDTO addRoleResourceMapping(RoleResourceMapping request) {
        log.info("{} MAPPING CREATOR : Received role-resource mapping request for appUUID - {}", LOG_TEXT, request.getAppUUID());
        return mapResourcesToRoleForApp(applicationRepository.findApplicationByUUID(request.getAppUUID()), request);
    }

    /**
     * Adds role-resource mappings for appUUID linked to username
     * Intended to be used from stateful user mapping controller
     * Username is of logged in user
     *
     * @param username
     * @param request
     * @return
     */
    public ApplicationDTO addRoleResourceMapping(String username, RoleResourceMapping request) {
        log.info("{} MAPPING CREATOR : Received role-resource mapping request for app linked to user - {}", LOG_TEXT, username);
        return mapResourcesToRoleForApp(userRepository.getUserByName(username).getApplication(), request);
    }

    /**
     * Accepts an application and role->[resource] mapping
     * Creates new resource->role mapping schema entries
     * Modifies exisiting resource-app entries
     * i.e. adds Role to resource if this role now owns this resource
     * Throws BAD REQUEST if application does not exist or does not allow resource mapping
     *
     * @param application
     * @param request
     * @return
     */
    @Transactional
    private ApplicationDTO mapResourcesToRoleForApp(Application application, RoleResourceMapping request) {

        if (ObjectUtils.isEmpty(application)) {
            log.error("{} Unable to find application to register mappings", LOG_TEXT);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.APPLICATION_NOT_FOUND).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }

        ApplicationDTO response = null;
        String appUUID = application.getAppUUID();

        if (!application.isResourcesEnabled()) {
            log.error("{} Resource mapping for roles not allowed for appUUID {}", LOG_TEXT, appUUID);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.INVALID_APP_FOR_RESOURCES).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }

        try {
            request.setAppUUID(appUUID);
            log.info("{} MAPPING CREATOR : Received role-resource mapping request for appUUID - {}", LOG_TEXT, appUUID);
            List<ResourceAppMap> newMappings = new LinkedList<>();

            response = new ApplicationDTO(application);

            /**
             * resourceRoleMap : resource -> [role]  <--> built to input new role resource mappings into db
             * resourceDescriptionMap : resource -> [description]  <--> collects all descriptions of a resource,
             *                                                          if multiple provided for multiple roles.
             *
             */
            Map<String, Set<String>> resourceRoleMap = new HashMap<>();
            Map<String, Set<String>> resourceDescriptionMap = new HashMap<>();

            List<RoleResourceMap> mappings = request.getMappings();
            log.info("{} MAPPING CREATOR : Mapping roles to resources for appUUID - {}", LOG_TEXT, appUUID);

            if (mappings != null && mappings.size() > 0) {

                /**
                 * Parse the request the build resourceDescMap
                 * resourceDescMap : resource -> resource description
                 * The objective of the map is to reverse the mapping from the request (role -> [resource] to resource -> [role])
                 * This is done so as to match the schema (which is resource + appUUID -> [role]
                 * appUUID is constant throught this module
                 */
                mappings.stream().forEach(mapping -> {

                    Map<String, String> resourceDescMap = mapping.getResources();
                    String role = mapping.getRole();

                    Set<String> resources = resourceDescMap != null ? resourceDescMap.keySet() : new HashSet<>();
                    resources.stream().forEach(resource -> {
                        resourceRoleMap.putIfAbsent(resource, new HashSet<>());
                        resourceRoleMap.get(resource).add(role);

                        resourceDescriptionMap.putIfAbsent(resource, new HashSet<>());
                        resourceDescriptionMap.get(resource).add(resourceDescMap.get(resource).trim());
                    });
                });

                /**
                 * Fetch existing mappings (any if found) for appUUID and all provided resources
                 * Build list of ResourceIndex keeping appUUID constant
                 * Build resourceMapping : resourceID -> ResourceAppMap
                 * Use this map to look up for existing entity while parsing resourceRoleMap to build newMappings
                 * Add any modified ResourceAppMap to newMappings
                 * Update newMappings to DB
                 *
                 */

                Iterable<ResourceAppMap> iterableMappings = mappingRepository.findAllById(request.getResourceIndices());
                List<ResourceAppMap> existingMappings = iterableMappings != null ? StreamSupport.stream(iterableMappings.spliterator(), false)
                        .collect(Collectors.toList()) : new LinkedList<>();

                Map<String, ResourceAppMap> resourceMapping = new HashMap<>();
                existingMappings.stream().forEach(mapping -> resourceMapping.put(mapping.getId().getResourceId(), mapping));

                resourceRoleMap.forEach(
                        (resource, roles) -> {
                            ResourceAppMap currentMapping = resourceMapping.getOrDefault(resource, null);
                            if (currentMapping == null) {
                                currentMapping = new ResourceAppMap(
                                        application,
                                        resource,
                                        resourceDescriptionMap.get(resource).stream().collect(Collectors.joining(". ")),
                                        roles
                                );
                            } else {
                                currentMapping.addRoles(roles);
                            }
                            newMappings.add(currentMapping);
                        });

                log.info("{} MAPPING CREATOR : Mapped roles to resources for appUUID - {}", LOG_TEXT, appUUID);

            }


            if (newMappings.size() > 0) {
                log.info("{} MAPPING CREATOR : Updating mappings for appUUID - {} . [New mappings : {}]", LOG_TEXT, appUUID, newMappings.size());

                mappingRepository.saveAll(newMappings);
                response.setRoleMappings(getRoleResourceMappingForApplication(appUUID));
            } else {
                log.info("{} MAPPING CREATOR : No mappings to update for appUUID - {} . [New mappings : {}]", LOG_TEXT, appUUID);
            }
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("{} Encountered exception updating mappings", LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }

        return response;

    }

    /**
     * Fetches List of [role -> [resource]] mappings for a app linked to provided username
     *
     * @param username
     * @param selectedRoles
     * @return
     */
    public RoleResourceMapping getRoleResourceMappingForUser(String username, String... selectedRoles) {
        log.info("{} Fetching role-resource mappings for app linked to user - {}", LOG_TEXT, username);
        Application application = userRepository.getUserByName(username).getApplication();
        if (ObjectUtils.isEmpty(application)) {
            log.error("{} Unable to find application to fetch mappings", LOG_TEXT);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.APPLICATION_NOT_FOUND).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }
        return getRoleResourceMappingForApplication(application.getAppUUID(), selectedRoles);
    }

    /**
     * Fetches List of [role -> [resource]] mappings for a provided appUUID
     * If selectedRoles are provided then only resource-role mappings are returned for those roles
     * Otherwise all role mappings are returned
     *
     * @param appUUID
     * @param selectedRoles
     * @return
     */
    public RoleResourceMapping getRoleResourceMappingForApplication(String appUUID, String... selectedRoles) {
        log.info("{} Fetching role-resource mappings for appUUID - {}", LOG_TEXT, appUUID);

        Set<String> allowedRoles = selectedRoles.length > 0 ? new HashSet<>(Arrays.asList(selectedRoles)) : new HashSet<>();
        final boolean allRoles = allowedRoles.size() == 0;

        try {
            List<ResourceAppMap> mappings = mappingRepository.findByIdAppUUID(appUUID);
            RoleResourceMapping response = new RoleResourceMapping(appUUID);

            if (mappings != null && mappings.size() > 0) {
                log.info("{} Fetched role-resource mappings for appUUID - {}. [New mappings : {}]", LOG_TEXT, appUUID, mappings.size());

                /**
                 * role -> set of resources
                 */
                Map<String, Set<String>> roleResourceMap = new HashMap<>();


                mappings.stream().forEach(mapping -> {
                    String resource = mapping.getId().getResourceId();
                    Set<String> roles = mapping.getRoles();
                    roles.stream().forEach(role -> {
                        if (allRoles || allowedRoles.contains(role)) {
                            roleResourceMap.putIfAbsent(role, new HashSet<>());
                            roleResourceMap.get(role).add(resource);
                        }
                    });
                });

                roleResourceMap.forEach((role, resources) -> response.addMapping(role, resources));
                log.info("{} Mapped role-resource mappings for appUUID - {}. [New mappings : {}]", LOG_TEXT, appUUID, mappings.size());

            } else
                log.info("{} Fetched NO role-resource mappings for appUUID - {}", LOG_TEXT, appUUID);

            return response;
        } catch (Exception exception) {
            log.error("{} Encountered exception fetching mappings", LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }
    }

    /**
     * Checks whether role for appUUID owns resource or not
     *
     * @param role
     * @param resource
     * @param appUUID
     * @return
     */
    @Cacheable(cacheNames = ApplicationConstants.ROLE_RESOURCE_MAPPING_CACHE)
    public boolean doesRoleOwnResourceForApp(String role, String resource, String appUUID) {
        log.info("{} ROLE-RESOURCE MAP CHECKER : Checking if Role {} owns Resource {} for App {}",
                LOG_TEXT,
                role,
                resource,
                appUUID);

        ResourceAppMap mapping = mappingRepository.findByAppAndResource(appUUID, resource);
        boolean allowed = false;

        if (!ObjectUtils.isEmpty(mapping)) {
            log.info("{} ROLE-RESOURCE MAP CHECKER : Found mapping for Role {} owning Resource {} for App {}",
                    LOG_TEXT,
                    role,
                    resource,
                    appUUID);
            allowed = mapping.belongsToRole(role);
        } else {
            log.error("{} ROLE-RESOURCE MAP CHECKER : Found NO mapping for Role {} owning Resource {} for App {}",
                    LOG_TEXT,
                    role,
                    resource,
                    appUUID);
        }

        log.info("{} ROLE-RESOURCE MAP CHECKER RESULT: Role {} DOES{} own Resource {} for App {}",
                LOG_TEXT,
                role,
                allowed ? "" : " NOT",
                resource,
                appUUID);

        return allowed;
    }

}
