package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.entities.WalletEntity;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.repositories.TransactionRepository;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.repositories.WalletRepository;
import br.com.wferreiracosta.louis.services.impl.TransactionServiceImpl;
import br.com.wferreiracosta.louis.services.impl.UserServiceImpl;
import br.com.wferreiracosta.louis.services.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static br.com.wferreiracosta.louis.utils.Generator.cpf;
import static br.com.wferreiracosta.louis.utils.Generator.email;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations = {"classpath:application-test.properties"})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // Evita transação de teste que esconderia dados das threads spawned
class TransactionConcurrencyTest {

    private TransactionService service;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long userAId;
    private Long userBId;
    private Long walletAId;
    private Long walletBId;

    @BeforeEach
    void setUp() {
        final var userService = new UserServiceImpl(userRepository);
        final var walletService = new WalletServiceImpl(walletRepository, userService);
        service = new TransactionServiceImpl(walletService, transactionRepository, userRepository);

        // Commita os dados explicitamente para que as threads spawned possam vê-los
        new TransactionTemplate(transactionManager).execute(status -> {
            final var walletA = WalletEntity.builder()
                    .amount(new BigDecimal("1000"))
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .transferring(new ArrayList<>())
                    .receiving(new ArrayList<>())
                    .build();
            final var userA = UserEntity.builder()
                    .name("UserA")
                    .document(cpf())
                    .type(COMMON)
                    .email(email())
                    .password("123")
                    .wallet(walletA)
                    .build();
            walletA.setUser(userA);
            final var savedA = userRepository.save(userA);
            userAId = savedA.getId();
            walletAId = savedA.getWallet().getId();

            final var walletB = WalletEntity.builder()
                    .amount(new BigDecimal("1000"))
                    .createdDate(LocalDateTime.now())
                    .updateDate(LocalDateTime.now())
                    .transferring(new ArrayList<>())
                    .receiving(new ArrayList<>())
                    .build();
            final var userB = UserEntity.builder()
                    .name("UserB")
                    .document(cpf())
                    .type(COMMON)
                    .email(email())
                    .password("123")
                    .wallet(walletB)
                    .build();
            walletB.setUser(userB);
            final var savedB = userRepository.save(userB);
            userBId = savedB.getId();
            walletBId = savedB.getWallet().getId();

            return null;
        });
    }

    @Test
    void concurrentBidirectionalTransfersShouldNotThrowAndShouldKeepBalancesConsistent() throws InterruptedException {
        final int threadCount = 10;
        final BigDecimal transferAmount = new BigDecimal("10");

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);
        final AtomicInteger errorCount = new AtomicInteger(0);

        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final boolean aToB = i % 2 == 0;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    // 5 threads A→B e 5 threads B→A, totalizando saldo líquido zero
                    final var param = aToB
                            ? new TransactionParameter(transferAmount, userAId, userBId)
                            : new TransactionParameter(transferAmount, userBId, userAId);
                    // TransactionTemplate simula o @Transactional que envolve transfer() em produção.
                    // Sem ele, os serviços (instanciados manualmente sem proxy Spring) não têm
                    // transação ativa, e as coleções lazy (transferring/receiving) lançam
                    // LazyInitializationException ao serem acessadas fora do contexto de persistência.
                    new TransactionTemplate(transactionManager).execute(status -> {
                        service.transfer(param);
                        return null;
                    });
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Dispara todas as threads simultaneamente
        doneLatch.await();
        executor.shutdown();

        assertEquals(0, errorCount.get(), "Nenhuma exceção deve ocorrer em transferências bidirecionais concorrentes");

        // Verifica conservação de dinheiro: A+B deve ser igual ao total inicial (2000).
        // H2 não replica o comportamento de lock do PostgreSQL em produção (trade-off documentado no design.md);
        // a invariante verificável aqui é que nenhum dinheiro foi criado ou destruído.
        final var walletAAfter = new TransactionTemplate(transactionManager).execute(
                status -> walletRepository.findById(walletAId).get());
        final var walletBAfter = new TransactionTemplate(transactionManager).execute(
                status -> walletRepository.findById(walletBId).get());

        final var totalBalance = walletAAfter.getAmount().add(walletBAfter.getAmount());
        assertEquals(0, totalBalance.compareTo(new BigDecimal("2000")),
                "A soma dos saldos deve ser preservada: nenhum dinheiro deve ser criado ou destruído");
    }
}
