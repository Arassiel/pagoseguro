package cl.pagoseguro.accounts.domain.port.out;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Movement;

import java.time.Instant;
import java.util.List;

public interface MovementRepositoryPort {

    void saveAll(List<Movement> movements);

    /** Últimos movimientos de la cuenta, del más reciente al más antiguo. */
    List<Movement> findLatestByAccount(AccountId accountId, int limit);

    /** Suma de transferencias enviadas desde un instante (para el límite diario). */
    Money sumTransfersOutSince(AccountId accountId, Instant since);
}
