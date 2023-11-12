package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.utils.tests.ControllerTest;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends ControllerTest {

    private final String MERCHANTS_API = "/users/merchants";

    private Gson gson;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        gson = new Gson();
    }

    @Test
    void testingSaveUserMerchant() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "Silva",
                "12345678996",
                "wesley@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(MERCHANTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.surname").value(user.surname()))
                .andExpect(jsonPath("$.document").value(user.document()))
                .andExpect(jsonPath("$.type").value(MERCHANT.name()))
                .andExpect(jsonPath("$.email").value(user.email()));
    }

    @Test
    void testingSaveUserMerchantWithNameBlank() throws Exception {
        final var user = new UserDTO(
                "",
                "Silva",
                "12345678996",
                "wesley@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(MERCHANTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your name is mandatory"));
    }

    @Test
    void testingSaveUserMerchantWithSurnameBlank() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "",
                "12345678996",
                "wesley@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(MERCHANTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("surname"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your surname is mandatory"));
    }

    @Test
    void testingSaveUserMerchantWithDocumentBlank() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "Silva",
                "",
                "wesley@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(MERCHANTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("document"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your document is mandatory"));
    }

    @Test
    void testingSaveUserMerchantWithEmailBlank() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "Silva",
                "12345678996",
                "",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(MERCHANTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your email is mandatory"));
    }

    @Test
    void testingSaveUserMerchantWithPasswordBlank() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "Silva",
                "12345678996",
                "wesley@mail.com",
                ""
        );

        final var request = MockMvcRequestBuilders
                .post(MERCHANTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your password is mandatory"));
    }

}
