package cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto;

import cl.pagoseguro.accounts.domain.model.Movement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementResponse(
        UUID id,
        String type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        UUID transferId,
        String description,
        Instant createdAt) {

    public static MovementResponse from(Movement m) {
        return new MovementResponse(m.id(), m.type().name(), m.amount().amount(), m.balanceAfter().amount(),
                m.transferId() == null ? null : m.transferId().value(), m.description(), m.createdAt());
    }
}
