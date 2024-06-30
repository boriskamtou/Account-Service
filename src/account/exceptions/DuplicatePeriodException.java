package account.exceptions;

public class DuplicatePeriodException extends RuntimeException {
    public DuplicatePeriodException() {
    }

    public DuplicatePeriodException(String message) {
        super(message);
    }
}
