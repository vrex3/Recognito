package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.ResourceAppMap;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.Message;
import org.vrex.recognito.model.dto.ApplicationDTO;
import org.vrex.recognito.model.dto.RoleResourceMap;
import org.vrex.recognito.model.dto.RoleResourceMapping;
import org.vrex.recognito.repository.ApplicationRepository;
import org.vrex.recognito.repository.MappingRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MappingService {

    private static final String LOG_TEXT = "Mapping-Service : ";
    private static final String LOG_TEXT_ERROR = "Mapping-Service - Encountered Exception - ";

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MappingRepository mappingRepository;

    /**
     * Accepts an appUUID and role->[resource] mapping
     * Creates new resource->role mapping schema entries
     * Throws BAD REQUEST if application does not exist or does not allow resource mapping
     *
     * @param request
     * @return
     */
    @Transactional
    public ApplicationDTO addRoleResourceMapping(RoleResourceMapping request) {

        ApplicationDTO response = null;
        String appUUID = request.getAppUUID();

        log.info("{} MAPPING CREATOR : Received role-resource mapping request for appUUID - {}", appUUID);

        Application application = applicationRepository.findApplicationByUUID(appUUID);
        List<ResourceAppMap> newMappings = new LinkedList<>();

        if (!ObjectUtils.isEmpty(application) && application.isResourcesEnabled()) {
            log.info("{} MAPPING CREATOR : Extracted app with appUUID - {}", appUUID);

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
            log.info("{} MAPPING CREATOR : Mapping roles to resources for appUUID - {}", appUUID);

            if (mappings != null && mappings.size() > 0) {

                mappings.stream().forEach(mapping -> {

                    /**
                     * resourceDescMap : resource -> resource description
                     */
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

                resourceRoleMap.forEach(
                        (resource, roles) -> newMappings.add(
                                new ResourceAppMap(
                                        application,
                                        resource,
                                        resourceDescriptionMap.get(resource).stream().collect(Collectors.joining(". ")),
                                        roles
                                )));

                log.info("{} MAPPING CREATOR : Mapped roles to resources for appUUID - {}", appUUID);

            }
        } else {
            log.error("{} MAPPING CREATOR : COULD NOT EXTRACT app with appUUID - {}", appUUID);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.APPLICATION_NOT_FOUND).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }

        if (newMappings.size() > 0) {
            log.info("{} MAPPING CREATOR : Updating mappings for appUUID - {} . [New mappings : {}]", appUUID, newMappings.size());

            mappingRepository.saveAll(newMappings);
            response.setRoleMappings(getRoleResourceMappingForApplication(appUUID));
        } else {
            log.info("{} MAPPING CREATOR : No mappings to update for appUUID - {} . [New mappings : {}]", appUUID);
        }

        return response;

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
        log.info("{} Fetching role-resource mappings for appUUID - {}", appUUID);

        Set<String> allowedRoles = selectedRoles.length > 0 ? new HashSet<>(Arrays.asList(selectedRoles)) : new HashSet<>();
        final boolean allRoles = allowedRoles.size()==0;

        List<ResourceAppMap> mappings = mappingRepository.findRoleResourceMappingsByAppUUID(appUUID);
        RoleResourceMapping response = new RoleResourceMapping(appUUID);

        if (mappings != null && mappings.size() > 0) {
            log.info("{} Fetched role-resource mappings for appUUID - {}. [New mappings : {}]", appUUID, mappings.size());

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
            log.info("{} Mapped role-resource mappings for appUUID - {}. [New mappings : {}]", appUUID, mappings.size());

        } else
            log.info("{} Fetched NO role-resource mappings for appUUID - {}", appUUID);

        return response;
    }

    public boolean doesRoleOwnResourceForApp(String role, String resource, String appUUID){
        ResourceAppMap mapping = mappingRepository.findByAppAndResource(appUUID,resource);
        boolean allowed = false;

        if(!ObjectUtils.isEmpty(mapping)){
            allowed = mapping.belongsToRole(role);
        }

        return allowed;
    }

}
