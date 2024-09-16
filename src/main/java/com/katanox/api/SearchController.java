package com.katanox.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("search")
public class SearchController {

    @Value("${env}")
    String environment;

    @Autowired
    LogWriterService logWriterService;

    public SearchController() {
        this.logWriterService = new LogWriterService();
    }

    @PostMapping(
            path = "/",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    ResponseEntity<SearchResponse> search(
            @RequestBody SearchRequest request
    ) {
        var result = new java.util.ArrayList<>(List.of());

        if (environment == "local") {
            logWriterService.logStringToConsoleOutput(result.toString());
        }
        return new ResponseEntity<>(new SearchResponse(), HttpStatus.ACCEPTED);
    }

}
