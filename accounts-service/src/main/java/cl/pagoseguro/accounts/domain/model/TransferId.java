package cl.pagoseguro.accounts.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TransferId(UUID value) {

    public TransferId {
        Objects.requireNonNull(value, "El id de la transferencia es obligatorio");
    }

    public static TransferId newId() {
        return new TransferId(UUID.randomUUID());
    }
}
