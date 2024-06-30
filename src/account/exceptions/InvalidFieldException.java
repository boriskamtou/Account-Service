package account.exceptions;

public class InvalidFieldException extends RuntimeException {
    public InvalidFieldException() {
    }

    public InvalidFieldException(String message) {
        super(message);
    }
}
