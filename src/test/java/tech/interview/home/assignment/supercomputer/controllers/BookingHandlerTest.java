package tech.interview.home.assignment.supercomputer.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;
import tech.interview.home.assignment.supercomputer.model.dto.BookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.NewBookingRequestDto;
import tech.interview.home.assignment.supercomputer.services.BookingService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingHandlerTest {
    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingHandler bookingHandler;

    private final static String customerId = "Customer123";
    private final static TimeInterval timeInterval = new TimeInterval(LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(4));

    private static BookingResponseDto dto;

    @BeforeAll
    static void setUp() {
        dto = new BookingResponseDto(1, customerId, "ACTIVE", new TimeInterval(LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(4)));
    }

    @Test
    @DisplayName("Test get bookings for customer")
    void getBookings() {
        var req = MockServerRequest.builder()
                .pathVariable("customerId", "1")
                .build();

        when(bookingService.findForCustomer(any(Mono.class))).thenReturn(Flux.just(dto));
        var response = bookingHandler.getBookings(req);

        StepVerifier
                .create(response)
                .consumeNextWith(res -> {
                    assertNotNull(res);
                    assertEquals(HttpStatus.OK,  res.statusCode());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test get booking by id")
    void getBookingById() {
        var req = MockServerRequest.builder()
                .pathVariable("bookingId", "1")
                .build();

        when(bookingService.findById(1L)).thenReturn(Mono.just(dto));
        var response = bookingHandler.getBookingById(req);

        StepVerifier
                .create(response)
                .consumeNextWith(res -> {
                    assertNotNull(res);
                    assertEquals(HttpStatus.OK,  res.statusCode());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test check booking time interval")
    void checkBooking() {
        var requestDto = new CheckBookingRequestDto(timeInterval);
        var req = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        when(bookingService.checkTimeInterval(any())).thenReturn(Mono.just(new CheckBookingResponseDto(timeInterval, true)));
        var response = bookingHandler.checkBooking(req);

        StepVerifier
                .create(response)
                .consumeNextWith(res -> {
                    assertNotNull(res);
                    assertEquals(HttpStatus.OK,  res.statusCode());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test add booking")
    void addBooking() {
        var requestDto = new NewBookingRequestDto(customerId, timeInterval);
        var req = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        long id = 1L;

        when(bookingService.addBooking(any())).thenReturn(Mono.just(id));
        var response = bookingHandler.addBooking(req);

        StepVerifier
                .create(response)
                .consumeNextWith(res -> {
                    assertNotNull(res);
                    assertAll(
                            () -> assertEquals(HttpStatus.CREATED, res.statusCode()),
                            () -> assertEquals("/api/supercomputer/v1/bookings/id/" + id, res.headers().get("location").get(0))
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test cancel booking")
    void cancelBooking() {
        var req = MockServerRequest.builder()
                .pathVariable("bookingId", "1")
                .build();

        when(bookingService.cancelBooking(1L)).thenReturn(Mono.just(true));

        var response = bookingHandler.cancelBooking(req);

        StepVerifier
                .create(response)
                .consumeNextWith(res -> {
                    assertNotNull(res);
                    assertEquals(HttpStatus.NO_CONTENT, res.statusCode());
                })
                .verifyComplete();
    }
}