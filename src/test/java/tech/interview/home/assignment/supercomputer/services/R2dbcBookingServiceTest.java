package tech.interview.home.assignment.supercomputer.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.interview.home.assignment.supercomputer.exceptions.BookingAlreadyCanceledException;
import tech.interview.home.assignment.supercomputer.exceptions.ResourceNotFoundException;
import tech.interview.home.assignment.supercomputer.exceptions.TimeIntervalAlreadyBookedException;
import tech.interview.home.assignment.supercomputer.model.domain.Booking;
import tech.interview.home.assignment.supercomputer.model.domain.BookingStatus;
import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;
import tech.interview.home.assignment.supercomputer.model.dto.BookingResponseDto;
import tech.interview.home.assignment.supercomputer.model.dto.CheckBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.dto.NewBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.mappers.BookingMapper;
import tech.interview.home.assignment.supercomputer.repositories.BookingRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class R2dbcBookingServiceTest {
    @Mock
    private BookingRepository repo;

    @Mock
    private BookingMapper mapper;

    @InjectMocks
    private R2dbcBookingService bookingService;

    private static Booking entity;
    private static Booking entity2;
    private static BookingResponseDto responseDto;
    private static BookingResponseDto responseDto2;

    private static final long id = 1L;
    private static final long id2 = 2L;
    private static final String customerId = "Customer12";
    private static final String customerId2 = customerId;
    private static final BookingStatus status = BookingStatus.ACTIVE;
    private static final BookingStatus status2 = BookingStatus.ACTIVE;
    private static final TimeInterval timeInterval = new TimeInterval(LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3));
    private static final TimeInterval timeInterval2 = new TimeInterval(LocalDateTime.now().plusHours(5), LocalDateTime.now().plusHours(7));
    @BeforeAll
    static void setUp() {
        entity = Booking.builder()
            .id(id)
            .customerId(customerId)
            .status(status)
            .timeIntervalFrom(timeInterval.from())
            .timeIntervalTo(timeInterval.to())
            .build();

        entity2 = Booking.builder()
                .id(id2)
                .customerId(customerId2)
                .status(status2)
                .timeIntervalFrom(timeInterval2.from())
                .timeIntervalTo(timeInterval2.to())
                .build();

        responseDto = new BookingResponseDto(id, customerId, status.name(), timeInterval);
        responseDto2 = new BookingResponseDto(id2, customerId2, status2.name(), timeInterval2);
    }

    @Test
    @DisplayName("Find by id: Happy case")
    void findById() {
        when(repo.findById(id)).thenReturn(Mono.just(entity));
        when(mapper.entityToResponseDto(entity)).thenReturn(responseDto);
        var find = bookingService.findById(id);

        StepVerifier
                .create(find)
                .consumeNextWith(booking -> {
                    assertNotNull(booking);
                    assertAll(
                            () -> Assertions.assertEquals(id, booking.id()),
                            () -> Assertions.assertEquals(customerId, booking.customerId()),
                            () -> Assertions.assertEquals(status.name(), booking.status()),
                            () -> Assertions.assertEquals(timeInterval.from(), booking.timeInterval().from()),
                            () -> Assertions.assertEquals(timeInterval.to(), booking.timeInterval().to())
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Find by id: Resource not found")
    void findByIdEmpty() {
        when(repo.findById(id)).thenReturn(Mono.empty());
        var find = bookingService.findById(id);

        StepVerifier
                .create(find)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Find for customer: Empty")
    void findForCustomerEmpty() {
        when(repo.findByCustomerId(any())).thenReturn(Flux.empty());
        var find = bookingService.findForCustomer(Mono.just(customerId));

        StepVerifier
                .create(find)
                .verifyComplete();
    }

    @Test
    @DisplayName("Find for customer: One item")
    void findForCustomerOneItem() {
        when(repo.findByCustomerId(any())).thenReturn(Flux.just(entity));
        when(mapper.entityToResponseDto(entity)).thenReturn(responseDto);
        var find = bookingService.findForCustomer(Mono.just(customerId));

        StepVerifier
                .create(find)
                .consumeNextWith(booking -> {
                    assertNotNull(booking);
                    assertAll(
                            () -> Assertions.assertEquals(id, booking.id()),
                            () -> Assertions.assertEquals(customerId, booking.customerId()),
                            () -> Assertions.assertEquals(status.name(), booking.status()),
                            () -> Assertions.assertEquals(timeInterval.from(), booking.timeInterval().from()),
                            () -> Assertions.assertEquals(timeInterval.to(), booking.timeInterval().to())
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Find for customer: Multiple items")
    void findForCustomerMultipleItems() {
        when(repo.findByCustomerId(any())).thenReturn(Flux.just(entity, entity2));
        when(mapper.entityToResponseDto(entity)).thenReturn(responseDto);
        when(mapper.entityToResponseDto(entity2)).thenReturn(responseDto2);
        var find = bookingService.findForCustomer(Mono.just(customerId));

        StepVerifier
                .create(find)
                .consumeNextWith(booking -> {
                    assertNotNull(booking);
                    assertAll(
                            () -> Assertions.assertEquals(id, booking.id()),
                            () -> Assertions.assertEquals(customerId, booking.customerId()),
                            () -> Assertions.assertEquals(status.name(), booking.status()),
                            () -> Assertions.assertEquals(timeInterval.from(), booking.timeInterval().from()),
                            () -> Assertions.assertEquals(timeInterval.to(), booking.timeInterval().to())
                    );
                })
                .consumeNextWith(booking -> {
                    assertNotNull(booking);
                    assertAll(
                            () -> Assertions.assertEquals(id2, booking.id()),
                            () -> Assertions.assertEquals(customerId2, booking.customerId()),
                            () -> Assertions.assertEquals(status2.name(), booking.status()),
                            () -> Assertions.assertEquals(timeInterval2.from(), booking.timeInterval().from()),
                            () -> Assertions.assertEquals(timeInterval2.to(), booking.timeInterval().to())
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Check time interval: Not booked")
    void checkTimeInterval() {
        when(repo.isTimeIntervalAlreadyBooked(timeInterval.from(), timeInterval.to())).thenReturn(Mono.just(false));
        var requestDto = new CheckBookingRequestDto(timeInterval);
        var checkTimeInterval = bookingService.checkTimeInterval(Mono.just(requestDto));

        StepVerifier
                .create(checkTimeInterval)
                .consumeNextWith(response -> {
                    assertNotNull(response);
                    assertAll(
                            () -> Assertions.assertEquals(timeInterval, response.timeInterval()),
                            () -> Assertions.assertTrue(response.available())
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Check time interval: Already booked")
    void checkTimeIntervalAlreadyBooked() {
        when(repo.isTimeIntervalAlreadyBooked(timeInterval.from(), timeInterval.to())).thenReturn(Mono.just(true));
        var requestDto = new CheckBookingRequestDto(timeInterval);
        var checkTimeInterval = bookingService.checkTimeInterval(Mono.just(requestDto));

        StepVerifier
                .create(checkTimeInterval)
                .consumeNextWith(response -> {
                    assertNotNull(response);
                    assertAll(
                            () -> Assertions.assertEquals(timeInterval, response.timeInterval()),
                            () -> Assertions.assertFalse(response.available())
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Add booking: Happy case")
    void addBookingSuccessful() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto(customerId, timeInterval);
        Booking mappedEntity = Booking.builder()
                .customerId(customerId)
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(timeInterval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);
        when(repo.isTimeIntervalAlreadyBooked(mappedEntity.getTimeIntervalFrom(), mappedEntity.getTimeIntervalTo())).thenReturn(Mono.just(false));

        when(repo.save(mappedEntity)).thenAnswer(x -> {
            Booking booking = (Booking)x.getArguments()[0];
            booking.setId(id);
            return Mono.just(booking);
        });

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .consumeNextWith(res -> {
                    assertNotNull(res);
                    assertEquals(id, res);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Add booking: Missing time interval from")
    void addBookingMissingTimeIntervalFrom() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto(customerId, new TimeInterval(null, timeInterval.to()));
        Booking mappedEntity = Booking.builder()
                .customerId(customerId)
                .timeIntervalFrom(null)
                .timeIntervalTo(timeInterval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Missing time interval to")
    void addBookingMissingTimeIntervalTo() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto(customerId, new TimeInterval(timeInterval.from(), null));
        Booking mappedEntity = Booking.builder()
                .customerId(customerId)
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(null)
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Missing customer id: Null value")
    void addBookingMissingCustomerId() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto(null, new TimeInterval(timeInterval.from(), timeInterval.to()));
        Booking mappedEntity = Booking.builder()
                .customerId(null)
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(timeInterval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Missing customer id: Empty string")
    void addBookingEmptyCustomerId() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto("", new TimeInterval(timeInterval.from(), timeInterval.to()));
        Booking mappedEntity = Booking.builder()
                .customerId("")
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(timeInterval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Missing customer id: Contains blank space")
    void addBookingCustomerIdContainsBlankSpace() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto("Test customer", new TimeInterval(timeInterval.from(), timeInterval.to()));
        Booking mappedEntity = Booking.builder()
                .customerId("Test customer")
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(timeInterval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Missing customer id: Contains special characters")
    void addBookingCustomerIdContainsSpecialCharacters() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto("Test/customer", new TimeInterval(timeInterval.from(), timeInterval.to()));
        Booking mappedEntity = Booking.builder()
                .customerId("Test/customer")
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(timeInterval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Time interval in the past")
    void addBookingMissingTimeIntervalInThePast() {
        TimeInterval interval = new TimeInterval(LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(3));
        NewBookingRequestDto requestDto = new NewBookingRequestDto(customerId, interval);
        Booking mappedEntity = Booking.builder()
                .customerId(customerId)
                .timeIntervalFrom(interval.from())
                .timeIntervalTo(interval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Time interval from is after time interval to")
    void addBookingMissingTimeIntervalFromAfterTo() {
        TimeInterval interval = new TimeInterval(LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(6));
        NewBookingRequestDto requestDto = new NewBookingRequestDto(customerId, interval);
        Booking mappedEntity = Booking.builder()
                .customerId(customerId)
                .timeIntervalFrom(interval.from())
                .timeIntervalTo(interval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Add booking: Time interval already booked")
    void addBookingTimeIntervalAlreadyBooked() {
        NewBookingRequestDto requestDto = new NewBookingRequestDto(customerId, timeInterval);
        Booking mappedEntity = Booking.builder()
                .customerId(customerId)
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(timeInterval.to())
                .build();

        when(mapper.requestDtoToEntity(requestDto)).thenReturn(mappedEntity);
        when(repo.isTimeIntervalAlreadyBooked(mappedEntity.getTimeIntervalFrom(), mappedEntity.getTimeIntervalTo())).thenReturn(Mono.just(true));

        var addBooking = bookingService.addBooking(Mono.just(requestDto));

        StepVerifier
                .create(addBooking)
                .expectError(TimeIntervalAlreadyBookedException.class)
                .verify();
    }

    @Test
    @DisplayName("Cancel booking: Happy case")
    void cancelBooking() {
        when(repo.findById(id)).thenReturn(Mono.just(entity));
        when(repo.save(entity)).thenReturn(Mono.just(entity));

        var cancel = bookingService.cancelBooking(id);

        StepVerifier
                .create(cancel)
                .consumeNextWith(Assertions::assertTrue)
                .verifyComplete();
    }

    @Test
    @DisplayName("Cancel booking: Non-existent booking")
    void cancelBookingNonExistentBooking() {
        when(repo.findById(id)).thenReturn(Mono.empty());

        var cancel = bookingService.cancelBooking(id);

        StepVerifier
                .create(cancel)
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Cancel booking: Booking was already canceled")
    void cancelBookingOnAlreadyCanceledBooking() {
        Booking alreadyCanceledBooking = Booking.builder()
                .id(id)
                .customerId(customerId)
                .status(BookingStatus.CANCELED)
                .timeIntervalFrom(timeInterval.from())
                .timeIntervalTo(timeInterval.to())
                .build();
        when(repo.findById(id)).thenReturn(Mono.just(alreadyCanceledBooking));

        var cancel = bookingService.cancelBooking(id);

        StepVerifier
                .create(cancel)
                .expectError(BookingAlreadyCanceledException.class)
                .verify();
    }
}