package org.vrex.recognito.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.vrex.recognito.model.ApplicationException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * RSA Key Util
 */

@Slf4j
@Component
public class KeyUtil {

    private static final String LOG_TEXT = "Key-Util : ";
    private static final String LOG_TEXT_ERROR = "Key-Util - Encountered Exception - ";

    @Autowired
    private KeyPairGenerator keyPairGenerator;


    /**
     * Generates a RSA key pair
     *
     * @return
     */
    public KeyPair generateKeyPair() {
        log.info("{} Generating Key Pair", LOG_TEXT);
        try {
            return keyPairGenerator.generateKeyPair();
        } catch (Exception exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
