package br.com.wferreiracosta.louis.services;

import br.com.wferreiracosta.louis.models.dtos.TransactionDTO;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;

public interface TransactionService {

    TransactionDTO transfer(TransactionParameter parameter);

}
