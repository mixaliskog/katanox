package com.katanox.api.controller;

import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.hotelId1;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.katanox.api.domain.booking.AvailableRoom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.katanox.api.domain.search.SearchRequest;
import com.katanox.api.domain.search.SearchResponse;
import com.katanox.api.service.LogWriterService;
import com.katanox.api.service.SearchAvailabilityService;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SearchController.class,
    excludeAutoConfiguration= SecurityAutoConfiguration.class)
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SearchAvailabilityService searchAvailabilityService;
    @MockBean
    private LogWriterService logWriterService;

    private SearchController searchController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        searchController = new SearchController(logWriterService, searchAvailabilityService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(searchController).build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testSearchAvailability_HappyPath() throws Exception {
        final LocalDate checkin = LocalDate.of(2023, 9, 1);
        final LocalDate checkout = LocalDate.of(2023, 9, 3);
        final String usd = "USD";
        final SearchRequest request = new SearchRequest(checkin, checkout, hotelId1, usd);

        final var availableRoom1 = AvailableRoom.totalPriceAfterTax(1L, new BigDecimal("150"), usd);
        final var availableRoom2 = AvailableRoom.totalPriceAfterTax(2L, new BigDecimal("200"), usd);

        SearchResponse searchResult = new SearchResponse(hotelId1, List.of(availableRoom1, availableRoom2));

        when(searchAvailabilityService.searchAvailability(request)).thenReturn(searchResult);

        mockMvc.perform(post("/search/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.hotelId").value(1L))
            .andExpect(jsonPath("$.availableRooms[0].roomId").value(1L))
            .andExpect(jsonPath("$.availableRooms[0].price").value("150"))
            .andExpect(jsonPath("$.availableRooms[0].currency").value(usd))
            .andExpect(jsonPath("$.availableRooms[1].roomId").value(2L))
            .andExpect(jsonPath("$.availableRooms[1].price").value("200"))
            .andExpect(jsonPath("$.availableRooms[1].currency").value(usd));

        verify(searchAvailabilityService, times(1)).searchAvailability(request);
        verify(logWriterService, times(1)).logStringToConsoleOutput(anyString());
    }

    @Test
    public void testSearchAvailability_NoRoomsAvailable() throws Exception {
        SearchRequest request = new SearchRequest(LocalDate.of(2023, 9, 1), LocalDate.of(2023, 9, 3), hotelId1, currency);

        when(searchAvailabilityService.searchAvailability(request))
            .thenReturn(SearchResponse.empty(hotelId1));

        mockMvc.perform(post("/search/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.availableRooms").isEmpty());

        verify(searchAvailabilityService, times(1)).searchAvailability(request);
    }

}
