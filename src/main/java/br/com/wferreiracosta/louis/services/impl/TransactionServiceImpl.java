package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.exceptions.TransactionException;
import br.com.wferreiracosta.louis.models.dtos.TransactionDTO;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.services.AuthorizeService;
import br.com.wferreiracosta.louis.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AuthorizeService authorizeService;

    @Override
    public TransactionDTO transfer(final TransactionParameter parameter) {
        if (!authorizeService.authorize()) {
            throw new TransactionException("Transaction was not authorized");
        }

        return null;
    }

}
