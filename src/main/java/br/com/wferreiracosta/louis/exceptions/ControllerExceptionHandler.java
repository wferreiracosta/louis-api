package br.com.wferreiracosta.louis.exceptions;

import br.com.wferreiracosta.louis.models.exceptions.FieldMessage;
import br.com.wferreiracosta.louis.models.exceptions.StandardError;
import br.com.wferreiracosta.louis.models.exceptions.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.Timestamp;
import java.util.ArrayList;

import static java.util.Comparator.comparing;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> methodArgumentNotValid(final MethodArgumentNotValidException e) {
        final var erros = new ArrayList<FieldMessage>();

        for (FieldError x : e.getBindingResult().getFieldErrors()) {
            erros.add(new FieldMessage(x.getField(), x.getDefaultMessage()));
        }

        final var err = ValidationError.builder()
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .errors(erros.stream()
                        .sorted(comparing(FieldMessage::fieldName))
                        .toList())
                .status(BAD_REQUEST.value())
                .message("Validation error")
                .build();

        return ResponseEntity.status(BAD_REQUEST).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> httpMessageNotReadable(final HttpMessageNotReadableException e) {
        final var err = StandardError.builder()
                .status(BAD_REQUEST.value())
                .message(e.getLocalizedMessage())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();

        return ResponseEntity.status(BAD_REQUEST).body(err);
    }

}
