package org.vrex.recognito.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

public class ApplicationConstants {

    private ApplicationConstants() {
        //enforces singleton pattern
    }

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
     * RSA CONSTANTS
     */
    public static final String KEY_GENERATOR_INSTANCE = "RSA";
    public static final Integer KEY_GENERATOR_KEYSIZE = 2048;

    public static final String NO_KEYS_FOUND = "No keys found";
    public static final String FILE_WRITER_EXCEPTION = "Encountered error writing to file";

    public static final String PASSAY_SPL_CHAR_ERROR_CODE = "PassayError";

    public static final String RSA_SETUP_EXCEPTION = "Error in RSA Config : ";

    /**
     * Error messages
     */
    public static final String INVALID_TOKEN_SIGNATURE = "Token signature could not be parsed or is invalid";
    public static final String INVALID_TOKEN_PAYLOAD = "Token payload could not be parsed or is invalid";

    public static final String EMPTY_APPLICATION_NAME = "Application name cannot be empty";
    public static final String EMPTY_APPLICATION_IDENTIFIER = "Either app name or UUID needs to be provided";


}
