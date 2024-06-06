package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.exceptions.TransactionException;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.services.impl.TransactionServiceImpl;
import br.com.wferreiracosta.louis.utils.ServiceTestAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TransactionServiceTest extends ServiceTestAnnotations {

    @MockBean
    private AuthorizeService authorizeService;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(authorizeService);
    }

    @Test
    void transferFailedBecauseItWasNotAuthorized() {
        when(authorizeService.authorize()).thenReturn(false);

        final var thrown = assertThrows(
                TransactionException.class,
                () -> service.transfer(new TransactionParameter(new BigDecimal(5), 1L, 2L))
        );

        final var message = "Transaction was not authorized";
        assertEquals(message, thrown.getLocalizedMessage());
    }

}
