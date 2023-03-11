package tech.interview.home.assignment.supercomputer.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.interview.home.assignment.supercomputer.model.dto.BookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.NewBookingRequestDto;

public interface BookingService {
    Mono<BookingResponseDto> findById(Long id);
    Flux<BookingResponseDto> findForCustomer(Mono<String> customerId);
    Mono<CheckBookingResponseDto> checkTimeInterval(Mono<CheckBookingRequestDto> timeInterval);
    Mono<Long> addBooking(Mono<NewBookingRequestDto> booking);
    Mono<Boolean> cancelBooking(Long bookingId);
}
