package org.vrex.recognito.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
@Configuration
public class CryptoConfig {

    @Bean
    public KeyGenerator aesKeyGenerator() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(ApplicationConstants.USER_KEY_GENERATOR_INSTANCE);
            keyGenerator.init(ApplicationConstants.USER_KEY_GENERATOR_KEYSIZE, SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException exception) {
            log.error(ApplicationConstants.CRYPTO_SETUP_EXCEPTION + "AES Key Generator", exception);
        }
        return keyGenerator;
    }

    @Bean
    public KeyPairGenerator keyPairGenerator() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(ApplicationConstants.KEY_GENERATOR_INSTANCE);
            generator.initialize(ApplicationConstants.KEY_GENERATOR_KEYSIZE);
        } catch (NoSuchAlgorithmException exception) {
            log.error(ApplicationConstants.CRYPTO_SETUP_EXCEPTION + "RSA Key Pair Generator", exception);
        }
        return generator;
    }

    @Bean
    public KeyFactory keyFactory() {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(ApplicationConstants.KEY_GENERATOR_INSTANCE);
        } catch (NoSuchAlgorithmException exception) {
            log.error(ApplicationConstants.CRYPTO_SETUP_EXCEPTION + "Key Factory", exception);
        }
        return keyFactory;
    }

    @Bean
    public Cipher cipher() {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ApplicationConstants.KEY_GENERATOR_INSTANCE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException exception) {
            log.error(ApplicationConstants.CRYPTO_SETUP_EXCEPTION + "Cipher", exception);
        }
        return cipher;
    }

}
