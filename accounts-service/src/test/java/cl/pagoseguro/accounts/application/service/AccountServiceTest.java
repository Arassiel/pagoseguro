package cl.pagoseguro.accounts.application.service;

import cl.pagoseguro.accounts.domain.exception.AccountNotFoundException;
import cl.pagoseguro.accounts.domain.exception.DomainException;
import cl.pagoseguro.accounts.domain.exception.InvalidRutException;
import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.AccountStatus;
import cl.pagoseguro.accounts.domain.model.Rut;
import cl.pagoseguro.accounts.domain.port.in.OpenAccountUseCase.OpenAccountCommand;
import cl.pagoseguro.accounts.domain.port.out.AccountNumberGeneratorPort;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test del caso de uso con Mockito: los puertos de salida se reemplazan por mocks.
 * Así probamos la orquestación sin levantar Spring ni la base de datos.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

    @Mock
    private AccountRepositoryPort repository;

    @Mock
    private AccountNumberGeneratorPort numberGenerator;

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(repository, numberGenerator, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void abreUnaCuentaConNumeroGeneradoYLaGuarda() {
        when(numberGenerator.next()).thenReturn("0011111111");
        when(repository.existsByAccountNumber("0011111111")).thenReturn(false);
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account account = service.open(new OpenAccountCommand("12.345.678-5", "Franco Vera"));

        assertThat(account.getAccountNumber()).isEqualTo("0011111111");
        assertThat(account.getHolderRut().value()).isEqualTo("12345678-5");
        assertThat(account.getCreatedAt()).isEqualTo(NOW);
        verify(repository).save(account);
    }

    @Test
    void reintentaSiElNumeroGeneradoYaExiste() {
        when(numberGenerator.next()).thenReturn("0011111111", "0022222222");
        when(repository.existsByAccountNumber("0011111111")).thenReturn(true);
        when(repository.existsByAccountNumber("0022222222")).thenReturn(false);
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account account = service.open(new OpenAccountCommand("12.345.678-5", "Franco Vera"));

        assertThat(account.getAccountNumber()).isEqualTo("0022222222");
    }

    @Test
    void fallaSiNoLograGenerarUnNumeroUnico() {
        when(numberGenerator.next()).thenReturn("0011111111");
        when(repository.existsByAccountNumber("0011111111")).thenReturn(true);

        assertThatThrownBy(() -> service.open(new OpenAccountCommand("12.345.678-5", "Franco Vera")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("número de cuenta único");
        verify(repository, never()).save(any());
    }

    @Test
    void noGuardaNadaSiElRutEsInvalido() {
        assertThatThrownBy(() -> service.open(new OpenAccountCommand("12.345.678-9", "Franco Vera")))
                .isInstanceOf(InvalidRutException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void lanzaNotFoundSiLaCuentaNoExiste() {
        AccountId id = AccountId.newId();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void cierraUnaCuentaExistente() {
        Account existing = Account.open("0011111111", new Rut("12.345.678-5"), "Franco", NOW);
        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Account closed = service.close(existing.getId());

        assertThat(closed.getStatus()).isEqualTo(AccountStatus.CLOSED);
        verify(repository).save(existing);
    }
}
