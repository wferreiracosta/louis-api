package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.models.dtos.TransactionDTO;
import br.com.wferreiracosta.louis.models.dtos.TransactionUserDTO;
import br.com.wferreiracosta.louis.models.entities.TransactionEntity;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.repositories.TransactionRespository;
import br.com.wferreiracosta.louis.services.TransactionService;
import br.com.wferreiracosta.louis.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final WalletService walletService;
    private final TransactionRespository respository;

    @Override
    public TransactionDTO transfer(final TransactionParameter parameter) {
        final var payerWallet = walletService.findByUserId(parameter.payer());
        final var payeeWallet = walletService.findByUserId(parameter.payee());

        payerWallet.setAmount(payerWallet.getAmount().subtract(parameter.value()));
        payeeWallet.setAmount(payeeWallet.getAmount().add(parameter.value()));

        final var transaction = TransactionEntity.builder()
                .amount(parameter.value())
                .transferring(payerWallet)
                .receiving(payeeWallet)
                .timestamp(now())
                .build();
        final var transactionSaved = respository.save(transaction);

        payerWallet.getTransferring().add(transactionSaved);
        final var payerWalletUpdate = walletService.update(payerWallet);

        payeeWallet.getReceiving().add(transactionSaved);
        final var payeeWalletUpdate = walletService.update(payeeWallet);

        final var payerUser = payerWalletUpdate.getUser();
        final var payeeUser = payeeWalletUpdate.getUser();

        final var payerTransaction = new TransactionUserDTO(payerUser.getName(), payerUser.getSurname(), payerUser.getEmail());
        final var payeeTransaction = new TransactionUserDTO(payeeUser.getName(), payeeUser.getSurname(), payeeUser.getEmail());

        return new TransactionDTO(transaction.getAmount(), transaction.getTimestamp(), payerTransaction, payeeTransaction);
    }

}
