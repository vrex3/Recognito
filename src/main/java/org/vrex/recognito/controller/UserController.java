package org.vrex.recognito.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.model.dto.ApplicationIdentifier;
import org.vrex.recognito.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Accepts either appName or appUUID
     * Throws exception if neither are provided
     * Attempts to locate user details for app otherwise
     *
     * @param appId
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/application")
    public ResponseEntity<?> getUsersForApplication(@Valid @ModelAttribute ApplicationIdentifier appId) throws Exception {
        return userService.getUsersForApplication(appId);
    }
}
