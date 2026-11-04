CREATE TABLE accounts (
    id              UNIQUEIDENTIFIER NOT NULL,
    account_number  VARCHAR(20)      NOT NULL,
    holder_rut      VARCHAR(12)      NOT NULL,
    holder_name     NVARCHAR(120)    NOT NULL,
    balance         DECIMAL(19, 2)   NOT NULL CONSTRAINT df_accounts_balance DEFAULT 0,
    status          VARCHAR(20)      NOT NULL,
    -- DATETIMEOFFSET guarda la zona horaria: es lo que Hibernate usa para java.time.Instant.
    created_at      DATETIMEOFFSET   NOT NULL,
    version         BIGINT           NOT NULL CONSTRAINT df_accounts_version DEFAULT 0,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
    CONSTRAINT ck_accounts_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT ck_accounts_status CHECK (status IN ('ACTIVE', 'CLOSED'))
);

-- Búsquedas frecuentes por titular (por ejemplo, "todas las cuentas de este RUT").
CREATE INDEX ix_accounts_holder_rut ON accounts (holder_rut);
