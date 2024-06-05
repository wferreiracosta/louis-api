package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.exceptions.ValidationError;
import br.com.wferreiracosta.louis.models.dtos.TransactionDTO;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface TransactionController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Transfer money",
            description = "Transfer money"
    )
    @ApiResponse(responseCode = "201", content = {
            @Content(schema = @Schema(implementation = TransactionDTO.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "500", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    TransactionDTO transfer(TransactionParameter parameter);

}
