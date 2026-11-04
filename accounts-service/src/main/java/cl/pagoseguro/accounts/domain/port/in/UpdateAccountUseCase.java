package cl.pagoseguro.accounts.domain.port.in;

import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;

public interface UpdateAccountUseCase {

    Account changeHolderName(AccountId id, String newHolderName);
}
