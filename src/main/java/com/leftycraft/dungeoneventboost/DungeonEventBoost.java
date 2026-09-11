package com.leftycraft.dungeoneventboost;

import com.leftycraft.dungeoneventboost.command.EventBoostCommand;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.listener.AuraSkillsExpListener;
import com.leftycraft.dungeoneventboost.listener.DungeonDamageListener;
import com.leftycraft.dungeoneventboost.listener.MythicMobsDropListener;
import com.leftycraft.dungeoneventboost.listener.PlayerConnectionListener;
import com.leftycraft.dungeoneventboost.placeholder.EventBoostExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class DungeonEventBoost extends JavaPlugin {

    private static DungeonEventBoost instance;

    private ConfigManager configManager;
    private EventManager eventManager;
    private BukkitTask tickTask;

    @Override
    public void onEnable() {
        instance = this;

        // Load Configuration
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        // Initialize Event Manager
        this.eventManager = new EventManager(this);
        this.eventManager.initialize();

        // Register Listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(this), this);

        // MythicMobs Hook
        if (pm.isPluginEnabled("MythicMobs")) {
            pm.registerEvents(new MythicMobsDropListener(this), this);
            pm.registerEvents(new DungeonDamageListener(this), this);
            getLogger().info("Berhasil terhubung dengan MythicMobs (Drop & Damage Boost aktif).");
        } else {
            getLogger().warning("MythicMobs tidak ditemukan! Fitur MythicMobs boost tidak akan berjalan.");
        }

        // AuraSkills Hook
        if (pm.isPluginEnabled("AuraSkills")) {
            pm.registerEvents(new AuraSkillsExpListener(this), this);
            getLogger().info("Berhasil terhubung dengan AuraSkills (2x EXP Boost aktif).");
        } else {
            getLogger().warning("AuraSkills tidak ditemukan! Fitur AuraSkills EXP boost tidak akan berjalan.");
        }

        // PlaceholderAPI Hook
        if (pm.isPluginEnabled("PlaceholderAPI")) {
            new EventBoostExpansion(this).register();
            getLogger().info("Berhasil mendaftarkan PlaceholderAPI expansion (%eventboost_...%).");
        }

        // Register Command
        PluginCommand cmd = getCommand("dungeoneventboost");
        if (cmd != null) {
            EventBoostCommand commandExecutor = new EventBoostCommand(this);
            cmd.setExecutor(commandExecutor);
            cmd.setTabCompleter(commandExecutor);
        }

        // Start 1-second Tick Loop
        this.tickTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            eventManager.tick();
        }, 20L, 20L);

        getLogger().info("=========================================");
        getLogger().info("  DungeonEventBoost v" + getDescription().getVersion() + " telah aktif!");
        getLogger().info("  Interval: " + configManager.getIntervalHours() + " Jam");
        getLogger().info("  Durasi: " + configManager.getDurationMinutes() + " Menit");
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
        }

        if (eventManager != null) {
            eventManager.removeBossBar();
            eventManager.saveState();
        }

        getLogger().info("DungeonEventBoost telah dimatikan dan state berhasil disimpan.");
    }

    public static DungeonEventBoost getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}
