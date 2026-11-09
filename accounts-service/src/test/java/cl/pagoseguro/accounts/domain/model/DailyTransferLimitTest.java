package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DailyLimitExceededException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DailyTransferLimitTest {

    private final DailyTransferLimit limit = new DailyTransferLimit(Money.of("2000000"));

    @Test
    void permiteTransferirHastaElLimiteExacto() {
        assertThatCode(() -> limit.check(Money.of("1500000"), Money.of("500000"))).doesNotThrowAnyException();
    }

    @Test
    void rechazaSiSuperaElLimiteAunqueSeaPorUnCentavo() {
        assertThatThrownBy(() -> limit.check(Money.of("1500000"), Money.of("500000.01")))
                .isInstanceOf(DailyLimitExceededException.class)
                .hasMessageContaining("límite diario");
    }

    @Test
    void rechazaUnaSolaTransferenciaMayorAlLimite() {
        assertThatThrownBy(() -> limit.check(Money.ZERO, Money.of("2000001")))
                .isInstanceOf(DailyLimitExceededException.class);
    }
}
