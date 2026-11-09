CREATE TABLE transfers (
    id                  UNIQUEIDENTIFIER NOT NULL,
    source_account_id   UNIQUEIDENTIFIER NOT NULL,
    target_account_id   UNIQUEIDENTIFIER NOT NULL,
    amount              DECIMAL(19, 2)   NOT NULL,
    description         NVARCHAR(140)    NULL,
    created_at          DATETIMEOFFSET   NOT NULL,

    CONSTRAINT pk_transfers PRIMARY KEY (id),
    CONSTRAINT fk_transfers_source FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transfers_target FOREIGN KEY (target_account_id) REFERENCES accounts (id),
    CONSTRAINT ck_transfers_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_transfers_different_accounts CHECK (source_account_id <> target_account_id)
);

-- Cartola: un registro inmutable por cada cambio de saldo.
CREATE TABLE movements (
    id              UNIQUEIDENTIFIER NOT NULL,
    account_id      UNIQUEIDENTIFIER NOT NULL,
    type            VARCHAR(20)      NOT NULL,
    amount          DECIMAL(19, 2)   NOT NULL,
    balance_after   DECIMAL(19, 2)   NOT NULL,
    transfer_id     UNIQUEIDENTIFIER NULL,
    description     NVARCHAR(140)    NULL,
    created_at      DATETIMEOFFSET   NOT NULL,

    CONSTRAINT pk_movements PRIMARY KEY NONCLUSTERED (id),
    CONSTRAINT fk_movements_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_movements_transfer FOREIGN KEY (transfer_id) REFERENCES transfers (id),
    CONSTRAINT ck_movements_type CHECK (type IN ('DEPOSIT', 'TRANSFER_OUT', 'TRANSFER_IN')),
    CONSTRAINT ck_movements_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_movements_balance_non_negative CHECK (balance_after >= 0)
);

-- Índice agrupado por (cuenta, fecha): las filas de una cuenta quedan físicamente juntas y ordenadas.
-- Sirve a las 3 consultas principales: cartola, límite diario y resumen mensual.
-- (Agrupar por un UUID aleatorio, en cambio, fragmentaría la tabla en cada inserción.)
CREATE CLUSTERED INDEX cx_movements_account_created ON movements (account_id, created_at);

CREATE INDEX ix_transfers_source_created ON transfers (source_account_id, created_at);
