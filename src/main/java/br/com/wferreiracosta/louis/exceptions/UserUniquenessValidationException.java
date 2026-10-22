package br.com.wferreiracosta.louis.exceptions;

import lombok.Getter;

import java.util.List;

@Getter
public class UserUniquenessValidationException extends RuntimeException {

    private final List<FieldMessage> errors;

    public UserUniquenessValidationException(final List<FieldMessage> errors) {
        super("Errors");
        this.errors = List.copyOf(errors);
        if (this.errors.isEmpty()) {
            throw new IllegalArgumentException("At least one uniqueness error is required");
        }
    }
}
