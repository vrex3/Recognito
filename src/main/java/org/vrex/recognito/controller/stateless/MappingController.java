package org.vrex.recognito.controller.stateless;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
     * <p>
     * TODO:
     * Service allows for returning data for any number of passed roles
     * Can be incorporated into request later.
     *
     * @param appUUID
     * @param role
     * @return
     */
    @GetMapping
    public ResponseEntity<?> viewRolesForApp(
            @RequestParam String appUUID,
            @RequestParam(required = false) String role) {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                !StringUtils.isEmpty(role) && RoleUtil.isValidRole(role) ?
                        mappingService.getRoleResourceMappingForApplication(appUUID, role) :
                        mappingService.getRoleResourceMappingForApplication(appUUID)
        );
    }
}
