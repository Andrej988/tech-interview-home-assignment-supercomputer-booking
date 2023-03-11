package tech.interview.home.assignment.supercomputer.exceptions;

public class BookingAlreadyCanceledException extends RuntimeException {
    public BookingAlreadyCanceledException(String message) {
        super(message);
    }
}
