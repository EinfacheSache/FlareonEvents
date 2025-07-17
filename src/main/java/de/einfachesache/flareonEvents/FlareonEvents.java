package de.einfachesache.flareonEvents;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.FileUtils;
import de.einfachesache.flareonEvents.command.CustomItemCommand;
import de.einfachesache.flareonEvents.command.EventCommand;
import de.einfachesache.flareonEvents.command.RecipeGuiCommand;
import de.einfachesache.flareonEvents.command.UpdateCommand;
import de.einfachesache.flareonEvents.item.EventInfoBook;
import de.einfachesache.flareonEvents.item.PassiveEffects;
import de.einfachesache.flareonEvents.item.Recipe;
import de.einfachesache.flareonEvents.item.tool.*;
import de.einfachesache.flareonEvents.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public final class FlareonEvents extends JavaPlugin {

    private static FlareonEvents plugin;

    private static FileUtils itemsFile;
    private static FileUtils configFile;
    private static FileUtils infoBookFile;
    private static FileUtils locationsFile;
    private static FileUtils participantsFile;
    private static FileUtils deathParticipantsFile;

    private static final Random random = new Random();
    private static final LogManager logger = LogManager.getLogger();

    public static final UUID ROOT_UUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        loadFiles();

        registerCommands();

        registerListener();

        setupEvent();
    }

    private void loadFiles() {
        itemsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/items.yml"), "plugins/FlareonEvents", "items.yml");
        configFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/config.yml"), "plugins/FlareonEvents", "config.yml");
        infoBookFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/infoBook.yml"), "plugins/FlareonEvents", "infoBook.yml");
        locationsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/locations.yml"), "plugins/FlareonEvents", "locations.yml");
        participantsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/participants.yml"), "plugins/FlareonEvents", "participants.yml");
        deathParticipantsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/deathParticipants.yml"), "plugins/FlareonEvents", "deathParticipants.yml");

        Config.loadFiles();
    }

    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("event")).setExecutor(new EventCommand());
        Objects.requireNonNull(this.getCommand("event")).setTabCompleter(new EventCommand());

        Objects.requireNonNull(this.getCommand("update")).setExecutor(new UpdateCommand());
        Objects.requireNonNull(this.getCommand("update")).setTabCompleter(new UpdateCommand());

        Objects.requireNonNull(this.getCommand("customitem")).setExecutor(new CustomItemCommand());
        Objects.requireNonNull(this.getCommand("recipegui")).setExecutor(new RecipeGuiCommand());
    }

    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(new RecipeGuiCommand(), this);

        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PortalCreateListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockUpdateListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerFoodListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomItemHandler(), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), this);

        Bukkit.getPluginManager().registerEvents(new BetterReinforcedPickaxe(), this);
        Bukkit.getPluginManager().registerEvents(new ReinforcedPickaxe(), this);

        Bukkit.getPluginManager().registerEvents(new PoseidonsTrident(), this);
        Bukkit.getPluginManager().registerEvents(new FireSword(), this);
        Bukkit.getPluginManager().registerEvents(new NyxBow(), this);

        Bukkit.getPluginManager().registerEvents(new EventScoreboard(), this);
        Bukkit.getPluginManager().registerEvents(new EventInfoBook(), this);
    }

    private void setupEvent() {
        Recipe.loadRecipes();
        Bukkit.getScheduler().runTaskTimer(this, PassiveEffects::applyPassiveEffects, 20L, 20L);
        Bukkit.getOnlinePlayers().forEach(player -> {
            EventScoreboard.addScoreboardToPlayer(player);
            if (Config.getEventState().getId() < 3) {
                Recipe.discoverRecipe(player);
                player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
            }
        });

        if (Config.getEventState().getId() < 3) {
            World world = Bukkit.getWorlds().getFirst();
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0, 0);
            border.setSize(3000);
            world.setTime(6000);
            world.setClearWeatherDuration(20 * 60 * 20);
        }
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

    public static FileUtils getInfoBookFile() {
        return infoBookFile;
    }

    public static FileUtils getFileConfig() {
        return configFile;
    }

    public static FileUtils getItemsFile() {
        return itemsFile;
    }

    public static FlareonEvents getPlugin() {
        return plugin;
    }

    public static Random getRandom() {
        return random;
    }
}
