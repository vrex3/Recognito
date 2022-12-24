package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.entity.Role;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.dto.ApplicationDTO;
import org.vrex.recognito.model.dto.ApplicationIdentifier;
import org.vrex.recognito.model.dto.InsertUserRequest;
import org.vrex.recognito.model.dto.UpsertApplicationRequest;
import org.vrex.recognito.model.dto.UserDTO;
import org.vrex.recognito.repository.ApplicationRepository;
import org.vrex.recognito.utility.KeyUtil;

@Slf4j
@Service
public class ApplicationService {

    private static final String LOG_TEXT = "App-Service : ";
    private static final String LOG_TEXT_ERROR = "App-Service - Encountered Exception - ";

    @Autowired
    private KeyUtil keyUtil;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserService userService;

    /**
     * AppUUID preffered to appName (IF BOTH PROVIDED)
     * Fetches application by appUUID if UUID provided
     * Otherwise fetches by appName
     * Hides keys (both sets)
     *
     * @param appId
     * @return
     */
    public ApplicationDTO getApplication(ApplicationIdentifier appId) {
        ApplicationDTO response = null;

        boolean id = StringUtils.isEmpty(appId.getAppUUID()) ? false : true;
        String identifier = id ? appId.getAppUUID() : appId.getAppName();
        String logIdentifier = id ? "UUID" : "name";

        log.info("{} Fetching application with {} - {}", LOG_TEXT, logIdentifier, identifier);

        try {
            response = new ApplicationDTO(id ?
                    applicationRepository.findApplicationByUUID(identifier) :
                    applicationRepository.findApplicationByName(identifier));
        } catch (Exception exception) {
            log.error("{} Fetching application with {} - {}", LOG_TEXT_ERROR, logIdentifier, identifier, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }

        return response;
    }

    /**
     * Returns the app invite secret for an appUUID
     *
     * @param appUUID
     * @return
     */
    public String findApplicationSecret(String appUUID) {

        if (StringUtils.isEmpty(appUUID)) {
            log.error("{} Cannot fetch secret for empty appUUID.", LOG_TEXT_ERROR);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.EMPTY_APPLICATION_IDENTIFIER).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }

        log.info("{} Fetching secret invite for app {}", LOG_TEXT, appUUID);
        Application application = applicationRepository.findApplicationSecretForAppUUID(appUUID);
        if (ObjectUtils.isEmpty(application)) {
            log.error("{} Could not locate application {}", LOG_TEXT_ERROR, appUUID);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.APPLICATION_NOT_FOUND).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }

        log.info("{} Fetched secret invite for app {}", LOG_TEXT, appUUID);
        return application.getAppSecret();
    }

    /**
     * Updates (if already existing) or creates new Application
     * and persists it in the schema.
     * App details EXCEPT keys are returned as response.
     *
     * @param request
     * @return
     */
    @Transactional
    @Retryable(maxAttempts = 5, backoff = @Backoff(delay = 500))
    public ApplicationDTO upsertApplication(UpsertApplicationRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            log.error("{} Request empty", LOG_TEXT_ERROR);
            throw ApplicationException.builder().
                    errorMessage(ApplicationConstants.EMPTY_APPLICATION_REQUEST).
                    status(HttpStatus.BAD_REQUEST).
                    build();
        }

        String name = request.getName();

        log.info("{} Attempting to create application {}", LOG_TEXT, name);

        Application application = null;
        UserDTO rootUser = null;

        try {
            log.info("{} Checking existence of application {}", LOG_TEXT, name);
            application = applicationRepository.findApplicationByName(name);

            boolean altered = false;
            boolean rootUserCreated = false;

            if (ObjectUtils.isEmpty(application)) {
                log.info("{} Application does not exist. Creating new - {}", LOG_TEXT, name);
                application = new Application(request);
                keyUtil.populateApplicationSecrets(application);
                altered = true;
                rootUserCreated = true;
            } else {
                log.info("{} Application does exist. Comparing data to request - {}", LOG_TEXT, name);
                altered = areAppDetailsAltered(application, request);
            }
            if (altered) {
                log.info("{} Persisting Application in schema - {}", LOG_TEXT, name);
                application = applicationRepository.save(application);
            }
            if (rootUserCreated) {
                log.info("{} Persisting Root User in schema for app - {}", LOG_TEXT, name);
                rootUser = userService.createUser(createRootUserRequest(request.getEmail(), application));
            }
        } catch (ApplicationException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("{} Updating application in schema - {}", LOG_TEXT_ERROR, name, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }

        return new ApplicationDTO(application, rootUser);

    }

    /**
     * Builds root user insert request with supplied user app admin email and app details
     *
     * @param email
     * @param application
     * @return
     */
    private InsertUserRequest createRootUserRequest(String email, Application application) {
        return new InsertUserRequest(
                ApplicationConstants.ROOT_USER + application.getAppUUID(),
                email,
                Role.APP_ADMIN.name(),
                application.getName(),
                application.getAppSecret());
    }

    /**
     * Checks if the request updates anything in the persisted version of the app.
     * If yes, updates the app object attribute to the request specified value.
     * Also updates timestamp of app.
     * Returns boolean to indicate if passed app instance has been changed or not.
     *
     * @param app
     * @param request
     * @return
     */
    private boolean areAppDetailsAltered(Application app, UpsertApplicationRequest request) {
        String name = app.getName();
        log.info("{} App Comparator - Attempting to compare persisted application {} to request", LOG_TEXT, name);
        boolean altered = false;
        if (name.equals(request.getName())) {

            /**
             * Keep adding attribute comparison logic here
             */

            /* DESCRIPTION */
            if (!app.getDescription().equals(request.getDescription().trim())) {
                altered = true;
                app.setDescription(request.getDescription());
                log.info("{} App Comparator - Description altered for app {}", LOG_TEXT, name);
            }

            /* RESOURCES ENABLED */
            if (app.isResourcesEnabled() != request.isResourcesEnabled()) {
                altered = true;
                app.setResourcesEnabled(request.isResourcesEnabled());
                log.info("{} App Comparator - Role-Resource Mapping for app {} - Turned {} ", LOG_TEXT, name, request.isResourcesEnabled() ? "ON" : "OFF");
            }

        }
        if (altered) {
            app.updateApp();
            log.info("{} App Comparator - App marked for update : {}", LOG_TEXT, name);
        }
        return altered;
    }


}
