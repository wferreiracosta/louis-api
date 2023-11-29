package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.utils.ControllerTestAnnotations;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static br.com.wferreiracosta.louis.utils.Generator.cnpj;
import static br.com.wferreiracosta.louis.utils.Generator.cpf;
import static java.util.List.of;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserCommonControllerTest extends ControllerTestAnnotations {

    private final String COMMON_API = "/users/common";
    private final String COMMON_PAGE_API = "/users/common/page";

    private Gson gson;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository repository;

    @BeforeEach
    void setUp() {
        gson = new Gson();
    }

    @Test
    void testingSaveUserCommon() throws Exception {
        final var user = new UserDTO(
                "Wendel",
                "Silva",
                cpf(),
                "wendel@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.surname").value(user.surname()))
                .andExpect(jsonPath("$.document").value(user.document()))
                .andExpect(jsonPath("$.type").value(COMMON.name()))
                .andExpect(jsonPath("$.email").value(user.email()));
    }

    @Test
    void testingSaveUserCommonWithNameBlank() throws Exception {
        final var user = new UserDTO(
                "",
                "Silva",
                cpf(),
                "tiago@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
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
    void testingSaveUserCommonWithSurnameBlank() throws Exception {
        final var user = new UserDTO(
                "Denis",
                "",
                cpf(),
                "denis@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
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
    void testingSaveUserCommonWithDocumentBlank() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "Silva",
                "",
                "wesley@mail.com",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
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
    void testingSaveUserCommonWithEmailBlank() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "Silva",
                cpf(),
                "",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
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
    void testingSaveUserCommonWithPasswordBlank() throws Exception {
        final var user = new UserDTO(
                "Carlos",
                "Silva",
                cpf(),
                "carlos@mail.com",
                ""
        );

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
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

    @Test
    void testingSaveUserCommonWithDocumentDuplicate() throws Exception {
        final var user = new UserDTO(
                "Anderson",
                "Silva",
                cpf(),
                "anderson@mail.com",
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Anderson")
                .surname("Silva")
                .document(user.document())
                .email("anderson.silva@mail.com")
                .build();
        repository.save(entity);

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("document"))
                .andExpect(jsonPath("$.errors[0].message").value("There is already a user registered with this document"));
    }

    @Test
    void testingSaveUserCommonWithEmailDuplicate() throws Exception {
        final var user = new UserDTO(
                "Anderson",
                "Silva",
                cpf(),
                "anderson@mail.com",
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Anderson")
                .surname("Silva")
                .document(cpf())
                .email(user.email())
                .build();
        repository.save(entity);

        final var request = MockMvcRequestBuilders
                .post(COMMON_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Validation error"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("There is already a user registered with this email"));
    }

    @Test
    void testingFindCommonUsersPageable() throws Exception {
        final var anderson = UserEntity.builder()
                .name("Anderson")
                .surname("Silva")
                .document(cnpj())
                .email("anderson123@mail.com")
                .type(MERCHANT)
                .build();

        final var adilson = UserEntity.builder()
                .name("Adilson")
                .surname("Silva")
                .document(cpf())
                .email("adilson123@mail.com")
                .type(COMMON)
                .build();

        repository.saveAll(of(anderson, adilson));

        final var request = MockMvcRequestBuilders
                .get(COMMON_PAGE_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content").isArray());
    }

}
