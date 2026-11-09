package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.MonthlySummary;
import cl.pagoseguro.accounts.domain.port.out.MonthlySummaryPort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

/**
 * Adaptador que llama al procedimiento almacenado dbo.sp_ResumenMovimientosMensual.
 *
 * Usa JdbcClient (SQL directo) en vez de JPA: para reportes y procedimientos almacenados,
 * JDBC es más simple y transparente. Convivir con ambos en un mismo servicio es normal.
 */
@Component
class MonthlySummaryStoredProcedureAdapter implements MonthlySummaryPort {

    private final JdbcClient jdbcClient;

    MonthlySummaryStoredProcedureAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public MonthlySummary summarize(AccountId accountId, YearMonth period) {
        return jdbcClient.sql("EXEC dbo.sp_ResumenMovimientosMensual @AccountId = ?, @Anio = ?, @Mes = ?")
                .params(accountId.value().toString(), period.getYear(), period.getMonthValue())
                .query((rs, rowNum) -> new MonthlySummary(
                        accountId,
                        period,
                        rs.getInt("total_movimientos"),
                        new Money(rs.getBigDecimal("total_depositos")),
                        new Money(rs.getBigDecimal("total_transferencias_recibidas")),
                        new Money(rs.getBigDecimal("total_transferencias_enviadas"))))
                .single();
    }
}
