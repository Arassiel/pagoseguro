package cl.pagoseguro.accounts.infrastructure.config;

import cl.pagoseguro.accounts.application.service.AccountService;
import cl.pagoseguro.accounts.application.service.AccountStatementService;
import cl.pagoseguro.accounts.application.service.MoneyMovementService;
import cl.pagoseguro.accounts.domain.model.DailyTransferLimit;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.port.out.AccountNumberGeneratorPort;
import cl.pagoseguro.accounts.domain.port.out.AccountRepositoryPort;
import cl.pagoseguro.accounts.domain.port.out.MonthlySummaryPort;
import cl.pagoseguro.accounts.domain.port.out.MovementRepositoryPort;
import cl.pagoseguro.accounts.domain.port.out.TransactionPort;
import cl.pagoseguro.accounts.domain.port.out.TransferRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;

/** Aquí se "enchufan" los adaptadores en los puertos. Es el único lugar donde se arma el hexágono. */
@Configuration
class BeanConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Zona horaria del negocio: define qué es "hoy" para el límite diario. */
    @Bean
    ZoneId businessZone(@Value("${pagoseguro.business-zone}") String zone) {
        return ZoneId.of(zone);
    }

    @Bean
    DailyTransferLimit dailyTransferLimit(@Value("${pagoseguro.transfers.daily-limit}") BigDecimal limit) {
        return new DailyTransferLimit(new Money(limit));
    }

    @Bean
    AccountService accountService(AccountRepositoryPort accountRepository,
                                  AccountNumberGeneratorPort accountNumberGenerator,
                                  Clock clock) {
        return new AccountService(accountRepository, accountNumberGenerator, clock);
    }

    @Bean
    MoneyMovementService moneyMovementService(AccountRepositoryPort accountRepository,
                                              MovementRepositoryPort movementRepository,
                                              TransferRepositoryPort transferRepository,
                                              TransactionPort transactionPort,
                                              DailyTransferLimit dailyTransferLimit,
                                              Clock clock,
                                              ZoneId businessZone) {
        return new MoneyMovementService(accountRepository, movementRepository, transferRepository,
                transactionPort, dailyTransferLimit, clock, businessZone);
    }

    @Bean
    AccountStatementService accountStatementService(AccountRepositoryPort accountRepository,
                                                    MovementRepositoryPort movementRepository,
                                                    MonthlySummaryPort monthlySummaryPort) {
        return new AccountStatementService(accountRepository, movementRepository, monthlySummaryPort);
    }
}
