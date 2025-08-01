package de.einfachesache.flareonevents;

import de.cubeattack.api.logger.LogManager;
import de.cubeattack.api.util.FileUtils;
import de.einfachesache.flareonevents.command.*;
import de.einfachesache.flareonevents.handler.ScoreboardHandler;
import de.einfachesache.flareonevents.item.ItemRecipe;
import de.einfachesache.flareonevents.item.PassiveItemEffects;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.tool.ReinforcedPickaxe;
import de.einfachesache.flareonevents.item.tool.SuperiorPickaxe;
import de.einfachesache.flareonevents.item.weapon.FireSword;
import de.einfachesache.flareonevents.item.weapon.NyxBow;
import de.einfachesache.flareonevents.item.weapon.PoseidonsTrident;
import de.einfachesache.flareonevents.listener.*;
import de.einfachesache.flareonevents.voicechat.VoiceModPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;

public final class FlareonEvents extends JavaPlugin {

    private static FlareonEvents plugin;

    private static FileUtils itemsFile;
    private static FileUtils teamsFile;
    private static FileUtils configFile;
    private static FileUtils infoBookFile;
    private static FileUtils locationsFile;
    private static FileUtils participantsFile;
    private static FileUtils deathParticipantsFile;

    private static final LogManager logger = LogManager.getLogger();

    public static final UUID DEV_UUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");
    private static boolean voiceChatEnabled;

    @Override
    public void onLoad() {
        plugin = this;
        voiceChatEnabled = getServer().getPluginManager().getPlugin("voicechat") != null;
    }

    @Override
    public void onEnable() {
        initializeFiles();

        setupEvent();

        registerCommands();
        registerListener();
    }

    private void initializeFiles() {
        itemsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/items.yml"), "plugins/FlareonEvents", "items.yml");
        teamsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/teams.yml"), "plugins/FlareonEvents", "teams.yml");
        configFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/config.yml"), "plugins/FlareonEvents", "config.yml");
        infoBookFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/infoBook.yml"), "plugins/FlareonEvents", "infoBook.yml");
        locationsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/locations.yml"), "plugins/FlareonEvents", "locations.yml");
        participantsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/participants.yml"), "plugins/FlareonEvents", "participants.yml");
        deathParticipantsFile = new FileUtils(FlareonEvents.class.getResourceAsStream("/deathParticipants.yml"), "plugins/FlareonEvents", "deathParticipants.yml");

        Config.loadFiles();
    }

    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("team")).setExecutor(new TeamCommand());
        Objects.requireNonNull(this.getCommand("team")).setTabCompleter(new TeamCommand());

        Objects.requireNonNull(this.getCommand("event")).setExecutor(new EventCommand());
        Objects.requireNonNull(this.getCommand("event")).setTabCompleter(new EventCommand());

        Objects.requireNonNull(this.getCommand("update")).setExecutor(new UpdateCommand());
        Objects.requireNonNull(this.getCommand("update")).setTabCompleter(new UpdateCommand());

        Objects.requireNonNull(this.getCommand("customitem")).setExecutor(new CustomItemCommand());
        Objects.requireNonNull(this.getCommand("recipegui")).setExecutor(new RecipeGuiCommand());
    }

    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PortalCreateListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockUpdateListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLoginListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerFoodListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomItemHandler(), this);
        Bukkit.getPluginManager().registerEvents(new CraftingListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), this);

        Bukkit.getPluginManager().registerEvents(new PoseidonsTrident(), this);
        Bukkit.getPluginManager().registerEvents(new FireSword(), this);
        Bukkit.getPluginManager().registerEvents(new NyxBow(), this);

        Bukkit.getPluginManager().registerEvents(new SuperiorPickaxe(), this);
        Bukkit.getPluginManager().registerEvents(new ReinforcedPickaxe(), this);

        Bukkit.getPluginManager().registerEvents(new ScoreboardHandler(), this);
        Bukkit.getPluginManager().registerEvents(new SoulHeartCrystal(), this);
        Bukkit.getPluginManager().registerEvents(new EventInfoBook(), this);

        Bukkit.getPluginManager().registerEvents(new RecipeGuiCommand(), this);

        if (isVoiceChatEnabled()){
            VoiceModPlugin.registerVoiceChatListener(this);
        }
    }


    private void setupEvent() {
        ItemRecipe.loadRecipes();
        Bukkit.getScheduler().runTaskTimer(this, PassiveItemEffects::applyPassiveEffects, 20L, 20L);
        Bukkit.getOnlinePlayers().forEach(player -> {
            ScoreboardHandler.addScoreboardToPlayer(player);
            if (!Config.isEventIsRunning()) {
                ItemRecipe.discoverRecipe(player);
                player.getInventory().setItem(8, EventInfoBook.createEventInfoBook());
            }
        });

        if (!Config.isEventIsRunning()) {
            Bukkit.getWorlds().forEach(world -> {

                world.setClearWeatherDuration(20 * 60 * 20);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

                if (!WorldUtils.isWorldGeneratedFresh()) return;

                world.setPVP(false);
                WorldBorder border = world.getWorldBorder();
                border.setCenter(0, 0);
                border.setSize(3000);
            });
        }
    }

    public static LogManager getLogManager() {
        return logger;
    }

    public static boolean isVoiceChatEnabled() {
        return voiceChatEnabled;
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

    public static FileUtils getTeamsFile() {
        return teamsFile;
    }

    public static FileUtils getItemsFile() {
        return itemsFile;
    }

    public static FlareonEvents getPlugin() {
        return plugin;
    }
}
