package org.vrex.recognito.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.ApplicationException;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

@Slf4j
@Component
public class JwtUtil {

    private static final String LOG_TEXT = "JWT-Util : ";
    private static final String LOG_TEXT_ERROR = "JWT-Util - Encountered Exception - ";

    private static final String ISSUER = "issuer";
    private static final String SUBJECT = "subject";
    private static final String EMAIL = "email";
    private static final String PROFILE_VERSION = "profileVersion";
    private static final String JWT_ISSUER = "recognito";
    private static final Integer JWT_LIFESPAN_MINUTES = 30;

    @Autowired
    private KeyUtil keyUtil;

    /**
     * Generates JWT signed by user's private key
     *
     * @param user
     * @return
     */
    private String generateToken(User user) {
        log.debug("{} Generating token for user {}", LOG_TEXT, user.getUsername());

        String token = null;

        try {
            token = Jwts.builder().
                    setClaims(generateClaims(user)).
                    signWith(SignatureAlgorithm.HS512, keyUtil.extractPrivateKey(user)).
                    compact();
            log.debug("{} Generated token for user {}", LOG_TEXT, user.getUsername());

        } catch (InvalidKeySpecException exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + "InvalidKeySpecException : " + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (Exception exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        return token;
    }

    /**
     * Creates claims with respect to a provided user entity
     *
     * @param user
     * @return
     */
    private Claims generateClaims(User user) {
        log.debug("{} Generating claims for user {}", LOG_TEXT, user.getUsername());

        Claims claims = Jwts.claims();

        claims.put(ISSUER, JWT_ISSUER);
        claims.put(SUBJECT, user.getUsername());
        claims.put(EMAIL, user.getEmail());
        claims.put(PROFILE_VERSION, user.getVersion());

        return claims;
    }
}
