# DiceCup

Vollständiger Stack mit Angular-Frontend und Spring-Boot-Backend. Nutzt PostgreSQL (über Docker) für die Entwicklung.

## Voraussetzungen

- Git
- Docker und Docker Compose (empfohlen für lokale DB)
- PostgreSQL (latest), falls kein Docker genutzt wird
- Java JDK 21/23/24
- Node.js (LTS) + Yarn
- Gradle Wrapper (enthalten)

## Projektstruktur

- `classlink-frontend/` – Angular-Client
- `src/main/java/...` – Spring-Boot-Backend
- `docker-compose.yml` – lokale PostgreSQL

## Schnellstart

### Datenbank (Docker)
```bash
docker compose up -d
```

### Backend

```bash
# Unix/macOS
./gradlew bootRun

# Windows (PowerShell)
.\gradlew.bat bootRun
```

### Frontend

```bash
cd classlink-frontend
yarn install
yarn start
```

## Tests ausführen

### Backend-Tests

```bash
# Unix/macOS
./gradlew test

# Windows (PowerShell)
.\gradlew.bat test
```

### Frontend-Tests

```bash
cd classlink-frontend
yarn test
```

## Typischer Entwicklungsablauf

1. Datenbank starten: `docker compose up -d`
2. Backend starten: `./gradlew bootRun` (oder `.\gradlew.bat bootRun`)
3. Frontend starten: `yarn start` (aus `classlink-frontend`)
4. Tests nach Bedarf: `./gradlew test` und `yarn test`

## Hinweise

- Frontend kommuniziert ausschließlich per REST mit dem Backend.
- Rollenbasierte Views (Admin/Lehrer/Schüler) sind vorhanden und erweiterbar.
- Docker wird für eine konsistente Entwicklungsumgebung empfohlen.
