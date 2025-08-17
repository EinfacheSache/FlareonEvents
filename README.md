# FlareonEvents

🌍 Available Languages: [English](README.md) | [Deutsch](README.de.md)

A comprehensive **Paper 1.21.x** event plugin for Minecraft servers featuring a **team system**, **custom items with recipes/GUI**, **event phases** (Preparation/Start/Running), **scoreboard**, **world border control**, resource pack distribution, and optional **Simple Voice Chat** integration (softdepend).

> **Tested environment:** Java 21, Paper 1.21.6.  
> **Build:** Maven (`mvn clean package`) → `target/FlareonEvents-1.0-SNAPSHOT.jar`  
> **API version:** 1.21 | **Softdepend:** `voicechat` (Simple Voice Chat API)

---

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Configuration](#configuration)
    - [config.yml](#configyml)
    - [items.yml (Custom Items)](#itemsyml-custom-items)
    - [teams.yml](#teamsyml)
    - [infoBook.yml](#infobookyml)
    - [locations.yml](#locationsyml)
    - [participants.yml & deathParticipants.yml](#participantsyml--deathparticipantsyml)
- [Commands & Permissions](#commands--permissions)
- [Custom Items – Overview](#custom-items--overview)
- [Flow & Mechanics](#flow--mechanics)
- [Integration: Simple Voice Chat](#integration-simple-voice-chat)
- [Development](#development)
- [FAQ](#faq)

---

## Features

- **Event phases**: Preparation → Start → Running → (End)
- **Team system**: Invite/Accept/Kick/Leave, configurable team size & invite distance
- **Custom items**: Fire Sword, Nyx Bow, Poseidon's Trident, reinforced/superior pickaxes, etc.
- **Recipe GUI**: In-game overview of all items & ingredients
- **World border control**: Step-based shrinking, announcements via Title/Actionbar
- **PvP/Nether control**: PvP toggle, Nether unlocks later
- **Participant management**: Whitelist for running event, optional kick-on-death
- **Scoreboard**: Phase, players/teams, kills, border size
- **Resource pack**: Enforced for non-OPs; status messages on download/failure
- **Quality of Life**: Custom chat renderer, spawn protection, bed/anchor disabled in Nether
- **Optional: Simple Voice Chat**: Automatic team groups

---

## Installation

1. **Requirements**
    - Java 21
    - Paper **1.21.6** (or compatible 1.21.x)
    - *Optional:* [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) (server & client side)

2. **Plugin installation**
    - Copy `FlareonEvents-*.jar` into `plugins/`
    - Start the server → default configs will be created in `plugins/FlareonEvents/`
    - If using Voice Chat, install the corresponding plugin as well

3. **Resource pack**
    - Default URL: `https://einfachesache.de/texturepack/Flareon-Events-V2.zip`
    - Non-OPs (except DEV-UUID) receive the pack **enforced** with prompt. Status/errors are displayed in chat.

---

## Configuration

Files are located under `plugins/FlareonEvents/`.

### `config.yml`

```yml
start-time: 0        # Timestamp/marker for start
stop-since: 0        # Timestamp/marker for stop
event-state: NOT_RUNNING
kick-on-death: false # true = dead players cannot (re)join the running event
```

### `items.yml` (Custom Items)

- Global flags under `generell.item_flags` (e.g. `HIDE_ATTRIBUTES`, `HIDE_ENCHANTS`)
- Per item: `key` (NamespacedKey), `material`, `display_name`, attributes/enchantments and **abilities/cooldowns**

**Excerpt of abilities (configurable):**
- `fire_sword`: Fire tick chance/duration, **cooldown** (right-click skill – fireball)
- `nyx_bow`: Wither/Slow+Blind chance & duration, **dash** (right-click), **shoot_cooldown**
- `poseidons_trident`: **Lightning** on throw & partially melee, **cooldown**
- `superior_pickaxe`: **X-Ray ability** (time, radius, cooldown)

See also [Custom Items – Overview](#custom-items--overview).

### `teams.yml`

```yml
max-invite-distanz: 45  # Invite distance in blocks
max-team-size: 3        # Maximum team size
next-team-id: 1
teams: { }              # Filled at runtime
```

### `infoBook.yml`

- Multi-page info/rule book players receive (e.g. on join).
- Contains info such as **PvP/Nether**, **border** (start/target size), and **banned items/functions**.

### `locations.yml`

```yml
main-spawn:
player-spawns: [ ]  # Player spawn points (set via command)
```

### `participants.yml` & `deathParticipants.yml`

- Lists of (dead) participants – controls join/kick logic during running events.

---

## Commands & Permissions

> **Default permission for admin commands:** `event.op`

**General**
- `/help` – Overview
- `/recipe` – Opens the **recipe GUI**

**Team**
- `/team invite <player>` – Invite a player to your team
- `/team accept <player>` – Accept invitation
- `/team leave` – Leave team
- `/team kick <player>` – Remove team member
- `/team list [TeamID]` – Show team members

**Event management** *(Admin, `event.op`)*
- `/event start [true|false]` – Prepare/start event (optional **force**)
- `/event pause` – Pause event
- `/event cancel` – Cancel event
- `/event pvp` – Toggle PvP
- `/event reset <player|all> [true|false]` – Reset player (inv/status)
- `/event spawncircle` – Generate circular spawn points
- `/event setspawn [number]` – Save current location as spawn point

**Maintenance/Reload** *(Admin, `event.op`)*
- `/update` – **Reload all configs & custom items** (incl. inventory updates)
- `/update book` – Update/re-distribute info book
- `/customitem <…>` – Admin item giver (e.g. `fire_sword`, `nyx_bow`, `poseidons_trident`, `reinforced_pickaxe`, `superior_pickaxe`, `soul`, `all`, `ingredients`, `gear` …)

> Note: The **command whitelist** mechanism hides commands for non-OPs unless explicitly allowed.

---

## Custom Items – Overview

| Item-ID           | Display Name         | Material           | Key (NamespacedKey)         |
|-------------------|----------------------|-------------------|-----------------------------|
| fire_sword        | §cFire Sword         | DIAMOND_SWORD     | flareonevents:fire_sword    |
| nyx_bow           | §5Nyx Bow            | BOW               | flareonevents:nyx_bow       |
| poseidons_trident | §bPoseidon's Trident | TRIDENT           | flareonevents:poseidons_trident |
| reinforced_pickaxe| §aReinforced Pickaxe | DIAMOND_PICKAXE   | flareonevents:reinforced_pickaxe |
| superior_pickaxe  | §6Superior Pickaxe   | NETHERITE_PICKAXE | flareonevents:superior_pickaxe |

---

## Flow & Mechanics

- **Phases**: `PREPARING` → `STARTING` → `RUNNING` → `ENDED`
    - In `STARTING`, non-OPs are frozen (only looking around allowed).
    - Before start, hunger/damage for Adventure players is disabled.
- **PvP**: Globally toggleable; kill counter is tracked.
- **Nether**: Portal creation initially disabled, **unlocked later** (broadcast in-game).
- **World border**: Shrinks **stepwise** to a target size; plugin announces steps via Title/Actionbar.
- **Spawn protection**: Blocks around set spawn points are protected; explosion damage removed there.
- **Death**: Lightning effect, team removal, optional kick (depending on `kick-on-death`). If only **one player/team** remains, event ends.
- **Resource pack**: Distributed with fixed UUID & URL; *enforced* for non-OPs (except DEV-UUID). Status messages on `ACCEPTED/DECLINED/FAILED_DOWNLOAD`.

---

## Integration: Simple Voice Chat

- **Softdepend:** `voicechat`
- If installed, **team groups are automatically created** and players are assigned to their team group on join/team change.
- On leave, players are properly removed. No config required.

---

## Development

### Build project
- **Requirements:** JDK 21, Maven 3.9+
- **Build:**
  ```bash
  mvn clean package
  ```
- JAR will be located under `target/`.

### Dependencies (excerpt)
- `io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT` *(provided)*
- `de.maxhenkel.voicechat:voicechat-api:2.5.31` *(provided)*

### Code overview
- **Main:** `de.einfachesache.flareonevents.FlareonEvents`
- **Config/Assets:** `plugins/FlareonEvents/*.yml`
- **Teams:** `handler.TeamHandler` (+ `/team`)
- **Game/Phases:** `handler.GameHandler` (+ `/event`)
- **Custom Items:** `item.*` (Crafting/GUI/Passive effects)
- **Listener:** `listener.*` (Chat, Damage, Food, Join/Login, Move, Interact, Crafting, etc.)
- **Voice Chat:** `voicechat.VoiceModPlugin`

---

## FAQ

**How do I set spawn points?**  
Use `/event setspawn [number]` at your location. With `/event spawncircle`, you can generate a ring of spawn points.

**How do I enable the Nether?**  
The Nether is automatically **unlocked at the right time** by the plugin (in-game broadcast).

**Why can’t I see some commands?**  
Non-OPs only see a **whitelist** of commands.

**Can I adjust item values?**  
Yes, in `items.yml` (e.g. cooldowns, effects, attributes, enchantments). Then run `/update`.

---

> ✨ **Tips**
> - Use `/recipe` to give players access to **custom item recipes**.
> - Adjust `teams.yml` (team size/invite distance) to match your event format.
> - `kick-on-death: true` for strict elimination-style events.
