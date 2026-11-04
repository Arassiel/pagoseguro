package cl.pagoseguro.accounts.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA: el "molde" de la tabla. Es un detalle de infraestructura, distinto del Account del dominio.
 * Así, si mañana cambia la tabla, el dominio no se entera.
 */
@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "holder_rut", nullable = false, length = 12)
    private String holderRut;

    @Column(name = "holder_name", nullable = false, length = 120)
    private String holderName;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatusColumn status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Bloqueo optimista: evita que dos operaciones simultáneas se pisen (clave en la semana 2). */
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected AccountJpaEntity() {
        // requerido por JPA
    }

    AccountJpaEntity(UUID id, String accountNumber, String holderRut, String holderName,
                     BigDecimal balance, AccountStatusColumn status, Instant createdAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.holderRut = holderRut;
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
    }

    void updateFrom(String holderName, BigDecimal balance, AccountStatusColumn status) {
        this.holderName = holderName;
        this.balance = balance;
        this.status = status;
    }

    UUID getId() { return id; }
    String getAccountNumber() { return accountNumber; }
    String getHolderRut() { return holderRut; }
    String getHolderName() { return holderName; }
    BigDecimal getBalance() { return balance; }
    AccountStatusColumn getStatus() { return status; }
    Instant getCreatedAt() { return createdAt; }

    enum AccountStatusColumn { ACTIVE, CLOSED }
}
