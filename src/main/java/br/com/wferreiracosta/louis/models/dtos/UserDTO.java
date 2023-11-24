package br.com.wferreiracosta.louis.models.dtos;

import br.com.wferreiracosta.louis.annotations.UserValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@UserValidator
public record UserDTO(

        @Schema(description = "Username", example = "Pedro")
        @NotBlank(message = "Filling in your name is mandatory")
        String name,

        @Schema(description = "Surname", example = "Silva")
        @NotBlank(message = "Filling in your surname is mandatory")
        String surname,

        @Schema(description = "User document", example = "12345678996")
        @NotBlank(message = "Filling in your document is mandatory")
        String document,

        @Schema(description = "User email", example = "pedro@mail.com")
        @NotBlank(message = "Filling in your email is mandatory")
        String email,

        @Schema(description = "User password", example = "123")
        @NotBlank(message = "Filling in your password is mandatory")
        String password
) {
}
