package com.leftycraft.dungeoneventboost.placeholder;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import com.leftycraft.dungeoneventboost.potion.PlayerBoost;
import com.leftycraft.dungeoneventboost.potion.PlayerBoostManager;
import com.leftycraft.dungeoneventboost.util.TextUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String lower = params.toLowerCase();
        Player player = offlinePlayer != null && offlinePlayer.isOnline() ? offlinePlayer.getPlayer() : null;
        PlayerBoostManager pbm = eventManager.getPlayerBoostManager();

        // Personal boost prefix checks
        if (lower.startsWith("personal_")) {
            if (offlinePlayer == null || pbm == null) {
                return "-";
            }

            if (lower.equals("personal_has_boost")) {
                Map<EventType, PlayerBoost> boosts = pbm.getActiveBoosts(offlinePlayer.getUniqueId());
                return String.valueOf(!boosts.isEmpty());
            }

            if (lower.equals("personal_boosts")) {
                Map<EventType, PlayerBoost> boosts = pbm.getActiveBoosts(offlinePlayer.getUniqueId());
                if (boosts.isEmpty()) return "-";
                List<String> list = new ArrayList<>();
                for (Map.Entry<EventType, PlayerBoost> entry : boosts.entrySet()) {
                    String name = configManager.getDisplayInfo(entry.getKey()).getName();
                    String time = TextUtil.formatDigitalClock(entry.getValue().getRemainingSeconds());
                    list.add(name + " (" + time + ")");
                }
                return String.join(", ", list);
            }

            if (lower.startsWith("personal_time_left_formatted_")) {
                String typeStr = lower.substring("personal_time_left_formatted_".length());
                EventType type = EventType.fromString(typeStr);
                if (type == null) return "-";
                long sec = pbm.getRemainingSeconds(offlinePlayer.getUniqueId(), type);
                return sec > 0 ? TextUtil.formatDuration(sec) : "-";
            }

            if (lower.startsWith("personal_time_left_")) {
                String typeStr = lower.substring("personal_time_left_".length());
                EventType type = EventType.fromString(typeStr);
                if (type == null) return "-";
                long sec = pbm.getRemainingSeconds(offlinePlayer.getUniqueId(), type);
                return sec > 0 ? TextUtil.formatDigitalClock(sec) : "-";
            }

            if (lower.startsWith("personal_active_")) {
                String typeStr = lower.substring("personal_active_".length());
                EventType type = EventType.fromString(typeStr);
                if (type == null) return "false";
                return String.valueOf(pbm.hasActiveBoost(offlinePlayer.getUniqueId(), type));
            }
        }

        switch (lower) {
            case "has_any_boost":
                boolean any = eventManager.isEventActive() || (offlinePlayer != null && pbm != null && !pbm.getActiveBoosts(offlinePlayer.getUniqueId()).isEmpty());
                return String.valueOf(any);
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
                return String.valueOf(eventManager.getEffectiveMultiplier(player, EventType.DROPS, configManager.getDropMultiplier()));
            case "exp_multiplier":
                return String.valueOf(eventManager.getEffectiveMultiplier(player, EventType.EXP_SKILLS, configManager.getExpMultiplier()));
            case "damage_multiplier":
                return String.valueOf(eventManager.getEffectiveMultiplier(player, EventType.DAMAGE_DEALT, configManager.getDamageMultiplier()));
            case "defense_multiplier":
                return String.valueOf(eventManager.getEffectiveMultiplier(player, EventType.DEFENSE, configManager.getDefenseMultiplier()));
            case "economy_multiplier":
                return String.valueOf(eventManager.getEffectiveMultiplier(player, EventType.ECONOMY, configManager.getEconomyMultiplier()));
            case "koki_multiplier":
                return String.valueOf(eventManager.getKokiMbgMultiplier(player));
            case "koki_multiplier_int":
                return String.valueOf((int) eventManager.getKokiMbgMultiplier(player));
            case "vanilla_exp_multiplier":
                return String.valueOf(eventManager.getEffectiveMultiplier(player, EventType.EXP_VANILLA, configManager.getVanillaExpMultiplier()));
            case "interval":
                return String.valueOf(configManager.getIntervalHours());
            case "duration":
                return String.valueOf(configManager.getDurationMinutes());
            default:
                return null;
        }
    }
}
