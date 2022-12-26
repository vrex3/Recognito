package org.vrex.recognito.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.config.ApplicationConstants;
import org.vrex.recognito.model.ApplicationException;
import org.vrex.recognito.model.UserToken;
import org.vrex.recognito.model.dto.InsertUserRequest;
import org.vrex.recognito.service.UserService;
import org.vrex.recognito.utility.HttpResponseUtil;
import org.vrex.recognito.utility.RoleUtil;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/client/user")
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
     * Registers a client user.
     * Finds user info of logged in user
     * Returns 404 for invalid username
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody InsertUserRequest request) throws Exception {
        validateUserRole(request);
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                userService.createUser(request)
        );
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
    public ResponseEntity<?> generateToken(
            @AuthenticationPrincipal String username,
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
     * Username gleaned from authentication principal
     * Token accepted as part of request header : x-auth-token
     * If token is authenticated, user info is returned
     *
     * @param username
     * @param token
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/token/authorize")
    public ResponseEntity<?> authorizeToken(
            @AuthenticationPrincipal String username,
            @RequestHeader(name = "x-auth-token") String token,
            @RequestParam(name = "x-resource", required = false) String resource) throws Exception {

        return HttpResponseUtil.returnRawPackageWithStatusOrElse(
                userService.authorizeUser(username, token, resource),
                HttpStatus.OK,
                HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Validates whether a role is a client role in the user request.
     * Throws a BAD REQUEST exception if role is not null, but not a client role.
     * Does nothing otherwise
     *
     * @param request
     */
    private void validateUserRole(InsertUserRequest request) {
        if (!ObjectUtils.isEmpty(request) && request.getRole() != null) {
            if (!RoleUtil.isClientRole(request.getRole()))
                throw ApplicationException.builder().
                        errorMessage(ApplicationConstants.INVALID_CLIENT_ROLE).
                        status(HttpStatus.BAD_REQUEST).
                        build();
        }
    }
}
