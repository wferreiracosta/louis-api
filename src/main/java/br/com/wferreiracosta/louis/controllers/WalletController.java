package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.models.exceptions.ValidationError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.OK;

public interface WalletController {

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    @Operation(
            summary = "Find wallet by id",
            description = "Find wallet by id"
    )
    @ApiResponse(responseCode = "200", content = {
            @Content(schema = @Schema(implementation = WalletEntity.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "500", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    WalletEntity findById(Long id);

    @GetMapping("/users/{id}")
    @ResponseStatus(OK)
    @Operation(
            summary = "Find wallet by user id",
            description = "Find wallet by user id"
    )
    @ApiResponse(responseCode = "200", content = {
            @Content(schema = @Schema(implementation = WalletEntity.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "500", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    WalletEntity findByUserId(Long id);

}
