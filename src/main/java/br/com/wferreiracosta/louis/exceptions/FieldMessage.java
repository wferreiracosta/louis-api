package br.com.wferreiracosta.louis.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

public record FieldMessage(

        @Schema(description = "Field with error", example = "name")
        String fieldName,

        @Schema(description = "Error message", example = "blank")
        String message

) implements Serializable {
}

