package cl.pagoseguro.accounts.domain.port.in;

import cl.pagoseguro.accounts.domain.model.Account;
import cl.pagoseguro.accounts.domain.model.AccountId;

import java.util.List;

public interface GetAccountUseCase {

    Account getById(AccountId id);

    List<Account> listAll();
}
