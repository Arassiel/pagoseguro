package cl.pagoseguro.accounts.domain.model;

import cl.pagoseguro.accounts.domain.exception.DomainException;
import cl.pagoseguro.accounts.domain.exception.InsufficientFundsException;

import java.time.Instant;
import java.util.Objects;

/**
 * Cuenta bancaria: la entidad central del dominio.
 *
 * Fíjate en que esta clase NO tiene anotaciones de Spring ni de JPA. Es Java puro.
 * Las reglas de negocio viven aquí, no en el controlador ni en el repositorio.
 */
public class Account {

    private static final int MAX_HOLDER_NAME_LENGTH = 120;

    private final AccountId id;
    private final String accountNumber;
    private final Rut holderRut;
    private String holderName;
    private Money balance;
    private AccountStatus status;
    private final Instant createdAt;

    /**
     * Versión para bloqueo optimista: la cuenta "recuerda" qué versión leyó de la base.
     * Si al guardar la versión en la base ya cambió, otra operación modificó la cuenta
     * entretanto y la nuestra se rechaza. Es null mientras la cuenta no se ha guardado nunca.
     */
    private final Long version;

    private Account(AccountId id, String accountNumber, Rut holderRut, String holderName,
                    Money balance, AccountStatus status, Instant createdAt, Long version) {
        this.id = Objects.requireNonNull(id);
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.holderRut = Objects.requireNonNull(holderRut);
        this.holderName = validateHolderName(holderName);
        this.balance = Objects.requireNonNull(balance);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    /** Abre una cuenta nueva: activa y con saldo cero. */
    public static Account open(String accountNumber, Rut holderRut, String holderName, Instant now) {
        return new Account(AccountId.newId(), accountNumber, holderRut, holderName,
                Money.ZERO, AccountStatus.ACTIVE, now, null);
    }

    /** Reconstruye una cuenta que ya existía (por ejemplo, al leerla de la base de datos). */
    public static Account restore(AccountId id, String accountNumber, Rut holderRut, String holderName,
                                  Money balance, AccountStatus status, Instant createdAt, Long version) {
        return new Account(id, accountNumber, holderRut, holderName, balance, status, createdAt, version);
    }

    /** Abona dinero a la cuenta. */
    public void deposit(Money amount) {
        ensureActive();
        ensurePositive(amount);
        this.balance = balance.plus(amount);
    }

    /** Descuenta dinero de la cuenta. Regla de negocio: nunca puede quedar saldo negativo. */
    public void withdraw(Money amount) {
        ensureActive();
        ensurePositive(amount);
        if (amount.isGreaterThan(balance)) {
            throw new InsufficientFundsException(accountNumber, balance, amount);
        }
        this.balance = balance.minus(amount);
    }

    public void changeHolderName(String newName) {
        ensureActive();
        this.holderName = validateHolderName(newName);
    }

    /** Regla de negocio: solo se puede cerrar una cuenta activa y con saldo cero. */
    public void close() {
        ensureActive();
        if (!balance.isZero()) {
            throw new DomainException("No se puede cerrar una cuenta con saldo. Saldo actual: " + balance.amount());
        }
        this.status = AccountStatus.CLOSED;
    }

    private void ensureActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new DomainException("La cuenta " + accountNumber + " no está activa");
        }
    }

    private static void ensurePositive(Money amount) {
        Objects.requireNonNull(amount, "El monto es obligatorio");
        if (amount.isZero()) {
            throw new DomainException("El monto debe ser mayor a cero");
        }
    }

    private static String validateHolderName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("El nombre del titular es obligatorio");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_HOLDER_NAME_LENGTH) {
            throw new DomainException("El nombre del titular no puede superar " + MAX_HOLDER_NAME_LENGTH + " caracteres");
        }
        return trimmed;
    }

    public AccountId getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public Rut getHolderRut() { return holderRut; }
    public String getHolderName() { return holderName; }
    public Money getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getVersion() { return version; }
}
