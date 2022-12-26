package org.vrex.recognito.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.model.dto.ApplicationIdentifier;
import org.vrex.recognito.model.dto.InsertUserRequest;
import org.vrex.recognito.service.UserService;
import org.vrex.recognito.utility.HttpResponseUtil;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping(value = "/system/user")
@SuppressWarnings("unused")
public class StatelessUserController {

    @Autowired
    private UserService userService;

    /**
     * API Only for RECOGNITO users
     * NOT APP users
     * Finds user info of logged in user
     * Returns 404 for invalid username
     *
     * @param username
     * @return
     * @throws Exception
     */
    @GetMapping
    public ResponseEntity<?> findUser(@AuthenticationPrincipal String username) throws Exception {
        return HttpResponseUtil.returnRawPackageWithStatusOrElse(
                userService.findUser(username),
                HttpStatus.OK,
                HttpStatus.NOT_FOUND
        );
    }


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
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                userService.createUser(request)
        );
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
    public ResponseEntity<?> getUsersForApplication(@RequestParam Map<String, String> appIdentifierParam) throws Exception {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                userService.getUsersForApplication(new ApplicationIdentifier(appIdentifierParam))
        );
    }

}
