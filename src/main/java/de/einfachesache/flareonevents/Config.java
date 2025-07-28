package de.einfachesache.flareonevents;

import de.cubeattack.api.util.FileUtils;
import de.einfachesache.flareonevents.item.ItemUtils;
import de.einfachesache.flareonevents.item.tool.*;
import net.kyori.adventure.text.Component;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings({"removal", "deprecation"})
public class Config {

    private static long startTime = 0;
    private static long stopSince = 0;
    private static EventState eventState = EventState.NOT_RUNNING;

    private static Location mainSpawnLocation;
    private static final List<String> participantsUUID = new ArrayList<>();
    private static final List<String> deathParticipantsUUID = new ArrayList<>();
    private static final List<Location> playerSpawnLocations = new ArrayList<>();
    private static final Map<Integer, Component> infoBookSorted = new TreeMap<>();

    private static int nextTeamId;
    private static int maxTeamSize;
    private static int maxInviteDistanz;
    private static final Map<Integer, Set<UUID>> TEAMS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> PLAYER_TEAMS = new ConcurrentHashMap<>();
    private static final Map<Integer, UUID> TEAM_LEADERS = new ConcurrentHashMap<>();

    public static void reloadBook() {
        FlareonEvents.getInfoBookFile().reloadConfiguration();
        loadInfoBook();
    }

    public static void reloadFiles() {
        FlareonEvents.getItemsFile().reloadConfiguration();
        FlareonEvents.getTeamsFile().reloadConfiguration();
        FlareonEvents.getFileConfig().reloadConfiguration();
        FlareonEvents.getInfoBookFile().reloadConfiguration();
        FlareonEvents.getLocationsFile().reloadConfiguration();
        FlareonEvents.getParticipantsFile().reloadConfiguration();
        FlareonEvents.getDeathParticipantsFile().reloadConfiguration();
        loadFiles();
    }

    public static void loadFiles() {
        loadItems();
        loadTeams();
        loadConfig();
        loadInfoBook();
        loadParticipants();
        loadDeathParticipants();
        loadMainSpawnLocations();
        loadPlayerSpawnLocations();

        if (WorldUtils.isWorldGeneratedFresh())
            resetGameOnNewWorldGeneration();
    }

    private static final FileUtils config = FlareonEvents.getFileConfig();

    private static void loadConfig() {
        startTime = config.getLong("start-time", 0);
        stopSince = config.getLong("stop-since", 0);
        eventState = EventState.valueOf(config.get("event-state", "NOT_RUNNING"));
    }

    private static final FileUtils participantsFile = FlareonEvents.getParticipantsFile();

    private static void loadParticipants() {
        participantsUUID.clear();
        participantsUUID.addAll(participantsFile.getStringList("participants"));
    }


    private static final FileUtils deathParticipantsFile = FlareonEvents.getDeathParticipantsFile();

    private static void loadDeathParticipants() {
        deathParticipantsUUID.clear();
        deathParticipantsUUID.addAll(deathParticipantsFile.getStringList("death-participants"));
    }


    private static final FileUtils infoBookFile = FlareonEvents.getInfoBookFile();

    private static void loadInfoBook() {
        infoBookSorted.clear();
        infoBookFile.getConfigurationSection("pages").getKeys(false).forEach(page -> {
            try {
                int pageNumber = Integer.parseInt(page);
                String text = infoBookFile.get("pages." + pageNumber);
                infoBookSorted.put(pageNumber, Component.text(text));
            } catch (NumberFormatException e) {
                FlareonEvents.getLogManager().warn("Ungültiger Seiten-Key: " + page);
            }
        });
    }


    private static final FileUtils teamsFile = FlareonEvents.getTeamsFile();

    private static void loadTeams() {
        nextTeamId = teamsFile.getInt("next-team-id", 1);
        maxTeamSize = teamsFile.getInt("max-team-size", 3);
        maxInviteDistanz = teamsFile.getInt("max-invite-distanz", 30);
        ConfigurationSection teamSection = teamsFile.getConfigurationSection("teams");
        if (teamSection != null) {
            for (String key : teamSection.getKeys(false)) {
                int teamId = Integer.parseInt(key);
                UUID leader = UUID.fromString(Objects.requireNonNull(teamSection.getString(key + ".leader")));
                List<String> memberList = teamSection.getStringList(key + ".members");

                TEAM_LEADERS.put(teamId, leader);
                Set<UUID> members = memberList.stream().map(UUID::fromString).collect(Collectors.toSet());
                TEAMS.put(teamId, members);
                for (UUID uuid : members) {
                    PLAYER_TEAMS.put(uuid, teamId);
                }
            }
        }
    }


