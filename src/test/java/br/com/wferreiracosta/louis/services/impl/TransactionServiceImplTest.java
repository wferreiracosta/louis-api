package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.exceptions.BusinessValidationException;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.repositories.TransactionRepository;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.services.WalletService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl service;

    @Test
    @DisplayName("Should throw BusinessValidationException and have no interactions when payer equals payee")
    void shouldThrowExceptionWhenPayerEqualsPayee() {
        final var parameter = new TransactionParameter(new BigDecimal("100"), 1L, 1L);

        final var exception = assertThrows(BusinessValidationException.class, () -> service.transfer(parameter));

        assertEquals("payer", exception.getFieldName());
        assertEquals("Self-transfer is not allowed", exception.getMessage());
        verifyNoInteractions(userRepository, walletService, repository);
    }

}
