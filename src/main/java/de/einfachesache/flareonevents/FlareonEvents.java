package de.einfachesache.flareonevents;


import de.einfachesache.api.logger.LogManager;
import de.einfachesache.api.util.FileUtils;
import de.einfachesache.flareonevents.command.*;
import de.einfachesache.flareonevents.handler.ScoreboardHandler;
import de.einfachesache.flareonevents.handler.SpectatorHandler;
import de.einfachesache.flareonevents.handler.TexturepackHandler;
import de.einfachesache.flareonevents.item.ItemRecipe;
import de.einfachesache.flareonevents.item.PassiveItemEffects;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsBoots;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsChestplate;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsHelmet;
import de.einfachesache.flareonevents.item.armor.assassins.AssassinsLeggings;
import de.einfachesache.flareonevents.item.misc.EventInfoBook;
import de.einfachesache.flareonevents.item.misc.SoulHeartCrystal;
import de.einfachesache.flareonevents.item.tool.ReinforcedPickaxe;
import de.einfachesache.flareonevents.item.tool.SuperiorPickaxe;
import de.einfachesache.flareonevents.item.weapon.FireSword;
import de.einfachesache.flareonevents.item.weapon.NyxBow;
import de.einfachesache.flareonevents.item.weapon.PoseidonsTrident;
import de.einfachesache.flareonevents.item.weapon.SoulEaterScythe;
import de.einfachesache.flareonevents.listener.*;
import de.einfachesache.flareonevents.util.WorldUtils;
import de.einfachesache.flareonevents.voicechat.VoiceModPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
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

    public static final String CH_TICKET = "proxymanager:ticket";
    public static final UUID DEV_UUID = UUID.fromString("201e5046-24df-4830-8b4a-82b635eb7cc7");
    public static final UUID[] ORGA_UUID = new UUID[]{
            UUID.fromString("66d27373-3e65-4549-bbf2-39e1d5dc8631"),
            UUID.fromString("47818d7b-5d4a-4077-8bd9-99bcc4105364")
    };
    public static final Component PLUGIN_PREFIX = Component.text("[FLAREON] ", NamedTextColor.GOLD);

    private static boolean voiceChatEnabled;

    @Override
    public void onLoad() {
        plugin = this;
        voiceChatEnabled = getServer().getPluginManager().getPlugin("voicechat") != null;
    }

    @Override
    public void onEnable() {
        TexturepackHandler.init();
        registerPluginChannels();
        initializeFiles();

        setupEvent();

        registerCommands();
        registerListener();
    }

    @Override
    public void onDisable() {
        var msgr = getServer().getMessenger();
        msgr.unregisterOutgoingPluginChannel(this);
    }


    private void registerPluginChannels() {
        var msgr = getServer().getMessenger();
        msgr.registerOutgoingPluginChannel(this, CH_TICKET);
    }

    private void initializeFiles() {
        itemsFile = new FileUtils(FlareonEvents.class.getResource("/items.yml"), "plugins/FlareonEvents", "items.yml");
        teamsFile = new FileUtils(FlareonEvents.class.getResource("/teams.yml"), "plugins/FlareonEvents", "teams.yml");
        configFile = new FileUtils(FlareonEvents.class.getResource("/config.yml"), "plugins/FlareonEvents", "config.yml");
        infoBookFile = new FileUtils(FlareonEvents.class.getResource("/infoBook.yml"), "plugins/FlareonEvents", "infoBook.yml");
        locationsFile = new FileUtils(FlareonEvents.class.getResource("/locations.yml"), "plugins/FlareonEvents", "locations.yml");
        participantsFile = new FileUtils(FlareonEvents.class.getResource("/participants.yml"), "plugins/FlareonEvents", "participants.yml");
        deathParticipantsFile = new FileUtils(FlareonEvents.class.getResource("/deathParticipants.yml"), "plugins/FlareonEvents", "deathParticipants.yml");

        Config.loadFiles();
    }

    private void registerCommands() {
        registerCommand("help", new HelpCommand());
        registerCommand("recipe", new RecipeGuiCommand());
        registerCommand("customitem", new CustomItemCommand());
        registerCommand("team", new TeamCommand());
        registerCommand("event", new EventCommand());
        registerCommand("update", new UpdateCommand());
        registerCommand("report", new BugReportCommand(this));
    }

    private void registerListener() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerInteractListener(), this);
        pm.registerEvents(new EntityDamageListener(), this);
        pm.registerEvents(new PortalCreateListener(), this);
        pm.registerEvents(new ResourcePackListener(), this);
        pm.registerEvents(new PlayerDeathListener(), this);
        pm.registerEvents(new BlockUpdateListener(), this);
        pm.registerEvents(new PlayerLoginListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new PlayerMoveListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new PlayerChatListener(), this);
        pm.registerEvents(new PlayerFoodListener(), this);
        pm.registerEvents(new CustomItemHandler(), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new CommandListener(), this);
        pm.registerEvents(new AnvilListener(), this);

        pm.registerEvents(new PoseidonsTrident(), this);
        pm.registerEvents(new SoulEaterScythe(), this);
        pm.registerEvents(new FireSword(), this);
        pm.registerEvents(new NyxBow(), this);

        pm.registerEvents(new ReinforcedPickaxe(), this);
        pm.registerEvents(new SuperiorPickaxe(), this);

        pm.registerEvents(new AssassinsHelmet(), this);
        pm.registerEvents(new AssassinsChestplate(), this);
        pm.registerEvents(new AssassinsLeggings(), this);
        pm.registerEvents(new AssassinsBoots(), this);

        pm.registerEvents(new ScoreboardHandler(), this);
        pm.registerEvents(new SpectatorHandler(), this);
        pm.registerEvents(new SoulHeartCrystal(), this);
        pm.registerEvents(new EventInfoBook(), this);

        if (isVoiceChatEnabled()) {
            VoiceModPlugin.registerVoiceChatListener(this);
        }
    }

    private <T> void registerCommand(String name, T handler) {
        var cmd = Objects.requireNonNull(getCommand(name), "Missing command: " + name);

        if (handler instanceof CommandExecutor exec) {
            cmd.setExecutor(exec);
        }
        if (handler instanceof TabCompleter tab) {
            cmd.setTabCompleter(tab);
        }
        if (handler instanceof Listener listener) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }


    private void setupEvent() {
        ItemRecipe.loadRecipes();
        PassiveItemEffects.applyPassiveEffects();
        Bukkit.getOnlinePlayers().forEach(player -> {
            ScoreboardHandler.addScoreboardToPlayer(player);
            if (!Config.isEventIsRunning()) {
                ItemRecipe.discoverRecipe(player);
                player.getInventory().setItem(8, EventInfoBook.create());
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

    public static FileUtils getConfigFile() {
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
