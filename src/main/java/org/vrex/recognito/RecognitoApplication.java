package org.vrex.recognito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@SpringBootApplication
public class RecognitoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecognitoApplication.class, args);
    }

}
