package org.vrex.recognito.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.ApplicationException;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
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
     * Extract private key for an application
     *
     * @param application
     * @return
     * @throws InvalidKeySpecException
     */
    public RSAPrivateKey extractPrivateKey(Application application) throws InvalidKeySpecException {
        log.info("{} Extracting private key for application {}", LOG_TEXT, application.getName());
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(application.readPrivateKey()));
    }

    /**
     * Extracts public key for an application
     *
     * @param application
     * @return
     * @throws InvalidKeySpecException
     */
    public RSAPublicKey extractPublicKey(Application application) throws InvalidKeySpecException {
        log.info("{} Extracting public key for application {}", LOG_TEXT, application.getName());
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(application.readPublicKey()));
    }
}
