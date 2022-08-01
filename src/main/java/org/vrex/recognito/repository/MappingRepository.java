package org.vrex.recognito.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.vrex.recognito.entity.ResourceAppMap;
import org.vrex.recognito.entity.ResourceIndex;

import java.util.List;
import java.util.Optional;

@Repository
public interface MappingRepository extends MongoRepository<ResourceAppMap, ResourceIndex> {

    @Query("{resourceId.$res_identifier:?0}")
    public ResourceAppMap findByResourceString(String resource);

    @Query("{resourceId.$appUUID:?0}")
    public List<ResourceAppMap> findRoleResourceMappingsByAppUUID(String appUUID);

    public Optional<ResourceAppMap> findById(ResourceIndex index);

    /**
     * Uses findById to return mapping with all allowed roles for an app and resource
     *
     * @param appUUID
     * @param resource
     * @return
     */
    default ResourceAppMap findByAppAndResource(String appUUID, String resource) {
        Optional<ResourceAppMap> optionalMapping = findById(new ResourceIndex(appUUID, resource));
        return optionalMapping.isPresent() ? optionalMapping.get() : null;
    }
}
