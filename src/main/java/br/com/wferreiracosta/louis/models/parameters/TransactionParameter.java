package br.com.wferreiracosta.louis.models.parameters;

import br.com.wferreiracosta.louis.annotations.Transaction;

import java.math.BigDecimal;

@Transaction
public record TransactionParameter(

        BigDecimal value,
        Long payer,
        Long payee

) {
}
