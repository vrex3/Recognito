package org.vrex.recognito.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.model.dto.ApplicationIdentifier;
import org.vrex.recognito.model.dto.InsertUserRequest;
import org.vrex.recognito.model.dto.UserDTO;
import org.vrex.recognito.service.UserService;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
@SuppressWarnings("unused")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Accepts username, email and app identifier
     * AppIdentifier -> appUUID or appName
     * Returns saved user data + secret (ONLY time this is returned)
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody InsertUserRequest request) throws Exception {
        return userService.createUser(request);
    }

    /**
     * Accepts either appName or appUUID
     * Throws exception if neither are provided
     * Attempts to locate user details for app otherwise
     *
     * @param appIdentifierParam
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/application")
    public ResponseEntity<?> getUsersForApplication(@RequestParam Map<String,String> appIdentifierParam) throws Exception {
        return userService.getUsersForApplication(new ApplicationIdentifier(appIdentifierParam));
    }

    /**
     * Generates token for username and returns it
     * Status 200 if token is returned
     * Status 500 in case of error
     *
     * @param username
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/token/generate")
    public ResponseEntity<String> generateToken(@RequestParam String username) throws Exception {
        return userService.generateTokenForUser(username);
    }

    /**
     * App UUID accepted as part of request header : x-app-uuid
     * Token accepted as part of request header : x-auth-token
     * If token is authenticated, user info is returned
     *
     * @param appUUID
     * @param token
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/token/authenticate")
    public ResponseEntity<UserDTO> authenticateToken(
            @RequestHeader(name = "x-app-uuid") String appUUID,
            @RequestHeader(name = "x-auth-token") String token) throws Exception {
        return userService.authenticateUser(appUUID, token);
    }
}
