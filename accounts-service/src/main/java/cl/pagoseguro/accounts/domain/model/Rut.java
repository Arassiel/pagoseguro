package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.InvalidRutException;

/**
 * RUT chileno (Value Object). Inmutable y siempre válido: si existe, el dígito verificador es correcto.
 * Se guarda normalizado, sin puntos y con guion: "12345678-5".
 */
public record Rut(String value) {

    public Rut {
        if (value == null || value.isBlank()) {
            throw new InvalidRutException("El RUT es obligatorio");
        }
        value = normalize(value);
        if (!value.matches("\\d{7,8}-[\\dK]")) {
            throw new InvalidRutException("Formato de RUT inválido: " + value);
        }
        String body = value.substring(0, value.indexOf('-'));
        char dv = value.charAt(value.length() - 1);
        if (calculateDv(body) != dv) {
            throw new InvalidRutException("Dígito verificador inválido para el RUT: " + value);
        }
    }

    private static String normalize(String raw) {
        String clean = raw.replace(".", "").replace("-", "").trim().toUpperCase();
        if (clean.length() < 2) {
            return clean;
        }
        return clean.substring(0, clean.length() - 1) + "-" + clean.charAt(clean.length() - 1);
    }

    /** Algoritmo módulo 11. */
    static char calculateDv(String body) {
        int sum = 0;
        int factor = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int result = 11 - (sum % 11);
        return switch (result) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> Character.forDigit(result, 10);
        };
    }
}
