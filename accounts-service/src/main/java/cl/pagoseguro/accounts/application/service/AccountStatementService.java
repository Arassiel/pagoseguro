package cl.pagoseguro.accounts.application.service;

import cl.pagoseguro.accounts.domain.exception.AccountNotFoundException;
import cl.pagoseguro.accounts.domain.exception.DomainException;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.MonthlySummary;
import cl.pagoseguro.accounts.domain.model.Movement;
import cl.pagoseguro.accounts.domain.port.in.AccountStatementUseCase;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;
import cl.pagoseguro.accounts.domain.port.out.MonthlySummaryPort;
import cl.pagoseguro.accounts.domain.port.out.MovementRepositoryPort;

import java.time.YearMonth;
import java.util.List;

public class AccountStatementService implements AccountStatementUseCase {

    public static final int MAX_MOVEMENTS = 200;

    private final AccountRepositoryPort accountRepository;
    private final MovementRepositoryPort movementRepository;
    private final MonthlySummaryPort monthlySummaryPort;

    public AccountStatementService(AccountRepositoryPort accountRepository,
                                   MovementRepositoryPort movementRepository,
                                   MonthlySummaryPort monthlySummaryPort) {
        this.accountRepository = accountRepository;
        this.movementRepository = movementRepository;
        this.monthlySummaryPort = monthlySummaryPort;
    }

    @Override
    public List<Movement> latestMovements(AccountId accountId, int limit) {
        if (limit < 1 || limit > MAX_MOVEMENTS) {
            throw new DomainException("El límite debe estar entre 1 y " + MAX_MOVEMENTS);
        }
        ensureExists(accountId);
        return movementRepository.findLatestByAccount(accountId, limit);
    }

    @Override
    public MonthlySummary monthlySummary(AccountId accountId, YearMonth period) {
        ensureExists(accountId);
        return monthlySummaryPort.summarize(accountId, period);
    }

    private void ensureExists(AccountId accountId) {
        if (accountRepository.findById(accountId).isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }
    }
}
