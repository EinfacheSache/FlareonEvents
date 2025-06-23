package de.einfachesache.flareonEvents;

import de.einfachesache.flareonEvents.command.EventCommand;
import de.einfachesache.flareonEvents.listener.PlayerDeathListener;
import de.einfachesache.flareonEvents.listener.PlayerJoinListener;
import de.einfachesache.flareonEvents.listener.PlayerQuitListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class FlareonEvents extends JavaPlugin {

    static FlareonEvents plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        Objects.requireNonNull(this.getCommand("event")).setExecutor(new EventCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static FlareonEvents getPlugin() {
        return plugin;
    }
}
