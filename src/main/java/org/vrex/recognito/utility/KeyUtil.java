package org.vrex.recognito.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.ApplicationException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
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
import java.util.Base64;

/**
 * RSA Key Util
 */

@Slf4j
@Component
public class KeyUtil {

    private static final String LOG_TEXT = "Key-Util : ";
    private static final String LOG_TEXT_ERROR = "Key-Util - Encountered Exception - ";

    @Autowired
    private Cipher cipher;

    @Autowired
    private KeyPairGenerator keyPairGenerator;

    @Autowired
    private KeyGenerator aesKeyGenerator;

    @Autowired
    private KeyFactory keyFactory;

    /**
     * Generates an unique user secret
     * BASE 64 encoded version of a 256 bit strong AES key
     *
     * @return
     */
    public String generateUserSecret() {
        log.info("{} Generating User Secret", LOG_TEXT);
        return Base64.getEncoder().encodeToString(generateAesSecretKey().getEncoded());
    }

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
                    .errorMessage(ApplicationConstants.APP_CRYPTO_EXCEPTION)
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
                    .errorMessage(ApplicationConstants.APP_CRYPTO_EXCEPTION)
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

    /**
     * Populates application RSA attributes
     * Creates key pair and stores RSA public and provate keys as byte streams for app
     * Generates invite secret for app -> public key encrypted version of appUUID
     * Invite secret must be provided by user while user creation and after decryption must match appUUID
     *
     * @param application
     */
    public void populateApplicationSecrets(Application application) {
        String appName = application.getName();
        log.info("{} Populating keys and secrets for application {}", LOG_TEXT, appName);

        KeyPair keyPair = generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        application.setPublicKey(publicKey.getEncoded());
        application.setPrivateKey(privateKey.getEncoded());

        log.info("{} RSA keys set for application {}", LOG_TEXT, appName);

        String encodedSecret = null;

        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encodedSecret = Base64.getEncoder().
                    encodeToString(cipher.doFinal(application.getAppUUID().getBytes(StandardCharsets.UTF_8)));

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException exception) {
            log.error("{} Encountered java crypto exception setting up invite secret for app {}", LOG_TEXT_ERROR, appName, exception);
            throw ApplicationException.builder()
                    .errorMessage(exception.getMessage())
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .build();
        } catch (Exception exception) {
            log.error("{} Encountered exception setting up invite secret for app {}", LOG_TEXT_ERROR, appName, exception);
            throw ApplicationException.builder()
                    .errorMessage(ApplicationConstants.APP_CRYPTO_EXCEPTION)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        application.setAppSecret(encodedSecret);

        log.info("{} Invite secret set for application {}", LOG_TEXT, application.getName());
    }

    /**
     * Verifies whether the secret invite passed by a user
     * matches the application UUID user is trying to associate to.
     * Secret invite after decryption by app private key must match appUUID and not be null.
     *
     * @param secret
     * @param application
     * @return
     */
    public boolean verifyApplicationSecret(String secret, Application application) {
        String appName = application.getName();
        log.info("{} Verifying invite secret for application {}", LOG_TEXT, appName);
        String decodedString = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, extractPrivateKey(application));
            decodedString = new String(cipher.doFinal(Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException exception) {
            log.error("{} Encountered java crypto exception verifying invite secret for app {}", LOG_TEXT_ERROR, appName, exception);
            throw ApplicationException.builder()
                    .errorMessage(exception.getMessage())
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .build();
        } catch (Exception exception) {
            log.error("{} Encountered exception verifying invite secret for app {}", LOG_TEXT_ERROR, appName, exception);
            throw ApplicationException.builder()
                    .errorMessage(ApplicationConstants.APP_CRYPTO_EXCEPTION)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        log.info("{} Decoded and checking invite secret for application {}", LOG_TEXT, appName);
        return decodedString != null && decodedString.equals(application.getAppUUID());
    }

}
