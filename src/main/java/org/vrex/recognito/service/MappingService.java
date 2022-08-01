package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.ResourceAppMap;
import org.vrex.recognito.model.Message;
import org.vrex.recognito.model.dto.ApplicationDTO;
import org.vrex.recognito.model.dto.RoleResourceMap;
import org.vrex.recognito.model.dto.RoleResourceMapping;
import org.vrex.recognito.repository.ApplicationRepository;
import org.vrex.recognito.repository.MappingRepository;

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

    @Transactional
    public ApplicationDTO addRoleResourceMapping(RoleResourceMapping request) {

        ApplicationDTO response = null;
        String appUUID = request.getAppUUID();


        Application application = applicationRepository.findApplicationByUUID(appUUID);
        List<ResourceAppMap> newMappings = new LinkedList<>();

        if (!ObjectUtils.isEmpty(application) && application.isResourcesEnabled()) {

            response = new ApplicationDTO(application);

            Map<String, Set<String>> resourceRoleMap = new HashMap<>();
            Map<String, Set<String>> resourceDescriptionMap = new HashMap<>();

            List<RoleResourceMap> mappings = request.getMappings();
            if (mappings != null && mappings.size() > 0) {

                mappings.stream().forEach(mapping -> {
                    String role = mapping.getRole();
                    Map<String, String> resourceDescMap = mapping.getResources();
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

            }
        }

        if (newMappings.size() > 0) {
            mappingRepository.saveAll(newMappings);
            response.setRoleMappings(getRoleResourceMappingForApplication(appUUID));
        }

        return response;

    }

    public RoleResourceMapping getRoleResourceMappingForApplication(String appUUID) {
        List<ResourceAppMap> mappings = mappingRepository.findRoleResourceMappingsByAppUUID(appUUID);
        RoleResourceMapping response = new RoleResourceMapping(appUUID);

        if (mappings != null && mappings.size() > 0) {

            /**
             * role -> set of resources
             */
            Map<String, Set<String>> roleResourceMap = new HashMap<>();


            mappings.stream().forEach(mapping -> {
                String resource = mapping.getId().getResourceId();
                Set<String> roles = mapping.getRoles();
                roles.stream().forEach(role -> {
                    roleResourceMap.putIfAbsent(role, new HashSet<>());
                    roleResourceMap.get(role).add(resource);
                });
            });

            roleResourceMap.forEach((role, resources) -> response.addMapping(role, resources));
        }

        return response;
    }


}
