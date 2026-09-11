package com.leftycraft.dungeoneventboost.command;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import com.leftycraft.dungeoneventboost.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EventBoostCommand implements CommandExecutor, TabCompleter {

    private final DungeonEventBoost plugin;
    private final EventManager eventManager;
    private final ConfigManager configManager;

    public EventBoostCommand(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.eventManager = plugin.getEventManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start":
                if (!sender.hasPermission("dungeoneventboost.admin")) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageNoPermission());
                    return true;
                }

                if (eventManager.isEventActive()) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageAlreadyActive());
                    return true;
                }

                EventType targetType = null;
                long durationSeconds = configManager.getDurationSeconds();

                if (args.length > 1) {
                    EventType parsed = EventType.fromString(args[1]);
                    if (parsed != null) {
                        targetType = parsed;
                        if (args.length > 2) {
                            durationSeconds = parseDurationMinutes(sender, args[2], durationSeconds);
                            if (durationSeconds <= 0) return true;
                        }
                    } else {
                        // Maybe args[1] is duration number
                        durationSeconds = parseDurationMinutes(sender, args[1], durationSeconds);
                        if (durationSeconds <= 0) return true;
                    }
                }

                if (targetType == null) {
                    targetType = eventManager.pickNextEventType();
                }

                eventManager.startEvent(targetType, durationSeconds);
                long minutes = Math.max(1, durationSeconds / 60);
                String eventName = configManager.getDisplayInfo(targetType).getName();
                String startMsg = configManager.getMessageStartedManual()
                        .replace("{event_name}", eventName)
                        .replace("{duration}", String.valueOf(minutes));
                TextUtil.sendMessage(sender, configManager.getMessagePrefix() + startMsg);
                return true;

            case "stop":
                if (!sender.hasPermission("dungeoneventboost.admin")) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageNoPermission());
                    return true;
                }

                if (!eventManager.isEventActive()) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageNotActive());
                    return true;
                }

                eventManager.stopEvent(false);
                TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageStoppedManual());
                return true;

            case "reload":
                if (!sender.hasPermission("dungeoneventboost.admin")) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageNoPermission());
                    return true;
                }

                configManager.loadConfig();
                TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageReloaded());
                return true;

            case "help":
                for (String line : configManager.getMessageHelp()) {
                    TextUtil.sendMessage(sender, line);
                }
                return true;

            default:
                TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Perintah tidak dikenal! Ketik <yellow>/" + label + " help</yellow> untuk bantuan.</red>");
                return true;
        }
    }

    private long parseDurationMinutes(CommandSender sender, String arg, long fallback) {
        try {
            double minutes = Double.parseDouble(arg);
            if (minutes <= 0) {
                TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Durasi harus lebih dari 0 menit!</red>");
                return -1;
            }
            return (long) (minutes * 60);
        } catch (NumberFormatException e) {
            TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Format angka durasi tidak valid: " + arg + "</red>");
            return -1;
        }
    }

    private void showStatus(CommandSender sender) {
        if (eventManager.isEventActive() && eventManager.getActiveEventType() != null) {
            EventType type = eventManager.getActiveEventType();
            ConfigManager.EventDisplayInfo display = configManager.getDisplayInfo(type);
            List<String> activeLines = configManager.getMessageStatusActive();
            for (String line : activeLines) {
                String replaced = line
                        .replace("{event_name}", display.getName())
                        .replace("{event_tag}", display.getShortTag())
                        .replace("{time_left}", TextUtil.formatDuration(eventManager.getRemainingSeconds()))
                        .replace("{drop_mult}", String.valueOf(configManager.getDropMultiplier()))
                        .replace("{exp_mult}", String.valueOf(configManager.getExpMultiplier()))
                        .replace("{dmg_mult}", String.valueOf(configManager.getDamageMultiplier()));
                TextUtil.sendMessage(sender, replaced);
            }
        } else {
            List<String> inactiveLines = configManager.getMessageStatusInactive();
            for (String line : inactiveLines) {
                String replaced = line
                        .replace("{next_event_time}", TextUtil.formatDuration(eventManager.getSecondsUntilNextEvent()))
                        .replace("{interval}", String.valueOf(configManager.getIntervalHours()))
                        .replace("{duration}", String.valueOf(configManager.getDurationMinutes()));
                TextUtil.sendMessage(sender, replaced);
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            List<String> subs = sender.hasPermission("dungeoneventboost.admin")
                    ? Arrays.asList("status", "start", "stop", "reload", "help")
                    : Arrays.asList("status", "help");

            String prefix = args[0].toLowerCase();
            for (String sub : subs) {
                if (sub.startsWith(prefix)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start") && sender.hasPermission("dungeoneventboost.admin")) {
            List<String> options = new ArrayList<>(Arrays.asList("drops", "exp_skills", "damage", "defense", "economy", "exp_vanilla", "swiftness", "all", "5", "10", "15", "30"));
            List<String> completions = new ArrayList<>();
            String prefix = args[1].toLowerCase();
            for (String opt : options) {
                if (opt.startsWith(prefix)) {
                    completions.add(opt);
                }
            }
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("start") && sender.hasPermission("dungeoneventboost.admin")) {
            return Arrays.asList("5", "10", "15", "30", "60");
        }

        return Collections.emptyList();
    }
}
