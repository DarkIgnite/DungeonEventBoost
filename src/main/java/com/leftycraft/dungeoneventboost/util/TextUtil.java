package com.leftycraft.dungeoneventboost.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})|<#([a-fA-F0-9]{6})>");

    private TextUtil() {}

    /**
     * Parse text supporting MiniMessage tags, legacy color codes (&a, &l), and hex codes (&#RRGGBB or <#RRGGBB>).
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Convert &#RRGGBB and <#RRGGBB> to MiniMessage format <#RRGGBB>
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            matcher.appendReplacement(sb, "<#" + hex + ">");
        }
        matcher.appendTail(sb);
        String converted = sb.toString();

        // If it contains legacy & color codes, convert them to MiniMessage compatible or deserialize
        if (converted.contains("&")) {
            // Deserialize legacy then serialize back, or parse legacy
            return LEGACY_SERIALIZER.deserialize(converted.replace('§', '&'));
        }

        try {
            return MINI_MESSAGE.deserialize(converted);
        } catch (Exception ignored) {
            return LEGACY_SERIALIZER.deserialize(converted);
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(parse(message));
        }
    }

    public static void broadcast(String message) {
        if (message != null && !message.isEmpty()) {
            Bukkit.broadcast(parse(message));
        }
    }

    /**
     * Format seconds into readable string (e.g., "5j 23m 10d" or "10m 00d")
     */
    public static String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 detik";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append(" jam ");
        }
        if (minutes > 0 || hours > 0) {
            builder.append(minutes).append(" menit ");
        }
        if (seconds > 0 || (hours == 0 && minutes == 0)) {
            builder.append(seconds).append(" detik");
        }

        return builder.toString().trim();
    }

    /**
     * Format seconds into mm:ss or hh:mm:ss
     */
    public static String formatDigitalClock(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "00:00";
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
