package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MythicMobsDropListener implements Listener {

    private final DungeonEventBoost plugin;
    private final ConfigManager config;
    private final EventManager eventManager;

    public MythicMobsDropListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!config.isDropBoostEnabled()) {
            return;
        }

        Player killer = event.getKiller() instanceof Player p ? p : null;
        if (!eventManager.isBoostActive(killer, EventType.DROPS)) {
            return;
        }

        ActiveMob activeMob = event.getMob();
        if (activeMob == null) {
            return;
        }

        // Check player killer requirement
        if (config.isDropRequirePlayerKiller() && killer == null) {
            return;
        }

        // Check world
        World world = event.getEntity() != null ? event.getEntity().getWorld() : null;
        if (world != null && config.isWorldWhitelistEnabled()) {
            if (!config.getAllowedWorlds().contains(world.getName().toLowerCase())) {
                return;
            }
        }

        // Check if mob matches dungeon criteria
        if (!isDungeonMob(activeMob)) {
            return;
        }

        double multiplier = eventManager.getEffectiveMultiplier(killer, EventType.DROPS, config.getDropMultiplier());
        if (multiplier <= 1.0) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        if (drops == null || drops.isEmpty()) {
            return;
        }

        // Duplicate or multiply item amounts
        List<ItemStack> extraDrops = new ArrayList<>();
        for (ItemStack item : drops) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            int originalAmount = item.getAmount();
            int totalNewAmount = (int) Math.round(originalAmount * multiplier);
            int additionalAmount = totalNewAmount - originalAmount;
            if (additionalAmount <= 0) {
                continue;
            }

            int maxStack = item.getMaxStackSize();
            if (originalAmount + additionalAmount <= maxStack) {
                item.setAmount(originalAmount + additionalAmount);
            } else {
                item.setAmount(maxStack);
                int remaining = (originalAmount + additionalAmount) - maxStack;
                while (remaining > 0) {
                    int stackAmount = Math.min(remaining, maxStack);
                    ItemStack clone = item.clone();
                    clone.setAmount(stackAmount);
                    extraDrops.add(clone);
                    remaining -= stackAmount;
                }
            }
        }

        if (!extraDrops.isEmpty()) {
            drops.addAll(extraDrops);
        }
    }

    private boolean isDungeonMob(ActiveMob activeMob) {
        if (config.isDropAllMythicMobs()) {
            return true;
        }

        // Filter by faction
        if (config.isDropFilterByFaction()) {
            if (activeMob.hasFaction()) {
                String mobFaction = activeMob.getFaction().toLowerCase();
                if (config.getDropAllowedFactions().contains(mobFaction)) {
                    return true;
                }
            }
        }

        // Filter by mob type name
        if (config.isDropFilterByMobTypes()) {
            String mobType = activeMob.getMobType() != null ? activeMob.getMobType().toLowerCase() : "";
            if (config.getDropAllowedMobTypes().contains(mobType)) {
                return true;
            }
        }

        return false;
    }
}
