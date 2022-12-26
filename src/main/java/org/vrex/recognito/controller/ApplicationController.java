package org.vrex.recognito.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.entity.Role;
import org.vrex.recognito.model.dto.ApplicationIdentifier;
import org.vrex.recognito.model.dto.UpsertApplicationRequest;
import org.vrex.recognito.service.ApplicationService;
import org.vrex.recognito.utility.HttpResponseUtil;
import org.vrex.recognito.utility.RoleUtil;

import java.util.Map;

@RestController
@RequestMapping(value = "/application")
@SuppressWarnings("unused")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * Accepts either appName or appUUID
     * Throws exception if neither are provided
     * Attempts to locate app details otherwise
     *
     * @param appIdentifierParam
     * @return
     * @throws Exception
     */
    @GetMapping
    public ResponseEntity<?> getApplication(@RequestParam Map<String, String> appIdentifierParam) throws Exception {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                applicationService.getApplication(new ApplicationIdentifier(appIdentifierParam))
        );
    }

    /**
     * Can be used to both create and update an application
     * NEEDS ACCESS CONTROL
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping
    public ResponseEntity<?> upsertApplication(@RequestBody UpsertApplicationRequest request) throws Exception {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                applicationService.upsertApplication(request)
        );
    }

    /**
     * Returns app invite secret for provided App UUID
     * <p>
     * MUST BE ALLOWED ONLY FOR SYSTEM ADMINS OF RECOGNITO
     * OR APP_ADMIN OF ANY REGISTERED APP
     *
     * @param appUUID
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/invite")
    public ResponseEntity<?> getAppInvite(@AuthenticationPrincipal String username,
                                          @RequestParam(required = false) String appUUID) throws Exception {
        return HttpResponseUtil.wrapInHttpStatusOkResponse(
                !StringUtils.isEmpty(appUUID) && RoleUtil.isSystemAdmin() ?
                        applicationService.findApplicationSecret(appUUID) :
                        applicationService.findApplicationSecretForUser(username)
        );
    }
}
