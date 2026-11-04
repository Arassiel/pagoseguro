package cl.pagoseguro.accounts.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Validación de FORMA (campos presentes, largo). La validación de NEGOCIO
 * (por ejemplo, que el dígito verificador del RUT sea correcto) la hace el dominio.
 */
public record OpenAccountRequest(
        @NotBlank(message = "El RUT es obligatorio") String holderRut,
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 120) String holderName) {
}
