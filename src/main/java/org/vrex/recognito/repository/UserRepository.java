package org.vrex.recognito.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.User;

import java.util.List;

@Repository
@SuppressWarnings("unused")
public interface UserRepository extends MongoRepository<User, String> {

    @Query("{userName:?0}")
    @Cacheable(cacheNames = ApplicationConstants.USER_PROFILE_CACHE,
            unless = "#result!=null")
    public User getUserByName(String username);

    @Query("{application.$id:?0}")
    public List<User> getUserForAppName(String appName);

    @Query(value = "{userName: ?0}", count = true)
    public long countUser(String username);

    /**
     * Checks whether a username is existing or not
     *
     * @param username
     * @return
     */
    default boolean existingUser(String username) {
        return username != null ? countUser(username) > 0 : false;
    }
}
