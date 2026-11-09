-- Resumen mensual de una cuenta, con el mes calculado en hora de Chile.
--
-- Detalle de rendimiento: el filtro usa un RANGO de fechas (created_at >= @Desde AND created_at < @Hasta)
-- y no funciones sobre la columna (YEAR(created_at) = @Anio). Con el rango, SQL Server puede usar el
-- índice cx_movements_account_created (index seek); con YEAR()/MONTH() tendría que leer toda la tabla.
CREATE OR ALTER PROCEDURE dbo.sp_ResumenMovimientosMensual
    @AccountId UNIQUEIDENTIFIER,
    @Anio      INT,
    @Mes       INT
AS
BEGIN
    SET NOCOUNT ON;

    IF @Mes NOT BETWEEN 1 AND 12
        THROW 50001, 'El mes debe estar entre 1 y 12', 1;

    DECLARE @InicioLocal DATETIME2 = DATETIME2FROMPARTS(@Anio, @Mes, 1, 0, 0, 0, 0, 0);
    DECLARE @Desde DATETIMEOFFSET = @InicioLocal AT TIME ZONE 'Pacific SA Standard Time';
    DECLARE @Hasta DATETIMEOFFSET = DATEADD(MONTH, 1, @InicioLocal) AT TIME ZONE 'Pacific SA Standard Time';

    SELECT
        COUNT(*)                                                             AS total_movimientos,
        COALESCE(SUM(CASE WHEN type = 'DEPOSIT'      THEN amount END), 0)    AS total_depositos,
        COALESCE(SUM(CASE WHEN type = 'TRANSFER_IN'  THEN amount END), 0)    AS total_transferencias_recibidas,
        COALESCE(SUM(CASE WHEN type = 'TRANSFER_OUT' THEN amount END), 0)    AS total_transferencias_enviadas
    FROM dbo.movements
    WHERE account_id = @AccountId
      AND created_at >= @Desde
      AND created_at <  @Hasta;
END
