package com.katanox.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.search.SearchRequest;
import com.katanox.api.domain.search.SearchResponse;
import com.katanox.api.repository.AvailabilityRepository;

@Service
public class SearchAvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ChargeCalculationService chargeCalculationService;

    public SearchAvailabilityService(final AvailabilityRepository availabilityRepository,
        final ChargeCalculationService chargeCalculationService) {
        this.availabilityRepository = availabilityRepository;
        this.chargeCalculationService = chargeCalculationService;
    }

    public SearchResponse searchAvailability(final SearchRequest request) {
        final var availableRooms =
            availabilityRepository.searchAvailableRooms(request.hotelId, request.checkin, request.checkout, request.currency);

        if (availableRooms.isEmpty()) {
            return SearchResponse.empty(request.hotelId);
        }

        final List<AvailableRoom> roomsWithEtraCharges = chargeCalculationService.applyExtraChargesToAvailableRooms(request.hotelId,
            request.checkin,
            request.checkout,
            availableRooms,
            request.currency);

        return new SearchResponse(request.hotelId, roomsWithEtraCharges);
    }

    public Optional<AvailableRoom> getAvailabilityForBooking(final BookingRequest bookingRequest) {
        return availabilityRepository.getAvailabilityForBooking(bookingRequest.hotelId, bookingRequest.roomId,
                bookingRequest.checkin, bookingRequest.checkout, bookingRequest.currency)
            .stream().findFirst();
    }

}
