package cl.pagoseguro.accounts.application.service;

import cl.pagoseguro.accounts.domain.exception.AccountNotFoundException;
import cl.pagoseguro.accounts.domain.exception.DomainException;
import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Rut;
import cl.pagoseguro.accounts.domain.port.in.CloseAccountUseCase;
import cl.pagoseguro.accounts.domain.port.in.GetAccountUseCase;
import cl.pagoseguro.accounts.domain.port.in.OpenAccountUseCase;
import cl.pagoseguro.accounts.domain.port.in.UpdateAccountUseCase;
import cl.pagoseguro.accounts.domain.port.out.AccountNumberGeneratorPort;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Implementa los casos de uso orquestando el dominio y los puertos de salida.
 *
 * No tiene @Service: se registra como bean en BeanConfig. Así esta capa no depende de Spring
 * y se puede testear con un simple "new AccountService(...)" y mocks de Mockito.
 */
public class AccountService implements OpenAccountUseCase, GetAccountUseCase, UpdateAccountUseCase, CloseAccountUseCase {

    private static final int MAX_NUMBER_ATTEMPTS = 5;

    private final AccountRepositoryPort accountRepository;
    private final AccountNumberGeneratorPort accountNumberGenerator;
    private final Clock clock;

    public AccountService(AccountRepositoryPort accountRepository,
                          AccountNumberGeneratorPort accountNumberGenerator,
                          Clock clock) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.clock = clock;
    }

    @Override
    public Account open(OpenAccountCommand command) {
        Rut rut = new Rut(command.holderRut());
        Account account = Account.open(uniqueAccountNumber(), rut, command.holderName(), Instant.now(clock));
        return accountRepository.save(account);
    }

    @Override
    public Account getById(AccountId id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public List<Account> listAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account changeHolderName(AccountId id, String newHolderName) {
        Account account = getById(id);
        account.changeHolderName(newHolderName);
        return accountRepository.save(account);
    }

    @Override
    public Account close(AccountId id) {
        Account account = getById(id);
        account.close();
        return accountRepository.save(account);
    }

    private String uniqueAccountNumber() {
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            String candidate = accountNumberGenerator.next();
            if (!accountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new DomainException("No fue posible generar un número de cuenta único");
    }
}
