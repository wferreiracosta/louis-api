package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.exceptions.FieldMessage;
import br.com.wferreiracosta.louis.exceptions.UserUniquenessValidationException;
import br.com.wferreiracosta.louis.models.entities.UserEntity;
import br.com.wferreiracosta.louis.models.enums.UserType;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserRepository repository;
    @InjectMocks private UserServiceImpl service;
    private final UserParameter parameter = new UserParameter("Pedro", "Silva", "123", "pedro@mail.com", "secret");

    @ParameterizedTest
    @CsvSource({"true,false,false", "false,true,false", "true,true,true", "true,true,false"})
    void rejectsAllConflictsWithoutSaving(boolean document, boolean email, boolean sameUser) {
        final var existing = new UserEntity();
        when(repository.findByDocument(parameter.document())).thenReturn(document ? Optional.of(existing) : Optional.empty());
        when(repository.findByEmail(parameter.email())).thenReturn(email ? Optional.of(sameUser ? existing : new UserEntity()) : Optional.empty());
        final var expected = new ArrayList<FieldMessage>();
        if (document) expected.add(new FieldMessage("document", "There is already a user registered with this document"));
        if (email) expected.add(new FieldMessage("email", "There is already a user registered with this email"));

        final var error = assertThrows(UserUniquenessValidationException.class, () -> service.save(parameter, UserType.COMMON));

        assertEquals(expected, error.getErrors());
        verify(repository).findByDocument(parameter.document());
        verify(repository).findByEmail(parameter.email());
        verify(repository, never()).save(any());
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @EnumSource(UserType.class)
    void createsUserAndWalletWhenBothFieldsAreAvailable(UserType type) {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        final var user = service.save(parameter, type);
        assertEquals(type, user.getType());
        assertEquals(parameter.document(), user.getDocument());
        assertEquals(parameter.email(), user.getEmail());
        assertEquals(0, BigDecimal.ZERO.compareTo(user.getWallet().getAmount()));
        assertSame(user, user.getWallet().getUser());
        final var order = inOrder(repository);
        order.verify(repository).findByDocument(parameter.document());
        order.verify(repository).findByEmail(parameter.email());
        order.verify(repository).save(user);
    }
}
