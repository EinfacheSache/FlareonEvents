package de.einfachesache.flareonevents.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        sender.sendMessage(Component.text("━━━━━━━━━━━━━━━ Befehle Übersicht ━━━━━━━━━━━━━━━", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/help", NamedTextColor.GREEN)
                .append(Component.text(" - Zeigt diese Übersicht aller Befehle an", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/team <Args>", NamedTextColor.GREEN)
                .append(Component.text(" - Team: einladen, annehmen, verlassen, kicken, list", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/recipe", NamedTextColor.GREEN)
                .append(Component.text(" - Öffnet das Rezept GUI-Menü", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/msg <Spieler> <Nachricht>", NamedTextColor.GREEN)
                .append(Component.text(" - Sendet eine private Nachricht", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/bugreport <Type> <Beschreibung>", NamedTextColor.GREEN)
                .append(Component.text(" - Erstelle einen Bugreport", NamedTextColor.GRAY)));
        return true;
    }
}