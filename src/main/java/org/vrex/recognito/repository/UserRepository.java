package org.vrex.recognito.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.vrex.recognito.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    @Query("{application.$appUUID:'?0'}")
    public List<User> getUsersForAppUUID(String appUUID);

    @Query("{application.$appName:'?0'}")
    public List<User> getUserForAppName(String appName);
}
