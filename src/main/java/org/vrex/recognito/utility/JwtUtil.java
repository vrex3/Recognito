package org.vrex.recognito.utility;

import com.google.gson.JsonSyntaxException;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.TokenPayload;
import org.vrex.recognito.model.UserToken;

import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.vrex.recognito.repository.ApplicationRepository;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class JwtUtil {

    private static final String LOG_TEXT = "JWT-Util : ";
    private static final String LOG_TEXT_ERROR = "JWT-Util - Encountered Exception - ";

    public static final String EMAIL = "email";
    public static final String PROFILE_VERSION = "profileVersion";
    private static final String JWT_ISSUER = "recognito";
    private static final Integer JWT_LIFESPAN_MINUTES = 30;

    @Autowired
    private KeyUtil keyUtil;

    @Autowired
    private ApplicationRepository applicationRepository;

    private Gson gson;

    @PostConstruct
    public void setup() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

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
            Application application = user.getApplication();

            //is keyId needed here ?
            // https://connect2id.com/products/nimbus-jose-jwt/examples/signed-and-encrypted-jwt
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                    generateClaims(user));

            signedJWT.sign(new RSASSASigner(keyUtil.extractPrivateKey(application)));

            JWEObject tokenWrapper = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                            .contentType("JWT") // required to indicate nested JWT
                            .build(),
                    new Payload(signedJWT));

            tokenWrapper.encrypt(new RSAEncrypter(keyUtil.extractPublicKey(application)));

            // encoding token as json with appId info
            token = gson.toJson(new UserToken(application, tokenWrapper));

            log.debug("{} Generated token for user {}", LOG_TEXT, user.getUsername());

        } catch (InvalidKeySpecException exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + "Generating jwt - InvalidKeySpecException : " + exception.getMessage())
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
    private JWTClaimsSet generateClaims(User user) {
        log.debug("{} Generating claims for user {}", LOG_TEXT, user.getUsername());

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder();

        claims.issuer(JWT_ISSUER);
        claims.subject(user.getUsername());
        claims.jwtID(UUID.randomUUID().toString());

        claims.claim(EMAIL, user.getEmail());
        claims.claim(PROFILE_VERSION, user.getVersion());


        Date tokenIssuedAt = ApplicationConstants.currentDate();
        claims.issueTime(tokenIssuedAt);
        claims.notBeforeTime(tokenIssuedAt);
        claims.expirationTime(DateUtils.addMinutes(tokenIssuedAt, JWT_LIFESPAN_MINUTES));

        return claims.build();
    }

    private TokenPayload extractPayload(String encodedToken) {
        TokenPayload payload = new TokenPayload();
        try {

            UserToken userToken = gson.fromJson(encodedToken, UserToken.class);
            String appId = userToken.getAppId();
            String token = userToken.getToken();

            Application application = applicationRepository.findApplicationByUUID(appId);

            JWEObject jweObject = JWEObject.parse(token);

// Decrypt with private key
            jweObject.decrypt(new RSADecrypter(keyUtil.extractPrivateKey(application)));

// Extract payload
            SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();

            if (!signedJWT.verify(new RSASSAVerifier(keyUtil.extractPublicKey(application))))
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.INVALID_TOKEN_SIGNATURE).
                        status(HttpStatus.UNAUTHORIZED).
                        build();

            payload.populatePayload(signedJWT.getJWTClaimsSet());


        } catch (ApplicationException exception) {
            log.error(LOG_TEXT_ERROR + "Parsing jwt - Invalid Signature/Payload : " + exception.getMessage(), exception);
            throw exception;
        } catch (JsonSyntaxException exception) {
            log.error(LOG_TEXT_ERROR + "Parsing jwt - Invalid token JSON syntax : " + exception.getMessage(), exception);
            throw exception;
        } catch (InvalidKeySpecException exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + "Parsing jwt - InvalidKeySpecException : " + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (JOSEException exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + "Parsing jwt - JOSEException : " + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (ParseException exception) {
            log.error(LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + "Parsing jwt - ParseException : " + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        return payload;
    }
}
