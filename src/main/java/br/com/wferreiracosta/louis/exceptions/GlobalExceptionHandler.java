package br.com.wferreiracosta.louis.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.Timestamp;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.util.Comparator.comparing;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> methodArgumentNotValid(final MethodArgumentNotValidException e) {
        final var errors = e.getBindingResult().getFieldErrors().stream()
                .map(x -> new FieldMessage(x.getField(), x.getDefaultMessage()))
                .sorted(comparing(FieldMessage::fieldName))
                .toList();
        final var error = map(BAD_REQUEST.value(), "Errors", currentTimeMillis(), errors);
        return ResponseEntity.status(BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> httpMessageNotReadable(final HttpMessageNotReadableException e) {
        final var err = map(BAD_REQUEST.value(), e.getLocalizedMessage(), currentTimeMillis());
        return ResponseEntity.status(BAD_REQUEST).body(err);
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<StandardError> objectNotFound(final ObjectNotFoundException e) {
        final var err = map(NOT_FOUND.value(), e.getLocalizedMessage(), currentTimeMillis());
        return ResponseEntity.status(NOT_FOUND).body(err);
    }

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<StandardError> configuration(final ConfigurationException e) {
        final var err = map(BAD_REQUEST.value(), e.getLocalizedMessage(), currentTimeMillis());
        return ResponseEntity.status(BAD_REQUEST).body(err);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<StandardError> transaction(final TransactionException e) {
        final var err = map(FORBIDDEN.value(), e.getLocalizedMessage(), currentTimeMillis());
        return ResponseEntity.status(FORBIDDEN).body(err);
    }

    private StandardError map(final Integer value, final String message, final Long currentTimeMillis) {
        return new StandardError(value, message,
                new Timestamp(currentTimeMillis));
    }

    private ValidationError map(final Integer value, final String message, final Long currentTimeMillis, final List<FieldMessage> erros) {
        return new ValidationError(value, message, new Timestamp(currentTimeMillis), erros);
    }

}
