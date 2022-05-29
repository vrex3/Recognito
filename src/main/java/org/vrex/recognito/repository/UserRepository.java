package org.vrex.recognito.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.vrex.recognito.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
}
