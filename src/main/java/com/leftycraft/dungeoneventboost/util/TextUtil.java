package com.leftycraft.dungeoneventboost.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[&§]([0-9a-fk-orA-FK-OR])");
    private static final Pattern CENTER_PATTERN = Pattern.compile("(?i)<center>(.*?)</center>");
    private static final Pattern STRIP_TAGS_PATTERN = Pattern.compile("<[^>]+>|&[0-9a-fk-orA-FK-OR]|§[0-9a-fk-orA-FK-OR]");

    private static final int CHAT_CENTER_PX = 154; // Half of standard MC chat width (320px) with margins
    private static final int SPACE_PX = 4;

    private static final Map<Character, String> LEGACY_TO_MINIMESSAGE = new HashMap<>();

    static {
        LEGACY_TO_MINIMESSAGE.put('0', "<black>");
        LEGACY_TO_MINIMESSAGE.put('1', "<dark_blue>");
        LEGACY_TO_MINIMESSAGE.put('2', "<dark_green>");
        LEGACY_TO_MINIMESSAGE.put('3', "<dark_aqua>");
        LEGACY_TO_MINIMESSAGE.put('4', "<dark_red>");
        LEGACY_TO_MINIMESSAGE.put('5', "<dark_purple>");
        LEGACY_TO_MINIMESSAGE.put('6', "<gold>");
        LEGACY_TO_MINIMESSAGE.put('7', "<gray>");
        LEGACY_TO_MINIMESSAGE.put('8', "<dark_gray>");
        LEGACY_TO_MINIMESSAGE.put('9', "<blue>");
        LEGACY_TO_MINIMESSAGE.put('a', "<green>");
        LEGACY_TO_MINIMESSAGE.put('b', "<aqua>");
        LEGACY_TO_MINIMESSAGE.put('c', "<red>");
        LEGACY_TO_MINIMESSAGE.put('d', "<light_purple>");
        LEGACY_TO_MINIMESSAGE.put('e', "<yellow>");
        LEGACY_TO_MINIMESSAGE.put('f', "<white>");
        LEGACY_TO_MINIMESSAGE.put('k', "<obf>");
        LEGACY_TO_MINIMESSAGE.put('l', "<bold>");
        LEGACY_TO_MINIMESSAGE.put('m', "<st>");
        LEGACY_TO_MINIMESSAGE.put('n', "<u>");
        LEGACY_TO_MINIMESSAGE.put('o', "<i>");
        LEGACY_TO_MINIMESSAGE.put('r', "<reset>");
    }

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
            return text.replaceAll("(?i)</?center>", "");
        }

        StringBuilder sb = new StringBuilder();
        matcher.reset();
        while (matcher.find()) {
            String content = matcher.group(1);
            String plain = STRIP_TAGS_PATTERN.matcher(content).replaceAll("");

            int textPx = 0;
            boolean bold = content.toLowerCase().contains("<bold>") || content.toLowerCase().contains("<b>") || content.contains("&l") || content.contains("§l");
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
     * Preserves literal ampersands '&' (such as in "Jobs & Economy", "Speed & Haste").
     */
    public static Component parse(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // 1. Process <center> tags
        String processed = centerText(text);

        // 2. Convert &#RRGGBB to MiniMessage format <#RRGGBB>
        Matcher hexMatcher = HEX_PATTERN.matcher(processed);
        StringBuilder sbHex = new StringBuilder();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(sbHex, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(sbHex);
        processed = sbHex.toString();

        // 3. Convert only valid legacy color codes (&a, §a, etc.) to MiniMessage tags (<green>, etc.)
        // Literal '&' like in "Jobs & Economy" or "Speed & Haste" are untouched!
        Matcher legacyMatcher = LEGACY_PATTERN.matcher(processed);
        StringBuilder sbLegacy = new StringBuilder();
        while (legacyMatcher.find()) {
            char code = Character.toLowerCase(legacyMatcher.group(1).charAt(0));
            String tag = LEGACY_TO_MINIMESSAGE.getOrDefault(code, "");
            legacyMatcher.appendReplacement(sbLegacy, Matcher.quoteReplacement(tag));
        }
        legacyMatcher.appendTail(sbLegacy);
        processed = sbLegacy.toString();

        // 4. Parse with MiniMessage
        try {
            return MINI_MESSAGE.deserialize(processed);
        } catch (Exception ignored) {
            return Component.text(processed);
        }
    }

    public static String toLegacySection(Component component) {
        return SECTION_SERIALIZER.serialize(component);
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
