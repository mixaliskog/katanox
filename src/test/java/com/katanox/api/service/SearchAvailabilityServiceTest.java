package com.katanox.api.service;

import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.firstOfMarch;
import static com.katanox.api.util.TestUtil.guest;
import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.payment;
import static com.katanox.api.util.TestUtil.roomId1;
import static com.katanox.api.util.TestUtil.secondOfMarch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.katanox.api.domain.booking.AvailableRoom;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.search.SearchRequest;
import com.katanox.api.repository.AvailabilityRepository;

public class SearchAvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private ChargeCalculationService chargeCalculationService;

    @InjectMocks
    private SearchAvailabilityService searchAvailabilityService;

    private final LocalDate checkin = LocalDate.of(2023, 9, 1);
    private final LocalDate checkout = LocalDate.of(2023, 9, 3);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSearchAvailability_HappyPath() {

        final var room1 = AvailableRoom.totalPriceAndFirstNightPriceAfterTax(1L, new BigDecimal("100"), new BigDecimal("10"), currency);
        final var room2 = AvailableRoom.totalPriceAndFirstNightPriceAfterTax(2L, new BigDecimal("200"), new BigDecimal("20"), currency);
        final List<AvailableRoom> availableRooms = List.of(room1, room2);

        when(availabilityRepository.searchAvailableRooms(hotelId1, checkin, checkout, currency)).thenReturn(availableRooms);

        // assume the price changed after charge calculation
        final AvailableRoom availableRoom = AvailableRoom.totalPriceAfterTax(1L, new BigDecimal("110"), currency);
        final AvailableRoom availableRoom1 = AvailableRoom.totalPriceAfterTax(2L, new BigDecimal("200"), currency);
        final List<AvailableRoom> updatedRooms = List.of(availableRoom, availableRoom1);

        when(chargeCalculationService.applyExtraChargesToAvailableRooms(hotelId1, checkin, checkout, availableRooms, currency))
            .thenReturn(updatedRooms);

        final var result = searchAvailabilityService.searchAvailability(new SearchRequest(checkin, checkout, hotelId1, currency));

        assertNotNull(result);
        assertEquals((long) hotelId1, result.hotelId);
        assertEquals(2, result.availableRooms.size());
        assertEquals(availableRoom, result.availableRooms.get(0));
        assertEquals(availableRoom1, result.availableRooms.get(1));

        verify(availabilityRepository, times(1)).searchAvailableRooms(hotelId1, checkin, checkout, currency);
        verify(chargeCalculationService, times(1)).applyExtraChargesToAvailableRooms(hotelId1, checkin, checkout, availableRooms, currency);
    }

    @Test
    public void testSearchAvailability_NoRoomsAvailable() {
        when(availabilityRepository.searchAvailableRooms(hotelId1, checkin, checkout, currency)).thenReturn(List.of());

        final var result = searchAvailabilityService.searchAvailability(new SearchRequest(checkin, checkout, hotelId1, currency));

        assertNotNull(result);
        assertEquals((long) hotelId1, result.hotelId);
        assertTrue(result.availableRooms.isEmpty());

        verify(availabilityRepository, times(1)).searchAvailableRooms(hotelId1, checkin, checkout, currency);
        verify(chargeCalculationService, never()).applyExtraChargesToAvailableRooms(anyLong(),
            any(LocalDate.class),
            any(LocalDate.class),
            anyList(),
            anyString());

        verify(availabilityRepository, times(1)).searchAvailableRooms(hotelId1, checkin, checkout, currency);
        verifyNoInteractions(chargeCalculationService);
    }

    @Test
    public void testGetAvailabilityForBooking_HappyPath() {
        final BookingRequest request = new BookingRequest(hotelId1, guest, roomId1, firstOfMarch, secondOfMarch, currency, payment);
        final AvailableRoom room = AvailableRoom.totalPriceAfterTax(roomId1, BigDecimal.TEN, currency);

        when(availabilityRepository.getAvailabilityForBooking(request.hotelId, request.roomId, request.checkin, request.checkout, request.currency))
            .thenReturn(List.of(room));

        final Optional<AvailableRoom> result = searchAvailabilityService.getAvailabilityForBooking(request);
        assertTrue(result.isPresent());

        assertEquals(room, result.get());

        verify(availabilityRepository, times(1)).getAvailabilityForBooking(request.hotelId,
            request.roomId,
            request.checkin,
            request.checkout,
            request.currency);
    }

    @Test
    public void testGetAvailabilityForBooking_roomNotAvailable() {
        final BookingRequest request = new BookingRequest(hotelId1, guest, roomId1, firstOfMarch, secondOfMarch, currency, payment);

        when(availabilityRepository.getAvailabilityForBooking(request.hotelId, request.roomId, request.checkin, request.checkout, request.currency))
            .thenReturn(List.of());

        final Optional<AvailableRoom> result = searchAvailabilityService.getAvailabilityForBooking(request);
        assertTrue(result.isEmpty());

        verify(availabilityRepository, times(1)).getAvailabilityForBooking(request.hotelId,
            request.roomId,
            request.checkin,
            request.checkout,
            request.currency);
    }
}
