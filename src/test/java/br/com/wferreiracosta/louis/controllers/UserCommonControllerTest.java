package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.utils.ControllerTestAnnotations;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static br.com.wferreiracosta.louis.utils.Generator.*;
import static java.lang.String.format;
import static java.util.List.of;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserCommonControllerTest extends ControllerTestAnnotations {

    private final String urlCommon = "/users/common";
    private final String urlCommonPage = "/users/common/page";

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
    void testingSaveUserCommonWithSucess() throws Exception {
        final var user = new UserParameter(
                "Pedro",
                "Silva",
                cpf(),
                email(),
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
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
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.wallet.id").isNotEmpty())
                .andExpect(jsonPath("$.wallet.amount").isNotEmpty())
                .andExpect(jsonPath("$.wallet.createdDate").isNotEmpty())
                .andExpect(jsonPath("$.wallet.updateDate").isNotEmpty());
    }

    @Test
    void testingSaveUserCommonWithNameBlank() throws Exception {
        final var user = new UserParameter(
                "",
                "Silva",
                cpf(),
                email(),
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your name is mandatory"));
    }

    @Test
    void testingSaveUserCommonWithSurnameBlank() throws Exception {
        final var user = new UserParameter(
                "Pedro",
                "",
                cpf(),
                email(),
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("surname"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your surname is mandatory"));
    }

    @Test
    void testingSaveUserCommonWithDocumentBlank() throws Exception {
        final var user = new UserParameter(
                "Pedro",
                "Silva",
                "",
                email(),
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("document"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your document is mandatory"));
    }

    @Test
    void testingSaveUserCommonWithEmailBlank() throws Exception {
        final var user = new UserParameter(
                "Pedro",
                "Silva",
                cpf(),
                "",
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your email is mandatory"));
    }

    @Test
    void testingSaveUserCommonWithPasswordBlank() throws Exception {
        final var user = new UserParameter(
                "Pedro",
                "Silva",
                cpf(),
                email(),
                ""
        );

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("Filling in your password is mandatory"));
    }

    @Test
    void testingSaveUserCommonWithDocumentDuplicate() throws Exception {
        final var user = new UserParameter(
                "Pedro",
                "Silva",
                cpf(),
                email(),
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Marcos")
                .surname("Silva")
                .document(user.document())
                .email(email())
                .build();
        repository.save(entity);

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("document"))
                .andExpect(jsonPath("$.errors[0].message").value("There is already a user registered with this document"));
    }

    @Test
    void testingSaveUserCommonWithEmailDuplicate() throws Exception {
        final var user = new UserParameter(
                "Pedro",
                "Silva",
                cpf(),
                email(),
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Marcos")
                .surname("Silva")
                .document(cpf())
                .email(user.email())
                .build();
        repository.save(entity);

        final var request = MockMvcRequestBuilders
                .post(urlCommon)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("There is already a user registered with this email"));
    }

    @Test
    void testingFindCommonUsersPageable() throws Exception {
        final var marcus = UserEntity.builder()
                .name("Marcus")
                .surname("Silva")
                .document(cnpj())
                .email(email())
                .type(MERCHANT)
                .build();

        final var pedro = UserEntity.builder()
                .name("Pedro")
                .surname("Silva")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .build();

        repository.saveAll(of(marcus, pedro));

        final var request = MockMvcRequestBuilders
                .get(urlCommonPage)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    void testingFindCommonUserNotExists() throws Exception {
        final var id = 100L;
        final var message = format("No user found with the Id: %s", id);

        final var request = MockMvcRequestBuilders
                .get(urlCommon.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void testingFindCommonUserById() throws Exception {
        final var wallet = WalletEntity.builder()
                .amount(new BigDecimal("1"))
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        final var entity = UserEntity.builder()
                .name("Pedro")
                .surname("Silva")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(wallet)
                .build();
        wallet.setUser(entity);

        final var entitySaved = repository.save(entity);
        final var walletSaved = entitySaved.getWallet();

        final var request = MockMvcRequestBuilders
                .get(urlCommon.concat("/" + entitySaved.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entitySaved.getId()))
                .andExpect(jsonPath("$.name").value(entitySaved.getName()))
                .andExpect(jsonPath("$.surname").value(entitySaved.getSurname()))
                .andExpect(jsonPath("$.document").value(entitySaved.getDocument()))
                .andExpect(jsonPath("$.type").value(entitySaved.getType().name()))
                .andExpect(jsonPath("$.email").value(entitySaved.getEmail()))
                .andExpect(jsonPath("$.wallet.id").value(walletSaved.getId()))
                .andExpect(jsonPath("$.wallet.amount").isNotEmpty())
                .andExpect(jsonPath("$.wallet.createdDate").isNotEmpty())
                .andExpect(jsonPath("$.wallet.updateDate").isNotEmpty());
    }

}
