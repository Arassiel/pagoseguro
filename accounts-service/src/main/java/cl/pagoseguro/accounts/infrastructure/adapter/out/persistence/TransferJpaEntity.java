package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
class TransferJpaEntity {

    @Id
    private UUID id;

    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @Column(name = "target_account_id", nullable = false)
    private UUID targetAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 140)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransferJpaEntity() {
        // requerido por JPA
    }

    TransferJpaEntity(UUID id, UUID sourceAccountId, UUID targetAccountId, BigDecimal amount,
                      String description, Instant createdAt) {
        this.id = id;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }
}
