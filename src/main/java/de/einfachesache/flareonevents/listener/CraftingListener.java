package de.einfachesache.flareonevents.listener;

import de.einfachesache.flareonevents.item.tool.BetterReinforcedPickaxe;
import de.einfachesache.flareonevents.item.tool.FireSword;
import de.einfachesache.flareonevents.item.tool.NyxBow;
import de.einfachesache.flareonevents.item.tool.PoseidonsTrident;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class CraftingListener implements Listener {

    private static final Set<UUID> playerCraftedCustomWeapon = new HashSet<>();
    private static final Set<NamespacedKey> craftedOnce = new HashSet<>();

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        HumanEntity player = event.getView().getPlayer();
        ItemStack result = inv.getResult();

        if (result == null) return;

        if (result.getType() == Material.MACE) {
            inv.setResult(null);
            player.sendMessage("§4Dieses Rezept ist gebannt du Hurensohn!");
        }
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack result = event.getRecipe().getResult();
        Player player = (Player) event.getView().getPlayer();


        if (inv.getResult() != null && inv.getResult().getType() == Material.BARRIER) {
            event.setResult(Event.Result.DENY);
            return;
        }

        if (isCustomItem(result)) {
            if (event.isShiftClick()) {
                event.setResult(Event.Result.DENY);
                event.getWhoClicked().sendMessage("§cDu kannst dieses Item nur einmal craften!");
                return;
            }
            if (playerCraftedCustomWeapon.contains(player.getUniqueId())) {
                event.setResult(Event.Result.DENY);
                event.getWhoClicked().sendMessage("§cJeder Spieler kann nur eine §6§lCustom Weapon §ccraften!");
                return;
            }
        }

        NamespacedKey key;

        if (FireSword.isFireSwordItem(inv.getResult())) {
            key = FireSword.getFireSwordRecipe().getKey();
        } else if (NyxBow.isNyxBowItem(inv.getResult())) {
            key = NyxBow.getNyxBowRecipe().getKey();
        } else if (PoseidonsTrident.isPoseidonsTridentItem(inv.getResult())) {
            key = PoseidonsTrident.getPoseidonsTridentRecipe().getKey();
        } else if (BetterReinforcedPickaxe.isBetterReinforcedPickaxeItem(inv.getResult())) {
            key = BetterReinforcedPickaxe.getBetterReinforcedPickaxeRecipe().getKey();
        } else {
            key = null;
        }

        if (key == null) {
            return;
        }

        if (key != BetterReinforcedPickaxe.getBetterReinforcedPickaxeRecipe().getKey()) {
            if (craftedOnce.contains(key)) {
                event.setResult(Event.Result.DENY);
                player.sendMessage("§cDieses Item wurde bereits gecraftet und kann nur einmal hergestellt werden.");
                return;
            }

            craftedOnce.add(key);
        }

        craftedItem(player, key, inv.getResult().getItemMeta().getDisplayName());
    }

    @EventHandler
    public void onAutoCraft(CrafterCraftEvent event) {
        ItemStack result = event.getResult();
        if (isCustomItem(result)) {
            event.setCancelled(true);
        }
    }

    private boolean isCustomItem(ItemStack item) {
        return FireSword.isFireSwordItem(item)
                || PoseidonsTrident.isPoseidonsTridentItem(item)
                || NyxBow.isNyxBowItem(item)
                || BetterReinforcedPickaxe.isBetterReinforcedPickaxeItem(item);
    }

    private void craftedItem(Player player, NamespacedKey itemKey, String itemName) {
        playerCraftedCustomWeapon.add(player.getUniqueId());
        Bukkit.getOnlinePlayers().forEach(p -> p.undiscoverRecipe(itemKey)); //überprüfen!!!
        Bukkit.removeRecipe(itemKey, true);
        Bukkit.broadcast(Component.text(itemName + "§c wurde von dem Spieler §b" + player.getName() + "§c gecrafted! §7(Dieses Item kann nur einmal gecraftet werden)"));
        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(
                player.getLocation(),
                Sound.ENTITY_WITHER_SPAWN,
                SoundCategory.MASTER,
                1.0f,
                1.0f
        ));
    }
}
