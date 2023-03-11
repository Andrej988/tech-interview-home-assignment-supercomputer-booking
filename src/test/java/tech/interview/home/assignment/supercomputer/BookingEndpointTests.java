package tech.interview.home.assignment.supercomputer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.interview.home.assignment.supercomputer.config.RouterConfig;
import tech.interview.home.assignment.supercomputer.controllers.BookingHandler;
import tech.interview.home.assignment.supercomputer.model.domain.BookingStatus;
import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;
import tech.interview.home.assignment.supercomputer.model.dto.BookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingResponseDto;
import tech.interview.home.assignment.supercomputer.services.BookingService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class BookingEndpointTests {
    private BookingService bookingService;
    private WebTestClient client;

    private static BookingResponseDto responseDto;
    private static BookingResponseDto responseDto2;
    private static final long id = 15L;
    private static final String customerId = "Customer123";
    private static final String status = BookingStatus.ACTIVE.name();
    private static final TimeInterval timeInterval = new TimeInterval(LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(4));

    @BeforeAll
    static void setUp() {
        responseDto = new BookingResponseDto(id, customerId, status, timeInterval);
        responseDto2 = new BookingResponseDto(16L, customerId, status, new TimeInterval(LocalDateTime.now().plusHours(6), LocalDateTime.now().plusHours(8)));
    }

    @BeforeEach
    void setUpBeforeEach() {
        bookingService = mock(BookingService.class);
        BookingHandler bookingHandler = new BookingHandler(bookingService);
        RouterFunction<?> routes = new RouterConfig().routes(bookingHandler);
        client = WebTestClient
                .bindToRouterFunction(routes)
                .build();
    }

    @Test
    @DisplayName("Test get booking by id")
    void getBookingById() {
        when(bookingService.findById(any())).thenReturn(Mono.just(responseDto));

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/supercomputer/v1/bookings/id/" + id)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.CONTENT_TYPE)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.customerId").isEqualTo(customerId)
                .jsonPath("$.status").isEqualTo(status)
                .jsonPath("$.timeInterval.from").isNotEmpty()
                .jsonPath("$.timeInterval.to").isNotEmpty();
    }

    @Test
    @DisplayName("Test get bookings for customer")
    void getCustomerBookings() {
        when(bookingService.findForCustomer(any())).thenReturn(Flux.just(responseDto, responseDto2));

        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/supercomputer/v1/bookings/customer/" + customerId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.CONTENT_TYPE)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$[0].id").isNotEmpty()
                .jsonPath("$[1].id").isNotEmpty();
    }

    @Test
    @DisplayName("Test check time interval")
    void testCheckTimeInterval() {
        when(bookingService.checkTimeInterval(any())).thenReturn(Mono.just(new CheckBookingResponseDto(timeInterval, true)));

        client.post()
                .uri(uriBuilder -> uriBuilder.path("/api/supercomputer/v1/bookings/check")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.CONTENT_TYPE)
                .expectHeader().valueMatches(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .expectBody()
                .jsonPath("$.timeInterval.from").isNotEmpty()
                .jsonPath("$.timeInterval.to").isNotEmpty()
                .jsonPath("$.available").isEqualTo("true");
    }

    @Test
    @DisplayName("Test add booking")
    void testAddBooking() {
        when(bookingService.addBooking(any())).thenReturn(Mono.just(id));

        client.post()
                .uri(uriBuilder -> uriBuilder.path("/api/supercomputer/v1/bookings/add")
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("location")
                .expectHeader().valueMatches("location", "/api/supercomputer/v1/bookings/id/" + id)
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("Test cancellation of booking")
    void testCancellationOfBooking() {
        when(bookingService.cancelBooking(any())).thenReturn(Mono.just(true));

        client.put()
                .uri(uriBuilder -> uriBuilder.path("/api/supercomputer/v1/bookings/cancel/" + id)
                        .build())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Test non-existing endpoint: 404 Not found")
    void testNonExistingEndpoint() {
        client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/supercomputer/v1/bookings/non-existing-endpoint")
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }
}
