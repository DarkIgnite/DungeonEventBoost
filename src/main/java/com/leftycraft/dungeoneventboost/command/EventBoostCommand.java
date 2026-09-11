package com.leftycraft.dungeoneventboost.command;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
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

                long durationSeconds = configManager.getDurationSeconds();
                if (args.length > 1) {
                    try {
                        double minutes = Double.parseDouble(args[1]);
                        if (minutes <= 0) {
                            TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Durasi harus lebih dari 0 menit!</red>");
                            return true;
                        }
                        durationSeconds = (long) (minutes * 60);
                    } catch (NumberFormatException e) {
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Format angka durasi tidak valid!</red>");
                        return true;
                    }
                }

                eventManager.startEvent(durationSeconds);
                long minutes = Math.max(1, durationSeconds / 60);
                String startMsg = configManager.getMessageStartedManual().replace("{duration}", String.valueOf(minutes));
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

    private void showStatus(CommandSender sender) {
        if (eventManager.isEventActive()) {
            List<String> activeLines = configManager.getMessageStatusActive();
            for (String line : activeLines) {
                String replaced = line
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
            return Arrays.asList("5", "10", "15", "30", "60");
        }

        return Collections.emptyList();
    }
}
