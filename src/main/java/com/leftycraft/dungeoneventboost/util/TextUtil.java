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
    private static final Pattern CENTER_PATTERN = Pattern.compile("(?i)<center>(.*?)</center>");
    private static final Pattern STRIP_TAGS_PATTERN = Pattern.compile("<[^>]+>|&[0-9a-fk-orA-FK-OR]|§[0-9a-fk-orA-FK-OR]");

    private static final int CHAT_CENTER_PX = 154; // Half of standard MC chat width (320px) with margins
    private static final int SPACE_PX = 4;

    private TextUtil() {}

    /**
     * Center text matching <center>...</center> using Minecraft font character widths.
     */
    public static String centerText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = CENTER_PATTERN.matcher(text);
        if (!matcher.find()) {
            // Also clean up any unclosed/rogue <center> or </center> tags
            return text.replaceAll("(?i)</?center>", "");
        }

        StringBuilder sb = new StringBuilder();
        matcher.reset();
        while (matcher.find()) {
            String content = matcher.group(1);
            String plain = STRIP_TAGS_PATTERN.matcher(content).replaceAll("");

            int textPx = 0;
            boolean bold = content.toLowerCase().contains("<bold>") || content.toLowerCase().contains("<b>") || content.contains("&l");
            for (char c : plain.toCharArray()) {
                textPx += getCharWidth(c, bold);
            }

            int halfDiff = CHAT_CENTER_PX - (textPx / 2);
            if (halfDiff <= 0) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(content));
            } else {
                int spaces = halfDiff / SPACE_PX;
                StringBuilder padding = new StringBuilder();
                for (int i = 0; i < spaces; i++) {
                    padding.append(" ");
                }
                matcher.appendReplacement(sb, Matcher.quoteReplacement(padding + content));
            }
        }
        matcher.appendTail(sb);
        return sb.toString().replaceAll("(?i)</?center>", "");
    }

    private static int getCharWidth(char c, boolean bold) {
        int width;
        switch (c) {
            case 'i': case '!': case ';': case ':': case '|': case '.': case ',': case '\'':
                width = 1;
                break;
            case 'l': case '`':
                width = 2;
                break;
            case 'I': case '[': case ']': case 't': case ' ':
                width = 3;
                break;
            case 'k': case 'f': case '*': case '(': case ')': case '{': case '}': case '<': case '>':
                width = 4;
                break;
            case '@': case '~':
                width = 6;
                break;
            default:
                width = 5;
                break;
        }
        return bold ? width + 1 : width;
    }

    /**
     * Parse text supporting MiniMessage tags, legacy color codes (&a, &l), hex codes, and <center> tags.
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Process <center> tags first
        String processed = centerText(text);

        // Convert &#RRGGBB and <#RRGGBB> to MiniMessage format <#RRGGBB>
        Matcher matcher = HEX_PATTERN.matcher(processed);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            matcher.appendReplacement(sb, "<#" + hex + ">");
        }
        matcher.appendTail(sb);
        String converted = sb.toString();

        if (converted.contains("&")) {
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
