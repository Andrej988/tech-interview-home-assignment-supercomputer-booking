package tech.interview.home.assignment.supercomputer.model.dto;

import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;

public record BookingResponseDto(
        long id,
        String customerId,
        String status,
        TimeInterval timeInterval) {
}
