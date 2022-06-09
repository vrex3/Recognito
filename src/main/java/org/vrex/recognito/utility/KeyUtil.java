package org.vrex.recognito.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.ApplicationException;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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

    @Autowired
    private KeyFactory keyFactory;


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
                    .errorMessage(LOG_TEXT_ERROR + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Extract private key for user
     *
     * @param user
     * @return
     * @throws InvalidKeySpecException
     */
    public PrivateKey extractPrivateKey(User user) throws InvalidKeySpecException {
        log.info("{} Extracting private key for user {}", LOG_TEXT, user.getUsername());
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(user.readPrivateKey()));
    }

    /**
     * Extracts public key for user
     *
     * @param user
     * @return
     * @throws InvalidKeySpecException
     */
    public PublicKey extractPublicKey(User user) throws InvalidKeySpecException {
        log.info("{} Extracting public key for user {}", LOG_TEXT, user.getUsername());
        return keyFactory.generatePublic(new X509EncodedKeySpec(user.readPublicKey()));
    }
}
