package com.leftycraft.dungeoneventboost.command;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import com.leftycraft.dungeoneventboost.potion.PlayerBoost;
import com.leftycraft.dungeoneventboost.potion.PotionItemConfig;
import com.leftycraft.dungeoneventboost.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EventBoostCommand implements CommandExecutor, TabCompleter {

    private static final List<String> BOOST_TYPES = Arrays.asList(
            "drops", "exp_skills", "damage", "defense", "economy", "exp_vanilla", "swiftness", "all", "koki_5x", "koki_10x"
    );

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

            case "give":
                if (!sender.hasPermission("dungeoneventboost.admin")) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageNoPermission());
                    return true;
                }

                if (args.length < 3) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<yellow>Penggunaan: <white>/eb give <player> <jenis> [durasi_menit] [jumlah]</white></yellow>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Pemain '<white>" + args[1] + "</white>' tidak ditemukan atau sedang offline!</red>");
                    return true;
                }

                EventType potionType = EventType.fromString(args[2]);
                if (potionType == null) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Jenis boost '<white>" + args[2] + "</white>' tidak valid! Pilihan: " + String.join(", ", BOOST_TYPES) + "</red>");
                    return true;
                }

                long potionDurationSeconds = -1L;
                if (args.length > 3) {
                    potionDurationSeconds = parseDurationMinutes(sender, args[3], -1L);
                    if (potionDurationSeconds <= 0) return true;
                } else {
                    PotionItemConfig cfg = plugin.getBoostItemManager().getConfig(potionType);
                    potionDurationSeconds = cfg != null ? cfg.getDefaultDurationSeconds() : 600L;
                }

                int amount = 1;
                if (args.length > 4) {
                    try {
                        amount = Integer.parseInt(args[4]);
                        if (amount <= 0) amount = 1;
                    } catch (NumberFormatException e) {
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Jumlah item harus berupa angka bulat positif!</red>");
                        return true;
                    }
                }

                ItemStack potionItem = plugin.getBoostItemManager().createBoostPotion(potionType, potionDurationSeconds, amount);
                HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(potionItem);
                for (ItemStack drop : leftover.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), drop);
                }

                PotionItemConfig cfg = plugin.getBoostItemManager().getConfig(potionType);
                String itemName = cfg != null ? cfg.getName() : configManager.getDisplayInfo(potionType).getName();
                String durFormatted = TextUtil.formatDuration(potionDurationSeconds);

                String givenMsg = configManager.getMessagePotionGiven()
                        .replace("{player}", target.getName())
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{item_name}", itemName)
                        .replace("{duration}", durFormatted);
                TextUtil.sendMessage(sender, configManager.getMessagePrefix() + givenMsg);

                String receivedMsg = configManager.getMessagePotionReceived()
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{item_name}", itemName)
                        .replace("{duration}", durFormatted);
                TextUtil.sendMessage(target, configManager.getMessagePrefix() + receivedMsg);
                return true;

            case "giveall":
                if (!sender.hasPermission("dungeoneventboost.admin")) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageNoPermission());
                    return true;
                }

                if (args.length < 2) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<yellow>Penggunaan: <white>/eb giveall <jenis> [durasi_menit] [jumlah]</white></yellow>");
                    return true;
                }

                EventType giveAllType = EventType.fromString(args[1]);
                if (giveAllType == null) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Jenis boost '<white>" + args[1] + "</white>' tidak valid! Pilihan: " + String.join(", ", BOOST_TYPES) + "</red>");
                    return true;
                }

                long giveAllDuration = -1L;
                if (args.length > 2) {
                    giveAllDuration = parseDurationMinutes(sender, args[2], -1L);
                    if (giveAllDuration <= 0) return true;
                } else {
                    PotionItemConfig c = plugin.getBoostItemManager().getConfig(giveAllType);
                    giveAllDuration = c != null ? c.getDefaultDurationSeconds() : 600L;
                }

                int giveAllAmount = 1;
                if (args.length > 3) {
                    try {
                        giveAllAmount = Integer.parseInt(args[3]);
                        if (giveAllAmount <= 0) giveAllAmount = 1;
                    } catch (NumberFormatException e) {
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Jumlah item harus berupa angka bulat positif!</red>");
                        return true;
                    }
                }

                PotionItemConfig allCfg = plugin.getBoostItemManager().getConfig(giveAllType);
                String allItemName = allCfg != null ? allCfg.getName() : configManager.getDisplayInfo(giveAllType).getName();
                String allDurFormatted = TextUtil.formatDuration(giveAllDuration);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    ItemStack pItem = plugin.getBoostItemManager().createBoostPotion(giveAllType, giveAllDuration, giveAllAmount);
                    HashMap<Integer, ItemStack> lo = p.getInventory().addItem(pItem);
                    for (ItemStack drop : lo.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), drop);
                    }

                    String recMsg = configManager.getMessagePotionReceived()
                            .replace("{amount}", String.valueOf(giveAllAmount))
                            .replace("{item_name}", allItemName)
                            .replace("{duration}", allDurFormatted);
                    TextUtil.sendMessage(p, configManager.getMessagePrefix() + recMsg);
                }

                TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<green>Berhasil membagikan <white>" + giveAllAmount + "x " + allItemName + "</white> ke seluruh pemain online (<white>" + allDurFormatted + "</white>)!</green>");
                return true;

            case "remove":
            case "clear":
                if (!sender.hasPermission("dungeoneventboost.admin")) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + configManager.getMessageNoPermission());
                    return true;
                }

                if (args.length < 2) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<yellow>Penggunaan: <white>/eb remove <player> [jenis]</white></yellow>");
                    return true;
                }

                Player removeTarget = Bukkit.getPlayer(args[1]);
                UUID targetUuid;
                String targetName;

                if (removeTarget != null) {
                    targetUuid = removeTarget.getUniqueId();
                    targetName = removeTarget.getName();
                } else {
                    org.bukkit.OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                    if (op.hasPlayedBefore() || op.isOnline()) {
                        targetUuid = op.getUniqueId();
                        targetName = op.getName() != null ? op.getName() : args[1];
                    } else {
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Pemain '<white>" + args[1] + "</white>' tidak ditemukan!</red>");
                        return true;
                    }
                }

                if (args.length > 2 && !args[2].equalsIgnoreCase("all")) {
                    EventType remType = EventType.fromString(args[2]);
                    if (remType == null) {
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Jenis boost '<white>" + args[2] + "</white>' tidak valid! Pilihan: all, " + String.join(", ", BOOST_TYPES) + "</red>");
                        return true;
                    }

                    boolean removed = eventManager.getPlayerBoostManager().removeBoost(targetUuid, remType);
                    if (removed) {
                        String remEventName = configManager.getDisplayInfo(remType).getName();
                        String adminMsg = configManager.getMessageBoostRemovedAdmin()
                                .replace("{player}", targetName)
                                .replace("{event_name}", remEventName);
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + adminMsg);

                        if (removeTarget != null && removeTarget.isOnline()) {
                            String playerMsg = configManager.getMessageBoostRemovedPlayer()
                                    .replace("{event_name}", remEventName);
                            TextUtil.sendMessage(removeTarget, configManager.getMessagePrefix() + playerMsg);
                        }
                    } else {
                        String notFoundMsg = configManager.getMessageBoostNotFound()
                                .replace("{player}", targetName)
                                .replace("{type}", args[2]);
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + notFoundMsg);
                    }
                } else {
                    boolean removed = eventManager.getPlayerBoostManager().clearAllBoosts(targetUuid);
                    if (removed) {
                        String adminMsg = configManager.getMessageBoostRemovedAllAdmin()
                                .replace("{player}", targetName);
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + adminMsg);

                        if (removeTarget != null && removeTarget.isOnline()) {
                            String playerMsg = configManager.getMessageBoostRemovedAllPlayer();
                            TextUtil.sendMessage(removeTarget, configManager.getMessagePrefix() + playerMsg);
                        }
                    } else {
                        String noneMsg = configManager.getMessageBoostNoneActive()
                                .replace("{player}", targetName);
                        TextUtil.sendMessage(sender, configManager.getMessagePrefix() + noneMsg);
                    }
                }
                return true;

            case "myboost":
            case "personal":
                if (!(sender instanceof Player player)) {
                    TextUtil.sendMessage(sender, configManager.getMessagePrefix() + "<red>Perintah ini hanya dapat digunakan oleh pemain di dalam game!</red>");
                    return true;
                }

                showPersonalBoosts(player);
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

    private void showPersonalBoosts(Player player) {
        Map<EventType, PlayerBoost> boosts = eventManager.getPlayerBoostManager().getActiveBoosts(player.getUniqueId());
        if (boosts.isEmpty()) {
            TextUtil.sendMessage(player, configManager.getMessagePrefix() + configManager.getMessageMyboostNone());
            return;
        }

        for (String line : configManager.getMessageMyboostHeader()) {
            TextUtil.sendMessage(player, line);
        }

        for (Map.Entry<EventType, PlayerBoost> entry : boosts.entrySet()) {
            EventType type = entry.getKey();
            PlayerBoost boost = entry.getValue();
            ConfigManager.EventDisplayInfo info = configManager.getDisplayInfo(type);
            String line = configManager.getMessageMyboostEntry()
                    .replace("{event_name}", info.getName())
                    .replace("{time_left}", TextUtil.formatDuration(boost.getRemainingSeconds()))
                    .replace("{time_left_digital}", TextUtil.formatDigitalClock(boost.getRemainingSeconds()));
            TextUtil.sendMessage(player, line);
        }

        for (String line : configManager.getMessageMyboostFooter()) {
            TextUtil.sendMessage(player, line);
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
                        .replace("{dmg_mult}", String.valueOf(configManager.getDamageMultiplier()))
                        .replace("{koki_mult}", String.valueOf((int) eventManager.getKokiMbgMultiplier()));
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
                    ? Arrays.asList("status", "start", "stop", "reload", "give", "giveall", "remove", "clear", "myboost", "help")
                    : Arrays.asList("status", "myboost", "help");

            String prefix = args[0].toLowerCase();
            for (String sub : subs) {
                if (sub.startsWith(prefix)) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        // Subcommand: start
        if (args.length == 2 && args[0].equalsIgnoreCase("start") && sender.hasPermission("dungeoneventboost.admin")) {
            List<String> options = new ArrayList<>(BOOST_TYPES);
            options.addAll(Arrays.asList("5", "10", "15", "30"));
            return filterPrefix(options, args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("start") && sender.hasPermission("dungeoneventboost.admin")) {
            return filterPrefix(Arrays.asList("5", "10", "15", "30", "60"), args[2]);
        }

        // Subcommand: give <player> <type> [duration] [amount]
        if (sender.hasPermission("dungeoneventboost.admin") && args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) {
                List<String> playerNames = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNames.add(p.getName());
                }
                return filterPrefix(playerNames, args[1]);
            }
            if (args.length == 3) {
                return filterPrefix(BOOST_TYPES, args[2]);
            }
            if (args.length == 4) {
                return filterPrefix(Arrays.asList("5", "10", "15", "30", "60"), args[3]);
            }
            if (args.length == 5) {
                return filterPrefix(Arrays.asList("1", "2", "5", "16", "64"), args[4]);
            }
        }

        // Subcommand: giveall <type> [duration] [amount]
        if (sender.hasPermission("dungeoneventboost.admin") && args[0].equalsIgnoreCase("giveall")) {
            if (args.length == 2) {
                return filterPrefix(BOOST_TYPES, args[1]);
            }
            if (args.length == 3) {
                return filterPrefix(Arrays.asList("5", "10", "15", "30", "60"), args[2]);
            }
            if (args.length == 4) {
                return filterPrefix(Arrays.asList("1", "2", "5", "16", "64"), args[3]);
            }
        }

        // Subcommand: remove / clear <player> [type]
        if (sender.hasPermission("dungeoneventboost.admin") && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("clear"))) {
            if (args.length == 2) {
                List<String> playerNames = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerNames.add(p.getName());
                }
                return filterPrefix(playerNames, args[1]);
            }
            if (args.length == 3) {
                List<String> types = new ArrayList<>();
                types.add("all");
                types.addAll(BOOST_TYPES);
                return filterPrefix(types, args[2]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> list, String input) {
        String lower = input.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (item.toLowerCase().startsWith(lower)) {
                result.add(item);
            }
        }
        return result;
    }
}
