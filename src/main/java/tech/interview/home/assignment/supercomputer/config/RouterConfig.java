package tech.interview.home.assignment.supercomputer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import tech.interview.home.assignment.supercomputer.controllers.BookingHandler;
import tech.interview.home.assignment.supercomputer.exceptions.BookingAlreadyCanceledException;
import tech.interview.home.assignment.supercomputer.exceptions.ResourceNotFoundException;
import tech.interview.home.assignment.supercomputer.exceptions.TimeIntervalAlreadyBookedException;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {
    private static final String ENDPOINT_BASE = "/api/supercomputer/v1/bookings/";
    public static final String CUSTOMER_ID_PATH_VARIABLE = "customerId";
    private static final String VIEW_BOOKINGS_FOR_CUSTOMER =  ENDPOINT_BASE + "customer/{" + CUSTOMER_ID_PATH_VARIABLE + "}";
    public static final String BOOKING_ID_PATH_VARIABLE = "bookingId";
    public static final String VIEW_BOOKING_BY_ID_BASE =  ENDPOINT_BASE + "id/";
    private static final String VIEW_BOOKING_BY_ID =  VIEW_BOOKING_BY_ID_BASE + "{" + BOOKING_ID_PATH_VARIABLE + "}";
    private static final String CHECK_BOOKING =  ENDPOINT_BASE + "check";
    private static final String ADD_BOOKING =  ENDPOINT_BASE + "add";
    private static final String CANCEL_BOOKING =  ENDPOINT_BASE + "cancel/" + "{" + BOOKING_ID_PATH_VARIABLE + "}";

    @Bean
    public RouterFunction<ServerResponse> routes(BookingHandler handler) {
        return route()
                .GET(VIEW_BOOKINGS_FOR_CUSTOMER, accept(APPLICATION_JSON), handler::getBookings)
                .GET(VIEW_BOOKING_BY_ID, accept(APPLICATION_JSON), handler::getBookingById)
                .POST(CHECK_BOOKING, accept(APPLICATION_JSON), handler::checkBooking)
                .POST(ADD_BOOKING, accept(APPLICATION_JSON), handler::addBooking)
                .PUT(CANCEL_BOOKING, accept(APPLICATION_JSON), handler::cancelBooking)
                .filter(resourceNotFoundExceptionHandler())
                .filter(resourceIllegalArgumentExceptionHandler())
                .filter(timeIntervalAlreadyBookedExceptionHandler())
                .filter(bookingAlreadyCanceledExceptionHandler())
                .build();
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse> resourceNotFoundExceptionHandler() {
        return (request, next) -> next.handle(request)
                .onErrorResume(ResourceNotFoundException.class, e -> ServerResponse.notFound().build());
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse> resourceIllegalArgumentExceptionHandler() {
        return (request, next) -> next.handle(request)
                .onErrorResume(IllegalArgumentException.class, e -> {
                    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
                    problemDetail.setTitle("Illegal arguments");
                    return ServerResponse.badRequest().body(BodyInserters.fromValue(problemDetail));
                });
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse> timeIntervalAlreadyBookedExceptionHandler() {
        return (request, next) -> next.handle(request)
                .onErrorResume(TimeIntervalAlreadyBookedException.class, e -> {
                    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
                    problemDetail.setTitle("Time interval already booked");
                    return ServerResponse.badRequest().body(BodyInserters.fromValue(problemDetail));
                });
    }

    private HandlerFilterFunction<ServerResponse, ServerResponse> bookingAlreadyCanceledExceptionHandler() {
        return (request, next) -> next.handle(request)
                .onErrorResume(BookingAlreadyCanceledException.class, e -> {
                    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
                    problemDetail.setTitle("Booking already canceled");
                    return ServerResponse.badRequest().body(BodyInserters.fromValue(problemDetail));
                });
    }
}

