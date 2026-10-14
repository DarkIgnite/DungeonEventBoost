package com.leftycraft.dungeoneventboost.placeholder;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import com.leftycraft.dungeoneventboost.util.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EventBoostExpansion extends PlaceholderExpansion {

    private final DungeonEventBoost plugin;
    private final EventManager eventManager;
    private final ConfigManager configManager;

    public EventBoostExpansion(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.eventManager = plugin.getEventManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "eventboost";
    }

    @Override
    public @NotNull String getAuthor() {
        return "DarkIgnite";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        switch (params.toLowerCase()) {
            case "status":
                return eventManager.isEventActive() ? "&aAktif" : "&cTidak Aktif";
            case "status_raw":
                return eventManager.isEventActive() ? "AKTIF" : "TIDAK_AKTIF";
            case "is_active":
                return String.valueOf(eventManager.isEventActive());
            case "event_type":
                return eventManager.getActiveEventType() != null ? eventManager.getActiveEventType().name() : "NONE";
            case "event_name":
                return eventManager.getActiveEventType() != null
                        ? configManager.getDisplayInfo(eventManager.getActiveEventType()).getName()
                        : "-";
            case "time_left":
                return eventManager.isEventActive()
                        ? TextUtil.formatDigitalClock(eventManager.getRemainingSeconds())
                        : "-";
            case "time_left_formatted":
                return eventManager.isEventActive()
                        ? TextUtil.formatDuration(eventManager.getRemainingSeconds())
                        : "-";
            case "time_until_next":
                return !eventManager.isEventActive()
                        ? TextUtil.formatDigitalClock(eventManager.getSecondsUntilNextEvent())
                        : "-";
            case "time_until_next_formatted":
                return !eventManager.isEventActive()
                        ? TextUtil.formatDuration(eventManager.getSecondsUntilNextEvent())
                        : "-";
            case "drop_multiplier":
                return String.valueOf(configManager.getDropMultiplier());
            case "exp_multiplier":
                return String.valueOf(configManager.getExpMultiplier());
            case "damage_multiplier":
                return String.valueOf(configManager.getDamageMultiplier());
            case "defense_multiplier":
                return String.valueOf(configManager.getDefenseMultiplier());
            case "economy_multiplier":
                return String.valueOf(configManager.getEconomyMultiplier());
            case "koki_multiplier":
                return String.valueOf(eventManager.getKokiMbgMultiplier());
            case "koki_multiplier_int":
                return String.valueOf((int) eventManager.getKokiMbgMultiplier());
            case "vanilla_exp_multiplier":
                return String.valueOf(configManager.getVanillaExpMultiplier());
            case "interval":
                return String.valueOf(configManager.getIntervalHours());
            case "duration":
                return String.valueOf(configManager.getDurationMinutes());
            default:
                return null;
        }
    }
}
