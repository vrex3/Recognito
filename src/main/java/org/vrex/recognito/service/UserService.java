package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.Message;
import org.vrex.recognito.model.dto.ApplicationDTO;
import org.vrex.recognito.model.dto.ApplicationIdentifier;
import org.vrex.recognito.model.dto.ApplicationUserListDTO;
import org.vrex.recognito.repository.UserRepository;

@Slf4j
@Service
public class UserService {

    private static final String LOG_TEXT = "User-Service : ";
    private static final String LOG_TEXT_ERROR = "User-Service - Encountered Exception - ";

    @Autowired
    private UserRepository userRepository;

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
}
