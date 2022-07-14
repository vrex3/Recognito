package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.vrex.recognito.entity.Application;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.Message;
import org.vrex.recognito.model.dto.ApplicationDTO;
import org.vrex.recognito.model.dto.UpsertApplicationRequest;
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

    /**
     * Updates (if already existing) or creates new Application
     * and persists it in the schema.
     * App details EXCEPT keys are returned as response.
     *
     * @param request
     * @return
     */
    public ResponseEntity<> upsertApplication(UpsertApplicationRequest request) {
        if (ObjectUtils.isEmpty(request)) {
            log.error("{} Request empty", LOG_TEXT_ERROR);
            return new ResponseEntity(Message.builder().text(LOG_TEXT_ERROR).build(), HttpStatus.BAD_REQUEST);
        }

        String name = request.getName();

        log.info("{} Attempting to create application {}", LOG_TEXT, name);

        Application application = null;

        try {
            log.info("{} Checking existence of application {}", LOG_TEXT, name);
            application = applicationRepository.findApplicationByName(name);

            boolean altered = false;
            if (ObjectUtils.isEmpty(application)) {
                log.info("{} Application does not exist. Creating new - {}", LOG_TEXT, name);
                application = new Application(request, keyUtil.generateKeyPair());
                altered = true;
            } else {
                log.info("{} Application does exist. Comparing data to request - {}", LOG_TEXT, name);
                altered = areAppDetailsAltered(application, request);
            }
            if (altered) {
                log.info("{} Persisting Application in schema - {}", LOG_TEXT, name);
                applicationRepository.save(application);
            }
        } catch (Exception exception) {
            log.error("{} Updating application in schema - {}", LOG_TEXT_ERROR, name, exception);
            throw ApplicationException.builder().
                    errorMessage(exception.getMessage()).
                    status(HttpStatus.INTERNAL_SERVER_ERROR).
                    build();
        }

        return new ResponseEntity(Message.builder().
                data(new ApplicationDTO(application)).
                build(),
                HttpStatus.OK);

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
            if (!app.getDescription().equals(request.getDescription().trim())) {
                altered = true;
                app.setDescription(request.getDescription());
                log.info("{} App Comparator - Description altered for app {}", LOG_TEXT, name)
            }
        }
        if (altered) {
            app.updateApp();
            log.info("{} App Comparator - App marked for update : {}", LOG_TEXT, name)
        }
        return altered;
    }


}
