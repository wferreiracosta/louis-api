package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.repositories.WalletRepository;
import br.com.wferreiracosta.louis.services.UserService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserUniquenessIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private UserService service;
    @SpyBean private UserRepository users;
    @Autowired private WalletRepository wallets;

    @BeforeEach
    void cleanDatabase() {
        wallets.deleteAll();
        users.deleteAll();
        clearInvocations(users);
    }

    @ParameterizedTest
    @CsvSource({"common,false", "common,true", "merchants,false", "merchants,true"})
    void returnsBothConflictsWithoutCreatingAnything(String route, boolean distinctUsers) throws Exception {
        final var oppositeType = route.equals("common") ? UserType.MERCHANT : UserType.COMMON;
        users.save(UserEntity.builder().document("123").email(distinctUsers ? "other@mail.com" : "taken@mail.com").type(oppositeType).build());
        if (distinctUsers) users.save(UserEntity.builder().document("456").email("taken@mail.com").type(oppositeType).build());
        final long beforeUsers = users.count();
        final long beforeWallets = wallets.count();
        mvc.perform(post("/users/" + route).contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(new UserParameter("Pedro", "Silva", "123", "taken@mail.com", "secret"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[0].fieldName").value("document"))
                .andExpect(jsonPath("$.errors[0].message").value("There is already a user registered with this document"))
                .andExpect(jsonPath("$.errors[1].fieldName").value("email"))
                .andExpect(jsonPath("$.errors[1].message").value("There is already a user registered with this email"));
        assertEquals(beforeUsers, users.count());
        assertEquals(beforeWallets, wallets.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"common", "merchants"})
    void invalidStructurePrecedesUniquenessQueries(String route) throws Exception {
        users.save(UserEntity.builder().document("123").email("taken@mail.com").build());
        clearInvocations(users);
        mvc.perform(post("/users/" + route).contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(new UserParameter("", "Silva", "123", "taken@mail.com", "secret"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].fieldName").value("name"));
        verify(users, never()).findByDocument(anyString());
        verify(users, never()).findByEmail(anyString());
        verify(users, never()).save(any());
    }

    @Test
    void serviceStartsTransactionBeforeBothReadsAndWrite() {
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        doAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            return java.util.Optional.empty();
        }).when(users).findByDocument("transaction-document");
        doAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            return java.util.Optional.empty();
        }).when(users).findByEmail("transaction@mail.com");
        doAnswer(invocation -> {
            assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
            return invocation.getArgument(0);
        }).when(users).save(any(UserEntity.class));

        final var user = service.save(new UserParameter("Pedro", "Silva", "transaction-document", "transaction@mail.com", "secret"), UserType.COMMON);

        assertNotNull(user.getWallet());
        assertSame(user, user.getWallet().getUser());
        verify(users).findByDocument("transaction-document");
        verify(users).findByEmail("transaction@mail.com");
        verify(users).save(any(UserEntity.class));
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
    }
}
