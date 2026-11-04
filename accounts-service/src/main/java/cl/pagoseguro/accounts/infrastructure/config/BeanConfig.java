package cl.pagoseguro.accounts.infrastructure.config;

import cl.pagoseguro.accounts.application.service.AccountService;
import cl.pagoseguro.accounts.domain.port.out.AccountNumberGeneratorPort;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Aquí se "enchufan" los adaptadores en los puertos. Es el único lugar donde se arma el hexágono. */
@Configuration
class BeanConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    AccountService accountService(AccountRepositoryPort accountRepository,
                                  AccountNumberGeneratorPort accountNumberGenerator,
                                  Clock clock) {
        return new AccountService(accountRepository, accountNumberGenerator, clock);
    }
}
