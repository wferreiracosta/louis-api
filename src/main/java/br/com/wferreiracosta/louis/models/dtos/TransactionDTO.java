package br.com.wferreiracosta.louis.models.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(

        BigDecimal amount,
        LocalDateTime date,
        TransactionUserDTO payer,
        TransactionUserDTO payee

) {
}
