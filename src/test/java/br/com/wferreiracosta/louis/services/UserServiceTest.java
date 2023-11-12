package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.services.impl.UserServiceImpl;
import br.com.wferreiracosta.louis.utils.tests.ServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

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
    void testingSaveMerchant() {
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

        final var entitySaved = service.saveMerchant(wesley);

        assertEquals(wesley.name(), entitySaved.getName());
        assertEquals(wesley.document(), entitySaved.getDocument());
        assertEquals(MERCHANT, entitySaved.getType());
        assertEquals(wesley.email(), entitySaved.getEmail());
        assertEquals(wesley.password(), entitySaved.getPassword());
    }

}
