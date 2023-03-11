package tech.interview.home.assignment.supercomputer.model.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@Table(name = "BOOKINGS")
public class Booking {
    @Id
    Long id;

    String customerId;
    BookingStatus status;
    LocalDateTime timeIntervalFrom;
    LocalDateTime timeIntervalTo;
}
