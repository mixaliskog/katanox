package com.katanox.api.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.katanox.api.domain.search.SearchRequest;
import com.katanox.api.domain.search.SearchResponse;
import com.katanox.api.service.LogWriterService;
import com.katanox.api.service.SearchAvailabilityService;

@RestController
@RequestMapping("search")
public class SearchController {

    private final LogWriterService logWriterService;
    private final SearchAvailabilityService searchAvailabilityService;

    public SearchController(final LogWriterService logWriterService, final SearchAvailabilityService searchAvailabilityService) {
        this.logWriterService = logWriterService;
        this.searchAvailabilityService = searchAvailabilityService;
    }

    @PostMapping(
        path = "/",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE)
    ResponseEntity<SearchResponse> search(@RequestBody final SearchRequest request) {
        SearchResponse result = searchAvailabilityService.searchAvailability(request);

        logWriterService.logStringToConsoleOutput(result.toString());

        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

}
