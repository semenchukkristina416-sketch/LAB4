package carservice.exception;

/**
 * Raised when a domain invariant or an application operation is violated.
 */
public class DomainException extends RuntimeException {

    public DomainException(final String message) {
        super(message);
    }
}
