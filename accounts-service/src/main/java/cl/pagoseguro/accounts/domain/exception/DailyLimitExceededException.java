package cl.pagoseguro.accounts.domain.exception;

import cl.pagoseguro.accounts.domain.model.Money;

public class DailyLimitExceededException extends DomainException {

    public DailyLimitExceededException(Money limit, Money alreadyTransferred, Money requested) {
        super("La transferencia supera el límite diario de " + limit.amount()
                + ". Ya transferido hoy: " + alreadyTransferred.amount()
                + ", monto solicitado: " + requested.amount());
    }
}
