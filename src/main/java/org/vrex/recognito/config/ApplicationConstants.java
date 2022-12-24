package org.vrex.recognito.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class ApplicationConstants {

    private ApplicationConstants() {
        //enforces singleton pattern
    }

    /**
     * GENERIC CONSTANTS
     */
    public static final String ROOT_USER = "root_";

    /**
     * DateTime CONSTANTS
     */
    public static final String DEFAULT_TIMEZONE = "UTC";

    /**
     * Returns the current time in UTC
     *
     * @return
     */
    public static final LocalDateTime currentTime() {
        return LocalDateTime.now(ZoneId.of(ApplicationConstants.DEFAULT_TIMEZONE));
    }

    /**
     * Returns the current date in UTC
     *
     * @return
     */
    public static final Date currentDate() {
        return Date.from(currentTime().toInstant(ZoneOffset.UTC));
    }

    /**
     * CACHE CONSTANTS
     */
    public static final String USER_PROFILE_CACHE = "users";
    public static final String APPLICATION_CACHE = "apps";
    public static final String ROLE_RESOURCE_MAPPING_CACHE = "roles";
    public static final String USER_CREDENTIALS_CACHE = "credentials";


    public static final Integer INITIAL_CAPACITY = 10;
    public static final Integer MAX_CAPACITY = 20;
    public static final Integer EXPIRE_AFTER_WRITE_IN_MINUTES = 5;

    /**
     * CRYPTO CONSTANTS
     */
    public static final String USER_KEY_GENERATOR_INSTANCE = "AES";
    public static final Integer USER_KEY_GENERATOR_KEYSIZE = 256;


    public static final String KEY_GENERATOR_INSTANCE = "RSA";
    public static final Integer KEY_GENERATOR_KEYSIZE = 2048;

    public static final String NO_KEYS_FOUND = "No keys found";
    public static final String FILE_WRITER_EXCEPTION = "Encountered error writing to file";

    public static final String PASSAY_SPL_CHAR_ERROR_CODE = "PassayError";

    public static final String CRYPTO_SETUP_EXCEPTION = "Error in Crypto Config : ";

    public static final String APP_CRYPTO_EXCEPTION = "Encountered error setting up keys and secrets for app";

    /**
     * Error messages
     */
    public static final String EMPTY_TOKEN = "No token found";
    public static final String EMPTY_TOKEN_PAYLOAD = "No token payload found";
    public static final String INVALID_TOKEN_SIGNATURE = "Token signature could not be parsed or is invalid";
    public static final String INVALID_TOKEN_PAYLOAD = "Token payload could not be parsed or is invalid";
    public static final String INVALID_TOKEN_ISSUER = "Token issuer could not be recognized";
    public static final String EXPIRED_TOKEN = "Token is no longer valid";

    public static final String EMPTY_APPLICATION_REQUEST = "Empty application upsert request";
    public static final String EMPTY_APPLICATION_NAME = "Application name cannot be empty";
    public static final String EMPTY_APPLICATION_IDENTIFIER = "Either app name or UUID needs to be provided";
    public static final String APPLICATION_NOT_FOUND = "Application could not be found";
    public static final String UNVERIFIED_APPLICATION_IN_TOKEN = "Application information in token could not be verified";
    public static final String EMPTY_APP_INVITE = "You must be invited to join the application";

    public static final String EMPTY_USERNAME = "Username cannot be empty";
    public static final String EMPTY_USER_CREATION_REQUEST = "Empty user creation payload";
    public static final String DUPLICATE_USER = "Username is already taken";
    public static final String INVALID_USER = "User does not exist";
    public static final String UNLINKED_USER = "User is not mapped to any application.";

    public static final String INVALID_ROLE = "User authority cannot be recognized";
    public static final String INVALID_CLIENT_ROLE = "Client role is not recognized";
    public static final String INVALID_RESOURCE = "Resource string must be unique for role, and no more than 15 characters";
    public static final String SYSTEM_ROLE_RESOURCE_MAPPING_ATTEMPT = "System roles cannot have mapped resources.";
    public static final String INVALID_APP_FOR_RESOURCES = "Application does not allow resource creation for it's roles";
    public static final String ROLE_NOT_ALLOWED_RESOURCE = "User role is not allowed permission to access resource. Please provide valid resource ID";
}
