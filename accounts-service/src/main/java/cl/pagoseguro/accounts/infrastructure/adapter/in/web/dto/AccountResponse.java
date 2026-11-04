package cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto;

import cl.pagoseguro.accounts.domain.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** DTO de salida: decide qué se expone en la API, independiente de cómo es el dominio por dentro. */
public record AccountResponse(
        UUID id,
        String accountNumber,
        String holderRut,
        String holderName,
        BigDecimal balance,
        String currency,
        String status,
        Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId().value(),
                account.getAccountNumber(),
                account.getHolderRut().value(),
                account.getHolderName(),
                account.getBalance().amount(),
                "CLP",
                account.getStatus().name(),
                account.getCreatedAt());
    }
}
