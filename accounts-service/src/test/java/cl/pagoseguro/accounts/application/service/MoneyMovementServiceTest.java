package cl.pagoseguro.accounts.application.service;

import cl.pagoseguro.accounts.domain.exception.AccountNotFoundException;
import cl.pagoseguro.accounts.domain.exception.DailyLimitExceededException;
import cl.pagoseguro.accounts.domain.exception.DomainException;
import cl.pagoseguro.accounts.domain.exception.InsufficientFundsException;
import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.AccountStatus;
import cl.pagoseguro.accounts.domain.model.DailyTransferLimit;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Movement;
import cl.pagoseguro.accounts.domain.model.MovementType;
import cl.pagoseguro.accounts.domain.model.Rut;
import cl.pagoseguro.accounts.domain.model.Transfer;
import cl.pagoseguro.accounts.domain.port.in.DepositMoneyUseCase.DepositCommand;
import cl.pagoseguro.accounts.domain.port.in.TransferMoneyUseCase.TransferCommand;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;
import cl.pagoseguro.accounts.domain.port.out.MovementRepositoryPort;
import cl.pagoseguro.accounts.domain.port.out.TransactionPort;
import cl.pagoseguro.accounts.domain.port.out.TransferRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoneyMovementServiceTest {

    /** 9 de noviembre de 2026, 02:30 UTC = 8 de noviembre, 23:30 en Santiago (UTC-3 en horario de verano). */
    private static final Instant NOW = Instant.parse("2026-11-09T02:30:00Z");
    private static final ZoneId SANTIAGO = ZoneId.of("America/Santiago");

    @Mock private AccountRepositoryPort accountRepository;
    @Mock private MovementRepositoryPort movementRepository;
    @Mock private TransferRepositoryPort transferRepository;
    @Captor private ArgumentCaptor<List<Movement>> movementsCaptor;

    /** En los tests la "transacción" solo ejecuta el bloque: no hay base de datos. */
    private final TransactionPort noTransaction = new TransactionPort() {
        @Override
        public <T> T inTransaction(Supplier<T> work) {
            return work.get();
        }
    };

    private MoneyMovementService service;
    private Account source;
    private Account target;

    @BeforeEach
    void setUp() {
        service = new MoneyMovementService(accountRepository, movementRepository, transferRepository,
                noTransaction, new DailyTransferLimit(Money.of("2000000")),
                Clock.fixed(NOW, ZoneOffset.UTC), SANTIAGO);
        source = accountWithBalance("0011111111", "50000");
        target = accountWithBalance("0022222222", "0");
    }

    @Test
    void depositoAbonaYRegistraElMovimiento() {
        when(accountRepository.findById(target.getId())).thenReturn(Optional.of(target));

        Movement movement = service.deposit(new DepositCommand(target.getId(), Money.of("15000"), "Sueldo"));

        assertThat(target.getBalance()).isEqualTo(Money.of("15000"));
        assertThat(movement.type()).isEqualTo(MovementType.DEPOSIT);
        assertThat(movement.balanceAfter()).isEqualTo(Money.of("15000"));
        verify(accountRepository).save(target);
        verify(movementRepository).saveAll(List.of(movement));
    }

    @Test
    void transferenciaMueveElDineroYRegistraDosMovimientos() {
        givenAccountsExist();
        when(movementRepository.sumTransfersOutSince(any(), any())).thenReturn(Money.ZERO);

        Transfer transfer = service.transfer(new TransferCommand(source.getId(), target.getId(), Money.of("20000"), "Arriendo"));

        assertThat(source.getBalance()).isEqualTo(Money.of("30000"));
        assertThat(target.getBalance()).isEqualTo(Money.of("20000"));
        verify(transferRepository).save(transfer);
        verify(accountRepository).save(source);
        verify(accountRepository).save(target);

        verify(movementRepository).saveAll(movementsCaptor.capture());
        List<Movement> movements = movementsCaptor.getValue();
        assertThat(movements).extracting(Movement::type)
                .containsExactly(MovementType.TRANSFER_OUT, MovementType.TRANSFER_IN);
        assertThat(movements).extracting(Movement::balanceAfter)
                .containsExactly(Money.of("30000"), Money.of("20000"));
        assertThat(movements).allSatisfy(m -> assertThat(m.transferId()).isEqualTo(transfer.id()));
    }

    @Test
    void elLimiteDiarioUsaElDiaDeChileYNoElDeUtc() {
        givenAccountsExist();
        when(movementRepository.sumTransfersOutSince(any(), any())).thenReturn(Money.ZERO);

        service.transfer(new TransferCommand(source.getId(), target.getId(), Money.of("1000"), null));

        // En UTC ya es 9 de noviembre, pero en Santiago todavía es 8: "hoy" empezó el 8 a las 00:00 (03:00 UTC).
        verify(movementRepository).sumTransfersOutSince(eq(source.getId()), eq(Instant.parse("2026-11-08T03:00:00Z")));
    }

    @Test
    void rechazaSiSuperaElLimiteDiarioSinMoverDinero() {
        givenAccountsExist();
        when(movementRepository.sumTransfersOutSince(any(), any())).thenReturn(Money.of("1990000"));

        assertThatThrownBy(() -> service.transfer(
                new TransferCommand(source.getId(), target.getId(), Money.of("20000"), null)))
                .isInstanceOf(DailyLimitExceededException.class);

        assertThat(source.getBalance()).isEqualTo(Money.of("50000"));
        verifyNoInteractions(transferRepository);
        verify(movementRepository, never()).saveAll(any());
    }

    @Test
    void rechazaSiNoHaySaldoSuficiente() {
        givenAccountsExist();
        when(movementRepository.sumTransfersOutSince(any(), any())).thenReturn(Money.ZERO);

        assertThatThrownBy(() -> service.transfer(
                new TransferCommand(source.getId(), target.getId(), Money.of("50000.01"), null)))
                .isInstanceOf(InsufficientFundsException.class);

        verifyNoInteractions(transferRepository);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void transferirALaMismaCuentaFallaSinConsultarLaBase() {
        assertThatThrownBy(() -> service.transfer(
                new TransferCommand(source.getId(), source.getId(), Money.of("100"), null)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("misma cuenta");

        verifyNoInteractions(accountRepository, movementRepository, transferRepository);
    }

    @Test
    void fallaSiLaCuentaDestinoNoExiste() {
        when(accountRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(accountRepository.findById(target.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(
                new TransferCommand(source.getId(), target.getId(), Money.of("100"), null)))
                .isInstanceOf(AccountNotFoundException.class);
    }

    private void givenAccountsExist() {
        when(accountRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(accountRepository.findById(target.getId())).thenReturn(Optional.of(target));
    }

    private static Account accountWithBalance(String number, String balance) {
        return Account.restore(AccountId.newId(), number, new Rut("12.345.678-5"), "Titular " + number,
                Money.of(balance), AccountStatus.ACTIVE, NOW, 0L);
    }
}
