package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class VanillaExpListener implements Listener {

    private final DungeonEventBoost plugin;
    private final ConfigManager config;
    private final EventManager eventManager;

    public VanillaExpListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        if (!config.isVanillaExpBoostEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (!eventManager.isBoostActive(player, EventType.EXP_VANILLA)) {
            return;
        }

        World world = player.getWorld();
        if (config.isWorldWhitelistEnabled() && !config.getAllowedWorlds().contains(world.getName().toLowerCase())) {
            return;
        }

        double multiplier = eventManager.getEffectiveMultiplier(player, EventType.EXP_VANILLA, config.getVanillaExpMultiplier());
        if (multiplier <= 1.0) {
            return;
        }

        int original = event.getAmount();
        if (original > 0) {
            event.setAmount((int) Math.round(original * multiplier));
        }
    }
}