    private static final FileUtils locationsFile = FlareonEvents.getLocationsFile();

    private static void loadMainSpawnLocations() {
        String worldName = config.get("main-spawn.world", "world");
        double x = locationsFile.getDouble("main-spawn.x");
        double y = locationsFile.getDouble("main-spawn.y");
        double z = locationsFile.getDouble("main-spawn.z");
        float yaw = (float) locationsFile.getDouble("main-spawn.yaw");
        float pitch = (float) locationsFile.getDouble("main-spawn.pitch");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            FlareonEvents.getLogManager().warn("❌ Welt '" + worldName + "' nicht gefunden!");
            return;
        }

        mainSpawnLocation = world.getHighestBlockAt(new Location(world, x, y, z, yaw, pitch)).getLocation().add(0, 1, 0);
    }

    public static void loadPlayerSpawnLocations() {

        Config.playerSpawnLocations.clear();

        ConfigurationSection section = locationsFile.getConfigurationSection("player-spawns");
        if (section == null) {
            FlareonEvents.getLogManager().warn("Keine 'player-spawns' Sektion in der Config gefunden.");
            return;
        }

        section.getKeys(false).stream()
                .sorted(Comparator.comparingInt(Integer::parseInt))
                .forEach(key -> {
                    String path = "player-spawns." + key;

                    String worldName = locationsFile.get(path + ".world", "world");
                    double x = locationsFile.getDouble(path + ".x");
                    double y = locationsFile.getDouble(path + ".y");
                    double z = locationsFile.getDouble(path + ".z");
                    float yaw = (float) locationsFile.getDouble(path + ".yaw");
                    float pitch = (float) locationsFile.getDouble(path + ".pitch");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        FlareonEvents.getLogManager().warn("❌ Welt '" + worldName + "' für Spawn #" + key + " wurde nicht gefunden.");
                        return;
                    }

                    Location loc = new Location(world, x, y, z, yaw, pitch);
                    Config.playerSpawnLocations.add(loc);
                });

        FlareonEvents.getLogManager().info("✅ " + Config.playerSpawnLocations.size() + " Spawnpunkte geladen.");
    }


    private static final FileUtils itemsFile = FlareonEvents.getItemsFile();

    private static ItemFlag[] itemFlags;

    private static void loadItems() {

        itemFlags = itemsFile.getStringList("generell.item_flags").stream().map(ItemFlag::valueOf).toArray(ItemFlag[]::new);

        loadFireSword();
        loadNyxBow();
        loadPoseidonsTrident();
        loadReinforcedPickaxe();
        loadBetterReinforcedPickaxe();
    }

    private static void loadFireSword() {
        String fsKeyString = itemsFile.get("items.fire_sword.key");
        FireSword.NAMESPACED_KEY = NamespacedKey.fromString(fsKeyString, FlareonEvents.getPlugin());
        FireSword.MATERIAL = Material.valueOf(itemsFile.get("items.fire_sword.material"));
        FireSword.DISPLAY_NAME = itemsFile.get("items.fire_sword.display_name");

        // Effekte & Cooldown
        FireSword.FIRE_TICKS_CHANCE = itemsFile.getDouble("items.fire_sword.fire_ticks_chance");
        FireSword.FIRE_TICKS_TIME = itemsFile.getInt("items.fire_sword.fire_ticks_time");
        FireSword.COOLDOWN = itemsFile.getInt("items.fire_sword.cooldown");

        // Enchantments einlesen
        FireSword.ENCHANTMENTS = loadEnchantments("fire_sword");
        // Attribute-Modifier einlesen
        FireSword.ATTRIBUTE_MODIFIERS = loadAttributes("fire_sword");
        // Item-Flags einlesen
        FireSword.ITEM_FLAGS = itemFlags;
    }

    private static void loadNyxBow() {
        // NamespacedKey, Material & Display-Name
        String nbKeyString = itemsFile.get("items.nyx_bow.key");
        NyxBow.NAMESPACED_KEY = NamespacedKey.fromString(nbKeyString, FlareonEvents.getPlugin());
        NyxBow.MATERIAL = Material.valueOf(itemsFile.get("items.nyx_bow.material"));
        NyxBow.DISPLAY_NAME = itemsFile.get("items.nyx_bow.display_name");

        // Effekte & Cooldown
        NyxBow.WITHER_EFFECT_CHANCE = itemsFile.getDouble("items.nyx_bow.wither_effect_chance");
        NyxBow.WITHER_EFFECT_TIME = itemsFile.getInt("items.nyx_bow.wither_effect_time");
        NyxBow.SLOW_BLIND_EFFECT_CHANCE = itemsFile.getDouble("items.nyx_bow.slow_blind_effect_chance");
        NyxBow.SLOW_BLIND_EFFECT_TIME = itemsFile.getInt("items.nyx_bow.slow_blind_effect_time");
        NyxBow.COOLDOWN = itemsFile.getInt("items.nyx_bow.cooldown");

        // Verzauberungen einlesen
        NyxBow.ENCHANTMENTS = loadEnchantments("nyx_bow");
        // Attribute-Modifier einlesen (falls in config definiert)
        NyxBow.ATTRIBUTE_MODIFIERS = loadAttributes("nyx_bow");
        // ItemFlags einlesen
        NyxBow.ITEM_FLAGS = itemFlags;
    }

    private static void loadPoseidonsTrident() {
        // NamespacedKey, Material & Display-Name
        String ptKeyString = itemsFile.get("items.poseidons_trident.key");
        PoseidonsTrident.NAMESPACED_KEY = NamespacedKey.fromString(ptKeyString, FlareonEvents.getPlugin());
        PoseidonsTrident.MATERIAL = Material.valueOf(itemsFile.get("items.poseidons_trident.material"));
        PoseidonsTrident.DISPLAY_NAME = itemsFile.get("items.poseidons_trident.display_name");

        // Effekte & Cooldown
        PoseidonsTrident.THROW_LIGHTNING_CHANCE = itemsFile.getDouble("items.poseidons_trident.throw_lightning_chance");
        PoseidonsTrident.ON_MELEE_LIGHTNING_CHANCE = itemsFile.getDouble("items.poseidons_trident.melee_lightning_chance");
        PoseidonsTrident.COOLDOWN = itemsFile.getInt("items.poseidons_trident.cooldown");

        // Verzauberungen einlesen
        PoseidonsTrident.ENCHANTMENTS = loadEnchantments("poseidons_trident");
        // Attribute-Modifier einlesen (falls in config definiert)
        PoseidonsTrident.ATTRIBUTE_MODIFIERS = loadAttributes("poseidons_trident");
        // ItemFlags einlesen
        PoseidonsTrident.ITEM_FLAGS = itemFlags;
    }

    private static void loadBetterReinforcedPickaxe() {
        // BetterReinforcedPickaxe
        String brpKeyString = itemsFile.get("items.better_reinforced_pickaxe.key");
        BetterReinforcedPickaxe.NAMESPACED_KEY = NamespacedKey.fromString(brpKeyString, FlareonEvents.getPlugin());
        BetterReinforcedPickaxe.MATERIAL = Material.valueOf(itemsFile.get("items.better_reinforced_pickaxe.material"));
        BetterReinforcedPickaxe.DISPLAY_NAME = itemsFile.get("items.better_reinforced_pickaxe.display_name");

        // X-Ray-Konfiguration
        BetterReinforcedPickaxe.XRAY_ENABLED_TIME = itemsFile.getInt("items.better_reinforced_pickaxe.xray.enabled_time");
        BetterReinforcedPickaxe.XRAY_RADIUS = itemsFile.getInt("items.better_reinforced_pickaxe.xray.radius");
        BetterReinforcedPickaxe.XRAY_COOLDOWN = itemsFile.getInt("items.better_reinforced_pickaxe.xray.cooldown");
        // Item-Flags einlesen
        BetterReinforcedPickaxe.ITEM_FLAGS = itemFlags;
    }

    private static void loadReinforcedPickaxe() {
        // ReinforcedPickaxe
        String rpKeyString = itemsFile.get("items.reinforced_pickaxe.key");
        ReinforcedPickaxe.NAMESPACED_KEY = NamespacedKey.fromString(rpKeyString, FlareonEvents.getPlugin());
        ReinforcedPickaxe.MATERIAL = Material.valueOf(itemsFile.get("items.reinforced_pickaxe.material"));
        ReinforcedPickaxe.DISPLAY_NAME = itemsFile.get("items.reinforced_pickaxe.display_name");

        // Enchantments einlesen
        ReinforcedPickaxe.ENCHANTMENTS = loadEnchantments("reinforced_pickaxe");
        // Attribute-Modifier einlesen
        ReinforcedPickaxe.ATTRIBUTE_MODIFIERS = loadAttributes("reinforced_pickaxe");
        // Item-Flags einlesen
        ReinforcedPickaxe.ITEM_FLAGS = itemFlags;
    }


    private static Map<Enchantment, Integer> loadEnchantments(String itemKey) {

        Map<Enchantment, Integer> enchantments = new HashMap<>();

        for (Map.Entry<?, ?> entry : itemsFile.getMap("items." + itemKey + ".enchantments", true).entrySet()) {
            String name = entry.getKey().toString();
            int level = Integer.parseInt(entry.getValue().toString());
            NamespacedKey key = NamespacedKey.minecraft(name);
            Enchantment enchantment = org.bukkit.Registry.ENCHANTMENT.get(key);

            if (enchantment == null) {
                FlareonEvents.getLogManager().warn("Could not find enchantment " + key);
                continue;
            }

            enchantments.put(enchantment, level);
        }

        return enchantments;
    }

    private static Map<Attribute, AttributeModifier> loadAttributes(String itemKey) {

        Map<Attribute, AttributeModifier> attributes = new HashMap<>();

        for (Map<?, ?> map : itemsFile.getMapList("items." + itemKey + ".attributes")) {
            String attrPath = map.get("attribute").toString();
            NamespacedKey key = NamespacedKey.minecraft(attrPath);
            Attribute attribute = org.bukkit.Registry.ATTRIBUTE.get(key);

            if (attribute == null) {
                FlareonEvents.getLogManager().warn("Could not find attribute " + key);
                continue;
            }

            double amount = ((Number) map.get("amount")).doubleValue();
            AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(map.get("operation").toString());
            EquipmentSlot slot = EquipmentSlot.valueOf(map.get("slot").toString());
            UUID uuid = ItemUtils.stringToUUID(attrPath);

            AttributeModifier mod = new AttributeModifier(uuid, attrPath, amount, op, slot);
            attributes.put(attribute, mod);
        }

        return attributes;
    }


    public static long getStartTime() {
        return startTime;
    }

    public static long getStopSince() {
        return stopSince;
    }

    public static boolean isEventStarted() {
        return eventState.getId() > 1;
    }

    public static boolean isEventIsRunning() {
        return eventState == EventState.RUNNING;
    }

    public static EventState getEventState() {
        return eventState;
    }

    public static Location getMainSpawnLocation() {
        return mainSpawnLocation;
    }

    public static void setStartTime(long startTime) {
        Config.startTime = startTime;
        save(config, "start-time", startTime);
    }

    public static void setStopSince(long stopSince) {
        Config.stopSince = stopSince;
        save(config, "stop-since", stopSince);
    }

    public static void setEventState(EventState eventState) {
        Config.eventState = eventState;
        save(config, "event-state", eventState.toString());
    }


    public static void setMainSpawnLocation(Location location) {
        Config.mainSpawnLocation = location;
        saveLocations("main-spawn", location);
    }

    public static void setPlayerSpawnLocation(Integer spawnCount, Location location) {
        int index = spawnCount - 1;

        while (Config.playerSpawnLocations.size() <= index) {
            Config.playerSpawnLocations.add(null);
        }

        Config.playerSpawnLocations.set(index, location);
        saveLocations("player-spawns." + spawnCount, location);
    }


    private static void saveLocations(String savePath, Location location) {
        locationsFile.set(savePath + ".world", location.getWorld().getName());
        locationsFile.set(savePath + ".x", location.getBlock().getX());
        locationsFile.set(savePath + ".y", location.getBlock().getY());
        locationsFile.set(savePath + ".z", location.getBlock().getZ());
        locationsFile.set(savePath + ".yaw", location.getYaw());
        locationsFile.set(savePath + ".pitch", location.getPitch());

        locationsFile.save();
    }

    public static void addParticipant(UUID playerUUID) {
        participantsUUID.add(playerUUID.toString());
        save(participantsFile, "participants", participantsUUID.toArray());
    }

    public static void clearParticipant() {
        participantsUUID.clear();
        save(participantsFile, "participants", Collections.emptyList());
    }


    public static void addDeathParticipant(UUID playerUUID) {
        deathParticipantsUUID.add(playerUUID.toString());
        save(deathParticipantsFile, "death-participants", deathParticipantsUUID.toArray());
    }

    public static void clearDeathParticipant() {
        deathParticipantsUUID.clear();
        save(deathParticipantsFile, "death-participants", Collections.emptyList());
    }

    public static void resetGameOnNewWorldGeneration() {
        setEventState(EventState.NOT_RUNNING);
        setStartTime(0L);
        setStopSince(0L);
    }


    public static void setTeamLeader(int teamId, UUID leader) {
        TEAM_LEADERS.put(teamId, leader);
        Config.save(teamsFile, "teams." + teamId + ".leader", leader.toString());
    }

    public static void addTeam(int teamId, UUID player) {
        TEAMS.put(teamId, new HashSet<>(Collections.singleton(player)));
        addPlayerToTeam(player, teamId);
    }

    public static void addPlayerToTeam(UUID player, int teamId) {
        TEAMS.computeIfAbsent(teamId, k -> new HashSet<>()).add(player);
        PLAYER_TEAMS.put(player, teamId);

        List<String> updated = TEAMS.get(teamId).stream().map(UUID::toString).toList();
        Config.save(teamsFile, "teams." + teamId + ".members", updated);
    }

    public static void removePlayerFromTeam(UUID player) {
        Integer teamId = PLAYER_TEAMS.remove(player);
        if (teamId == null) return;

        Set<UUID> team = TEAMS.get(teamId);
        if (team != null) {
            team.remove(player);
            if (team.isEmpty()) {
                TEAMS.remove(teamId);
                TEAM_LEADERS.remove(teamId);
                Config.save(teamsFile, "teams." + teamId, null);
            } else {
                List<String> updated = team.stream().map(UUID::toString).toList();
                Config.save(teamsFile, "teams." + teamId + ".members", updated);
            }
        }
    }

    public static void deleteAllTeams() {
        PLAYER_TEAMS.clear();
        TEAM_LEADERS.clear();
        TEAMS.clear();
        nextTeamId = 1;
        Config.save(teamsFile, "teams", null);
        Config.save(teamsFile, "next-team-id", 1);
    }


    public static void save(FileUtils file, String key, Object value) {
        file.set(key, value);
        file.save();
    }


    public static List<String> getDeathParticipantsUUID() {
        return deathParticipantsUUID;
    }

    public static List<Location> getPlayerSpawnLocations() {
        return playerSpawnLocations;
    }

    public static List<String> getParticipantsUUID() {
        return participantsUUID;
    }

    public static Map<Integer, Component> getInfoBookSorted() {
        return infoBookSorted;
    }


    public static int getMaxInviteDistanz() {
        return maxInviteDistanz;
    }

    public static int getMaxTeamSize() {
        return maxTeamSize;
    }

    public static int getNextTeamId() {
        nextTeamId++;
        save(teamsFile, "next-team-id", nextTeamId);
        return nextTeamId-1;
    }

    public static Map<Integer, Set<UUID>> getTeams() {
        return TEAMS;
    }

    public static Map<Integer, UUID> getTeamLeaders() {
        return TEAM_LEADERS;
    }

    public static Map<UUID, Integer> getPlayerTeams() {
        return PLAYER_TEAMS;
    }
}
