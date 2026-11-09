package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import cl.pagoseguro.accounts.domain.model.MovementType;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface SpringDataMovementRepository extends JpaRepository<MovementJpaEntity, UUID> {

    /** Spring Data genera el SQL a partir del nombre del método. Limit se traduce a TOP / FETCH FIRST. */
    List<MovementJpaEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Limit limit);

    @Query("""
            select coalesce(sum(m.amount), 0)
            from MovementJpaEntity m
            where m.accountId = :accountId
              and m.type = :type
              and m.createdAt >= :since
            """)
    BigDecimal sumAmountSince(@Param("accountId") UUID accountId,
                              @Param("type") MovementType type,
                              @Param("since") Instant since);
}
