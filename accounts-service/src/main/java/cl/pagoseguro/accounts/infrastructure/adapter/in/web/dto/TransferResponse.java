package cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto;

import cl.pagoseguro.accounts.domain.model.Transfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String currency,
        String description,
        Instant createdAt) {

    public static TransferResponse from(Transfer t) {
        return new TransferResponse(t.id().value(), t.sourceAccountId().value(), t.targetAccountId().value(),
                t.amount().amount(), "CLP", t.description(), t.createdAt());
    }
}
