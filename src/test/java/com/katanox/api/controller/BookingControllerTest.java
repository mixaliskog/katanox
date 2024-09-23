package com.katanox.api.controller;

import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.firstOfMarch;
import static com.katanox.api.util.TestUtil.guest;
import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.payment;
import static com.katanox.api.util.TestUtil.roomId1;
import static com.katanox.api.util.TestUtil.secondOfMarch;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.BookingDTO;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.booking.UpdateBookingRequest;
import com.katanox.api.service.BookingService;
import com.katanox.api.service.LogWriterService;
import com.katanox.test.sql.enums.BookingStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = BookingController.class,
    excludeAutoConfiguration= SecurityAutoConfiguration.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private BookingService bookingService;
    @MockBean
    private LogWriterService logWriterService;

    private BookingController bookingController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        bookingController = new BookingController(bookingService, logWriterService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testBooking_HappyPath() throws Exception {
        final var bookingRequest = new BookingRequest(hotelId1, guest, roomId1, firstOfMarch, secondOfMarch, currency, payment);
        final Long bookingId = 34L;
        final AvailableRoom room = AvailableRoom.totalPriceAfterTax(roomId1, BigDecimal.TEN, currency);
        final var bookingDTO = new BookingDTO(hotelId1, bookingId, guest, room, firstOfMarch, secondOfMarch, currency, payment, BookingStatus.booked);

        // Mock the booking service and log writer
        when(bookingService.booking(bookingRequest)).thenReturn(bookingDTO);

        mockMvc.perform(post("/booking/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.bookingId").value(bookingId))
            .andExpect(jsonPath("$.hotelId").value(hotelId1))
            .andExpect(jsonPath("$.guest.name").value(guest.name))
            .andExpect(jsonPath("$.guest.surname").value(guest.surname))
            .andExpect(jsonPath("$.room.roomId").value(roomId1))
            .andExpect(jsonPath("$.room.price").value(room.totalPriceAfterTax))
            .andExpect(jsonPath("$.room.currency").value(room.currency));

        // Verify interactions
        verify(bookingService, times(1)).booking(bookingRequest);
        verify(logWriterService, times(1)).logStringToConsoleOutput(anyString());
    }

    @Test
    public void testUpdateBooking_Successful() throws Exception {
        final var updateBookingRequest = new UpdateBookingRequest(34L, BookingStatus.booked);


        mockMvc.perform(post("/booking/update/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookingRequest)))
            .andExpect(status().isOk());

        // Verify that the updateBooking method was called
        verify(bookingService, times(1)).updateBooking(updateBookingRequest);
    }

}
