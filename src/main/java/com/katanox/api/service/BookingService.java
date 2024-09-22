package com.katanox.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.katanox.api.domain.booking.BookingDTO;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.booking.UpdateBookingRequest;
import com.katanox.api.exception.BookingException;
import com.katanox.api.repository.BookingRepository;
import com.katanox.api.repository.PricesRepository;
import com.katanox.test.sql.enums.BookingStatus;

@Service
public class BookingService {

  private final RabbitMQBookingSenderService rabbitMQBookingSenderService;
  private final ChargeCalculationService chargeCalculationService;
  private final SearchAvailabilityService searchAvailabilityService;
  private final BookingRepository bookingRepository;
  private final PricesRepository pricesRepository;

  public BookingService(final RabbitMQBookingSenderService rabbitMQBookingSenderService,
                        final ChargeCalculationService chargeCalculationService,
                        final SearchAvailabilityService searchAvailabilityService,
                        final BookingRepository bookingRepository, PricesRepository pricesRepository) {
    this.rabbitMQBookingSenderService = rabbitMQBookingSenderService;
    this.chargeCalculationService = chargeCalculationService;
    this.searchAvailabilityService = searchAvailabilityService;
    this.bookingRepository = bookingRepository;
    this.pricesRepository = pricesRepository;
  }

  @Transactional
  public BookingDTO booking(final BookingRequest bookingRequest) {
    try {
      final var roomToBeBooked = searchAvailabilityService.getAvailabilityForBooking(bookingRequest);

      if (roomToBeBooked.isEmpty()) {
        throw new IllegalArgumentException("The rooms requested are not available");
      }

      final var roomToBeBookedWithCharges = chargeCalculationService.applyExtraChargesForBooking(bookingRequest, roomToBeBooked.get());
      // reserve rooms for the booking
      pricesRepository.reserveQuantityForBooking(bookingRequest);
      final var bookingId = bookingRepository.createBooking(bookingRequest, roomToBeBookedWithCharges);
      final var booking = bookingRequest.toBookingDTO(bookingId, roomToBeBookedWithCharges);

      rabbitMQBookingSenderService.ObjectRabbitMQSender(booking);
      return booking;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new BookingException("Booking for hotel: " + bookingRequest.hotelId + " and " +
        " guest : " + bookingRequest.guest + " failed");
    }
  }

  public void completeBooking(final Long bookingId) {
    bookingRepository.updateBookingStatus(bookingId, BookingStatus.booked);
  }


  public void cancelBooking(final BookingDTO booking) {
    pricesRepository.releaseQuantityForBooking(booking);
    bookingRepository.updateBookingStatus(booking.bookingId, BookingStatus.cancelled);
  }

  @Transactional
  public void updateBooking(final UpdateBookingRequest request) {
    bookingRepository.getBookingById(request.bookingId)
        .filter(bookingDTO -> !bookingDTO.status.equals(request.status))
        .ifPresent( booking -> {
          switch (request.status){
            case booked:
              completeBooking(booking.bookingId);
              return;
            case failed:
            case cancelled:
              cancelBooking(booking);
              return;
            case pending:
          }
        });
  }
}
