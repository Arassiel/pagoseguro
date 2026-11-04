# 💳 PagoSeguro

Mini plataforma bancaria de cuentas y transferencias, construida como monorepo con microservicios.
Proyecto de portafolio para practicar **Java 21 + Spring Boot con arquitectura hexagonal**, **NestJS**, **React + TypeScript**, **SQL Server**, **MongoDB**, **Docker** y **CI/CD**.

> 🚧 En construcción. Estado actual: **semana 1, servicio de cuentas**.

## Servicios

| Servicio | Stack | Estado |
|---|---|---|
| `accounts-service` | Java 21, Spring Boot 4, JPA, Flyway, SQL Server | ✅ CRUD de cuentas |
| `notifications-service` | NestJS, TypeScript, MongoDB | ⏳ Semana 4 |
| `web` | React, TypeScript, Vite | ⏳ Semana 5 |

## Arquitectura hexagonal (accounts-service)

```
                 ┌──────────────────────────────────────────┐
  HTTP  ───────► │ adapter/in/web     AccountController     │
                 │        │ usa puertos de entrada           │
                 │        ▼                                  │
                 │ ┌──────────────────────────────────────┐  │
                 │ │ application   AccountService          │  │
                 │ │ domain        Account, Rut, Money     │  │  ◄── Java puro, sin Spring
                 │ │               (reglas de negocio)     │  │
                 │ └──────────────────────────────────────┘  │
                 │        │ usa puertos de salida            │
                 │        ▼                                  │
                 │ adapter/out/persistence  JPA ───────────► │ ───► SQL Server
                 └──────────────────────────────────────────┘
```

```
src/main/java/cl/pagoseguro/accounts/
├── domain/                  # Núcleo: no depende de nada externo
│   ├── model/               # Account, AccountId, Rut, Money, AccountStatus
│   ├── exception/           # DomainException, AccountNotFoundException
│   └── port/
│       ├── in/              # Casos de uso que la app OFRECE
│       └── out/             # Lo que la app NECESITA (repositorio, generador de números)
├── application/service/     # AccountService: implementa los casos de uso
└── infrastructure/
    ├── adapter/in/web/      # REST: controlador, DTOs, manejo de errores
    ├── adapter/out/         # JPA (SQL Server) y generador de números de cuenta
    └── config/              # BeanConfig: conecta adaptadores con puertos
```

**Reglas de negocio implementadas:**
- El RUT del titular se valida con el algoritmo módulo 11.
- Los montos usan `BigDecimal` con 2 decimales y nunca son negativos.
- Una cuenta nueva parte activa y con saldo cero.
- Solo se puede cerrar una cuenta activa con saldo cero.
- Una cuenta cerrada no se puede modificar.

## Cómo levantarlo

**Requisitos:** Docker Desktop y JDK 21.

```bash
# 1. Configura la contraseña de SQL Server
cp .env.example .env        # y edita MSSQL_SA_PASSWORD

# 2. Levanta SQL Server (crea la base "pagoseguro")
docker compose up -d

# 3. Corre el servicio (Flyway crea las tablas al iniciar)
cd accounts-service
./mvnw spring-boot:run       # en Windows: .\mvnw.cmd spring-boot:run
```

Queda disponible en `http://localhost:8081`.

## API

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/accounts` | Abre una cuenta |
| `GET` | `/api/v1/accounts` | Lista las cuentas |
| `GET` | `/api/v1/accounts/{id}` | Obtiene una cuenta |
| `PATCH` | `/api/v1/accounts/{id}` | Cambia el nombre del titular |
| `POST` | `/api/v1/accounts/{id}/close` | Cierra una cuenta |
| `GET` | `/actuator/health` | Estado del servicio |

```bash
curl -X POST http://localhost:8081/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{"holderRut": "12.345.678-5", "holderName": "Franco Vera"}'
```

Los errores siguen el estándar **RFC 9457 (ProblemDetail)**:
`400` para campos inválidos, `404` si la cuenta no existe y `422` si se incumple una regla de negocio.

## Tests

```bash
cd accounts-service
./mvnw test
```

| Capa | Herramienta | Qué prueba |
|---|---|---|
| Dominio | JUnit 5 + AssertJ | Reglas de `Account`, `Rut` y `Money`, sin Spring |
| Aplicación | Mockito | Orquestación de `AccountService` con puertos mockeados |
| Web | `@WebMvcTest` + MockMvc | Códigos HTTP, validación y formato de errores |

## Roadmap

- [x] Semana 1: CRUD de cuentas con arquitectura hexagonal, SQL Server y Flyway
- [ ] Semana 2: Depósitos y transferencias (saldo, límite diario, atomicidad)
- [ ] Semana 3: Tests de integración con Testcontainers y cobertura con JaCoCo
- [ ] Semana 4: `notifications-service` en NestJS + MongoDB
- [ ] Semana 5-6: Frontend en React + TypeScript con Vitest
- [ ] Semana 7: docker-compose completo y GitHub Actions
- [ ] Semana 8: Migración de un endpoint legado .NET (patrón *strangler fig*)
