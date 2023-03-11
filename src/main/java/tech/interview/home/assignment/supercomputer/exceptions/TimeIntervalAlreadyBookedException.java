package tech.interview.home.assignment.supercomputer.exceptions;

import lombok.Getter;
import tech.interview.home.assignment.supercomputer.model.domain.TimeInterval;

@Getter
public class TimeIntervalAlreadyBookedException extends RuntimeException {
    private final TimeInterval timeInterval;

    public TimeIntervalAlreadyBookedException(TimeInterval timeInterval) {
        super("Time interval from " + timeInterval.from() + " to " + timeInterval.to() + " is already booked!");
        this.timeInterval = timeInterval;
    }
}
