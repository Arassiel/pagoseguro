package cl.pagoseguro.accounts.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identificador de cuenta. Un tipo propio evita confundir un UUID de cuenta con cualquier otro UUID. */
public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "El id de la cuenta es obligatorio");
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId of(String value) {
        return new AccountId(UUID.fromString(value));
    }
}
