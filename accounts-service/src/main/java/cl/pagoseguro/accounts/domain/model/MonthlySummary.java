package cl.pagoseguro.accounts.domain.model;

import java.time.YearMonth;

/** Resumen mensual de una cuenta (lo calcula un procedimiento almacenado en SQL Server). */
public record MonthlySummary(
        AccountId accountId,
        YearMonth period,
        int totalMovements,
        Money totalDeposits,
        Money totalTransfersIn,
        Money totalTransfersOut) {
}
