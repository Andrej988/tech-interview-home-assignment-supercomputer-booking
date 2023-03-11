package tech.interview.home.assignment.supercomputer.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.interview.home.assignment.supercomputer.exceptions.BookingAlreadyCanceledException;
import tech.interview.home.assignment.supercomputer.exceptions.ResourceNotFoundException;
import tech.interview.home.assignment.supercomputer.exceptions.TimeIntervalAlreadyBookedException;
import tech.interview.home.assignment.supercomputer.model.domain.Booking;
import tech.interview.home.assignment.supercomputer.model.domain.BookingStatus;
import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;
import tech.interview.home.assignment.supercomputer.model.dto.BookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.NewBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.mappers.BookingMapper;
import tech.interview.home.assignment.supercomputer.repositories.BookingRepository;

import java.time.LocalDateTime;

@Service
public class R2dbcBookingService implements BookingService {
    private final BookingRepository repo;
    private final BookingMapper mapper;

    public R2dbcBookingService(final BookingRepository repo,
                               final BookingMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Mono<BookingResponseDto> findById(Long id) {
        return findByIdReturnEntity(id)
                .map(this.mapper::entityToResponseDto);
    }

    @Override
    public Flux<BookingResponseDto> findForCustomer(Mono<String> customerId) {
        return this.repo.findByCustomerId(customerId)
                .map(this.mapper::entityToResponseDto);
    }

    @Override
    public Mono<CheckBookingResponseDto> checkTimeInterval(Mono<CheckBookingRequestDto> timeInterval) {
        return timeInterval.map(x -> {
            checkDates(x.timeInterval().from(), x.timeInterval().to());
            return x;
        }).flatMap(req -> this.repo.isTimeIntervalAlreadyBooked(req.timeInterval().from(), req.timeInterval().to())
                .map(x -> !x)
                .map(x -> new CheckBookingResponseDto(req.timeInterval(), x)));
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Mono<Long> addBooking(Mono<NewBookingRequestDto> booking) {
        return booking
                .map(this.mapper::requestDtoToEntity)
                .map(req -> {
                    checkData(req);
                    req.setStatus(BookingStatus.ACTIVE);
                    return req;
                })
                .flatMap(req -> this.repo.isTimeIntervalAlreadyBooked(req.getTimeIntervalFrom(), req.getTimeIntervalTo())
                        .map(alreadyBooked -> {
                            if(alreadyBooked) {
                                throw new TimeIntervalAlreadyBookedException(new TimeInterval(req.getTimeIntervalFrom(), req.getTimeIntervalTo()));
                            }
                            this.repo.save(req).subscribe();
                            return req;
                        }))
                .map(Booking::getId);
    }

    @Override
    public Mono<Boolean> cancelBooking(Long bookingId) {
        return findByIdReturnEntity(bookingId).doOnNext(x -> {
            if(x.getStatus() != BookingStatus.ACTIVE) {
                throw new BookingAlreadyCanceledException("Booking with ID "+ bookingId + " is already canceled!");
            }

            x.setStatus(BookingStatus.CANCELED);
            this.repo.save(x).subscribe();
        }).flatMap(x -> Mono.just(true));
    }

    private Mono<Booking> findByIdReturnEntity(Long id) {
        return this.repo.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Booking with id " + id + " does not exist!")));
    }

    private void checkData(Booking booking) {
        checkCustomerId(booking.getCustomerId());
        checkDates(booking.getTimeIntervalFrom(), booking.getTimeIntervalTo());
    }

    private void checkCustomerId(String customerId) {
        if(customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("Illegal arguments: Customer ID is not present!");
        }

        if(!customerId.matches("^[a-zA-Z0-9]*$")) {
            throw new IllegalArgumentException("Illegal arguments: Customer ID should contain only alphanumeric characters!");
        }
    }

    private void checkDates(LocalDateTime intervalFrom, LocalDateTime intervalTo) {
        checkIfDatesArePresent(intervalFrom, intervalTo);
        checkDatesFromAfterTo(intervalFrom, intervalTo);
        checkDatesInThePast(intervalFrom);
    }

    private void checkIfDatesArePresent(LocalDateTime intervalFrom, LocalDateTime intervalTo) {
        if(intervalFrom == null) {
            throw new IllegalArgumentException("Illegal time interval arguments: Missing value for time interval from!!!");
        }

        if(intervalTo == null) {
            throw new IllegalArgumentException("Illegal time interval arguments: Missing value for time interval to!!!");
        }
    }

    private void checkDatesFromAfterTo(LocalDateTime intervalFrom, LocalDateTime intervalTo) {
        if (intervalFrom.isAfter(intervalTo) || intervalFrom.isEqual(intervalTo)) {
            throw new IllegalArgumentException("Illegal time interval arguments: Time interval from must be before time interval to!!!");
        }
    }

    private void checkDatesInThePast(LocalDateTime intervalFrom) {
        if(intervalFrom.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Illegal time interval: Time interval should not be in the past!");
        }
    }
}
