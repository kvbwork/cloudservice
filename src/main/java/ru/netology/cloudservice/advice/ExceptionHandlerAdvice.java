package ru.netology.cloudservice.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.cloudservice.model.response.ErrorResponse;

import javax.validation.ValidationException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    private final AtomicInteger idGenerator = new AtomicInteger();

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> badCredentialsException(Exception ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResponse(idGenerator.incrementAndGet(), ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> AuthenticationException(Exception ex) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(new ErrorResponse(idGenerator.incrementAndGet(), ex.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindException(BindException ex) {
        FieldError fieldError = ex.getFieldError();
        String message = String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResponse(idGenerator.incrementAndGet(), message));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> validationException(ValidationException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(new ErrorResponse(idGenerator.incrementAndGet(), ex.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> ioException(IOException ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(idGenerator.incrementAndGet(), ex.getMessage()));
    }

}
