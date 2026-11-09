package cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "La cuenta de origen es obligatoria") UUID sourceAccountId,
        @NotNull(message = "La cuenta de destino es obligatoria") UUID targetAccountId,
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        @Digits(integer = 17, fraction = 2, message = "Monto inválido (máximo 2 decimales)")
        BigDecimal amount,
        @Size(max = 140) String description) {
}
