package com.leftycraft.dungeoneventboost.potion;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import com.leftycraft.dungeoneventboost.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBoostManager {

    private final DungeonEventBoost plugin;
    private final File dataFile;
    private final Map<UUID, Map<EventType, PlayerBoost>> playerBoosts = new ConcurrentHashMap<>();
    private final Map<UUID, Map<EventType, BossBar>> playerBossBars = new ConcurrentHashMap<>();

    public PlayerBoostManager(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "player_boosts.yml");
    }

    public void initialize() {
        loadData();
    }

    public boolean addBoost(Player player, EventType type, long durationSeconds, double customMultiplier) {
        if (player == null || type == null || durationSeconds <= 0) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        Map<EventType, PlayerBoost> boosts = playerBoosts.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        PlayerBoost existing = boosts.get(type);

        String stackMode = plugin.getConfigManager().getPotionStackMode();
        PlayerBoost activeBoost;

        if (existing != null && !existing.isExpired()) {
            if ("DENY".equalsIgnoreCase(stackMode)) {
                return false;
            } else if ("REPLACE".equalsIgnoreCase(stackMode)) {
                existing.replaceDuration(durationSeconds);
                if (customMultiplier > 0) {
                    existing.setCustomMultiplier(customMultiplier);
                }
                activeBoost = existing;
            } else {
                // Default: EXTEND
                existing.extendDuration(durationSeconds);
                if (customMultiplier > 0) {
                    existing.setCustomMultiplier(customMultiplier);
                }
                activeBoost = existing;
            }
        } else {
            long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
            activeBoost = new PlayerBoost(type, endTime, durationSeconds, customMultiplier);
            boosts.put(type, activeBoost);
        }

        // Immediately show or update BossBar
        updateBossBar(player, type, activeBoost);

        return true;
    }

    public boolean removeBoost(UUID uuid, EventType type) {
        if (uuid == null || type == null) {
            return false;
        }

        Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
        if (boosts == null) {
            return false;
        }

        PlayerBoost removed = boosts.remove(type);
        if (removed != null) {
            removeBossBar(uuid, type);
            if (boosts.isEmpty()) {
                playerBoosts.remove(uuid);
            }
            saveData();
            return true;
        }
        return false;
    }

    public boolean clearAllBoosts(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        Map<EventType, PlayerBoost> boosts = playerBoosts.remove(uuid);
        removeAllBossBars(uuid);
        if (boosts != null && !boosts.isEmpty()) {
            saveData();
            return true;
        }
        return false;
    }

    public boolean hasActiveBoost(UUID uuid, EventType type) {
        if (uuid == null || type == null) {
            return false;
        }
        Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
        if (boosts == null || boosts.isEmpty()) {
            return false;
        }

        if (type == EventType.MEGA_FRENZY) {
            PlayerBoost mb = boosts.get(EventType.MEGA_FRENZY);
            return mb != null && !mb.isExpired();
        }

        PlayerBoost direct = boosts.get(type);
        if (direct != null && !direct.isExpired()) {
            return true;
        }

        PlayerBoost mega = boosts.get(EventType.MEGA_FRENZY);
        return mega != null && !mega.isExpired();
    }

    public boolean hasDirectBoost(UUID uuid, EventType type) {
        if (uuid == null || type == null) return false;
        Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
        if (boosts == null) return false;
        PlayerBoost b = boosts.get(type);
        return b != null && !b.isExpired();
    }

    public long getRemainingSeconds(UUID uuid, EventType type) {
        if (uuid == null || type == null) return 0L;
        Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
        if (boosts == null) return 0L;

        long directRemaining = 0L;
        PlayerBoost direct = boosts.get(type);
        if (direct != null && !direct.isExpired()) {
            directRemaining = direct.getRemainingSeconds();
        }

        long megaRemaining = 0L;
        if (type != EventType.MEGA_FRENZY) {
            PlayerBoost mega = boosts.get(EventType.MEGA_FRENZY);
            if (mega != null && !mega.isExpired()) {
                megaRemaining = mega.getRemainingSeconds();
            }
        }

        return Math.max(directRemaining, megaRemaining);
    }

    public double getMultiplier(UUID uuid, EventType type, double fallback) {
        if (uuid == null || type == null) return fallback;
        Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
        if (boosts == null) return fallback;

        PlayerBoost direct = boosts.get(type);
        if (direct != null && !direct.isExpired() && direct.getCustomMultiplier() > 0) {
            return direct.getCustomMultiplier();
        }

        PlayerBoost mega = boosts.get(EventType.MEGA_FRENZY);
        if (mega != null && !mega.isExpired() && mega.getCustomMultiplier() > 0) {
            return mega.getCustomMultiplier();
        }

        return fallback;
    }

    public double getKokiMultiplier(UUID uuid) {
        if (uuid == null) return 1.0;
        Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
        if (boosts == null) return 1.0;

        PlayerBoost b10 = boosts.get(EventType.KOKI_MBG_10X);
        if (b10 != null && !b10.isExpired()) {
            return plugin.getConfigManager().getKokiMultiplier10x();
        }

        PlayerBoost b5 = boosts.get(EventType.KOKI_MBG_5X);
        if (b5 != null && !b5.isExpired()) {
            return plugin.getConfigManager().getKokiMultiplier5x();
        }

        PlayerBoost mega = boosts.get(EventType.MEGA_FRENZY);
        if (mega != null && !mega.isExpired()) {
            return plugin.getConfigManager().getKokiMegaFrenzyMultiplier();
        }

        return 1.0;
    }

    public Map<EventType, PlayerBoost> getActiveBoosts(UUID uuid) {
        Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
        if (boosts == null || boosts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<EventType, PlayerBoost> active = new EnumMap<>(EventType.class);
        for (Map.Entry<EventType, PlayerBoost> entry : boosts.entrySet()) {
            if (!entry.getValue().isExpired()) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return active;
    }

    public void updateBossBar(Player player, EventType type, PlayerBoost boost) {
        if (!plugin.getConfigManager().isPotionBossbarEnabled() || player == null || !player.isOnline()) {
            return;
        }

        Map<EventType, BossBar> userBars = playerBossBars.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
        BossBar bar = userBars.get(type);

        ConfigManager.EventDisplayInfo display = plugin.getConfigManager().getDisplayInfo(type);
        long remainingSeconds = boost.getRemainingSeconds();
        long totalDuration = boost.getTotalDurationSeconds();

        double progress = totalDuration > 0
                ? Math.min(1.0, Math.max(0.0, (double) remainingSeconds / totalDuration))
                : 0.0;

        String digitalTime = TextUtil.formatDigitalClock(remainingSeconds);
        String rawTitle = plugin.getConfigManager().getPotionBossbarFormat()
                .replace("{event_name}", display.getName())
                .replace("{time_left}", digitalTime)
                .replace("{event_tag}", display.getShortTag());

        String legacyTitle = TextUtil.toLegacySection(TextUtil.parse(rawTitle));

        if (bar == null) {
            bar = Bukkit.createBossBar(
                    legacyTitle,
                    display.getBarColor(),
                    plugin.getConfigManager().getPotionBossbarStyle()
            );
            bar.setProgress(progress);
            bar.addPlayer(player);
            bar.setVisible(true);
            userBars.put(type, bar);
        } else {
            bar.setTitle(legacyTitle);
            bar.setProgress(progress);
            if (!bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            }
            bar.setVisible(true);
        }
    }

    public void removeBossBar(UUID uuid, EventType type) {
        Map<EventType, BossBar> userBars = playerBossBars.get(uuid);
        if (userBars != null) {
            BossBar bar = userBars.remove(type);
            if (bar != null) {
                bar.removeAll();
                bar.setVisible(false);
            }
            if (userBars.isEmpty()) {
                playerBossBars.remove(uuid);
            }
        }
    }

    public void removeAllBossBars(UUID uuid) {
        Map<EventType, BossBar> userBars = playerBossBars.remove(uuid);
        if (userBars != null) {
            for (BossBar bar : userBars.values()) {
                bar.removeAll();
                bar.setVisible(false);
            }
            userBars.clear();
        }
    }

    public void removeAllBossBars() {
        for (Map<EventType, BossBar> userBars : playerBossBars.values()) {
            for (BossBar bar : userBars.values()) {
                bar.removeAll();
                bar.setVisible(false);
            }
            userBars.clear();
        }
        playerBossBars.clear();
    }

    public void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Map<EventType, PlayerBoost> boosts = playerBoosts.get(uuid);
            if (boosts == null || boosts.isEmpty()) {
                removeAllBossBars(uuid);
                continue;
            }

            List<EventType> expired = new ArrayList<>();

            for (Map.Entry<EventType, PlayerBoost> entry : boosts.entrySet()) {
                EventType type = entry.getKey();
                PlayerBoost boost = entry.getValue();

                if (boost.isExpired()) {
                    expired.add(type);
                } else {
                    // Update player's BossBar every second
                    updateBossBar(player, type, boost);
                }
            }

            // Process expired
            for (EventType type : expired) {
                boosts.remove(type);
                removeBossBar(uuid, type);

                ConfigManager.EventDisplayInfo info = plugin.getConfigManager().getDisplayInfo(type);
                String expireMsg = plugin.getConfigManager().getMessagePotionExpired()
                        .replace("{event_name}", info.getName());
                TextUtil.sendMessage(player, plugin.getConfigManager().getMessagePrefix() + expireMsg);

                if (plugin.getConfigManager().isPotionSoundExpireEnabled()) {
                    Sound sound = plugin.getConfigManager().getPotionSoundExpire();
                    if (sound != null) {
                        player.playSound(
                                player.getLocation(),
                                sound,
                                plugin.getConfigManager().getPotionSoundExpireVolume(),
                                plugin.getConfigManager().getPotionSoundExpirePitch()
                        );
                    }
                }
            }

            if (boosts.isEmpty()) {
                playerBoosts.remove(uuid);
                removeAllBossBars(uuid);
            }
        }
    }

    public void onPlayerJoin(Player player) {
        // Recreate and attach BossBars for active boosts when player rejoins
        Map<EventType, PlayerBoost> boosts = playerBoosts.get(player.getUniqueId());
        if (boosts != null && !boosts.isEmpty()) {
            for (Map.Entry<EventType, PlayerBoost> entry : boosts.entrySet()) {
                if (!entry.getValue().isExpired()) {
                    updateBossBar(player, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void onPlayerQuit(Player player) {
        removeAllBossBars(player.getUniqueId());
    }

    public void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        long now = System.currentTimeMillis();
        int loadedCount = 0;

        for (String key : yaml.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException e) {
                continue;
            }

            ConfigurationSection userSec = yaml.getConfigurationSection(key);
            if (userSec == null) continue;

            Map<EventType, PlayerBoost> boosts = new ConcurrentHashMap<>();
            for (String typeName : userSec.getKeys(false)) {
                EventType type = EventType.fromString(typeName);
                if (type == null) continue;

                ConfigurationSection boostSec = userSec.getConfigurationSection(typeName);
                if (boostSec == null) continue;

                long endTime = boostSec.getLong("endTime", 0L);
                long totalDuration = boostSec.getLong("totalDuration", 600L);
                double customMultiplier = boostSec.getDouble("multiplier", 0.0);

                if (endTime > now) {
                    boosts.put(type, new PlayerBoost(type, endTime, totalDuration, customMultiplier));
                    loadedCount++;
                }
            }

            if (!boosts.isEmpty()) {
                playerBoosts.put(uuid, boosts);
            }
        }

        if (loadedCount > 0) {
            plugin.getLogger().info("Berhasil memuat " + loadedCount + " efek ramuan boost personal pemain.");
        }
    }

    public void saveData() {
        YamlConfiguration yaml = new YamlConfiguration();

        for (Map.Entry<UUID, Map<EventType, PlayerBoost>> entry : playerBoosts.entrySet()) {
            UUID uuid = entry.getKey();
            Map<EventType, PlayerBoost> boosts = entry.getValue();
            if (boosts.isEmpty()) continue;

            for (Map.Entry<EventType, PlayerBoost> boostEntry : boosts.entrySet()) {
                EventType type = boostEntry.getKey();
                PlayerBoost boost = boostEntry.getValue();

                if (!boost.isExpired()) {
                    String path = uuid.toString() + "." + type.name();
                    yaml.set(path + ".endTime", boost.getEndTime());
                    yaml.set(path + ".totalDuration", boost.getTotalDurationSeconds());
                    yaml.set(path + ".multiplier", boost.getCustomMultiplier());
                }
            }
        }

        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan data ramuan boost pemain: " + e.getMessage());
        }
    }
}
