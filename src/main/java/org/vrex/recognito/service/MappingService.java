package org.vrex.recognito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vrex.recognito.repository.MappingRepository;

@Slf4j
@Service
public class MappingService {

    private static final String LOG_TEXT = "Mapping-Service : ";
    private static final String LOG_TEXT_ERROR = "Mapping-Service - Encountered Exception - ";

    @Autowired
    private MappingRepository mappingRepository;




}
