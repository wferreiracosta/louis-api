package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.utils.ControllerTestAnnotations;
import com.google.gson.Gson;
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
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserMerchantsControllerTest extends ControllerTestAnnotations {

    private final String MERCHANTS_API = "/users/merchants";
    private final String MERCHANTS_PAGE_API = "/users/merchants/page";

    @Autowired
    private Gson gson;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository repository;

    @Test
    void testingSaveUserMerchantWithSucess() throws Exception {
        final var user = new UserDTO(
                "Wesley",
                "Silva",
                cnpj(),
                email(),
                "123"
        );

        final var request = MockMvcRequestBuilders
                .post(MERCHANTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(user));

        this.mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.surname").value(user.surname()))
                .andExpect(jsonPath("$.document").value(user.document()))
                .andExpect(jsonPath("$.type").value(MERCHANT.name()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.wallet.id").isNotEmpty())
                .andExpect(jsonPath("$.wallet.amount").isNotEmpty())
                .andExpect(jsonPath("$.wallet.createdDate").isNotEmpty())
                .andExpect(jsonPath("$.wallet.updateDate").isNotEmpty());
    }

    @Test
    void testingSaveUserMerchantWithNameBlank() throws Exception {
        final var user = new UserDTO(
                "",
                "Silva",
                cnpj(),
                email(),
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
                "Pedro",
                "",
                cnpj(),
                email(),
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
                "Pedro",
                "Silva",
                "",
                email(),
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
                "Pedro",
                "Silva",
                cnpj(),
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
                "Pedro",
                "Silva",
                cnpj(),
                email(),
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

    @Test
    void testingSaveUserMerchantWithDocumentDuplicate() throws Exception {
        final var user = new UserDTO(
                "Marcos",
                "Silva",
                cnpj(),
                email(),
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Pedro")
                .surname("Silva")
                .document(user.document())
                .email(email())
                .build();
        repository.save(entity);

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
                .andExpect(jsonPath("$.errors[0].message").value("There is already a user registered with this document"));
    }

    @Test
    void testingSaveUserMerchantWithEmailDuplicate() throws Exception {
        final var user = new UserDTO(
                "Marcos",
                "Silva",
                cnpj(),
                email(),
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Pedro")
                .surname("Silva")
                .document(cnpj())
                .email(user.email())
                .build();
        repository.save(entity);

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
                .andExpect(jsonPath("$.errors[0].message").value("There is already a user registered with this email"));
    }

    @Test
    void testingFindMerchantsUsersPageable() throws Exception {
        final var marcos = UserEntity.builder()
                .name("Marcos")
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

        repository.saveAll(of(marcos, pedro));

        final var request = MockMvcRequestBuilders
                .get(MERCHANTS_PAGE_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].id").value(marcos.getId()))
                .andExpect(jsonPath("$.content[0].name").value(marcos.getName()))
                .andExpect(jsonPath("$.content[0].surname").value(marcos.getSurname()))
                .andExpect(jsonPath("$.content[0].document").value(marcos.getDocument()))
                .andExpect(jsonPath("$.content[0].email").value(marcos.getEmail()))
                .andExpect(jsonPath("$.content[0].type").value(marcos.getType().name()))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testingFindMerchantsUserByIdWithSucess() throws Exception {
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
                .type(MERCHANT)
                .wallet(wallet)
                .build();
        wallet.setUser(entity);
        final var entitySaved = repository.save(entity);
        final var walletSaved = entitySaved.getWallet();

        final var request = MockMvcRequestBuilders
                .get(MERCHANTS_API.concat("/" + entitySaved.getId()))
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

    @Test
    void testingFindMerchantsUserNotExists() throws Exception {
        final var id = 100L;
        final var message = format("No user found with the Id: %s", id);

        final var request = MockMvcRequestBuilders
                .get(MERCHANTS_API.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));
    }

}
