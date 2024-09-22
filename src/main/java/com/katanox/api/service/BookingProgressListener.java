package com.katanox.api.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.katanox.api.domain.booking.BookingDTO;

@Service
public class BookingProgressListener {

  private final ObjectMapper objectMapper;
  private final BookingService bookingService;
  private final LogWriterService logWriterService;

  public BookingProgressListener(final BookingService bookingService, final LogWriterService logWriterService) {
    this.logWriterService = logWriterService;
    this.objectMapper = new ObjectMapper();
    this.bookingService = bookingService;
  }



  public void receiveBookingUpdateMessage(final String message) {
    logWriterService.logStringToConsoleOutput("Received booking update message: " + message);
    // Parse the message (e.g., BookingId and new status)
    final BookingDTO booking = parseMessage(message);

  }

  private BookingDTO parseMessage(String message) {
    return objectMapper.convertValue(message, BookingDTO.class);
  }
}
