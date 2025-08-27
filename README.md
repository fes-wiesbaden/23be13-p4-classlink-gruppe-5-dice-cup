# 🎓 Classlink – Die digitale Schulplattform

![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/fes-wiesbaden/23be13-p4-classlink-gruppe-5-dice-cup/ci.yml?branch=master&logo=github&style=flat-square)
![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fes-wiesbaden_23be13-p4-classlink-gruppe-5-dice-cup&metric=alert_status)
![GitHub last commit](https://img.shields.io/github/last-commit/fes-wiesbaden/23be13-p4-classlink-gruppe-5-dice-cup?style=flat-square&logo=git)
![License](https://img.shields.io/badge/Lizenz-Proprietär-blue?style=flat-square)

---

## ✨ Überblick
**Classlink** ist eine moderne, modulare Schulplattform, entwickelt von **DiceCup**.  
Sie unterstützt Lehrkräfte und Schulen dabei, Verwaltungs- und Unterrichtsprozesse digital, sicher und effizient abzubilden.

Unsere Lösung basiert auf **aktueller Enterprise-Technologie** (Spring Boot + Angular + PostgreSQL) und ist so ausgelegt, dass sie langfristig wartbar und erweiterbar bleibt.

---

## 💡 Vorteile für Schulen und Lehrkräfte
- 🎯 **Einfach**: Intuitive Bedienung für Lehrkräfte und Schüler
- 🔐 **Sicher**: Rollenbasiertes Zugriffsmodell, verschlüsselte Kommunikation
- ⚙️ **Flexibel**: Module für Verwaltung, Unterricht und Kommunikation
- 🚀 **Zukunftssicher**: Moderne Architektur, regelmäßige Updates
- 🛠️ **Individuell**: Anpassbar an Anforderungen einzelner Schulen oder Träger

---

## 📊 Qualität & Transparenz
Wir legen höchsten Wert auf Qualität:
- Kontinuierliche **Build- und Test-Pipelines**
- **Code-Qualität** geprüft über SonarCloud
- **Automatisierte Sicherheitsprüfungen**
- Stabile Releases, die für den produktiven Schulbetrieb geeignet sind

![Repobeats analytics image](https://repobeats.axiom.co/api/embed/34f6a6fdfc1c7a23a64b44f3c896e1962349d270.svg)

---

## 🚀 Installation & Inbetriebnahme
Die Software wird als **Docker-basierte Lösung** ausgeliefert und kann sowohl **On-Premise** in der Schule als auch auf **dedizierten Servern** betrieben werden.

### Voraussetzungen
- Aktuelle Docker & Docker Compose Installation
- Server mit mind. 4 GB RAM
- Datenbank: PostgreSQL (wird automatisch per Compose mitgeliefert)

### Erste Schritte
```bash
git clone https://github.com/fes-wiesbaden/23be13-p4-classlink-gruppe-5-dice-cup.git
cd classlink
docker compose up -d
```

Danach sind erreichbar:
- Backend (Spring Boot): http://localhost:4000
- Frontend (Angular): http://localhost:4200
- API-Dokumentation (Scalar UI): http://localhost:4200/scalar

---

## 🗂️ Funktionsübersicht
- 👩‍🏫 Benutzer- und Rollenverwaltung (Lehrer, Schüler, Administratoren)
- 📝 Kurs- und Klassenorganisation
- 📢 Kommunikationsmodul für Nachrichten & Ankündigungen
- 📊 Übersichten und Berichte
- 🔧 Erweiterbar um weitere Module

---

## 📅 Roadmap
- [x] Basisplattform (Monorepo, DB, API-Doku)
- [] Authentifizierung & Autorisierung (JWT-basiert)
- [ ] Erste produktive Pilotschule
- [ ] Module für Stundenpläne & Leistungsbewertung
- [ ] Mobile App (iOS/Android)  
