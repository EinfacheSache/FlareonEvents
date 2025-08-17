# FlareonEvents

Ein umfangreiches **Paper 1.21.x** Event-Plugin für Minecraft-Server mit **Team-System**, **Custom Items samt Rezepten/GUI**, **Event-Phasen** (Vorbereitung/Start/Running), **Scoreboard**, **Welt­border-Steuerung**, Ressourcenpaket-Verteilung sowie optionaler **Simple Voice Chat**-Integration (Softdepend).

> **Getestete Umgebung:** Java 21, Paper 1.21.6.  
> **Build:** Maven (`mvn clean package`) → `target/FlareonEvents-1.0-SNAPSHOT.jar`  
> **API-Version:** 1.21 | **Softdepend:** `voicechat` (Simple Voice Chat API)

---

## Inhalt
- [Features](#features)
- [Installation](#installation)
- [Konfiguration](#konfiguration)
    - [config.yml](#configyml)
    - [items.yml (Custom Items)](#itemsyml-custom-items)
    - [teams.yml](#teamsyml)
    - [infoBook.yml](#infobookyml)
    - [locations.yml](#locationsyml)
    - [participants.yml & deathParticipants.yml](#participantsyml--deathparticipantsyml)
- [Befehle & Rechte](#befehle--rechte)
- [Custom Items – Übersicht](#custom-items--übersicht)
- [Ablauf & Mechaniken](#ablauf--mechaniken)
- [Integration: Simple Voice Chat](#integration-simple-voice-chat)
- [Entwicklung](#entwicklung)
- [FAQ](#faq)

---

## Features

- **Event-Phasen**: Vorbereitung → Start → Running → (Ende)
- **Team-System**: Einladen/Annehmen/Kicken/Verlassen, Teamgröße & Einladungsreichweite konfigurierbar
- **Custom Items**: Feuer-Schwert, Nyx-Bogen, Poseidons Dreizack, verstärkte/superior Spitzhacken u. a.
- **Rezept-GUI**: Übersicht aller Items & Zutaten direkt im Spiel
- **Welt­border-Steuerung**: Stepweise Verkleinerung, Ankündigungen per Title/Actionbar
- **PvP-/Nether-Steuerung**: PvP Umschaltbar, Nether wird erst später freigegeben
- **Teilnehmer-Verwaltung**: Whitelist fürs laufende Event, optionales Kick-on-Death
- **Scoreboard**: Phase, Spieler/Teams, Kills, Border-Größe
- **Ressourcenpaket**: Erzwingbar für Nicht-OPs; Statusmeldungen bei Download/Fehler
- **Quality-of-Life**: Angepasster Chat-Renderer, Schutz rund um Spawnpunkte, Bed/Anchor im Nether deaktiviert
- **Optional: Simple Voice Chat**: Automatische Team-Gruppen

---

## Installation

1. **Voraussetzungen**
    - Java 21
    - Paper **1.21.6** (oder kompatible 1.21.x)
    - *Optional:* [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) (Server- & Clientseite)

2. **Plugin installieren**
    - `FlareonEvents-*.jar` nach `plugins/` kopieren
    - Server starten → Standard-Konfigs werden unter `plugins/FlareonEvents/` erzeugt
    - Bei Nutzung von Voice Chat das entsprechende Plugin zusätzlich installieren

3. **Ressourcenpaket**
    - Standard-URL: `https://einfachesache.de/texturepack/Flareon-Events-V2.zip`
    - Nicht-OPs (außer DEV-UUID) bekommen das Pack **erzwingend** mit Prompt. Status/Fehler werden im Chat angezeigt.

---

## Konfiguration

Die Dateien liegen unter `plugins/FlareonEvents/`.

### `config.yml`

```yml
start-time: 0        # Timestamp/Marker für Start
stop-since: 0        # Timestamp/Marker fürs Stoppen
event-state: NOT_RUNNING
kick-on-death: false # true = Tote dürfen dem laufenden Event nicht (wieder) beitreten
```

### `items.yml` (Custom Items)

- Globale Flags unter `generell.item_flags` (z. B. `HIDE_ATTRIBUTES`, `HIDE_ENCHANTS`)
- Je Item: `key` (NamespacedKey), `material`, `display_name`, Attribute/Verzauberungen und **Fähigkeiten/Cooldowns**

**Auszug der Fähigkeiten (konfigurierbar):**
- `fire_sword`: Feuer-Ticks-Chance/-Dauer, **cooldown** (Rechtsklick-Skill – Feuerball)
- `nyx_bow`: Wither-/Slow+Blind-Chancen & Dauer, **dash** (Rechtsklick), **shoot_cooldown**
- `poseidons_trident`: **Lightning** bei Wurf & teils im Nahkampf, **cooldown**
- `superior_pickaxe`: **X-Ray-Fähigkeit** (Zeit, Radius, Cooldown)

Siehe auch Abschnitt [Custom Items – Übersicht](#custom-items--übersicht).

### `teams.yml`

```yml
max-invite-distanz: 45  # Reichweite zum Einladen in Blöcken
max-team-size: 3        # Maximale Teamgröße
next-team-id: 1
teams: { }              # Wird zur Laufzeit gefüllt
```

### `infoBook.yml`

- Mehrseitiges Info-/Regelbuch, das Spieler erhalten (z. B. beim Join).
- Beinhaltet u. a. Hinweise zu **PvP/Nether**, **Border** (Start- und Zielgrößen) und **gebannte Items/Funktionen**.

### `locations.yml`

```yml
main-spawn:
player-spawns: [ ]  # Spawnpunkte der Teilnehmer (per Befehl gesetzt)
```

### `participants.yml` & `deathParticipants.yml`

- Listen von (toten) Teilnehmern – steuern Join/Kick-Logik während laufendem Event.

---

## Befehle & Rechte

> **Standard-Permission für Admin-Kommandos:** `event.op`

**Allgemein**
- `/help` – Übersicht
- `/recipe` – Öffnet das **Rezept-GUI**

**Team**
- `/team invite <Spieler>` – Spieler ins eigene Team einladen
- `/team accept <Spieler>` – Einladung annehmen
- `/team leave` – Team verlassen
- `/team kick <Spieler>` – Teammitglied entfernen
- `/team list [TeamID]` – Teammitglieder anzeigen

**Event-Verwaltung** *(Admin, `event.op`)*
- `/event start [true|false]` – Event vorbereiten/starten (optional **force**)
- `/event pause` – Event pausieren
- `/event cancel` – Event abbrechen
- `/event pvp` – PvP umschalten
- `/event reset <Spieler|all> [true|false]` – Spieler zurücksetzen (Inv/Status)
- `/event spawncircle` – Kreisförmige Spawnpunkte generieren
- `/event setspawn [number]` – Eigenen Standort als Spawnpunkt speichern

**Wartung/Reload** *(Admin, `event.op`)*
- `/update` – **Alle Konfigs & Custom Items** neu laden (inkl. Inventar-Updates)
- `/update book` – Info-Buch aktualisieren/neu verteilen
- `/customitem <…>` – Admin-Itemgeber (z. B. `fire_sword`, `nyx_bow`, `poseidons_trident`, `reinforced_pickaxe`, `superior_pickaxe`, `soul`, `all`, `ingredients`, `gear` …)

> Hinweis: Der **Command-Whitelist**-Mechanismus blendet Nicht-OPs Befehle aus, die nicht explizit erlaubt sind.

---

## Custom Items – Übersicht

| Item-ID           | Anzeige-Name          | Material           | Key (NamespacedKey)         |
|-------------------|----------------------|-------------------|-----------------------------|
| fire_sword        | §cFire Sword         | DIAMOND_SWORD     | flareonevents:fire_sword    |
| nyx_bow           | §5Nyx Bow            | BOW               | flareonevents:nyx_bow       |
| poseidons_trident | §bPoseidon's Trident | TRIDENT           | flareonevents:poseidons_trident |
| reinforced_pickaxe| §aReinforced Pickaxe | DIAMOND_PICKAXE   | flareonevents:reinforced_pickaxe |
| superior_pickaxe  | §6Superior Pickaxe   | NETHERITE_PICKAXE | flareonevents:superior_pickaxe |

---

## Ablauf & Mechaniken

- **Phasen**: `PREPARING` → `STARTING` → `RUNNING` → `ENDED`
    - In `STARTING` ist Bewegung für Nicht-OPs gesperrt (nur Blickrichtung erlaubt).
    - Vor dem Start sind Hunger/Damage für Adventure-Spieler deaktiviert.
- **PvP**: Global umschaltbar; Kill-Zähler wird erfasst.
- **Nether**: Portalerzeugung ist zunächst deaktiviert und wird **später freigeschaltet** (Ankündigung im Spiel).
- **Weltborder**: Verkleinert sich **schrittweise** auf eine Zielgröße; das Plugin kündigt Schritte per Title/Actionbar an.
- **Spawn-Schutz**: Blöcke rund um festgelegte Spawnpunkte sind geschützt; Explosionsschäden werden dort entfernt.
- **Tod**: Blitz-Effekt, Team-Entfernung, optional Kick (abhängig von `kick-on-death`). Wenn nur noch **ein Spieler/ein Team** übrig ist, wird das Event beendet.
- **Ressourcenpaket**: Wird mit fester UUID & URL verteilt; für Nicht-OPs (außer DEV-UUID) *erzwingend*. Statusmeldungen bei `ACCEPTED/DECLINED/FAILED_DOWNLOAD`.

---

## Integration: Simple Voice Chat

- **Softdepend:** `voicechat`
- Wenn installiert, werden **Team-Gruppen automatisch erstellt** und Spieler beim Beitritt/Teamwechsel der passenden Voice-Gruppe zugeordnet.
- Beim Verlassen werden sie sauber entfernt. Keine Konfiguration notwendig.

---

## Entwicklung

### Projekt bauen
- **Voraussetzungen:** JDK 21, Maven 3.9+
- **Build:**
  ```bash
  mvn clean package
  ```
- Das JAR liegt anschließend unter `target/`.

### Abhängigkeiten (Auszug)
- `io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT` *(provided)*
- `de.maxhenkel.voicechat:voicechat-api:2.5.31` *(provided)*

### Code-Überblick
- **Main:** `de.einfachesache.flareonevents.FlareonEvents`
- **Config/Assets:** `plugins/FlareonEvents/*.yml`
- **Teams:** `handler.TeamHandler` (+ `/team`)
- **Game/Phasen:** `handler.GameHandler` (+ `/event`)
- **Custom Items:** `item.*` (Crafting/GUI/Passiveffekte)
- **Listener:** `listener.*` (Chat, Damage, Food, Join/Login, Move, Interact, Crafting, etc.)
- **Voice Chat:** `voicechat.VoiceModPlugin`

---

## FAQ

**Wie setze ich die Spawnpunkte?**  
Nutze `/event setspawn [number]` an deinem Standort. Mit `/event spawncircle` kannst du einen Ring aus Spawnpunkten generieren.

**Wie aktiviere ich den Nether?**  
Der Nether wird automatisch **zur richtigen Zeit** vom Plugin freigeschaltet (Ingame-Broadcast).

**Warum sehe ich manche Befehle nicht?**  
Nicht-OPs erhalten nur eine **Whitelist** an sichtbaren Befehlen.

**Kann ich die Item-Werte anpassen?**  
Ja, in `items.yml` (z. B. Cooldowns, Effekte, Attribute, Verzauberungen). Danach `/update` ausführen.

---

> ✨ **Tipps**
> - Nutze `/recipe`, um Spielern die **Custom-Item-Rezepte** zugänglich zu machen.
> - Halte `teams.yml` (Teamgröße/Einladungsreichweite) passend zu deinem Event-Format.
> - `kick-on-death: true` für strikte Event-Formate (Elimination).
