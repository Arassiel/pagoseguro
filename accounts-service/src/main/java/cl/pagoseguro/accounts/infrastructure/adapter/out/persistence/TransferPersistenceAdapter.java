package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import cl.pagoseguro.accounts.domain.model.Transfer;
import cl.pagoseguro.accounts.domain.port.out.TransferRepositoryPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class TransferPersistenceAdapter implements TransferRepositoryPort {

    private final EntityManager entityManager;

    TransferPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void save(Transfer transfer) {
        entityManager.persist(new TransferJpaEntity(
                transfer.id().value(),
                transfer.sourceAccountId().value(),
                transfer.targetAccountId().value(),
                transfer.amount().amount(),
                transfer.description(),
                transfer.createdAt()));
    }
}
