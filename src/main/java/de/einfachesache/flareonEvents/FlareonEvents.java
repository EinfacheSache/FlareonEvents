package de.einfachesache.flareonEvents;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.FileUtils;
import de.einfachesache.flareonEvents.command.CustomItemCommand;
import de.einfachesache.flareonEvents.command.EventCommand;
import de.einfachesache.flareonEvents.command.RecipeGuiCommand;
import de.einfachesache.flareonEvents.item.*;
import de.einfachesache.flareonEvents.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class FlareonEvents extends JavaPlugin {

    private static FlareonEvents plugin;

    private static FileUtils configFile;
    private static FileUtils locationsFile;
    private static FileUtils participantsFile;
    private static FileUtils deathParticipantsFile;

    private static final LogManager logger = LogManager.getLogger().setLogger("de.einfachesache");

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {

        configFile =  new FileUtils(FlareonEvents.class.getResourceAsStream("/config.yml"), "plugins/FlareonEvents", "config.yml");
        locationsFile =  new FileUtils(FlareonEvents.class.getResourceAsStream("/locations.yml"), "plugins/FlareonEvents", "locations.yml");
        participantsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/participants.yml"), "plugins/FlareonEvents", "participants.yml");
        deathParticipantsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/deathParticipants.yml"), "plugins/FlareonEvents", "deathParticipants.yml");

        Config.loadFiles();

        Objects.requireNonNull(this.getCommand("event")).setExecutor(new EventCommand());
        Objects.requireNonNull(this.getCommand("customitem")).setExecutor(new CustomItemCommand());
        Objects.requireNonNull(this.getCommand("recipegui")).setExecutor(new RecipeGuiCommand());

        Bukkit.getPluginManager().registerEvents(new RecipeGuiCommand(), this);

        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PortalCreateListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomItemHandler(), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), this);

        Bukkit.getScheduler().runTaskTimer(this, PassiveEffects::applyPassiveEffects, 20L, 20L);
        Bukkit.getPluginManager().registerEvents(new PoseidonsTrident(), this);
        Bukkit.getPluginManager().registerEvents(new ReinforcedPickaxe(), this);
        Bukkit.getPluginManager().registerEvents(new FireSword(), this);
        Bukkit.getPluginManager().registerEvents(new NyxBow(), this);

        Recipe.loadRecipes();
        Bukkit.getOnlinePlayers().forEach(Recipe::discoverRecipe);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static LogManager getLogManager() {
        return logger;
    }

    public static FileUtils getDeathParticipantsFile() {
        return deathParticipantsFile;
    }

    public static FileUtils getParticipantsFile() {
        return participantsFile;
    }

    public static FileUtils getLocationsFile() {
        return locationsFile;
    }

    public static FileUtils getFileConfig() {
        return configFile;
    }

    public static FlareonEvents getPlugin() {
        return plugin;
    }
}
