package org.vrex.recognito.controller.stateless;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.model.dto.RoleResourceMapping;
import org.vrex.recognito.service.MappingService;
import org.vrex.recognito.utility.HttpResponseUtil;
import org.vrex.recognito.utility.RoleUtil;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/app/role/mapping")
@SuppressWarnings("unused")
public class MappingController {

    @Autowired
    private MappingService mappingService;

    /**
     * Adds resources for roles for speicifiec App
     * Throws Error if app is invalid or does not allow resource mapping
     *
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<?> addMapping(@Valid @RequestBody RoleResourceMapping request) {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                mappingService.addRoleResourceMapping(request)
        );

    }

    /**
     * Returns the role mappings for an app
     * If a role is specified OR valid only details of that role is returned
     * Otherwise details of ALL roles are returned
     * Service allows for returning data for any number of passed roles (comma separated)
     * If appUUID is not passed, roles are displayed for linked app for logged in user.
     *
     * @param username
     * @param appUUID
     * @param roles
     * @return
     */
    @GetMapping
    public ResponseEntity<?> viewRolesForApp(
            @AuthenticationPrincipal String username,
            @RequestParam(required = false) String appUUID,
            @RequestParam(required = false) String roles) {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                StringUtils.isEmpty(appUUID) ?
                        mappingService.getRoleResourceMappingForUser(username, RoleUtil.extractValidRoles(roles)) :
                        mappingService.getRoleResourceMappingForApplication(appUUID, RoleUtil.extractValidRoles(roles)));
    }
}
