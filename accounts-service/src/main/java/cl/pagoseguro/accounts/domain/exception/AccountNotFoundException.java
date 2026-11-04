package cl.pagoseguro.accounts.domain.exception;

import cl.pagoseguro.accounts.domain.model.AccountId;

/** La capa web la traduce a HTTP 404. */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(AccountId id) {
        super("No existe la cuenta con id " + id.value());
    }
}
