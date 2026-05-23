package br.com.wferreiracosta.louis.exceptions;

import lombok.Getter;

@Getter
public class BusinessValidationException extends RuntimeException {

    private final String fieldName;

    public BusinessValidationException(final String fieldName, final String message) {
        super(message);
        this.fieldName = fieldName;
    }

}
