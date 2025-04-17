package br.com.wferreiracosta.louis.annotations.validators;

import br.com.wferreiracosta.louis.annotations.Transaction;
import br.com.wferreiracosta.louis.exceptions.FieldMessage;
import br.com.wferreiracosta.louis.models.parameters.TransactionParameter;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

import static br.com.wferreiracosta.louis.models.enums.UserType.MERCHANT;
import static java.lang.String.format;

@RequiredArgsConstructor
public class TransactionValidator implements ConstraintValidator<Transaction, TransactionParameter> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(final TransactionParameter parameter, final ConstraintValidatorContext context) {
        final var payerField = "payer";
        final var payeeField = "payee";

        final var list = new ArrayList<FieldMessage>();

        final var userPayer = userRepository.findById(parameter.payer());
        final var userPayee = userRepository.findById(parameter.payee());

        if (userPayer.isEmpty()) {
            list.add(new FieldMessage(payerField, format("User payer with id %s not exists", parameter.payer())));
        }

        if (userPayee.isEmpty()) {
            list.add(new FieldMessage(payeeField, format("User payee with id %s not exists", parameter.payee())));
        }

        if (userPayer.isPresent()) {
            final var payer = userPayer.get();
            if (payer.getWallet().getAmount().compareTo(parameter.amount()) < 0) {
                list.add(new FieldMessage(payerField, "Payer does not have a balance in their wallet"));
            }

            if (payer.getType().equals(MERCHANT)) {
                list.add(new FieldMessage(payerField, "Merchants users only receive transfers, they do not send money to anyone"));
            }
        }

        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.message()).addPropertyNode(e.fieldName())
                    .addConstraintViolation();
        }

        return list.isEmpty();
    }

}
