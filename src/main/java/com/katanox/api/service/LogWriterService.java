package com.katanox.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LogWriterService implements LogInterface {

    @Value("${env:local}")
    String environment;

    public void logStringToConsoleOutput(String o) {
        if ("local".equals(environment)) {
            log.warn(o);
        }
    }
}
