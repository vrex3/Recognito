package org.vrex.recognito.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
public class RsaConfig {

    @Bean
    public KeyPairGenerator keyPairGenerator() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance(ApplicationConstants.KEY_GENERATOR_INSTANCE);
            generator.initialize(ApplicationConstants.KEY_GENERATOR_KEYSIZE);
        } catch (NoSuchAlgorithmException exception) {
            log.error(ApplicationConstants.RSA_SETUP_EXCEPTION + "Key Pair Generator", exception);
        }
        return generator;
    }

    @Bean
    public KeyFactory keyFactory() {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(ApplicationConstants.KEY_GENERATOR_INSTANCE);
        } catch (NoSuchAlgorithmException exception) {
            log.error(ApplicationConstants.RSA_SETUP_EXCEPTION + "Key Factory", exception);
        }
        return keyFactory;
    }

    @Bean
    public Cipher cipher() {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ApplicationConstants.KEY_GENERATOR_INSTANCE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException exception) {
            log.error(ApplicationConstants.RSA_SETUP_EXCEPTION + "Cipher", exception);
        }
        return cipher;
    }

}
