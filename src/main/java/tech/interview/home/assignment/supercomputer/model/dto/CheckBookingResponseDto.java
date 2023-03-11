package tech.interview.home.assignment.supercomputer.model.dto;

import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;

public record CheckBookingResponseDto(TimeInterval timeInterval, boolean available) {
}
