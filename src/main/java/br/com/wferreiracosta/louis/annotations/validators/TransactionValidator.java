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
        return true;
    }

}
