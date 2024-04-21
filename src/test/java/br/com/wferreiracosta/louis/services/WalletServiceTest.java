package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.exceptions.ObjectNotFoundException;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.repositories.WalletRepository;
import br.com.wferreiracosta.louis.services.impl.WalletServiceImpl;
import br.com.wferreiracosta.louis.utils.ServiceTestAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static br.com.wferreiracosta.louis.utils.Generator.cpf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletServiceTest extends ServiceTestAnnotations {

    @Autowired
    private WalletRepository repository;

    @Autowired
    private UserRepository userRepository;

    private WalletService service;

    @BeforeEach
    void setUp() {
        service = new WalletServiceImpl(repository);
    }

    @Test
    void testingFindByIdWithSucess() {
        final var wallet = WalletEntity.builder()
                .amount(new BigDecimal("1"))
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        final var user = UserEntity.builder()
                .name("Anderson")
                .surname("Silva")
                .document(cpf())
                .email("anderson.silva12348@mail.com")
                .type(COMMON)
                .wallet(wallet)
                .build();

        wallet.setUser(user);

        final var userSaved = userRepository.save(user);
        final var walletSaved = userSaved.getWallet();

        final var walletReturn = service.findById(userSaved.getWallet().getId());
        final var userReturn = walletReturn.getUser();

        assertEquals(walletSaved.getId(), walletReturn.getId());
        assertEquals(walletSaved.getAmount(), walletReturn.getAmount());
        assertEquals(walletSaved.getCreatedDate(), walletReturn.getCreatedDate());
        assertEquals(walletSaved.getUpdateDate(), walletReturn.getUpdateDate());

        assertEquals(userSaved.getId(), userReturn.getId());
        assertEquals(userSaved.getName(), userReturn.getName());
        assertEquals(userSaved.getDocument(), userReturn.getDocument());
        assertEquals(userSaved.getType(), userReturn.getType());
        assertEquals(userSaved.getEmail(), userReturn.getEmail());
        assertEquals(userSaved.getPassword(), userReturn.getPassword());
    }

    @Test
    void testingFindByIdWalletNotExists() {
        final var wallet = WalletEntity.builder()
                .id(2L)
                .amount(new BigDecimal("1"))
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .build();

        final var user = UserEntity.builder()
                .id(2L)
                .name("Anderson")
                .surname("Silva")
                .document(cpf())
                .email("anderson.silva12348@mail.com")
                .type(COMMON)
                .wallet(wallet)
                .build();

        wallet.setUser(user);

        final var message = String.format("No wallet found with the Id: %s", wallet.getId());

        try {
            service.findById(wallet.getId());
        } catch (final ObjectNotFoundException ex) {
            assertEquals(message, ex.getLocalizedMessage());
        }
    }

}
