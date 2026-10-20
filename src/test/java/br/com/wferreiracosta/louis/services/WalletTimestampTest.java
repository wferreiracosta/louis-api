package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.repositories.WalletRepository;
import br.com.wferreiracosta.louis.services.impl.TransactionServiceImpl;
import br.com.wferreiracosta.louis.services.impl.UserServiceImpl;
import br.com.wferreiracosta.louis.services.impl.WalletServiceImpl;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({TransactionServiceImpl.class, WalletServiceImpl.class, UserServiceImpl.class})
class WalletTimestampTest {

    @Autowired
    private WalletRepository wallets;

    @Autowired
    private UserRepository users;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldGenerateTimestampsWhenInsertingWalletWithoutDates() {
        final var beforeInsert = LocalDateTime.now().minusSeconds(1);
        final var wallet = wallets.saveAndFlush(WalletEntity.builder()
                .amount(new BigDecimal("100.00")).build());
        entityManager.clear();

        final var persisted = wallets.findById(wallet.getId()).orElseThrow();
        assertThat(persisted.getCreatedDate()).isBetween(beforeInsert, LocalDateTime.now().plusSeconds(1));
        assertThat(persisted.getUpdateDate()).isNotNull();
    }

    @Test
    void shouldAdvanceUpdateTimestampAndPreserveCreationDateAfterBalanceChange() throws InterruptedException {
        final var wallet = wallets.saveAndFlush(WalletEntity.builder()
                .amount(new BigDecimal("100.00")).build());
        entityManager.clear();
        final var persisted = wallets.findById(wallet.getId()).orElseThrow();
        final var createdDate = persisted.getCreatedDate();
        final var updateDate = persisted.getUpdateDate();

        // Separa os eventos mesmo em relógios com resolução de milissegundos.
        Thread.sleep(50);
        persisted.setAmount(new BigDecimal("90.00"));
        walletService.update(persisted);
        entityManager.flush();
        entityManager.clear();

        final var updated = wallets.findById(wallet.getId()).orElseThrow();
        assertThat(updated.getAmount()).isEqualByComparingTo("90.00");
        assertThat(updated.getCreatedDate()).isEqualTo(createdDate);
        assertThat(updated.getUpdateDate()).isAfter(updateDate).isAfter(createdDate);
    }

    @Test
    void shouldRefreshBothWalletTimestampsAfterTransfer() throws InterruptedException {
        final var payer = createUser("payer");
        final var payee = createUser("payee");
        entityManager.flush();
        entityManager.clear();
        final var payerBefore = wallets.findById(payer.getWallet().getId()).orElseThrow();
        final var payeeBefore = wallets.findById(payee.getWallet().getId()).orElseThrow();
        final var payerCreated = payerBefore.getCreatedDate();
        final var payeeCreated = payeeBefore.getCreatedDate();
        final var payerUpdated = payerBefore.getUpdateDate();
        final var payeeUpdated = payeeBefore.getUpdateDate();

        Thread.sleep(50);
        transactionService.transfer(new TransactionParameter(new BigDecimal("10.00"), payer.getId(), payee.getId()));
        entityManager.flush();
        entityManager.clear();

        final var payerAfter = wallets.findById(payerBefore.getId()).orElseThrow();
        final var payeeAfter = wallets.findById(payeeBefore.getId()).orElseThrow();
        assertThat(payerAfter.getAmount()).isEqualByComparingTo("90.00");
        assertThat(payeeAfter.getAmount()).isEqualByComparingTo("110.00");
        assertThat(payerAfter.getCreatedDate()).isEqualTo(payerCreated);
        assertThat(payeeAfter.getCreatedDate()).isEqualTo(payeeCreated);
        assertThat(payerAfter.getUpdateDate()).isAfter(payerUpdated).isAfter(payerCreated);
        assertThat(payeeAfter.getUpdateDate()).isAfter(payeeUpdated).isAfter(payeeCreated);
    }

    private UserEntity createUser(String name) {
        final var wallet = WalletEntity.builder().amount(new BigDecimal("100.00")).build();
        final var user = UserEntity.builder().name(name).document(name)
                .email(name + "@example.com").type(COMMON).wallet(wallet).build();
        wallet.setUser(user);
        return users.save(user);
    }
}
