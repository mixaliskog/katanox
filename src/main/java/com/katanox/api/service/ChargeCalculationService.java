package com.katanox.api.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.charges.ExtraCharges;
import com.katanox.api.exception.BookingException;
import com.katanox.api.repository.ExtraChargesRepository;

@Service
public class ChargeCalculationService {
    static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private final ExtraChargesRepository extraChargesRepository;

    public ChargeCalculationService(final ExtraChargesRepository extraChargesRepository) {
        this.extraChargesRepository = extraChargesRepository;
    }

    public AvailableRoom applyExtraChargesForBooking(final BookingRequest booking,
        final AvailableRoom room) {
        return applyExtraChargesToAvailableRooms(booking.hotelId, booking.checkin, booking.checkout, List.of(room),
            booking.currency).stream().findFirst().orElseThrow(() -> new BookingException("Could not calculate charges for booking"));
    }

    public List<AvailableRoom> applyExtraChargesToAvailableRooms(final long hotelId, final LocalDate checkin,
        final LocalDate checkout,
        final List<AvailableRoom> rooms, final String currency) {
        final var numberOfNights = (int) DAYS.between(checkin, checkout);
        final var extraCharges = extraChargesRepository.getExtraCharges(hotelId, numberOfNights);
        return rooms.stream().map(room -> applyChargesToRoom(room, extraCharges, currency)).collect(Collectors.toList());
    }

    AvailableRoom applyChargesToRoom(final AvailableRoom room, final ExtraCharges extraCharges, final String currency) {
        // Calculate after-tax price
        final var updatedAfterTaxPrice =
            applyExtraCharges(room.totalPriceAfterTax, room.firstNightPriceAfterTax, extraCharges);
        // Calculate before-tax price if applicable
        final var updatedBeforeTaxPrice = room.totalPriceBeforeTax.compareTo(BigDecimal.ZERO) > 0
            ? applyExtraCharges(room.totalPriceBeforeTax, room.firstNightPriceBeforeTax, extraCharges)
            : BigDecimal.ZERO;

        return AvailableRoom.totalPricesOnly(room.roomId, updatedAfterTaxPrice, updatedBeforeTaxPrice, currency);
    }

    private BigDecimal applyExtraCharges(final BigDecimal totalPrice, final BigDecimal firstNightPrice, final ExtraCharges extraCharges) {
        final var totalAmountCharges = calculatePercentage(totalPrice, extraCharges.totalAmountPercentage);
        final var firstNightCharges = calculatePercentage(firstNightPrice, extraCharges.firstNightAmountPercentage);
        return totalPrice.add(extraCharges.flatCharges).add(totalAmountCharges).add(firstNightCharges);
    }

    private BigDecimal calculatePercentage(final BigDecimal value, final BigDecimal percentage) {
        if (BigDecimal.ZERO.equals(percentage) || BigDecimal.ZERO.equals(value)) {
            return BigDecimal.ZERO;
        }
        return value.multiply(percentage).divide(HUNDRED).setScale(2, RoundingMode.HALF_EVEN);
    }
}
