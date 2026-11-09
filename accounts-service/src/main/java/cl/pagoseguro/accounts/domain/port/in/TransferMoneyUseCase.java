package cl.pagoseguro.accounts.domain.port.in;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Transfer;

public interface TransferMoneyUseCase {

    Transfer transfer(TransferCommand command);

    record TransferCommand(AccountId sourceAccountId, AccountId targetAccountId, Money amount, String description) {
    }
}
