package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DailyLimitExceededException;

import java.util.Objects;

/**
 * Política de negocio: cuánto puede transferir una cuenta en un día.
 * Es un objeto del dominio (no un "if" perdido en el servicio) para poder testearla y reutilizarla.
 */
public record DailyTransferLimit(Money limit) {

    public DailyTransferLimit {
        Objects.requireNonNull(limit, "El límite diario es obligatorio");
    }

    /** Lanza excepción si sumar el nuevo monto a lo ya transferido hoy supera el límite. */
    public void check(Money alreadyTransferredToday, Money requested) {
        if (alreadyTransferredToday.plus(requested).isGreaterThan(limit)) {
            throw new DailyLimitExceededException(limit, alreadyTransferredToday, requested);
        }
    }
}
