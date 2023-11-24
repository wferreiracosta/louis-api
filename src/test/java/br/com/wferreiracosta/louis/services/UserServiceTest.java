package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.exceptions.DataViolationException;
import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.services.impl.UserServiceImpl;
import br.com.wferreiracosta.louis.utils.tests.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserServiceTest extends ServiceTest {

    @MockBean
    private UserRepository repository;

    private UserService service;


    @BeforeEach
    public void setUp() {
        service = new UserServiceImpl(repository);
    }

    @Test
    void testingSave() {
        final var wesley = new UserDTO(
                "Wesley",
                "Silva",
                "12345678996",
                "wesley@mail.com",
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Wesley")
                .document("12345678996")
                .type(MERCHANT)
                .email("wesley@mail.com")
                .password("123")
                .build();

        when(repository.save(entity)).thenReturn(entity);

        final var entitySaved = service.save(wesley, MERCHANT);

        assertEquals(wesley.name(), entitySaved.getName());
        assertEquals(wesley.document(), entitySaved.getDocument());
        assertEquals(MERCHANT, entitySaved.getType());
        assertEquals(wesley.email(), entitySaved.getEmail());
        assertEquals(wesley.password(), entitySaved.getPassword());
    }

    @Test
    void testingSaveUserWithDocumentDuplicate() throws Exception {
        final var wesley = new UserDTO(
                "Wesley",
                "Silva",
                "12345678996",
                "wesley@mail.com",
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Wesley")
                .document("12345678996")
                .type(MERCHANT)
                .email("wesley@mail.com")
                .password("123")
                .build();

        when(repository.findByDocument(wesley.document())).thenReturn(Optional.of(entity));

        try {
            service.save(wesley, MERCHANT);
        } catch (final DataViolationException e) {
            final var message = "There is already a user registered with this document.";
            assertEquals(message, e.getLocalizedMessage());
        }

    }

    @Test
    void testingSaveUserWithEmailDuplicate() throws Exception {
        final var wesley = new UserDTO(
                "Wesley",
                "Silva",
                "12345678996",
                "wesley@mail.com",
                "123"
        );

        final var entity = UserEntity.builder()
                .name("Wesley")
                .document("12345678996")
                .type(MERCHANT)
                .email("wesley@mail.com")
                .password("123")
                .build();

        when(repository.findByEmail(wesley.email())).thenReturn(Optional.of(entity));

        try {
            service.save(wesley, MERCHANT);
        } catch (final DataViolationException e) {
            final var message = "There is already a user registered with this email.";
            assertEquals(message, e.getLocalizedMessage());
        }

    }

}
