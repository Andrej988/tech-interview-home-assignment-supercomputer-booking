package tech.interview.home.assignment.supercomputer.repositories;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tech.interview.home.assignment.supercomputer.model.domain.Booking;
import tech.interview.home.assignment.supercomputer.model.domain.BookingStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingRepositoryTest {
    @Autowired
    private BookingRepository repository;

    @Test
    @DisplayName("Test check availability on empty database")
    @Order(1)
    void testCheckAvailabilityOnEmptyDatabase() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.MILLIS);

        Mono<Boolean> check = repository.deleteAll().then(repository.isTimeIntervalAlreadyBooked(timeFrom, timeTo));
        StepVerifier
                .create(check)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertFalse(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity and querying for saved entity")
    @Order(2)
    void testSave() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.MILLIS);
        String customerId = "Test customer 123";

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Booking>find = repository.findById(1L);
        Mono<Booking> composite = Mono.from(setup).then(find);

        StepVerifier
                .create(composite)
                .consumeNextWith(insertedBooking -> {
                    assertNotNull(insertedBooking);
                    assertAll(
                            () -> assertEquals(1L, insertedBooking.getId()),
                            () -> assertEquals(customerId, insertedBooking.getCustomerId()),
                            () -> assertEquals(BookingStatus.ACTIVE, insertedBooking.getStatus()),
                            () -> assertEquals(timeFrom, insertedBooking.getTimeIntervalFrom()),
                            () -> assertEquals(timeTo, insertedBooking.getTimeIntervalTo())
                    );
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity first then checking time interval availability")
    @Order(3)
    void testCheckTimeIntervalAvailability() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MILLIS);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5).truncatedTo(ChronoUnit.MILLIS);
        String customerId = "Test customer 123";

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(timeFrom, timeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertTrue(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity first then checking time interval availability: slightly overlapping: Left side")
    @Order(4)
    void testCheckTimeIntervalAvailability2() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(7);
        String customerId = "Test customer 123";

        LocalDateTime overlappingTimeFrom = LocalDateTime.now().plusHours(1);
        LocalDateTime overlappingTimeTo = LocalDateTime.now().plusHours(5);

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(overlappingTimeFrom, overlappingTimeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertTrue(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity first then checking time interval availability: slightly overlapping: Right side")
    @Order(5)
    void testCheckTimeIntervalAvailability3() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5);
        String customerId = "Test customer 123";

        LocalDateTime overlappingTimeFrom = LocalDateTime.now().plusHours(4);
        LocalDateTime overlappingTimeTo = LocalDateTime.now().plusHours(7);

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(overlappingTimeFrom, overlappingTimeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertTrue(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity first then checking time interval availability: slightly overlapping: Is enclosed by")
    @Order(6)
    void testCheckTimeIntervalAvailability4() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(6);
        String customerId = "Test customer 123";

        LocalDateTime overlappingTimeFrom = LocalDateTime.now().plusHours(4);
        LocalDateTime overlappingTimeTo = LocalDateTime.now().plusHours(5);

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(overlappingTimeFrom, overlappingTimeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertTrue(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity first then checking time interval availability: Is available 1")
    @Order(7)
    void testCheckTimeIntervalAvailability5() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5);
        String customerId = "Test customer 123";

        LocalDateTime overlappingTimeFrom = LocalDateTime.now().plusHours(1);
        LocalDateTime overlappingTimeTo = LocalDateTime.now().plusHours(2);

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(overlappingTimeFrom, overlappingTimeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertFalse(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity first then checking time interval availability: Is available 2")
    @Order(8)
    void testCheckTimeIntervalAvailability6() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5);
        String customerId = "Test customer 123";

        LocalDateTime overlappingTimeFrom = LocalDateTime.now().plusHours(6);
        LocalDateTime overlappingTimeTo = LocalDateTime.now().plusHours(8);

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(overlappingTimeFrom, overlappingTimeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertFalse(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test saving entity first then checking time interval availability: Same time interval but first record is in status Cancelled")
    @Order(9)
    void testCheckTimeIntervalAvailability7() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5);
        String customerId = "Test customer 123";

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.CANCELED);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(timeFrom, timeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertFalse(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test Find by customer id: Empty database")
    @Order(10)
    void testFindByCustomerIdEmptyDatabase() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(5);
        String customerId = "Test customer 123";

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.CANCELED);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);

        Mono<Booking> setup = repository.deleteAll().then(repository.save(booking));
        Mono<Boolean> checkAvailability = repository.isTimeIntervalAlreadyBooked(timeFrom, timeTo);
        Mono<Boolean> composite = Mono.from(setup).then(checkAvailability);

        StepVerifier
                .create(composite)
                .consumeNextWith(isAlreadyBooked -> {
                    assertNotNull(isAlreadyBooked);
                    assertFalse(isAlreadyBooked);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test Find by customer id: Some records are inserted")
    @Order(11)
    void testFindByCustomerIdRecordsAreInserted() {
        Booking booking1 = buildDummyBooking();
        Booking booking2 = buildDummyBooking();
        Booking booking3 = buildDummyBooking();

        //Should not be in the list as it is in status canceled
        Booking booking4 = buildDummyBooking();
        booking4.setStatus(BookingStatus.CANCELED);

        Mono<Booking> setup = repository.deleteAll()
                .then(repository.save(booking1))
                .then(repository.save(booking2))
                .then(repository.save(booking3))
                .then(repository.save(booking4));
        Mono<List<Booking>> find = repository.findByCustomerId(Mono.just("Test customer 123")).collect(Collectors.toList());
        Mono<List<Booking>> composite = Mono.from(setup).then(find);

        StepVerifier
                .create(composite)
                .consumeNextWith(x -> {
                    assertNotNull(x);
                    assertEquals(3, x.size());
                })
                .verifyComplete();
    }

    private Booking buildDummyBooking() {
        LocalDateTime timeFrom = LocalDateTime.now().plusHours(3);
        LocalDateTime timeTo = LocalDateTime.now().plusHours(6);
        String customerId = "Test customer 123";

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTimeIntervalFrom(timeFrom);
        booking.setTimeIntervalTo(timeTo);
        return booking;
    }
}