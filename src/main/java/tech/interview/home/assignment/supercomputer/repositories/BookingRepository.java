package tech.interview.home.assignment.supercomputer.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.interview.home.assignment.supercomputer.model.domain.Booking;

import java.time.LocalDateTime;

public interface BookingRepository extends ReactiveCrudRepository<Booking, Long> {
    @Query("SELECT * " +
            "FROM BOOKINGS " +
            "WHERE STATUS = 'ACTIVE' " +
            "AND CUSTOMER_ID = :customerId")
    Flux<Booking> findByCustomerId(Mono<String> customerId);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END " +
            "FROM BOOKINGS " +
            "WHERE STATUS = 'ACTIVE' " +
            "AND TIME_INTERVAL_FROM <= :timeIntervalTo " +
            "AND TIME_INTERVAL_TO >= :timeIntervalFrom")
    Mono<Boolean> isTimeIntervalAlreadyBooked(LocalDateTime timeIntervalFrom, LocalDateTime timeIntervalTo);
}
