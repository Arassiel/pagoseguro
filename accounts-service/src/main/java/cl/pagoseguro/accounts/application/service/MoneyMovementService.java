package cl.pagoseguro.accounts.application.service;

import cl.pagoseguro.accounts.domain.exception.AccountNotFoundException;
import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.DailyTransferLimit;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Movement;
import cl.pagoseguro.accounts.domain.model.Transfer;
import cl.pagoseguro.accounts.domain.port.in.DepositMoneyUseCase;
import cl.pagoseguro.accounts.domain.port.in.TransferMoneyUseCase;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;
import cl.pagoseguro.accounts.domain.port.out.MovementRepositoryPort;
import cl.pagoseguro.accounts.domain.port.out.TransactionPort;
import cl.pagoseguro.accounts.domain.port.out.TransferRepositoryPort;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Casos de uso que mueven dinero: depósitos y transferencias.
 *
 * Todo ocurre dentro de transactionPort.inTransaction(...): si algo falla a mitad de camino
 * (por ejemplo, después de descontar al origen pero antes de abonar al destino), la base
 * de datos deshace TODO. Eso es la atomicidad: la "A" de ACID.
 */
public class MoneyMovementService implements DepositMoneyUseCase, TransferMoneyUseCase {

    private final AccountRepositoryPort accountRepository;
    private final MovementRepositoryPort movementRepository;
    private final TransferRepositoryPort transferRepository;
    private final TransactionPort transactionPort;
    private final DailyTransferLimit dailyLimit;
    private final Clock clock;
    private final ZoneId businessZone;

    public MoneyMovementService(AccountRepositoryPort accountRepository,
                                MovementRepositoryPort movementRepository,
                                TransferRepositoryPort transferRepository,
                                TransactionPort transactionPort,
                                DailyTransferLimit dailyLimit,
                                Clock clock,
                                ZoneId businessZone) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.transferRepository = transferRepository;
        this.transactionPort = transactionPort;
        this.dailyLimit = dailyLimit;
        this.clock = clock;
        this.businessZone = businessZone;
    }

    @Override
    public Movement deposit(DepositCommand command) {
        return transactionPort.inTransaction(() -> {
            Account account = load(command.accountId());
            account.deposit(command.amount());

            Movement movement = Movement.deposit(account, command.amount(), command.description(), Instant.now(clock));
            accountRepository.save(account);
            movementRepository.saveAll(List.of(movement));
            return movement;
        });
    }

    @Override
    public Transfer transfer(TransferCommand command) {
        // Validaciones baratas primero: si es a la misma cuenta o monto cero, falla sin tocar la base.
        Transfer transfer = Transfer.create(command.sourceAccountId(), command.targetAccountId(),
                command.amount(), command.description(), Instant.now(clock));

        return transactionPort.inTransaction(() -> {
            Account source = load(transfer.sourceAccountId());
            Account target = load(transfer.targetAccountId());

            Money transferredToday = movementRepository.sumTransfersOutSince(source.getId(), startOfToday());
            dailyLimit.check(transferredToday, transfer.amount());

            source.withdraw(transfer.amount());   // puede lanzar InsufficientFundsException
            target.deposit(transfer.amount());    // puede lanzar si la cuenta destino está cerrada

            transferRepository.save(transfer);
            accountRepository.save(source);
            accountRepository.save(target);
            movementRepository.saveAll(List.of(
                    Movement.transferOut(source, transfer),
                    Movement.transferIn(target, transfer)));
            return transfer;
        });
    }

    /** "Hoy" según la hora de Chile, no la de UTC: a las 22:00 en Santiago ya es mañana en UTC. */
    private Instant startOfToday() {
        return LocalDate.now(clock.withZone(businessZone)).atStartOfDay(businessZone).toInstant();
    }

    private Account load(AccountId id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
