package org.vrex.recognito.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrex.recognito.service.MappingService;

@RestController
@RequestMapping(value = "/app/role/mapping")
@SuppressWarnings("unused")
public class MappingController {

    @Autowired
    private MappingService mappingService;
}
