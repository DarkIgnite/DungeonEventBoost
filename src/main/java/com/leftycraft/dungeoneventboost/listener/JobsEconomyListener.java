package com.leftycraft.dungeoneventboost.listener;

import com.gamingmesh.jobs.api.JobsPrePaymentEvent;
import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import io.lumine.mythic.bukkit.events.MythicMobLootDropEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class JobsEconomyListener implements Listener {

    private final DungeonEventBoost plugin;
    private final ConfigManager config;
    private final EventManager eventManager;

    public JobsEconomyListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onJobsPayment(JobsPrePaymentEvent event) {
        if (!config.isEconomyBoostEnabled()) {
            return;
        }

        OfflinePlayer op = event.getPlayer();
        Player player = op != null ? op.getPlayer() : null;
        if (!eventManager.isBoostActive(player, EventType.ECONOMY)) {
            return;
        }

        double multiplier = eventManager.getEffectiveMultiplier(player, EventType.ECONOMY, config.getEconomyMultiplier());
        if (multiplier <= 1.0) {
            return;
        }

        event.setAmount(event.getAmount() * multiplier);
        if (config.isEconomyDoublePoints()) {
            event.setPoints(event.getPoints() * multiplier);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMythicMobMoney(MythicMobLootDropEvent event) {
        if (!config.isEconomyBoostEnabled()) {
            return;
        }

        Player player = event.getKiller() instanceof Player p ? p : null;
        if (!eventManager.isBoostActive(player, EventType.ECONOMY)) {
            return;
        }

        double multiplier = eventManager.getEffectiveMultiplier(player, EventType.ECONOMY, config.getEconomyMultiplier());
        if (multiplier <= 1.0) {
            return;
        }

        if (event.getMoney() > 0) {
            event.setMoney((int) Math.round(event.getMoney() * multiplier));
        }
    }
}
