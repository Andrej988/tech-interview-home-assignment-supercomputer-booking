package tech.interview.home.assignment.supercomputer.model.dto;

import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;

public record NewBookingRequestDto(
        String customerId,
        TimeInterval timeInterval) {
}
