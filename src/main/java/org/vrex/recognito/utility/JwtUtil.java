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
import org.apache.commons.lang3.ObjectUtils;
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

    private static final String LOG_TEXT = "JWT-Util : Token Util - ";
    private static final String LOG_TEXT_ERROR = "JWT-Util - Token Util - Encountered Exception - ";

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
     * Generates JWT signed by user's application's keys
     * Extracts application from user entity
     * Token signed by application RSA private key
     * Token encrypted by application RSA public key
     * Token encoded in wrapper containing appUUID and jsonified
     *
     * @param user
     * @return
     */
    public String generateToken(User user) {
        String username = user.getUsername();
        log.info("{} Generating token for user {}", LOG_TEXT, username);

        String token = null;
        try {
            Application application = user.getApplication();

            if (ObjectUtils.isEmpty(application)) {
                log.error("{} No application found linked for user {}", LOG_TEXT_ERROR, username);
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.UNLINKED_USER).
                        status(HttpStatus.BAD_REQUEST).
                        build();
            }

            String appName = application.getName();

            log.info("{} Found application for user {} - {}", LOG_TEXT, username, appName);

            //is keyId needed here ?
            // https://connect2id.com/products/nimbus-jose-jwt/examples/signed-and-encrypted-jwt
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.
                            Builder(JWSAlgorithm.RS256).
                            build(),
                    generateClaims(user));

            log.info("{} Signing token with app private key for user {} - {}", LOG_TEXT, username, appName);

            signedJWT.sign(new RSASSASigner(keyUtil.extractPrivateKey(application)));

            JWEObject tokenWrapper = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                            .contentType("JWT") // required to indicate nested JWT
                            .build(),
                    new Payload(signedJWT));

            log.info("{} Encrypting token with app public key for user {} - {}", LOG_TEXT, username, appName);

            tokenWrapper.encrypt(new RSAEncrypter(keyUtil.extractPublicKey(application)));

            log.info("{} Encoding token with appUUID information for user {} - [{}:{}]", LOG_TEXT, username, application.getAppUUID(), appName);
            token = gson.toJson(new UserToken(application, tokenWrapper));

        } catch (InvalidKeySpecException exception) {
            log.error("{} Encountered InvalidKeySpecException generating token for user {}", LOG_TEXT_ERROR, username, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + "Generating jwt - InvalidKeySpecException : " + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("{} Encountered Exception generating token for user {}", LOG_TEXT_ERROR, username, exception);
            throw ApplicationException.builder()
                    .errorMessage(LOG_TEXT_ERROR + exception.getMessage())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        log.debug("{} Generated token for user {}", LOG_TEXT, username);
        return token;
    }

    /**
     * Extracts information from encoded token
     * Extracts appUUID - uses this to fetch app RSA keys
     * Throws 401 if app cannot be found
     * Decrypts token with app private key
     * Verifies signature with app public key
     * Populates payload with token claims information
     * Returns payload
     * <p>
     * TBA : User data verification?
     *
     * @param encodedToken
     * @return
     */
    public TokenPayload extractPayload(String encodedToken) {
        log.info("{} Extracting user info from token", LOG_TEXT);

        TokenPayload payload = new TokenPayload();
        String appId = null;

        try {

            log.info("{} Un-wrapping token", LOG_TEXT);
            UserToken userToken = gson.fromJson(encodedToken, UserToken.class);
            appId = userToken.getAppId();
            String token = userToken.getToken();

            log.info("{} Extracted token and appUUID - {}", LOG_TEXT, appId);
            log.info("{} Extracting application details - {}", LOG_TEXT, appId);

            Application application = applicationRepository.findApplicationByUUID(appId);

            if (ObjectUtils.isEmpty(application)) {
                log.error("{} Could not verify application identifier encoded in token - {}", appId);
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.UNVERIFIED_APPLICATION_IN_TOKEN).
                        status(HttpStatus.UNAUTHORIZED).
                        build();
            }

            log.info("{} Extracted application details - {}", LOG_TEXT, appId);

            JWEObject jweObject = JWEObject.parse(token);

            log.info("{} Decrypting token with private key for app - {}", LOG_TEXT, appId);
            jweObject.decrypt(new RSADecrypter(keyUtil.extractPrivateKey(application)));

            log.info("{} Extracting payload for token for app - {}", LOG_TEXT, appId);
            SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();

            log.info("{} Verifying token signature with public key for app - {}", LOG_TEXT, appId);
            if (!signedJWT.verify(new RSASSAVerifier(keyUtil.extractPublicKey(application))))
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.INVALID_TOKEN_SIGNATURE).
                        status(HttpStatus.UNAUTHORIZED).
                        build();

            log.info("{} Token verified for app - {}", LOG_TEXT, appId);

            log.info("{} Populating payload for app - {}", LOG_TEXT, appId);
            payload.populatePayload(signedJWT.getJWTClaimsSet());


        } catch (ApplicationException exception) {
            throw exception;
        } catch (JsonSyntaxException exception) {
            log.error("{} Encountered JSON Token parsing exception decoding token for appID {} : {}", LOG_TEXT_ERROR, appId, exception.getMessage(), exception);
            throw ApplicationException.builder()
                    .errorMessage(exception.getMessage())
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        } catch (InvalidKeySpecException exception) {
            log.error("{} Encountered InvalidKeySpecException decoding token for appID {} : {}", LOG_TEXT_ERROR, appId, exception.getMessage(), exception);
            throw ApplicationException.builder()
                    .errorMessage(ApplicationConstants.INVALID_TOKEN_PAYLOAD)
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        } catch (JOSEException exception) {
            log.error("{} Encountered JOSEException decoding token signtaure for appID {} : {}", LOG_TEXT_ERROR, appId, exception.getMessage(), exception);
            throw ApplicationException.builder()
                    .errorMessage(ApplicationConstants.INVALID_TOKEN_SIGNATURE)
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        } catch (ParseException exception) {
            log.error("{} Encountered ParseException decoding token for appID {} : {}", LOG_TEXT_ERROR, appId, exception.getMessage(), exception);
            throw ApplicationException.builder()
                    .errorMessage(ApplicationConstants.INVALID_TOKEN_PAYLOAD)
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        log.info("{} Payload populated for app - {}", LOG_TEXT, appId);

        return payload;
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
}
