package org.vrex.recognito.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.vrex.recognito.entity.ResourceAppMap;
import org.vrex.recognito.entity.ResourceIndex;

@Repository
public interface MappingRepository extends MongoRepository<ResourceAppMap, ResourceIndex> {

    @Query("{resourceId.$res_identifier:?0}")
    public ResourceAppMap findByResourceString(String resource);
}
