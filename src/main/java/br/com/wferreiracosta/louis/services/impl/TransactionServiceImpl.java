package br.com.wferreiracosta.louis.services.impl;

import br.com.wferreiracosta.louis.exceptions.BusinessValidationException;
import br.com.wferreiracosta.louis.models.dtos.TransactionDTO;
import br.com.wferreiracosta.louis.models.dtos.TransactionUserDTO;
import br.com.wferreiracosta.louis.models.entities.TransactionEntity;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.repositories.TransactionRepository;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import br.com.wferreiracosta.louis.services.TransactionService;
import br.com.wferreiracosta.louis.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final WalletService walletService;
    private final TransactionRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionDTO transfer(final TransactionParameter parameter) {
        final var payerField = "payer";
        final var payeeField = "payee";

        final var userPayer = userRepository.findById(parameter.payer());
        if (userPayer.isEmpty()) {
            throw new BusinessValidationException(payerField, format("User payer with id %s not exists", parameter.payer()));
        }

        final var userPayee = userRepository.findById(parameter.payee());
        if (userPayee.isEmpty()) {
            throw new BusinessValidationException(payeeField, format("User payee with id %s not exists", parameter.payee()));
        }

        final var payer = userPayer.get();
        if (payer.getType().equals(MERCHANT)) {
            throw new BusinessValidationException(payerField, "Merchants users only receive transfers, they do not send money to anyone");
        }

        final var payerWallet = walletService.findByUserIdWithLock(parameter.payer());
        final var payeeWallet = walletService.findByUserIdWithLock(parameter.payee());

        if (payerWallet.getAmount().compareTo(parameter.amount()) < 0) {
            throw new BusinessValidationException(payerField, "Payer does not have a balance in their wallet");
        }

        payerWallet.setAmount(payerWallet.getAmount().subtract(parameter.amount()));
        payeeWallet.setAmount(payeeWallet.getAmount().add(parameter.amount()));

        final var transaction = TransactionEntity.builder()
                .amount(parameter.amount())
                .transferring(payerWallet)
                .receiving(payeeWallet)
                .timestamp(now())
                .build();
        final var transactionSaved = repository.save(transaction);

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
