package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monto en pesos chilenos (Value Object). Nunca negativo, siempre con 2 decimales.
 * Usar BigDecimal (y no double) es obligatorio para dinero: evita errores de redondeo.
 */
public record Money(BigDecimal amount) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (amount == null) {
            throw new DomainException("El monto es obligatorio");
        }
        if (amount.signum() < 0) {
            throw new DomainException("El monto no puede ser negativo");
        }
        try {
            amount = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new DomainException("El monto admite como máximo 2 decimales");
        }
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }
}
