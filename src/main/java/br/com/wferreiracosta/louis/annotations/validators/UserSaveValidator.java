package br.com.wferreiracosta.louis.annotations.validators;

import br.com.wferreiracosta.louis.annotations.UserValidator;
import br.com.wferreiracosta.louis.exceptions.FieldMessage;
import br.com.wferreiracosta.louis.models.parameters.UserParameter;
import br.com.wferreiracosta.louis.repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class UserSaveValidator implements ConstraintValidator<UserValidator, UserParameter> {

    private final UserRepository repository;

    @Override
    public boolean isValid(final UserParameter parameter, final ConstraintValidatorContext context) {
        final var list = new ArrayList<FieldMessage>();

        if (repository.findByDocument(parameter.document()).isPresent()) {
            list.add(new FieldMessage("document", "There is already a user registered with this document"));
        }

        if (repository.findByEmail(parameter.email()).isPresent()) {
            list.add(new FieldMessage("email", "There is already a user registered with this email"));
        }

        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.message()).addPropertyNode(e.fieldName())
                    .addConstraintViolation();
        }

        return list.isEmpty();
    }

}
