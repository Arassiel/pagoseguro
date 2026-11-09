package cl.pagoseguro.accounts.domain.port.out;

import cl.pagoseguro.accounts.domain.model.Transfer;

public interface TransferRepositoryPort {

    void save(Transfer transfer);
}
