package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.AccountStatus;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Rut;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;
import cl.pagoseguro.accounts.infrastructure.adapter.out.persistence.AccountJpaEntity.AccountStatusColumn;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto del dominio usando Spring Data JPA + SQL Server.
 * Traduce entre Account (dominio) y AccountJpaEntity (tabla).
 */
@Component
class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository repository;

    AccountPersistenceAdapter(SpringDataAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Account save(Account account) {
        AccountJpaEntity entity = repository.findById(account.getId().value())
                .map(existing -> {
                    ensureSameVersion(existing, account);
                    existing.updateFrom(account.getHolderName(), account.getBalance().amount(),
                            toColumn(account.getStatus()));
                    return existing;
                })
                .orElseGet(() -> toEntity(account));
        return toDomain(repository.save(entity));
    }

    /**
     * Bloqueo optimista, parte 1: si la versión que leyó el dominio ya no es la de la base,
     * alguien modificó la cuenta entretanto y rechazamos el cambio.
     * Parte 2 la hace Hibernate al confirmar: UPDATE ... WHERE id = ? AND version = ?
     * Si ese UPDATE no afecta ninguna fila, también lanza ObjectOptimisticLockingFailureException.
     */
    private static void ensureSameVersion(AccountJpaEntity existing, Account account) {
        if (!Objects.equals(account.getVersion(), existing.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(AccountJpaEntity.class, existing.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findById(AccountId id) {
        return repository.findById(id.value()).map(AccountPersistenceAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return repository.findAll(Sort.by("createdAt")).stream()
                .map(AccountPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAccountNumber(String accountNumber) {
        return repository.existsByAccountNumber(accountNumber);
    }

    private static AccountJpaEntity toEntity(Account account) {
        return new AccountJpaEntity(
                account.getId().value(),
                account.getAccountNumber(),
                account.getHolderRut().value(),
                account.getHolderName(),
                account.getBalance().amount(),
                toColumn(account.getStatus()),
                account.getCreatedAt());
    }

    private static Account toDomain(AccountJpaEntity entity) {
        return Account.restore(
                new AccountId(entity.getId()),
                entity.getAccountNumber(),
                new Rut(entity.getHolderRut()),
                entity.getHolderName(),
                new Money(entity.getBalance()),
                AccountStatus.valueOf(entity.getStatus().name()),
                entity.getCreatedAt(),
                entity.getVersion());
    }

    private static AccountStatusColumn toColumn(AccountStatus status) {
        return AccountStatusColumn.valueOf(status.name());
    }
}
