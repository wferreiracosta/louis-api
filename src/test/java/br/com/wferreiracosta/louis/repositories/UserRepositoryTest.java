package br.com.wferreiracosta.louis.repositories;

import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.utils.tests.RepositoryTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static br.com.wferreiracosta.louis.models.enums.UserType.COMMON;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends RepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository repository;

    @Test
    void testingFindById() {
        final var entity = UserEntity.builder()
                .name("Wesley")
                .document("12345678996")
                .type(COMMON)
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
                .document("12345678996")
                .type(COMMON)
                .email("wesley@mail.com")
                .password("123")
                .build();
        final var pedro = UserEntity.builder()
                .name("Pedro")
                .document("98765432136")
                .type(COMMON)
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
                .document("12345678996")
                .type(COMMON)
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


}
