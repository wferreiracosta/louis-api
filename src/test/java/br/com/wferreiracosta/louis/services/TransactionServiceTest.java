package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.repositories.TransactionRespository;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.repositories.WalletRepository;
import br.com.wferreiracosta.louis.services.impl.TransactionServiceImpl;
import br.com.wferreiracosta.louis.services.impl.UserServiceImpl;
import br.com.wferreiracosta.louis.services.impl.WalletServiceImpl;
import br.com.wferreiracosta.louis.utils.ServiceTestAnnotations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static br.com.wferreiracosta.louis.utils.Generator.cpf;
import static br.com.wferreiracosta.louis.utils.Generator.email;


class TransactionServiceTest extends ServiceTestAnnotations {

    private TransactionService service;

    @Autowired
    private TransactionRespository respository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    public void setUp() {
        final var userService = new UserServiceImpl(userRepository);
        final var walletService = new WalletServiceImpl(walletRepository, userService);
        service = new TransactionServiceImpl(walletService, respository);
    }

    @Test
    void transferMoneySuccessfully() {
        final var walletPayer = WalletEntity.builder()
                .amount(new BigDecimal("1000"))
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .transferring(new ArrayList<>())
                .receiving(new ArrayList<>())
                .build();
        final var payer = UserEntity.builder()
                .name("Pedro")
                .document(cpf())
                .type(MERCHANT)
                .email(email())
                .password("123")
                .wallet(walletPayer)
                .build();
        walletPayer.setUser(payer);
        final var payerSaved = userRepository.save(payer);


        final var payeeWallet = WalletEntity.builder()
                .amount(new BigDecimal("1000"))
                .createdDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .transferring(new ArrayList<>())
                .receiving(new ArrayList<>())
                .build();
        final var payee = UserEntity.builder()
                .name("Carlos")
                .document(cpf())
                .type(MERCHANT)
                .email(email())
                .password("123")
                .wallet(payeeWallet)
                .build();
        payeeWallet.setUser(payee);
        final var payeeSaved = userRepository.save(payee);

        final var parameter = new TransactionParameter(new BigDecimal(500), payerSaved.getId(), payeeSaved.getId());

        final var dto = service.transfer(parameter);

        Assertions.assertEquals(parameter.value(), dto.value());

        final var transactionPayer = dto.payer();
        Assertions.assertEquals(payerSaved.getName(), transactionPayer.name());
        Assertions.assertEquals(payerSaved.getSurname(), transactionPayer.surname());
        Assertions.assertEquals(payerSaved.getEmail(), transactionPayer.email());

        final var transactionPayee = dto.payee();
        Assertions.assertEquals(payeeSaved.getName(), transactionPayee.name());
        Assertions.assertEquals(payeeSaved.getSurname(), transactionPayee.surname());
        Assertions.assertEquals(payeeSaved.getEmail(), transactionPayee.email());
    }


}
