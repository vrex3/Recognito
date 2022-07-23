package org.vrex.recognito.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.ApplicationException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
    private KeyGenerator aesKeyGenerator;

    @Autowired
    private KeyFactory keyFactory;

    /**
     * Generates a strong 256 bit AES key
     *
     * @return
     */
    public SecretKey generateAesSecretKey() {
        log.info("{} Generating AES Key", LOG_TEXT);
        try {
            return aesKeyGenerator.generateKey();
        } catch (Exception exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    /**
     * Generates a 2048 bit signed RSA key pair
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
    public PrivateKey extractPrivateKey(Application application) throws InvalidKeySpecException {
        log.info("{} Extracting private key for application {}", LOG_TEXT, application.getName());
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(application.getPrivateKey()));
    }

    /**
     * Extracts public key for an application
     *
     * @param application
     * @return
     * @throws InvalidKeySpecException
     */
    public PublicKey extractPublicKey(Application application) throws InvalidKeySpecException {
        log.info("{} Extracting public key for application {}", LOG_TEXT, application.getName());
        return keyFactory.generatePublic(new X509EncodedKeySpec(application.getPublicKey()));
    }
}
