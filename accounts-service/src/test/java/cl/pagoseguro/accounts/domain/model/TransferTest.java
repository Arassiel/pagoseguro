package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferTest {

    private static final Instant NOW = Instant.parse("2026-11-09T12:00:00Z");

    @Test
    void noSePuedeTransferirALaMismaCuenta() {
        AccountId same = AccountId.newId();

        assertThatThrownBy(() -> Transfer.create(same, same, Money.of("100"), null, NOW))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("misma cuenta");
    }

    @Test
    void elMontoDebeSerMayorACero() {
        assertThatThrownBy(() -> Transfer.create(AccountId.newId(), AccountId.newId(), Money.ZERO, null, NOW))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void normalizaLaDescripcion() {
        Transfer withSpaces = Transfer.create(AccountId.newId(), AccountId.newId(), Money.of("1"), "  Arriendo  ", NOW);
        Transfer blank = Transfer.create(AccountId.newId(), AccountId.newId(), Money.of("1"), "   ", NOW);

        assertThat(withSpaces.description()).isEqualTo("Arriendo");
        assertThat(blank.description()).isNull();
    }
}
