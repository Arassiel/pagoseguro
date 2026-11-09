package cl.pagoseguro.accounts.domain.model;

public enum MovementType {
    /** Abono por depósito. */
    DEPOSIT,
    /** Cargo por transferencia enviada. */
    TRANSFER_OUT,
    /** Abono por transferencia recibida. */
    TRANSFER_IN
}
