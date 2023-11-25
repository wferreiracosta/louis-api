package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.utils.RepositoryTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static br.com.wferreiracosta.louis.utils.RandomDocumentGenerator.generateRandomCNPJ;
import static br.com.wferreiracosta.louis.utils.RandomDocumentGenerator.generateRandomCPF;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends RepositoryTestAnnotations {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository repository;

    @Test
    void testingFindById() {
        final var entity = UserEntity.builder()
                .name("Wesley")
                .document(generateRandomCNPJ())
                .type(MERCHANT)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var entitySaved = testEntityManager.persist(entity);
        final var entityReturnOptional = repository.findById(entitySaved.getId());

        assertTrue(entityReturnOptional.isPresent());

        final var entityReturn = entityReturnOptional.get();
        assertEquals(entity.getName(), entityReturn.getName());
        assertEquals(entity.getDocument(), entityReturn.getDocument());
        assertEquals(entity.getType(), entityReturn.getType());
        assertEquals(entity.getEmail(), entityReturn.getEmail());
        assertEquals(entity.getPassword(), entityReturn.getPassword());
    }

    @Test
    void testingFindAll() {
        final var wesley = UserEntity.builder()
                .name("Wesley")
                .document(generateRandomCNPJ())
                .type(MERCHANT)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var pedro = UserEntity.builder()
                .name("Pedro")
                .document(generateRandomCNPJ())
                .type(MERCHANT)
                .email("pedro@mail.com")
                .password("123")
                .build();

        testEntityManager.persist(wesley);
        testEntityManager.persist(pedro);

        final var entityAll = repository.findAll();

        assertEquals(2, entityAll.size());
        assertTrue(entityAll.contains(wesley));
        assertTrue(entityAll.contains(pedro));
    }

    @Test
    void testingSave() {
        final var entity = UserEntity.builder()
                .name("Wesley")
                .document(generateRandomCNPJ())
                .type(MERCHANT)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var entitySaved = repository.save(entity);

        assertNotNull(entitySaved.getId());
        assertEquals(entity.getName(), entitySaved.getName());
        assertEquals(entity.getDocument(), entitySaved.getDocument());
        assertEquals(entity.getType(), entitySaved.getType());
        assertEquals(entity.getEmail(), entitySaved.getEmail());
        assertEquals(entity.getPassword(), entitySaved.getPassword());
    }

    @Test
    void testingFindByDocumentReturnUser() {
        final var wesley = UserEntity.builder()
                .name("Wesley")
                .document(generateRandomCPF())
                .type(COMMON)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var pedro = UserEntity.builder()
                .name("Pedro")
                .document(generateRandomCPF())
                .type(COMMON)
                .email(generateRandomCNPJ())
                .password("123")
                .build();

        testEntityManager.persist(wesley);
        testEntityManager.persist(pedro);

        final var result = repository.findByDocument(wesley.getDocument());

        assertTrue(result.isPresent());
        assertEquals(result.get(), wesley);
    }

    @Test
    void testingFindByDocumentReturnEmpty() {
        final var wesley = UserEntity.builder()
                .name("Wesley")
                .document(generateRandomCPF())
                .type(COMMON)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var pedro = UserEntity.builder()
                .name("Pedro")
                .document(generateRandomCPF())
                .type(COMMON)
                .email("pedro@mail.com")
                .password("123")
                .build();

        testEntityManager.persist(wesley);
        testEntityManager.persist(pedro);

        final var result = repository.findByDocument(generateRandomCNPJ());

        assertFalse(result.isPresent());
    }

    @Test
    void testingFindByEmailReturnUser() {
        final var wesley = UserEntity.builder()
                .name("Wesley")
                .document(generateRandomCPF())
                .type(COMMON)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var pedro = UserEntity.builder()
                .name("Pedro")
                .document(generateRandomCPF())
                .type(COMMON)
                .email(generateRandomCNPJ())
                .password("123")
                .build();

        testEntityManager.persist(wesley);
        testEntityManager.persist(pedro);

        final var result = repository.findByEmail(wesley.getEmail());

        assertTrue(result.isPresent());
        assertEquals(result.get(), wesley);
    }

    @Test
    void testingFindByEmailReturnEmpty() {
        final var wesley = UserEntity.builder()
                .name("Wesley")
                .document(generateRandomCPF())
                .type(COMMON)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var pedro = UserEntity.builder()
                .name("Pedro")
                .document(generateRandomCPF())
                .type(COMMON)
                .email("pedro@mail.com")
                .password("123")
                .build();

        testEntityManager.persist(wesley);
        testEntityManager.persist(pedro);

        final var result = repository.findByDocument("teste@teste.com");

        assertFalse(result.isPresent());
    }

}
