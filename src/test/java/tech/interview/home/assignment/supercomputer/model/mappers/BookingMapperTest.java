package tech.interview.home.assignment.supercomputer.model.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.interview.home.assignment.supercomputer.model.domain.Booking;
import tech.interview.home.assignment.supercomputer.model.domain.BookingStatus;
import tech.interview.home.assignment.supercomputer.model.dto.NewBookingRequestDto;
import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
    private static final BookingMapper mapper = new BookingMapperImpl();

    private static final long id = 12;
    private static final String customerId = "TestCustomer 124";
    private static final BookingStatus status = BookingStatus.ACTIVE;
    private static final LocalDateTime timeIntervalFrom = LocalDateTime.now().minus(3, ChronoUnit.HOURS);
    private static final LocalDateTime timeIntervalTo = LocalDateTime.now();

    @Test
    @DisplayName("Test mapping request DTO to entity")
    void requestDtoToEntity() {
        NewBookingRequestDto dto = new NewBookingRequestDto(customerId, new TimeInterval(timeIntervalFrom, timeIntervalTo));
        var res = mapper.requestDtoToEntity(dto);

        assertNotNull(res);
        assertAll(
                () -> assertNull(res.getId()),
                () -> Assertions.assertEquals(customerId, res.getCustomerId()),
                () -> assertNull(res.getStatus()),
                () -> Assertions.assertEquals(timeIntervalFrom, res.getTimeIntervalFrom()),
                () -> Assertions.assertEquals(timeIntervalTo, res.getTimeIntervalTo())
        );
    }

    @Test
    @DisplayName("Test mapping entity to response DTO")
    void entityToResponseDto() {
        Booking entity = Booking.builder()
                .id(id)
                .customerId(customerId)
                .status(status)
                .timeIntervalFrom(timeIntervalFrom)
                .timeIntervalTo(timeIntervalTo)
                .build();

        var res = mapper.entityToResponseDto(entity);

        assertNotNull(res);
        assertAll(
                () -> Assertions.assertEquals(id, res.id()),
                () -> Assertions.assertEquals(customerId, res.customerId()),
                () -> Assertions.assertEquals(status.name(), res.status()),
                () -> Assertions.assertEquals(timeIntervalFrom, res.timeInterval().from()),
                () -> Assertions.assertEquals(timeIntervalTo, res.timeInterval().to())
        );


    }
}