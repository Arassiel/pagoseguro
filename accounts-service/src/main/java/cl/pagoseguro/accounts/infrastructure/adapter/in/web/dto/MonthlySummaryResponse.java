package cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto;

import cl.pagoseguro.accounts.domain.model.MonthlySummary;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlySummaryResponse(
        UUID accountId,
        String period,
        int totalMovements,
        BigDecimal totalDeposits,
        BigDecimal totalTransfersIn,
        BigDecimal totalTransfersOut) {

    public static MonthlySummaryResponse from(MonthlySummary s) {
        return new MonthlySummaryResponse(s.accountId().value(), s.period().toString(), s.totalMovements(),
                s.totalDeposits().amount(), s.totalTransfersIn().amount(), s.totalTransfersOut().amount());
    }
}
