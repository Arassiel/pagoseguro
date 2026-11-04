package cl.pagoseguro.accounts.infrastructure.adapter.out.numbering;

import cl.pagoseguro.accounts.domain.port.out.AccountNumberGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Genera números de cuenta de 10 dígitos con el prefijo "00". */
@Component
class RandomAccountNumberGenerator implements AccountNumberGeneratorPort {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String next() {
        return "00" + String.format("%08d", random.nextInt(100_000_000));
    }
}
