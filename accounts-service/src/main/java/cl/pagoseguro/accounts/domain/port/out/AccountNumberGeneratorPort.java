package cl.pagoseguro.accounts.domain.port.out;

/** Genera números de cuenta. Al ser un puerto, en los tests podemos controlar qué número sale. */
public interface AccountNumberGeneratorPort {

    String next();
}
