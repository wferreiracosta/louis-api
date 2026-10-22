package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class UserUniquenessConcurrencyTest {
    @Autowired private UserRepository repository;
    @Autowired private PlatformTransactionManager transactionManager;

    @ParameterizedTest
    @ValueSource(strings = {"document", "email"})
    void databaseRejectsConcurrentDuplicatesAfterBothPrechecksPass(String field) throws Exception {
        final var barrier = new CyclicBarrier(2);
        final var executor = Executors.newFixedThreadPool(2);
        try {
            final var first = executor.submit(() -> insert(field, 1, barrier));
            final var second = executor.submit(() -> insert(field, 2, barrier));
            assertEquals(1, first.get(15, TimeUnit.SECONDS) + second.get(15, TimeUnit.SECONDS));
            final var matches = repository.findAll().stream().filter(user -> field.equals("document")
                    ? user.getDocument().equals("shared-document") : user.getEmail().equals("shared@email.com")).count();
            assertEquals(1, matches);
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
            repository.deleteAll();
        }
    }

    private int insert(String field, int index, CyclicBarrier barrier) {
        try {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                final var document = field.equals("document") ? "shared-document" : "document-" + index;
                final var email = field.equals("email") ? "shared@email.com" : "email-" + index + "@mail.com";
                assertTrue(repository.findByDocument(document).isEmpty());
                assertTrue(repository.findByEmail(email).isEmpty());
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                } catch (Exception exception) {
                    throw new AssertionError("Both prechecks must finish before either insert", exception);
                }
                repository.saveAndFlush(UserEntity.builder().document(document).email(email).build());
            });
            return 1;
        } catch (DataIntegrityViolationException expected) {
            return 0;
        }
    }
}
