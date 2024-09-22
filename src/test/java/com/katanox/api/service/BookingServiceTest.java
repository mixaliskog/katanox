package com.katanox.api.service;

import com.katanox.api.domain.booking.BookingDTO;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.booking.Guest;
import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.UpdateBookingRequest;
import com.katanox.api.exception.BookingException;
import com.katanox.api.repository.BookingRepository;
import com.katanox.api.repository.PricesRepository;
import com.katanox.test.sql.enums.BookingStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.katanox.api.util.TestUtil.bookingId;
import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.firstOfMarch;
import static com.katanox.api.util.TestUtil.guest;
import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.payment;
import static com.katanox.api.util.TestUtil.roomId1;
import static com.katanox.api.util.TestUtil.sixthOfMarch;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BookingServiceTest {

    @Mock
    private RabbitMQBookingSenderService rabbitMQBookingSenderService;

    @Mock
    private ChargeCalculationService chargeCalculationService;

    @Mock
    private SearchAvailabilityService searchAvailabilityService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PricesRepository pricesRepository;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequest bookingRequest;
    private AvailableRoom availableRoom;
    private BookingDTO bookingDTO;

    @Before
    public void setUp() {
        availableRoom =AvailableRoom.totalPricesOnly(roomId1, BigDecimal.valueOf(200), BigDecimal.valueOf(180),"USD");
        bookingRequest = new BookingRequest(hotelId1, guest, availableRoom.roomId, firstOfMarch, sixthOfMarch,
            currency, payment);
        bookingDTO = new BookingDTO(hotelId1, bookingId, guest, availableRoom, firstOfMarch,
            sixthOfMarch, currency, payment, BookingStatus.pending);
    }

    @Test
    public void testBooking_Successful() {
        when(searchAvailabilityService.getAvailabilityForBooking(bookingRequest))
            .thenReturn(Optional.of(availableRoom));
        when(chargeCalculationService.applyExtraChargesForBooking(bookingRequest, availableRoom))
            .thenReturn(availableRoom);
        when(bookingRepository.createBooking(bookingRequest, availableRoom))
            .thenReturn(bookingId);

        final var result = bookingService.booking(bookingRequest);

        assertNotNull(result);
        assertEquals(bookingDTO.guest, result.guest);
        assertEquals(bookingDTO.bookingId, result.bookingId);

        verify(bookingRepository, times(1)).createBooking(bookingRequest, availableRoom);
        verify(pricesRepository, times(1)).reserveQuantityForBooking(bookingRequest);
        verify(searchAvailabilityService, times(1)).getAvailabilityForBooking(bookingRequest);
        verify(rabbitMQBookingSenderService, times(1)).ObjectRabbitMQSender(bookingDTO);
        verify(chargeCalculationService, times(1)).applyExtraChargesForBooking(bookingRequest, availableRoom);
    }

    @Test(expected = BookingException.class)
    public void testBooking_RoomNotAvailable() {
        // Mock no rooms being available
        when(searchAvailabilityService.getAvailabilityForBooking(bookingRequest))
            .thenReturn(Optional.empty());

        bookingService.booking(bookingRequest);

        verify(searchAvailabilityService, times(1)).getAvailabilityForBooking(bookingRequest);
        verify(bookingRepository, never()).createBooking(any(), any());
        verify(rabbitMQBookingSenderService, never()).ObjectRabbitMQSender(any());
    }

    @Test(expected = BookingException.class)
    public void testBooking_InternalServerError() {

        when(searchAvailabilityService.getAvailabilityForBooking(bookingRequest))
            .thenReturn(Optional.of(availableRoom));

        when(chargeCalculationService.applyExtraChargesForBooking(any(BookingRequest.class), any(AvailableRoom.class)))
            .thenThrow(new RuntimeException("Error calculating charges"));

        bookingService.booking(bookingRequest);

        verify(searchAvailabilityService, times(1)).getAvailabilityForBooking(bookingRequest);
        verify(bookingRepository, never()).createBooking(any(), any());
        verify(rabbitMQBookingSenderService, never()).ObjectRabbitMQSender(any());
        verify(chargeCalculationService, times(1)).applyExtraChargesForBooking(bookingRequest, availableRoom);
    }

    @Test
    public void testCancelBooking() {
        bookingService.cancelBooking(bookingDTO);
        verify(pricesRepository, times(1)).releaseQuantityForBooking(bookingDTO);
        verify(bookingRepository, times(1)).updateBookingStatus(bookingDTO.bookingId, BookingStatus.cancelled);
    }

    @Test
    public void testCompleteBooking() {
        bookingService.completeBooking(1L);
        verify(bookingRepository, times(1)).updateBookingStatus(1L, BookingStatus.booked);
    }

    @Test
    public void testUpdateBooking_BookedStatus() {
        final var updateBookingRequest = new UpdateBookingRequest(bookingId, BookingStatus.booked);

        when(bookingRepository.getBookingById(bookingId))
            .thenReturn(Optional.of(bookingDTO));

        bookingService.updateBooking(updateBookingRequest);

        verify(bookingRepository, times(1)).updateBookingStatus(bookingId, BookingStatus.booked);
        verify(pricesRepository, never()).releaseQuantityForBooking(any());
        verify(pricesRepository, never()).reserveQuantityForBooking(any());
    }

    @Test
    public void testUpdateBooking_CancelledStatus() {
        final var updateBookingRequest = new UpdateBookingRequest(bookingId, BookingStatus.cancelled);

        when(bookingRepository.getBookingById(bookingId))
            .thenReturn(Optional.of(bookingDTO));

        bookingService.updateBooking(updateBookingRequest);

        verify(bookingRepository, times(1)).updateBookingStatus(bookingId, BookingStatus.cancelled);
        verify(pricesRepository, times(1)).releaseQuantityForBooking(bookingDTO);
    }

    @Test
    public void testUpdateBooking_NoStatusChange() {
        final var updateBookingRequest = new UpdateBookingRequest(bookingId, BookingStatus.pending);

        when(bookingRepository.getBookingById(bookingId))
            .thenReturn(Optional.of(bookingDTO));

        bookingService.updateBooking(updateBookingRequest);

        verify(bookingRepository, never()).updateBookingStatus(anyLong(), any());
        verify(pricesRepository, never()).releaseQuantityForBooking(any());
        verify(pricesRepository, never()).reserveQuantityForBooking(any());
    }
}

