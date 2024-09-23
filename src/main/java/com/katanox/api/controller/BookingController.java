package com.katanox.api.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.booking.BookingResponse;
import com.katanox.api.domain.booking.UpdateBookingRequest;
import com.katanox.api.service.BookingService;
import com.katanox.api.service.LogWriterService;

@RestController
@RequestMapping("booking")
public class BookingController {

    private final BookingService bookingService;
    private final LogWriterService logWriterService;

    public BookingController(BookingService bookingService, LogWriterService logWriterService) {
        this.bookingService = bookingService;
        this.logWriterService = logWriterService;
    }

    @PostMapping(path = "/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<BookingResponse> booking(final @RequestBody BookingRequest request) {
        final var result = bookingService.booking(request);
        logWriterService.logStringToConsoleOutput(result.toString());
        return new ResponseEntity<>(BookingResponse.from(result), HttpStatus.ACCEPTED);
    }

    @PostMapping(path = "/update/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<Void> webhookController(final @RequestBody UpdateBookingRequest request) {
        bookingService.updateBooking(request);
        return ResponseEntity.ok().build();
    }

}
