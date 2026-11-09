package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import cl.pagoseguro.accounts.domain.model.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movements")
class MovementJpaEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MovementType type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(name = "description", length = 140)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MovementJpaEntity() {
        // requerido por JPA
    }

    MovementJpaEntity(UUID id, UUID accountId, MovementType type, BigDecimal amount, BigDecimal balanceAfter,
                      UUID transferId, String description, Instant createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.transferId = transferId;
        this.description = description;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    UUID getAccountId() { return accountId; }
    MovementType getType() { return type; }
    BigDecimal getAmount() { return amount; }
    BigDecimal getBalanceAfter() { return balanceAfter; }
    UUID getTransferId() { return transferId; }
    String getDescription() { return description; }
    Instant getCreatedAt() { return createdAt; }
}
