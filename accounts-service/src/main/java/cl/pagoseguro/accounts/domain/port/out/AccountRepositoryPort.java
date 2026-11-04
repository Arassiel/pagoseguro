package cl.pagoseguro.accounts.domain.port.out;

import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: lo que la aplicación NECESITA del mundo exterior.
 * El dominio no sabe si detrás hay SQL Server, MongoDB o una lista en memoria (útil en tests).
 */
public interface AccountRepositoryPort {

    Account save(Account account);

    Optional<Account> findById(AccountId id);

    List<Account> findAll();

    boolean existsByAccountNumber(String accountNumber);
}
