package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import cl.pagoseguro.accounts.domain.model.AccountId;
import cl.pagoseguro.accounts.domain.model.Money;
import cl.pagoseguro.accounts.domain.model.Movement;
import cl.pagoseguro.accounts.domain.model.MovementType;
import cl.pagoseguro.accounts.domain.model.TransferId;
import cl.pagoseguro.accounts.domain.port.out.MovementRepositoryPort;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Component
class MovementPersistenceAdapter implements MovementRepositoryPort {

    private final SpringDataMovementRepository repository;
    private final EntityManager entityManager;

    MovementPersistenceAdapter(SpringDataMovementRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    /**
     * Usamos persist() y no repository.save(): como el id (UUID) ya viene asignado, save() haría
     * un SELECT previo para ver si existe. Los movimientos siempre son nuevos, así que ese SELECT sobra.
     */
    @Override
    @Transactional
    public void saveAll(List<Movement> movements) {
        movements.forEach(m -> entityManager.persist(toEntity(m)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movement> findLatestByAccount(AccountId accountId, int limit) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId.value(), Limit.of(limit)).stream()
                .map(MovementPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Money sumTransfersOutSince(AccountId accountId, Instant since) {
        return new Money(repository.sumAmountSince(accountId.value(), MovementType.TRANSFER_OUT, since)
                .setScale(2, RoundingMode.UNNECESSARY));
    }

    private static MovementJpaEntity toEntity(Movement m) {
        return new MovementJpaEntity(
                m.id(),
                m.accountId().value(),
                m.type(),
                m.amount().amount(),
                m.balanceAfter().amount(),
                m.transferId() == null ? null : m.transferId().value(),
                m.description(),
                m.createdAt());
    }

    private static Movement toDomain(MovementJpaEntity e) {
        return new Movement(
                e.getId(),
                new AccountId(e.getAccountId()),
                e.getType(),
                new Money(e.getAmount()),
                new Money(e.getBalanceAfter()),
                e.getTransferId() == null ? null : new TransferId(e.getTransferId()),
                e.getDescription(),
                e.getCreatedAt());
    }
}
