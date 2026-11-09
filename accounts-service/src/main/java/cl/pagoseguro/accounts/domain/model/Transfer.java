package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DomainException;

import java.time.Instant;
import java.util.Objects;

/** Transferencia entre dos cuentas. Inmutable: una vez hecha, no se modifica. */
public record Transfer(
        TransferId id,
        AccountId sourceAccountId,
        AccountId targetAccountId,
        Money amount,
        String description,
        Instant createdAt) {

    private static final int MAX_DESCRIPTION_LENGTH = 140;

    public Transfer {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sourceAccountId);
        Objects.requireNonNull(targetAccountId);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(createdAt);
        if (sourceAccountId.equals(targetAccountId)) {
            throw new DomainException("No se puede transferir a la misma cuenta");
        }
        if (amount.isZero()) {
            throw new DomainException("El monto debe ser mayor a cero");
        }
        description = description == null || description.isBlank() ? null : description.trim();
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new DomainException("La descripción no puede superar " + MAX_DESCRIPTION_LENGTH + " caracteres");
        }
    }

    public static Transfer create(AccountId source, AccountId target, Money amount, String description, Instant now) {
        return new Transfer(TransferId.newId(), source, target, amount, description, now);
    }
}
