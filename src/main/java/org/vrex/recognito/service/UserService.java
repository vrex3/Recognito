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
import org.vrex.recognito.model.dto.UserDTO;
import org.vrex.recognito.repository.ApplicationRepository;
import org.vrex.recognito.repository.UserRepository;
import org.vrex.recognito.utility.TokenUtil;
import org.vrex.recognito.utility.KeyUtil;
import org.vrex.recognito.utility.RoleUtil;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

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

    @Autowired
    private TokenUtil tokenUtil;

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

                return new ResponseEntity<>(Message.builder().data(new UserDTO(user)).build(), HttpStatus.OK);

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

        boolean id = !StringUtils.isEmpty(appId.getAppUUID());
        String identifier = id ? appId.getAppUUID() : appId.getAppName();
        String logIdentifier = id ? "UUID" : "name";

        log.info("{} Fetching users for application with {} - {}", LOG_TEXT, logIdentifier, identifier);

        try {
            return new ResponseEntity<>(Message.builder().
                    data(new ApplicationUserListDTO(identifier, fetchUsersForApp(id, identifier))).
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
     * Accepts username
     * Generates token for user
     * Token is signed by user linked app's private key
     * Token is encrypted by user linked app's public key
     * Token is wrapped into json which also contains appUUID
     *
     * @param username
     * @return
     */
    public ResponseEntity<String> generateTokenForUser(String username) {
        log.info("{} Token Generator - Received request to generate token for user - {}", LOG_TEXT, username);

        String token = null;
        try {
            log.info("{} Token Generator - Extracting user details from repository - {}", LOG_TEXT, username);
            User user = userRepository.getUserByName(username);

            if (ObjectUtils.isEmpty(user)) {
                log.info("{} Token Generator - User details not found for - {}", LOG_TEXT, username);
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.INVALID_USER).
                        status(HttpStatus.BAD_REQUEST).
                        build();
            } else {
                log.info("{} Token Generator - User details found. Generating token for - {}", LOG_TEXT, username);
                token = tokenUtil.generateToken(user);
                log.info("{} Token Generator - User details found. Generated token for - {}", LOG_TEXT, username);
            }

        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("{} Token Generator - Encountered exception generating token for user {}", LOG_TEXT_ERROR, username, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }

        return new ResponseEntity<>(
                token,
                token != null ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Authenticates user information encoded in token
     * Returns HTTP response accordingly
     *
     * @param appUUID
     * @param token
     * @return
     */
    public ResponseEntity<UserDTO> authenticateUser(String appUUID, String token) {
        log.info("{} Token Generator - Extracting token for appUUID {}", LOG_TEXT, appUUID);

        UserDTO user = extractUserFromToken(appUUID, token);
        return new ResponseEntity<>(user, user != null ? HttpStatus.OK : HttpStatus.UNAUTHORIZED);
    }

    /**
     * Accepts encoded signed token
     * Verifies and Authenticates token
     * Extracts userDTO information from token
     * <p>
     * OPTIONAL : Add validation of user data later
     *
     * @param appUUID
     * @param token
     * @return
     */
    public UserDTO extractUserFromToken(String appUUID, String token) {
        UserDTO user = null;
        try {

            if (StringUtils.isEmpty(appUUID)) {
                log.error("{} Token Generator - Encountered empty appUUID ", LOG_TEXT_ERROR, appUUID);
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.EMPTY_APPLICATION_IDENTIFIER).
                        status(HttpStatus.UNAUTHORIZED).
                        build();
            }

            if (StringUtils.isEmpty(token)) {
                log.error("{} Token Generator - Encountered empty token for appUUID {} ", LOG_TEXT_ERROR, appUUID);
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.EMPTY_TOKEN).
                        status(HttpStatus.UNAUTHORIZED).
                        build();
            }

            user = new UserDTO(tokenUtil.extractPayload(appUUID, token));
            log.info("{} Token Generator - Extracted payload from token for appUUID {}", LOG_TEXT, appUUID);

        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("{} Token Generator - Encountered exception extracting token", LOG_TEXT_ERROR, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }

        return user;
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
        String inviteSecret = request.getAppInvite();

        if (StringUtils.isEmpty(inviteSecret)) {
            log.error("{} User Builder - No app invite detected for app {} and user {}", LOG_TEXT_ERROR, appIdentifier, username);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.EMPTY_APP_INVITE).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }

        log.info("{} User Builder - Building user entity for user {} linked to application {}", LOG_TEXT, username, appIdentifier);

        log.info("{} User Builder - Looking for application - {}", LOG_TEXT, appIdentifier);
        Application application = applicationRepository.findApplicationByUUIDorName(appIdentifier);

        if (!ObjectUtils.isEmpty(application)) {
            log.info("{} User Builder - Found application - {}", LOG_TEXT, appIdentifier);

            if (!keyUtil.verifyApplicationSecret(inviteSecret, application)) {
                log.error("{} User Builder - Invalid app invite detected for app {} and user {}", LOG_TEXT_ERROR, appIdentifier, username);
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.EMPTY_APP_INVITE).
                        status(HttpStatus.UNAUTHORIZED).
                        build();
            }

            String userRole = request.getRole();
            String roleEnabled = application.isResourcesEnabled() ? "yes" : "no";
            log.info("{} User Builder - App : {} - Verifying Role : {} - Roles Enabled for App ? {}.",
                    LOG_TEXT,
                    appIdentifier,
                    userRole,
                    roleEnabled
            );

            if (application.isResourcesEnabled() && !RoleUtil.isValidRole(userRole)) {
                log.error("{} User Builder - App : {} - COULD NOT VERIFY Role : {} - Roles Enabled for App ? {}.",
                        LOG_TEXT_ERROR,
                        appIdentifier,
                        userRole,
                        roleEnabled);

                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.INVALID_ROLE).
                        status(HttpStatus.BAD_REQUEST).
                        build();
            }


            user = new User(
                    username,
                    Base64.getEncoder().encodeToString(keyUtil.generateAesSecretKey().getEncoded()),
                    request.getEmail(),
                    application,
                    userRole
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

    /**
     * Utility method to fetch users for app identifier
     * For UUID, app is fetched first from application collection
     * For appname, users are fetched comparing appid to app name
     *
     * @param id
     * @param identifier
     * @return
     */
    private List<User> fetchUsersForApp(boolean id, String identifier) {
        List<User> users = null;
        if (id) {
            Application application = applicationRepository.findApplicationByUUID(identifier);
            if (!ObjectUtils.isEmpty(application))
                users = userRepository.getUserForAppName(application.getName());
        } else
            users = userRepository.getUserForAppName(identifier);

        return users != null ? users : new LinkedList<>();

    }
}
