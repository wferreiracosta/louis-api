package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.dtos.UserDTO;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.services.impl.UserServiceImpl;
import br.com.wferreiracosta.louis.utils.ServiceTestAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static br.com.wferreiracosta.louis.utils.Generator.cnpj;
import static br.com.wferreiracosta.louis.utils.Generator.cpf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceTest extends ServiceTestAnnotations {

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
                cnpj(),
                "wesley@mail.com",
                "123"
        );

        final var entity = new UserEntity(wesley);
        entity.setType(MERCHANT);

        when(repository.save(entity)).thenReturn(entity);

        final var entitySaved = service.save(wesley, MERCHANT);

        assertEquals(wesley.name(), entitySaved.getName());
        assertEquals(wesley.document(), entitySaved.getDocument());
        assertEquals(MERCHANT, entitySaved.getType());
        assertEquals(wesley.email(), entitySaved.getEmail());
        assertEquals(wesley.password(), entitySaved.getPassword());
    }

    @Test
    void testingFindAllMerchantPageable() {
        final var page = 0;
        final var linesPerPage = 5;
        final var orderBy = "name";
        final var direction = "DESC";

        final var pedro = UserEntity.builder()
                .name("Pedro")
                .document(cpf())
                .type(MERCHANT)
                .email("pedro@mail.com")
                .password("123")
                .build();

        final var userList = List.of(pedro);

        final var pageable = new PageImpl<>(userList);

        when(repository.findByType(eq(MERCHANT), any(PageRequest.class))).thenReturn(pageable);

        final var resultPage = service.findAllPageableByType(page, linesPerPage, orderBy, direction, MERCHANT);

        assertEquals(userList, resultPage.getContent());
        verify(repository, times(1)).findByType(eq(MERCHANT), any(PageRequest.class));
    }

}
