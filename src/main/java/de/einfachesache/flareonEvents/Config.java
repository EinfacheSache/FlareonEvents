package de.einfachesache.flareonEvents;

import de.cubeattack.api.util.FileUtils;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Config {

    private static EventState eventState = EventState.NOT_RUNNING;

    private static Location mainSpawnLocation;
    private static final List<String> participantsUUID = new ArrayList<>();
    private static final List<String> deathParticipantsUUID = new ArrayList<>();
    private static final List<Location> playerSpawnLocations = new ArrayList<>();

    public static void loadFiles() {
        loadConfig();
        loadParticipants();
        loadDeathParticipants();
        loadMainSpawnLocations();
        loadPlayerSpawnLocations();
    }

    private static final FileUtils config = FlareonEvents.getFileConfig();

    private static void loadConfig() {
        eventState = EventState.valueOf(config.getString("eventState", "NOT_RUNNING"));
    }

    private static final FileUtils participantsFile = FlareonEvents.getParticipantsFile();

    private static void loadParticipants() {
        participantsUUID.clear();
        participantsUUID.addAll(participantsFile.getListAsList("participants"));
    }

    private static final FileUtils deathParticipantsFile = FlareonEvents.getDeathParticipantsFile();

    private static void loadDeathParticipants() {
        deathParticipantsUUID.clear();
        deathParticipantsUUID.addAll(deathParticipantsFile.getListAsList("death-participants"));
    }

    private static final FileUtils locationsFile = FlareonEvents.getLocationsFile();

    private static void loadMainSpawnLocations() {
        String worldName = config.getString("main-spawn.world", "world");
        double x = locationsFile.getDouble("main-spawn.x");
        double y = locationsFile.getDouble("main-spawn.y");
        double z = locationsFile.getDouble("main-spawn.z");
        float yaw = (float) locationsFile.getDouble("main-spawn.yaw");
        float pitch = (float) locationsFile.getDouble("main-spawn.pitch");

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            FlareonEvents.getLogManager().warn("❌ Welt '" + worldName + "' nicht gefunden!");
        }

        mainSpawnLocation = new Location(world, x, y, z, yaw, pitch);
    }

    public static void loadPlayerSpawnLocations() {

        Config.playerSpawnLocations.clear();

        ConfigurationSection section = locationsFile.getConfigurationSection("player-spawns");
        if (section == null) {
            FlareonEvents.getLogManager().warn("⚠️ Keine 'player-spawns' Sektion in der Config gefunden.");
            return;
        }

        section.getKeys(false).stream()
                .sorted(Comparator.comparingInt(Integer::parseInt))
                .forEach(key -> {
                    String path = "player-spawns." + key;

                    String worldName = locationsFile.getString(path + ".world", "world");
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

    public static boolean isEventStarted() {
        return (eventState == EventState.RUNNING || eventState == EventState.STARTING);
    }

    public static EventState getEventState() {
        return eventState;
    }

    public static Location getMainSpawnLocation() {
        return mainSpawnLocation;
    }


    public static void setEventState(EventState eventState) {
        Config.eventState = eventState;
        save(config, "event-state", eventState.toString());
    }

    public static void setPlayerSpawnLocation(Integer spawnCount, Location location) {
        int index = spawnCount - 1;

        while (Config.playerSpawnLocations.size() <= index) {
            Config.playerSpawnLocations.add(null);
        }

        Config.playerSpawnLocations.set(index, location);
        saveLocations("player-spawns." + spawnCount, location);
    }

    public static void setMainSpawnLocation(Location location) {
        Config.mainSpawnLocation = location;
        saveLocations("main-spawn", location);
    }

    private static void saveLocations(String savePath, Location location) {
        locationsFile.set(savePath + ".world", location.getWorld().getName());
        locationsFile.set(savePath + ".x", location.getX());
        locationsFile.set(savePath + ".y", location.getY());
        locationsFile.set(savePath + ".z", location.getZ());
        locationsFile.set(savePath + ".yaw", location.getYaw());
        locationsFile.set(savePath + ".pitch", location.getPitch());

        locationsFile.save();
    }

    public static void addParticipant(UUID playerUUID) {
        participantsUUID.add(playerUUID.toString());
        save(participantsFile, "participants", participantsUUID.toArray());
    }

    public static void addDeathParticipant(UUID playerUUID) {
        deathParticipantsUUID.add(playerUUID.toString());
        save(deathParticipantsFile, "death-participants", deathParticipantsUUID.toArray());
    }

    public static void clearDeathParticipant() {
        deathParticipantsUUID.clear();
        save(deathParticipantsFile, "death-participants", Collections.emptyList());
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
}
