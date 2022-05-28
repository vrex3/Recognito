package org.vrex.recognito.config;

public class ApplicationConstants {

    private ApplicationConstants() {
        //enforces singleton pattern
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
}
