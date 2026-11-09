package cl.pagoseguro.accounts.domain.port.in;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Movement;

public interface DepositMoneyUseCase {

    Movement deposit(DepositCommand command);

    record DepositCommand(AccountId accountId, Money amount, String description) {
    }
}
