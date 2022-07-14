package org.vrex.recognito.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.vrex.recognito.entity.Application;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    @Query("{appUUID:'?0'}")
    public Application findApplicationByUUID(String UUID);

    @Query("{appName:'?0'}")
    public Application findApplicationByName(String name);
}
