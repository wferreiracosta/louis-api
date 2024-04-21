package br.com.wferreiracosta.louis.controllers;

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
import static br.com.wferreiracosta.louis.utils.Generator.cpf;
import static br.com.wferreiracosta.louis.utils.Generator.email;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalletControllerTest extends ControllerTestAnnotations {

    private final String WALLETS_API = "/wallets";

    private Gson gson;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository repository;

    @Test
    void testingFindById() throws Exception {
        final var wallet = WalletEntity.builder()
                .amount(new BigDecimal("1"))
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        final var user = UserEntity.builder()
                .name("Anderson")
                .surname("Silva")
                .document(cpf())
                .email(email())
                .type(COMMON)
                .wallet(wallet)
                .build();
        wallet.setUser(user);

        final var userSaved = repository.save(user);
        final var walletSaved = userSaved.getWallet();

        final var request = MockMvcRequestBuilders
                .get(WALLETS_API.concat("/" + walletSaved.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletSaved.getId()))
                .andExpect(jsonPath("$.amount").isNotEmpty())
                .andExpect(jsonPath("$.createdDate").isNotEmpty())
                .andExpect(jsonPath("$.updateDate").isNotEmpty());
    }

}
