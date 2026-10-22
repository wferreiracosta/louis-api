package br.com.wferreiracosta.louis.models.parameters;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class UserParameterTest {
    @Test
    void validatesRequiredFieldsWithoutSpringOrDatabase() {
        try (final var factory = Validation.buildDefaultValidatorFactory()) {
            final var validator = factory.getValidator();
            assertTrue(validator.validate(new UserParameter("Pedro", "Silva", "123", "pedro@mail.com", "secret")).isEmpty());
            final var errors = validator.validate(new UserParameter("", " ", null, "", ""));
            assertEquals(5, errors.size());
            assertEquals(Set.of("name", "surname", "document", "email", "password"),
                    errors.stream().map(error -> error.getPropertyPath().toString()).collect(Collectors.toSet()));
        }
    }
}
