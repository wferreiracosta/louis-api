package br.com.wferreiracosta.louis.controllers;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
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

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static br.com.wferreiracosta.louis.utils.Generator.*;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest extends ControllerTestAnnotations {

    private final String urlTransaction = "/transaction";

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
    void transferWithErrorBecauseUserPayerNotExists() throws Exception {
        final var payee = UserEntity.builder()
                .name("Marcos")
                .surname("Silva")
                .document(cpf())
                .email(email())
                .build();
        final var payeeSaved = repository.save(payee);

        final var parameter = new TransactionParameter(new BigDecimal(5), 2L, payeeSaved.getId());

        final var request = MockMvcRequestBuilders
                .post(urlTransaction)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(parameter));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("payer"))
                .andExpect(jsonPath("$.errors[0].message").value(format("User payer with id %s not exists", parameter.payer())));
    }

    @Test
    void transferWithErrorBecauseUserPayeeNotExists() throws Exception {
        final var walletPayer = WalletEntity.builder()
                .amount(new BigDecimal(10))
                .createdDate(now())
                .build();

        final var payer = UserEntity.builder()
                .name("Marcos")
                .surname("Silva")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(walletPayer)
                .build();
        walletPayer.setUser(payer);
        final var payerSaved = repository.save(payer);

        final var parameter = new TransactionParameter(new BigDecimal(5), payerSaved.getId(), 2L);

        final var request = MockMvcRequestBuilders
                .post(urlTransaction)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(parameter));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("payee"))
                .andExpect(jsonPath("$.errors[0].message").value(format("User payee with id %s not exists", parameter.payee())));
    }

    @Test
    void transferWithErrorBecauseUserMerchantNotSendMoneyToAnyone() throws Exception {
        final var walletPayer = WalletEntity.builder()
                .amount(new BigDecimal(10))
                .createdDate(now())
                .build();

        final var payer = UserEntity.builder()
                .name("Marcos")
                .surname("Silva")
                .document(cnpj())
                .email(email())
                .type(MERCHANT)
                .wallet(walletPayer)
                .build();
        walletPayer.setUser(payer);
        final var payerSaved = repository.save(payer);

        final var walletPayee = WalletEntity.builder()
                .amount(new BigDecimal(10))
                .createdDate(now())
                .build();

        final var payee = UserEntity.builder()
                .name("Carlos")
                .surname("Perreira")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(walletPayee)
                .build();
        walletPayee.setUser(payee);
        final var payeeSaved = repository.save(payee);

        final var parameter = new TransactionParameter(new BigDecimal(5), payerSaved.getId(), payeeSaved.getId());

        final var request = MockMvcRequestBuilders
                .post(urlTransaction)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(parameter));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("payer"))
                .andExpect(jsonPath("$.errors[0].message").value("Merchants users only receive transfers, they do not send money to anyone"));
    }

    @Test
    void transferWithErrorBecauseUserPayerDoesNotHaveABalanceToTransfer() throws Exception {
        final var walletPayer = WalletEntity.builder()
                .amount(new BigDecimal(3))
                .createdDate(now())
                .build();

        final var payer = UserEntity.builder()
                .name("Marcos")
                .surname("Silva")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(walletPayer)
                .build();
        walletPayer.setUser(payer);
        final var payerSaved = repository.save(payer);

        final var walletPayee = WalletEntity.builder()
                .amount(new BigDecimal(3))
                .createdDate(now())
                .build();

        final var payee = UserEntity.builder()
                .name("Carlos")
                .surname("Perreira")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(walletPayee)
                .build();
        walletPayee.setUser(payee);
        final var payeeSaved = repository.save(payee);

        final var parameter = new TransactionParameter(new BigDecimal(5), payerSaved.getId(), payeeSaved.getId());

        final var request = MockMvcRequestBuilders
                .post(urlTransaction)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(parameter));

        this.mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Errors"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("payer"))
                .andExpect(jsonPath("$.errors[0].message").value("Payer does not have a balance in their wallet"));
    }

    @Test
    void transferWithSuccess() throws Exception {
        final var walletPayer = WalletEntity.builder()
                .amount(new BigDecimal(1000))
                .createdDate(now())
                .build();
        final var payer = UserEntity.builder()
                .name("Marcos")
                .surname("Silva")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(walletPayer)
                .build();
        walletPayer.setUser(payer);
        final var payerSaved = repository.save(payer);

        final var walletPayee = WalletEntity.builder()
                .amount(new BigDecimal(500))
                .createdDate(now())
                .build();
        final var payee = UserEntity.builder()
                .name("Carlos")
                .surname("Perreira")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(walletPayee)
                .build();
        walletPayee.setUser(payee);
        final var payeeSaved = repository.save(payee);

        final var parameter = new TransactionParameter(new BigDecimal(500), payerSaved.getId(), payeeSaved.getId());

        final var request = MockMvcRequestBuilders
                .post(urlTransaction)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(gson.toJson(parameter));

        this.mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(parameter.amount()))
                .andExpect(jsonPath("$.payer.name").value(payerSaved.getName()))
                .andExpect(jsonPath("$.payer.surname").value(payerSaved.getSurname()))
                .andExpect(jsonPath("$.payer.email").value(payerSaved.getEmail()))
                .andExpect(jsonPath("$.payee.name").value(payeeSaved.getName()))
                .andExpect(jsonPath("$.payee.surname").value(payeeSaved.getSurname()))
                .andExpect(jsonPath("$.payee.email").value(payeeSaved.getEmail()));

        final var payerResult = repository.findById(payerSaved.getId()).get();
        final var payerAmountExpected = walletPayer.getAmount().subtract(parameter.amount());
        assertEquals(0, payerAmountExpected.compareTo(payerResult.getWallet().getAmount()));


        final var payeeResult = repository.findById(payeeSaved.getId()).get();
        final var payeeAmountExpected = walletPayee.getAmount().add(parameter.amount());
        assertEquals(0, payeeAmountExpected.compareTo(payeeResult.getWallet().getAmount()));
    }

}
