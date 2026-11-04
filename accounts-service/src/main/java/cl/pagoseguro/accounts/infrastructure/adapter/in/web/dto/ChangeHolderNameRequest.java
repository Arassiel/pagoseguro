package cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeHolderNameRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String holderName) {
}
