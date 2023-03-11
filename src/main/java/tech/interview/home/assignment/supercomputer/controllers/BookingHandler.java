package tech.interview.home.assignment.supercomputer.controllers;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tech.interview.home.assignment.supercomputer.config.RouterConfig;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.dto.NewBookingRequestDto;
import tech.interview.home.assignment.supercomputer.services.BookingService;

import java.net.URI;
import java.util.stream.Collectors;

@Component
public class BookingHandler {
    private final BookingService service;

    public BookingHandler(final BookingService service) {
        this.service = service;
    }

    public Mono<ServerResponse> getBookings(ServerRequest request) {
        return this.service.findForCustomer(Mono.just(request.pathVariable(RouterConfig.CUSTOMER_ID_PATH_VARIABLE)))
                .collect(Collectors.toList())
                .flatMap(x -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(x)));
    }

    public Mono<ServerResponse> getBookingById(ServerRequest request) {
        return this.service.findById(Long.parseLong(request.pathVariable(RouterConfig.BOOKING_ID_PATH_VARIABLE)))
                .flatMap(x -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(x))
                );
    }

    public Mono<ServerResponse> checkBooking(ServerRequest request) {
        return this.service.checkTimeInterval(request.bodyToMono(CheckBookingRequestDto.class))
                .flatMap(x -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(x)));
    }

    public Mono<ServerResponse> addBooking(ServerRequest request) {
        return this.service.addBooking(request.bodyToMono(NewBookingRequestDto.class))
                .flatMap(bookingId -> ServerResponse
                                .created(URI.create(RouterConfig.VIEW_BOOKING_BY_ID_BASE + bookingId.toString()))
                                .build()
                );
    }

    public Mono<ServerResponse> cancelBooking(ServerRequest request) {
        return this.service.cancelBooking(Long.parseLong(request.pathVariable(RouterConfig.BOOKING_ID_PATH_VARIABLE)))
                .flatMap(x -> ServerResponse
                        .noContent()
                        .build());
    }
}
