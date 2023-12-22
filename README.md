# ledger-core

Equipo dueño: **core-banking** (Mariana Sosa, Juan Ferreyra, Rocío Almada). Si tocás algo acá y no
sabés a quién avisar, es a nosotros.

## Qué es esto

`ledger-core` es el sistema original de Cauri. Nació en 2021 como el monolito que tenía todo:
clientes, cuentas, movimientos, transferencias. Con el tiempo fuimos sacando piezas —
`accounts-api` salió de acá para dar lecturas rápidas sin pegarle a este proceso, `reporting-etl`
se llevó el cierre diario— pero el núcleo de la plata sigue viviendo acá, y va a seguir viviendo
acá. Todo lo demás en Cauri, directa o indirectamente, depende de que esto funcione.

El modelo es de **doble entrada** (double-entry bookkeeping), como cualquier sistema contable en
serio: cada movimiento de plata entre dos cuentas genera dos `ledger_entries` — un `debit` en el
origen y un `credit` en el destino — y ambos balances se actualizan en la misma transacción. No hay
transferencias "a medias": o entran las dos entradas y se mueven los dos balances, o no pasa nada.
Usamos `SELECT ... FOR UPDATE` sobre ambas cuentas (siempre en el mismo orden, por id, para que dos
transferencias concurrentes entre el mismo par de cuentas no se hagan deadlock entre sí) para que
dos transferencias concurrentes sobre la misma cuenta no puedan dejar el balance en un estado
inconsistente.

> **Antes de tocar el esquema de `accounts` o `ledger_entries`: coordinalo en #core-banking.**
> `accounts-api` lee esta misma base directo (join sobre `ledger.accounts` y
> `ledger.ledger_entries`, ver su README) y hace un `UPDATE` directo para el freeze de cuentas. No
> tiene sus propias migraciones sobre esta base — las nuestras son la única fuente de verdad del
> esquema. Si cambiás una columna o un tipo acá y no avisás, `accounts-api` se rompe en producción
> y no vas a tener ni un stack trace lindo para explicarlo.

## Stack

Java 21, Spring Boot 3.3 (Web, Security como resource server de OAuth2/JWT, Data JPA, Actuator +
Micrometer), Flyway para migraciones, PostgreSQL. Build con Gradle (Kotlin DSL), wrapper commiteado.

## Autenticación

Los tokens son JWT de Keycloak (`KEYCLOAK_ISSUER`). El claim `realm_access.roles` se mapea a
authorities `ROLE_<rol>` (ver `config/KeycloakRealmRoleConverter`). Cualquier token válido entra;
`POST /v1/ledger/entries`, `POST /v1/ledger/transfers`, `PATCH /v1/customers/{id}/kyc-status` y
`/v1/legacy-batch/**` piden además el rol `service`. `GET /v1/ledger/entries?account_id=` acepta
cualquier token válido, igual que el resto de las lecturas. `/actuator/health` y
`/actuator/prometheus` son públicos.

## Reglas de negocio no obvias

- `POST /v1/ledger/entries` con `type=debit` puede dejar el balance en negativo **solo** si el
  `reference` empieza con `card-auth:`. Es una decisión histórica: cards-api debita antes de saber
  si la autorización realmente se va a asentar, y en ese momento puntual se permite el descubierto.
  Cualquier otro `debit` que dejaría el balance en negativo vuelve `422 insufficient_funds`.
- `POST /v1/ledger/transfers` exige que `from_account` y `to_account` tengan la misma moneda que el
  `currency` del pedido, que `from_account` esté `active` (si no, `422 account_not_active`) y que
  tenga saldo suficiente (si no, `422 insufficient_funds`).
- `PATCH /v1/customers/{id}/kyc-status` solo acepta los cuatro valores del `CHECK` de la tabla
  (`pending`, `approved`, `rejected`, `manual_review`); cualquier otro valor es
  `422 invalid_kyc_status` (no está en el contrato con ese nombre exacto, lo elegimos nosotros).

## El módulo `legacy/`

`POST /v1/legacy-batch/run` es el batch de cierre diario de antes de que existiera `reporting-etl`.
Está **deprecado desde reporting-etl, pendiente de borrar**. Que quede clarísimo: a la fecha de este
README nadie en la organización lo llama. Lo dejamos andando porque sacarlo implica borrar la tabla
`legacy_batch_runs` y nadie quiere ser quien firme ese PR sin antes confirmar con auditoría que no
hay un reporte trimestral en algún lado que dependa de esos números. Quedó en el backlog.

## Correr local

```bash
export DB_HOST=localhost DB_PORT=5432 DB_NAME=ledger DB_USER=cauri DB_PASSWORD=cauri
export KEYCLOAK_ISSUER=http://localhost:8180/realms/cauri-staging
./gradlew bootRun
```

## Tests

```bash
./gradlew test
```

Usamos **H2 en modo PostgreSQL** (`MODE=PostgreSQL`) en vez de Testcontainers — para un repo de
este tamaño, levantar Docker-en-Docker en el runner de CI no vale la pena, y las migraciones de
Flyway corren igual contra H2 sin traducción rara (ver `src/test/resources/application-test.yml`).
Si algún día una migración usa algo específico de Postgres que H2 no entiende, ahí sí migramos a
Testcontainers. El `JwtDecoder` se mockea en los tests de controller (`@MockBean`) para no depender
de que Keycloak esté levantado.

## Build de imagen

```bash
docker build -t ledger-core .
```

Multi-stage: build con Gradle sobre `eclipse-temurin:21-jdk`, runtime sobre
`eclipse-temurin:21-jre` con usuario no root. `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70` para que
la JVM respete los límites de memoria del pod en vez de mirar la memoria del nodo.

## Deploy

Estilo A: Actions + YAML pelado (`k8s/staging/`, `k8s/prod/`). `deploy.yml` construye y pushea a
`ghcr.io/demo-org-np-migration/ledger-core`, aplica en `staging` contra el runner self-hosted
(`cauri-k8s`) y, **solo en tags `v*`**, aplica en `prod` — ese job tiene `environment: production`,
así que necesita approval manual de un tech lead en GitHub antes de correr. No hay atajos para eso,
y no los va a haber.

## Runbook rápido

- **Balance no cierra / transferencia se ve "a medias":** no debería pasar nunca por el
  `@Transactional` + `SELECT FOR UPDATE` de `LedgerService#transfer`, pero si pasa, lo primero es
  mirar `ledger_entries` por `reference` y confirmar si están las dos entradas (`debit` y `credit`)
  o solo una. Si solo hay una, es un bug de verdad y hay que escalarlo, no parchear el balance a
  mano.
- **KYC no cambia:** confirmá que quien llama a `PATCH /v1/customers/{id}/kyc-status` tiene un
  token con rol `service` (401/403 si no). El valor tiene que ser uno de los cuatro del `CHECK`.
- **accounts-api devuelve datos raros después de un deploy nuestro:** revisá si la migración que
  acabás de aplicar tocó `accounts` o `ledger_entries` sin coordinarlo en #core-banking (ver arriba).
- **No reinicies el pod para "arreglar" un balance mal.** Este servicio no cachea nada de negocio en
  memoria; si el balance está mal, está mal en la base, y reiniciar no hace más que ocultar el
  síntoma un rato.

## TODO conocido

- Probes de Kubernetes: ver el comentario en `k8s/{staging,prod}/deployment.yaml`
  (`TODO(msosa): probes, ver incidente 2025-11`). Quedó pendiente después del incidente de noviembre
  y no tuvo prioridad desde entonces.
- Sacar `legacy/` de una vez.
