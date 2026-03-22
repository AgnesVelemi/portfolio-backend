# Full-Stack Portfolio Backend - Spring Boot & WebSocket

Willkommen beim Backend-Projekt meines professionellen Portfolios. Diese Anwendung dient als Kernlogik und Datenlieferant für mein Full-Stack-Portfolio-System, entwickelt mit Fokus auf Echtzeitkommunikation via Raw-Data-WebSocket und <span title="Simple Text Oriented Messaging Protocol">STOMP</span> über WebSocket.

## 🔗 Portfolio-Ökosystem

Dieses Projekt ist Teil einer größeren Full-Stack-Anwendung. Sie können das ergänzende Frontend-Projekt hier erkunden:

- **Frontend-Repository**: [Portfolio Frontend (Angular)](https://github.com/AgnesVelemi/portfolio-frontend.git)
- **Backend-Repository**: [Portfolio Backend (Spring Boot)](https://github.com/AgnesVelemi/portfolio-backend.git)

Das Backend ist derzeit so konfiguriert, dass es mit dem Angular-Frontend kommuniziert, das auf `http://localhost:4200` läuft.

## 🚀 Hauptmerkmale

- **Echtzeit-Kommunikation**: Duale WebSocket-Architektur, die sowohl Raw-Data-WebSockets als auch STOMP über WebSockets für eine vielseitige Datensynchronisation nutzt.
- **MVC & RESTful Integration**:
    - **@Controller Architektur**: Der `DashboardController` verwaltet das zentrale Dashboard über Spring MVC und injiziert initiale serverseitige Daten (Umgebung, Zeitzone, Server-Laufzeit) in das `Model` für das **Thymeleaf**-Rendering.
    - **Saubere API-Endpunkte**: Dokumentierte Endpunkte für den Abruf von Lebenslaufdaten und die sitzungsbasierte Nachrichtenverarbeitung.
- **Dynamisches Frontend**: Die Dashboard-UI wird mit Thymeleaf vorgerendert und durch integrierte `dashboard.js`-Logik dynamisch aktualisiert.

## 📡 WebSocket-Architektur

Das System implementiert eine differenzierte Dual-Kanal-Kommunikationsstrategie:

1.  **Lokaler Raw-WebSocket (`/ws/dashboard`)**:
    - Dediziert für lokale Datensynchronisation und interne Statusüberwachung.
    - Verarbeitet die Rohdatenübertragung direkt innerhalb des Backend-Ökosystems.
2.  **Externer STOMP-WebSocket (`/ws/stomp`)**:
    - Primärer Integrationspunkt für das **Portfolio-Frontend**.
    - **Sicherheit/Routing**: Verwendet benutzerdefinierte `connectHeaders` (z. B. `client-type: frontend`).
    - **Protokollablauf**: Empfängt Daten über `/app/*`-Ziele und sendet Echtzeit-Updates über `@SendTo("/topic/messages")` zurück, inklusive automatisierter serverseitiger Zeitstempel.

## 🛠️ Technologie-Stack

- **Backend-Framework**: Spring Boot 3.4.2
- **Sprache**: Java 21 (LTS)
- **Messaging-Protokoll**: STOMP / WebSocket
- **Build-Tool**: Maven 3.9+
- **Template-Engine**: Thymeleaf (für serverseitiges Rendering, wo anwendbar)
- **Dienstprogramme**: Lombok (zur Reduzierung von Boilerplate-Code)

## ⚙️ Schnellstart

Detaillierte Anweisungen zur Einrichtung dieses Backends auf Ihrem lokalen Rechner finden Sie im:

👉 **[Live-Installationsleitfaden](DOC/md/install_for_backend_live.md)**

## 🌍 Mehrsprachige Dokumentation

- 🇬🇧 **[README in Englisch / English Version](README_live.md)**

---
*Entwickelt von Agnes Velemi - Leidenschaftliche Full-Stack-Entwicklerin*
