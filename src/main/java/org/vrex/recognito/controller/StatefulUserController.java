package org.vrex.recognito.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.model.dto.UserDTO;
import org.vrex.recognito.service.UserService;
import org.vrex.recognito.utility.HttpResponseUtil;

@RestController
@RequestMapping(value = "/app/user")
@SuppressWarnings("unused")
public class StatefulUserController {

    @Autowired
    private UserService userService;

    /**
     * Login API for APP users
     *
     * @param username
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/login")
    public ResponseEntity<?> loginUser(@AuthenticationPrincipal String username) throws Exception {
        return HttpResponseUtil.returnRawPackageWithStatusOrElse(
                userService.findUser(username),
                HttpStatus.OK,
                HttpStatus.NOT_FOUND
        );
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
    public ResponseEntity<String> generateToken(@AuthenticationPrincipal String username) throws Exception {
        return HttpResponseUtil.returnRawPackageWithStatusOrElse(
                userService.generateTokenForUser(username),
                HttpStatus.OK,
                HttpStatus.INTERNAL_SERVER_ERROR);
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
    @GetMapping(value = "/token/authorize")
    public ResponseEntity<UserDTO> authenticateToken(
            @RequestHeader(name = "x-app-uuid") String appUUID,
            @RequestHeader(name = "x-auth-token") String token) throws Exception {

        return HttpResponseUtil.returnRawPackageWithStatusOrElse(
                userService.authenticateUser(appUUID, token),
                HttpStatus.OK,
                HttpStatus.UNAUTHORIZED
        );
    }
}
