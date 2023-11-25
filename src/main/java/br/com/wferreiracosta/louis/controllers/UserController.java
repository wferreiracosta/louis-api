package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.exceptions.ValidationError;
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

public interface UserController {

    @PostMapping("/merchants")
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
    UserEntity saveMerchant(UserDTO dto);

    @PostMapping("/common")
    @ResponseStatus(CREATED)
    @Operation(
            summary = "Insert a new common user",
            description = "Insert a common user"
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
    UserEntity saveCommon(UserDTO dto);

    @GetMapping("/merchants")
    @ResponseStatus(OK)
    @Operation(
            summary = "Find all merchant users",
            description = "Find all merchant users"
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
    Page<UserEntity> findMerchantPageable(
            Integer page,
            Integer linesPerPage,
            String orderBy,
            String direction
    );

}
