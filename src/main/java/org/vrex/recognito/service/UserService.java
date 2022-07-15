package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.User;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.Message;
import org.vrex.recognito.model.dto.ApplicationIdentifier;
import org.vrex.recognito.model.dto.ApplicationUserListDTO;
import org.vrex.recognito.model.dto.InsertUserRequest;
import org.vrex.recognito.repository.ApplicationRepository;
import org.vrex.recognito.repository.UserRepository;
import org.vrex.recognito.utility.KeyUtil;

import java.util.Base64;

@Slf4j
@Service
public class UserService {

    private static final String LOG_TEXT = "User-Service : ";
    private static final String LOG_TEXT_ERROR = "User-Service - Encountered Exception - ";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private KeyUtil keyUtil;

    /**
     * Accepts an UNIQUE username, existing application (UUID or name)
     * Generates secret for user
     * Persists user
     * Returns user details back to user
     * <p>
     * **TBA**
     * <p>
     * Email validation
     * * Generate secret key only if email is validated
     * * Send key across in email
     * Validation Repository
     * One Time Code cleanup
     *
     * @param request
     * @return
     */
    public ResponseEntity<?> createUser(InsertUserRequest request) {
        String username = request != null ? request.getUsername() : null;
        String appIdentifier = request != null ? request.getAppIdentifier() : null;

        if (ObjectUtils.isEmpty(request)) {
            log.error("{} Empty user creation request", LOG_TEXT_ERROR);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.EMPTY_USER_CREATION_REQUEST).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        } else if (userRepository.existingUser(username)) {
            log.error("{} Username is already taken - {}", LOG_TEXT_ERROR, username);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.DUPLICATE_USER).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        } else {
            log.info("{} Building and persisting user {} for application {}", LOG_TEXT, username, appIdentifier);
            try {
                User user = userRepository.save(buildUserEntity(request));
                log.info("{} Created user {} for application {}", LOG_TEXT, username, appIdentifier);

                return new ResponseEntity<>(Message.builder().data(user).build(), HttpStatus.OK);

            } catch (ApplicationException exception) {
                throw exception;
            } catch (Exception exception) {
                log.error("{} Creating User {} for application {} ", LOG_TEXT_ERROR, username, appIdentifier, exception);
                throw ApplicationException.builder().
                        errorMessage(exception.getMessage()).
                        status(HttpStatus.INTERNAL_SERVER_ERROR).
                        build();
            }
        }
    }

    /**
     * Fetches all users for an application
     * AppUUID preffered to appName (IF BOTH PROVIDED)
     * Fetches users for by appUUID if UUID provided
     * Otherwise fetches by appName
     * Hides user secret
     *
     * @param appId
     * @return
     */
    public ResponseEntity<?> getUsersForApplication(ApplicationIdentifier appId) {

        boolean id = StringUtils.isEmpty(appId.getAppUUID()) ? false : true;
        String identifier = id ? appId.getAppUUID() : appId.getAppName();
        String logIdentifier = id ? "UUID" : "name";

        log.info("{} Fetching users for application with {} - {}", LOG_TEXT, logIdentifier, identifier);

        try {
            return new ResponseEntity<>(Message.builder().
                    data(new ApplicationUserListDTO(identifier, id ?
                            userRepository.getUsersForAppUUID(identifier) :
                            userRepository.getUserForAppName(identifier))).
                    build(),
                    HttpStatus.OK);

        } catch (Exception exception) {
            log.error("{} Fetching users for application with {} - {}", LOG_TEXT_ERROR, logIdentifier, identifier, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }
    }

    /**
     * Builds a user entity from an insert user request
     * Generates AES secret key and BASE64 encodes it for user to use as secret
     * Locates application and links to user
     * Throws HTTP BAD REQUEST if application is not found
     *
     * @param request
     * @return
     */
    private User buildUserEntity(InsertUserRequest request) {
        User user = null;

        String appIdentifier = request.getAppIdentifier();
        String username = request.getUsername();

        log.info("{} User Builder - Building user entity for user {} linked to application {}", LOG_TEXT, username, appIdentifier);

        log.info("{} User Builder - Looking for application - {}", LOG_TEXT, appIdentifier);
        Application application = applicationRepository.findApplicationByUUIDorName(appIdentifier);

        if (!ObjectUtils.isEmpty(application)) {
            log.info("{} User Builder - Found application - {}", LOG_TEXT, appIdentifier);
            user = new User(
                    username,
                    Base64.getEncoder().encodeToString(keyUtil.generateAesSecretKey().getEncoded()),
                    request.getEmail(),
                    application
            );
            log.info("{} User Builder - Built user entity for user {} linked to application {}", LOG_TEXT, username, appIdentifier);
        } else {
            log.error("{} User Builder - Could not locate application {}", LOG_TEXT_ERROR, appIdentifier);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.APPLICATION_NOT_FOUND).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }

        return user;
    }
}
