package org.vrex.recognito.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.vrex.recognito.entity.Application;

@Repository
@SuppressWarnings("unused")
public interface ApplicationRepository extends MongoRepository<Application, String> {

    @Query("{'appUUID':?0}")
    public Application findApplicationByUUID(String UUID);

    @Query("{'appName':?0}")
    public Application findApplicationByName(String name);

    @Query("{'$or':[ {'appUUID':?0}, {'appName':?0} ]}")
    public Application findApplicationByUUIDorName(String identifier);

    @Query(value = "{'appName': ?0}", count = true)
    public long countApplication(String appName);

    /**
     * Checks whether an application is existing or not
     *
     * @param appName
     * @return
     */
    default boolean existingApplication(String appName) {
        return appName != null ? countApplication(appName) > 0 : false;
    }
}
