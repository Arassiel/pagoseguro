package cl.pagoseguro.accounts.domain.port.in;

import cl.pagoseguro.accounts.domain.model.Account;

/** Puerto de entrada: lo que la aplicación OFRECE. El controlador REST depende de esta interfaz. */
public interface OpenAccountUseCase {

    Account open(OpenAccountCommand command);

    record OpenAccountCommand(String holderRut, String holderName) {
    }
}
