package carservice.exception;

public class ConflictException extends DomainException {
    public ConflictException(final String message) {
        super(message);
    }
}
