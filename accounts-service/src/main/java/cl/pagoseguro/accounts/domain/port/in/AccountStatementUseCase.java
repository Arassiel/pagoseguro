package cl.pagoseguro.accounts.domain.port.in;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.MonthlySummary;
import cl.pagoseguro.accounts.domain.model.Movement;

import java.time.YearMonth;
import java.util.List;

/** Cartola: consultas de movimientos y resumen mensual. */
public interface AccountStatementUseCase {

    List<Movement> latestMovements(AccountId accountId, int limit);

    MonthlySummary monthlySummary(AccountId accountId, YearMonth period);
}
