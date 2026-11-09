package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Movimiento de cartola: registro inmutable de cada cambio de saldo.
 *
 * En banca el saldo nunca se modifica "a ciegas": cada cambio deja un movimiento con el saldo
 * resultante (balanceAfter). Así se puede auditar y reconstruir la historia de una cuenta.
 */
public record Movement(
        UUID id,
        AccountId accountId,
        MovementType type,
        Money amount,
        Money balanceAfter,
        TransferId transferId,
        String description,
        Instant createdAt) {

    public Movement {
        Objects.requireNonNull(id);
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(amount);
        Objects.requireNonNull(balanceAfter);
        Objects.requireNonNull(createdAt);
        description = description == null || description.isBlank() ? null : description.trim();
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new DomainException("La descripción no puede superar " + MAX_DESCRIPTION_LENGTH + " caracteres");
        }
    }

    private static final int MAX_DESCRIPTION_LENGTH = 140;

    public static Movement deposit(Account account, Money amount, String description, Instant now) {
        return new Movement(UUID.randomUUID(), account.getId(), MovementType.DEPOSIT, amount,
                account.getBalance(), null, description, now);
    }

    public static Movement transferOut(Account source, Transfer transfer) {
        return new Movement(UUID.randomUUID(), source.getId(), MovementType.TRANSFER_OUT, transfer.amount(),
                source.getBalance(), transfer.id(), transfer.description(), transfer.createdAt());
    }

    public static Movement transferIn(Account target, Transfer transfer) {
        return new Movement(UUID.randomUUID(), target.getId(), MovementType.TRANSFER_IN, transfer.amount(),
                target.getBalance(), transfer.id(), transfer.description(), transfer.createdAt());
    }
}
