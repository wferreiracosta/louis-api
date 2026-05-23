package br.com.wferreiracosta.louis.models.parameters;

import java.math.BigDecimal;

public record TransactionParameter(

        BigDecimal amount,
        Long payer,
        Long payee

) {
}
