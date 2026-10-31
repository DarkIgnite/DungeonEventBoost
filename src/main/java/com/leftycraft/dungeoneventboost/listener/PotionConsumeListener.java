package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.potion.BoostItemManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PotionConsumeListener implements Listener {

    private final DungeonEventBoost plugin;
    private final BoostItemManager boostItemManager;

    public PotionConsumeListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.boostItemManager = plugin.getBoostItemManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (!boostItemManager.isBoostPotion(item)) {
            return;
        }

        if (plugin.getConfigManager().isPotionInstantConsume()) {
            event.setCancelled(true);
            EquipmentSlot slot = event.getHand() != null ? event.getHand() : EquipmentSlot.HAND;
            boostItemManager.consumeBoost(event.getPlayer(), item, slot);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (plugin.getConfigManager().isPotionInstantConsume()) {
            return;
        }

        ItemStack item = event.getItem();
        if (boostItemManager.isBoostPotion(item)) {
            boostItemManager.consumeBoost(event.getPlayer(), item, EquipmentSlot.HAND);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBrewingStandClick(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.BREWING) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (boostItemManager.isBoostPotion(current) || boostItemManager.isBoostPotion(cursor)) {
                event.setCancelled(true);
            }
        }
    }
}
