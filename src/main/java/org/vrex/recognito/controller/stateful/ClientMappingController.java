package org.vrex.recognito.controller.stateful;

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

@RestController
@RequestMapping(value = "/client/role/mapping")
@SuppressWarnings("unused")
public class ClientMappingController {

    @Autowired
    private MappingService mappingService;

    /**
     * Adds resources to specific roles for app linked to logged in user
     *
     * @param username
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<?> addMapping(
            @AuthenticationPrincipal String username,
            @RequestBody RoleResourceMapping request) {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                mappingService.addRoleResourceMapping(username, request)
        );
    }

    /**
     * Views current roles and their mapped resources for app linked to logged in user
     * Service allows for returning data for any number of passed roles (comma separated)
     *
     * @param username
     * @param roles
     * @return
     */
    @GetMapping
    public ResponseEntity<?> viewRolesForApp(
            @AuthenticationPrincipal String username,
            @RequestParam(required = false) String roles) {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                mappingService.getRoleResourceMappingForUser(username, RoleUtil.extractValidRoles(roles)));
    }


}
