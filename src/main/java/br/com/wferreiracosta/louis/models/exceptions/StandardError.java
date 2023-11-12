package br.com.wferreiracosta.louis.models.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.sql.Timestamp;

@Builder
public record StandardError(
        @Schema(description = "Error status", example = "404")
        Integer status,

        @Schema(description = "Error message", example = "Department not found")
        String message,

        @Schema(description = "Error timestamp", example = "2021-03-24 16:48:05.591")
        Timestamp timestamp
) {
}
