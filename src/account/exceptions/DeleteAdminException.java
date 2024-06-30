package account.exceptions;

public class DeleteAdminException extends RuntimeException {
    public DeleteAdminException() {
    }

    public DeleteAdminException(String message) {
        super(message);
    }
}
