package br.com.wferreiracosta.louis.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserUniquenessValidationExceptionTest {
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void returnsAllErrorsWith422(int count) {
        final var errors = List.of(
                new FieldMessage("document", "There is already a user registered with this document"),
                new FieldMessage("email", "There is already a user registered with this email")).subList(0, count);
        final var response = new GlobalExceptionHandler().userUniquenessValidation(new UserUniquenessValidationException(errors));
        assertEquals(422, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().status());
        assertEquals("Errors", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
        assertEquals(errors, response.getBody().errors());
    }

    @Test
    void retainsAnImmutableNonemptySnapshot() {
        final var errors = new ArrayList<>(List.of(new FieldMessage("email", "duplicate")));
        final var exception = new UserUniquenessValidationException(errors);
        errors.clear();
        assertEquals(1, exception.getErrors().size());
        assertThrows(UnsupportedOperationException.class, () -> exception.getErrors().clear());
        assertThrows(IllegalArgumentException.class, () -> new UserUniquenessValidationException(List.of()));
    }

    @Test
    void otherBusinessErrorsRemain400() {
        final var response = new GlobalExceptionHandler().businessValidation(new BusinessValidationException("payer", "Self-transfer is not allowed"));
        assertEquals(400, response.getStatusCode().value());
        assertEquals(400, response.getBody().status());
    }
}
