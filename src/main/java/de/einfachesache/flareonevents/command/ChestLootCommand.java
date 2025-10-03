package de.einfachesache.flareonevents.command;

import de.einfachesache.flareonevents.FlareonEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.loot.Lootable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChestLootCommand implements CommandExecutor, TabCompleter, Listener {

    private final FlareonEvents plugin;
    private final NamespacedKey tableKey;

    public ChestLootCommand(FlareonEvents plugin) {
        this.plugin = plugin;
        this.tableKey = new NamespacedKey(plugin, "table");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Dieser Command kann nur von Spielern verwendet werden!", NamedTextColor.RED));
            return true;
        }

        if (args.length != 1) {
            sendUsage(player);
            return true;
        }

        String input = args[0];

        if (input.equalsIgnoreCase("reset")) {
            resetLootTable(player);
            return true;
        } else {
            setLootTable(player, input);
        }

        return true;
    }

    private void setLootTable(Player player, String input) {

        NamespacedKey key = NamespacedKey.fromString(input);
        if (key == null) {
            player.sendMessage("§cUngültiges LootTable-Format. Beispiel: minecraft:chests/simple_dungeon");
            return;
        }

        LootTable table = Bukkit.getLootTable(key);
        if (table == null) {
            player.sendMessage("§cLootTable '" + key + "' nicht gefunden!");
            return;
        }

        player.setMetadata("pendingLootTable", new FixedMetadataValue(plugin, key.toString()));
        player.sendMessage("§7LootTable §a" + key + "§7 vorgemerkt. Jetzt eine Kiste rechtsklicken.");
    }

    private void resetLootTable(Player player) {
        Block target = player.getTargetBlockExact(6);
        if (target == null || !(target.getBlockData() instanceof Chest)) {
            player.sendMessage("§cSchau eine Kiste an (max. 6 Blöcke).");
            return;
        }

        BlockState state = target.getState();
        if (!(state instanceof Container container) || !(state instanceof Lootable lootable)) {
            player.sendMessage("§cDie Ziel-BlockEntity unterstützt kein Loot.");
            return;
        }

        PersistentDataContainer pdc = container.getPersistentDataContainer();
        String tableString = pdc.get(tableKey, PersistentDataType.STRING);

        if (tableString == null) {
            player.sendMessage("§eKeine LootTable an dieser Kiste gespeichert.");
            return;
        }

        NamespacedKey namespacedKey = NamespacedKey.fromString(tableString);

        if (namespacedKey == null) {
            return;
        }

        LootTable table = Bukkit.getLootTable(namespacedKey);
        if (table == null) {
            player.sendMessage("§cLootTable '" + namespacedKey + "' nicht gefunden oder Datapack nicht geladen.");
            return;
        }

        lootable.setLootTable(table);
        container.update(true, false);
        player.sendMessage("§aKiste zurückgesetzt auf LootTable §f" + tableString + "§a. Loot beim nächsten Öffnen.");
    }

    private void sendUsage(Player player) {
        player.sendMessage("§c/lootchest <1-3|reset> und dann die Kiste rechtsklicken.");
    }


    @EventHandler
    public void onMark(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getBlockData() instanceof Chest)) return;

        Player p = e.getPlayer();
        if (!p.hasMetadata("pendingLootTable")) return;

        String pendingTable = p.getMetadata("pendingLootTable").getFirst().asString();

        BlockState state = e.getClickedBlock().getState();

        e.setCancelled(true);

        if (!(state instanceof Container container) || !(state instanceof Lootable lootable)) {
            p.sendMessage("§cDiese BlockEntity unterstützt kein Loot.");
            return;
        }

        PersistentDataContainer pdc = container.getPersistentDataContainer();
        pdc.set(tableKey, PersistentDataType.STRING, pendingTable);
        NamespacedKey namespacedKey = NamespacedKey.fromString(pendingTable);

        if(namespacedKey == null) {
            return;
        }

        LootTable table = Bukkit.getLootTable(namespacedKey);
        if (table == null) {
            p.sendMessage("§cLootTable '" + namespacedKey + "' nicht gefunden oder Datapack nicht geladen.");
            return;
        }

        lootable.setLootTable(table);
        container.update(true, false);

        p.removeMetadata("pendingLootTable", plugin);
        p.sendMessage("§aKiste als Tier " + pendingTable + " markiert & Vanilla-LootTable gesetzt.");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) return List.of();

        String input = args[0].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();

        if ("reset".startsWith(input))
            out.add("reset");

        for (LootTables lt : LootTables.values()) {
            String full = lt.getKey().toString();
            if (lt.getKey().getKey().startsWith("chests/")) {
                if (full.toLowerCase(Locale.ROOT).startsWith(input)) out.add(full);
            }
        }

        Collections.sort(out);
        return out;
    }
}