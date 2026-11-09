package cl.pagoseguro.accounts.domain.exception;

import cl.pagoseguro.accounts.domain.model.Money;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(String accountNumber, Money balance, Money requested) {
        super("Saldo insuficiente en la cuenta " + accountNumber
                + ". Saldo: " + balance.amount() + ", monto solicitado: " + requested.amount());
    }
}
