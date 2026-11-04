package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void siempreTieneDosDecimales() {
        assertThat(Money.of("1500").amount()).hasToString("1500.00");
    }

    @Test
    void sumaYRestaSinPerderPrecision() {
        Money result = Money.of("0.10").plus(Money.of("0.20"));
        assertThat(result).isEqualTo(Money.of("0.30")); // con double esto daría 0.30000000000000004
        assertThat(result.minus(Money.of("0.30")).isZero()).isTrue();
    }

    @Test
    void noPermiteMontosNegativos() {
        assertThatThrownBy(() -> Money.of("-1")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Money.of("10").minus(Money.of("20"))).isInstanceOf(DomainException.class);
    }

    @Test
    void noPermiteMasDeDosDecimales() {
        assertThatThrownBy(() -> Money.of("1.005"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("2 decimales");
    }

    @Test
    void comparaMontos() {
        assertThat(Money.of("100").isGreaterThan(Money.of("99.99"))).isTrue();
        assertThat(Money.of("100").isGreaterThan(Money.of("100"))).isFalse();
    }
}
