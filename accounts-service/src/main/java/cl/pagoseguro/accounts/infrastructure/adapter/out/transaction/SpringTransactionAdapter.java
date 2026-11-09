package cl.pagoseguro.accounts.infrastructure.adapter.out.transaction;

import cl.pagoseguro.accounts.domain.port.out.TransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Implementa el puerto de transacciones con Spring. Es lo mismo que hace @Transactional,
 * pero de forma explícita: abre la transacción, ejecuta el bloque y hace commit,
 * o rollback si el bloque lanza una excepción.
 */
@Component
class SpringTransactionAdapter implements TransactionPort {

    private final TransactionTemplate transactionTemplate;

    SpringTransactionAdapter(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
