package cl.pagoseguro.accounts.domain.port.out;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.MonthlySummary;

import java.time.YearMonth;

/** El dominio solo pide "un resumen mensual"; que lo calcule un procedimiento almacenado es un detalle. */
public interface MonthlySummaryPort {

    MonthlySummary summarize(AccountId accountId, YearMonth period);
}
