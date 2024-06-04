package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.exceptions.ValidationError;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

public interface UserMerchantsController {

    @PostMapping
    @ResponseStatus(CREATED)
    @Operation(
            summary = "Insert a new merchant user",
            description = "Insert a merchant user"
    )
    @ApiResponse(responseCode = "201", content = {
            @Content(schema = @Schema(implementation = UserEntity.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "500", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    UserEntity save(UserParameter parameter);

    @GetMapping("/page")
    @ResponseStatus(OK)
    @Operation(
            summary = "Find all merchant users pageable",
            description = "Find all merchant users pageable"
    )
    @ApiResponse(responseCode = "201", content = {
            @Content(schema = @Schema(implementation = UserEntity.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "500", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    Page<UserEntity> findPageable(
            Integer page,
            Integer linesPerPage,
            String orderBy,
            String direction
    );

    @GetMapping("/{id}")
    @ResponseStatus(OK)
    @Operation(
            summary = "Find merchant by id",
            description = "Find merchant by id"
    )
    @ApiResponse(responseCode = "200", content = {
            @Content(schema = @Schema(implementation = UserEntity.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "404", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    @ApiResponse(responseCode = "500", content = {
            @Content(schema = @Schema(implementation = ValidationError.class), mediaType = "application/json")
    })
    UserEntity findById(Long id);

}
