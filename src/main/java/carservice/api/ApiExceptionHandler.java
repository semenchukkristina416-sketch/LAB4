package carservice.api;

import carservice.exception.ConflictException;
import carservice.exception.DomainException;
import carservice.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(final ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage());
    }

    @ExceptionHandler({ConflictException.class, DomainException.class})
    ProblemDetail handleDomainConflict(final DomainException exception) {
        return problem(HttpStatus.CONFLICT, "Business rule violation", exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleIntegrityViolation(final DataIntegrityViolationException exception) {
        return problem(HttpStatus.CONFLICT, "Resource conflict",
                "The request conflicts with an existing database record");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticLock(final OptimisticLockingFailureException exception) {
        return problem(HttpStatus.CONFLICT, "Concurrent update conflict",
                "The resource was changed by another request");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(final MethodArgumentNotValidException exception) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more request fields are invalid");
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("fieldErrors", fieldErrors);
        return detail;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class, IllegalArgumentException.class})
    ProblemDetail handleMalformedRequest(final Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request", "The request body or parameter is invalid");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpectedException(final Exception exception) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "The server could not complete the request");
    }

    private ProblemDetail problem(final HttpStatus status, final String title, final String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
