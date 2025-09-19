package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherChangeListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (Config.isEventIsRunning()) return;
        if (event.getCause() == WeatherChangeEvent.Cause.COMMAND || event.getCause() == WeatherChangeEvent.Cause.PLUGIN) return;

        event.setCancelled(true);
    }
}
