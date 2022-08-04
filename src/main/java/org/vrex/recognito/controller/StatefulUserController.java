package org.vrex.recognito.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.model.UserToken;
import org.vrex.recognito.service.UserService;
import org.vrex.recognito.utility.HttpResponseUtil;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping(value = "/app/user")
@SuppressWarnings("unused")
public class StatefulUserController {

    @Autowired
    private UserService userService;

    private Gson gson;

    @PostConstruct
    public void setup() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

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
     * tokenAsJson -> true : return format is Json
     * tokenAsJson -> false : return format is UserToken
     *
     * @param username
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/token/generate")
    public ResponseEntity<?> generateToken(@AuthenticationPrincipal String username,
                                           @RequestParam(required = false, defaultValue = "false") String tokenAsJson) throws Exception {
        UserToken token = userService.generateTokenForUser(username);
        boolean jsonify = false;
        try {
            jsonify = Boolean.parseBoolean(tokenAsJson.toLowerCase().trim());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return HttpResponseUtil.returnRawPackageWithStatusOrElse(
                jsonify ? gson.toJson(token) : token,
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
    public ResponseEntity<?> authorizeToken(
            @RequestHeader(name = "x-app-uuid") String appUUID,
            @RequestHeader(name = "x-auth-token") String token,
            @RequestParam(required = false) String resource) throws Exception {

        return HttpResponseUtil.returnRawPackageWithStatusOrElse(
                userService.authorizeUser(appUUID, token, resource),
                HttpStatus.OK,
                HttpStatus.UNAUTHORIZED
        );
    }
}
