package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests del dominio puro: sin Spring, sin base de datos, sin mocks. Corren en milisegundos. */
class AccountTest {

    private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");
    private static final Rut RUT = new Rut("12.345.678-5");

    @Test
    void unaCuentaNuevaEstaActivaYConSaldoCero() {
        Account account = Account.open("0012345678", RUT, "  Franco Vera  ", NOW);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualTo(Money.ZERO);
        assertThat(account.getHolderName()).isEqualTo("Franco Vera");
        assertThat(account.getCreatedAt()).isEqualTo(NOW);
        assertThat(account.getId()).isNotNull();
    }

    @Test
    void elNombreDelTitularEsObligatorio() {
        assertThatThrownBy(() -> Account.open("0012345678", RUT, " ", NOW))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void cambiaElNombreDelTitular() {
        Account account = Account.open("0012345678", RUT, "Franco", NOW);

        account.changeHolderName("Franco Vera Parra");

        assertThat(account.getHolderName()).isEqualTo("Franco Vera Parra");
    }

    @Test
    void cierraUnaCuentaConSaldoCero() {
        Account account = Account.open("0012345678", RUT, "Franco", NOW);

        account.close();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void noSePuedeCerrarUnaCuentaConSaldo() {
        Account account = Account.restore(AccountId.newId(), "0012345678", RUT, "Franco",
                Money.of("5000"), AccountStatus.ACTIVE, NOW);

        assertThatThrownBy(account::close)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("saldo");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void unaCuentaCerradaNoSePuedeModificar() {
        Account account = Account.open("0012345678", RUT, "Franco", NOW);
        account.close();

        assertThatThrownBy(() -> account.changeHolderName("Otro"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("no está activa");
        assertThatThrownBy(account::close).isInstanceOf(DomainException.class);
    }
}
