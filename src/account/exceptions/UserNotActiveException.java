package account.exceptions;

public class UserNotActiveException extends RuntimeException {
    public UserNotActiveException() {
    }

    public UserNotActiveException(String message) {
        super(message);
    }
}
