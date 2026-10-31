package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.event.EventManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final EventManager eventManager;

    public PlayerConnectionListener(DungeonEventBoost plugin) {
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        eventManager.onPlayerJoin(event.getPlayer());
        if (eventManager.getPlayerBoostManager() != null) {
            eventManager.getPlayerBoostManager().onPlayerJoin(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        eventManager.onPlayerQuit(event.getPlayer());
        if (eventManager.getPlayerBoostManager() != null) {
            eventManager.getPlayerBoostManager().onPlayerQuit(event.getPlayer());
        }
    }
}
