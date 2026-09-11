package com.leftycraft.dungeoneventboost.event;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.util.TextUtil;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventManager {

    private final DungeonEventBoost plugin;
    private final ConfigManager configManager;

    private boolean eventActive = false;
    private long eventEndTime = 0L;
    private long totalDurationSeconds = 0L;
    private long nextEventTime = 0L;

    private BossBar bossBar;
    private final Set<Long> announcedWarnings = new HashSet<>();
    private final File dataFile;

    public EventManager(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void initialize() {
        loadState();
        if (configManager.isAutoStartTimer() && nextEventTime <= 0L && !eventActive) {
            scheduleNextEvent();
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();

        if (eventActive) {
            if (now >= eventEndTime) {
                stopEvent(true);
                return;
            }

            long remainingSeconds = Math.max(0, (eventEndTime - now) / 1000L);
            updateBossBar(remainingSeconds);
        } else {
            if (nextEventTime > 0L) {
                if (now >= nextEventTime) {
                    startEvent(configManager.getDurationSeconds());
                    return;
                }

                checkCountdownWarnings(now);
            }
        }
    }

    public boolean startEvent(long durationSeconds) {
        if (eventActive) {
            return false;
        }

        this.eventActive = true;
        this.totalDurationSeconds = durationSeconds;
        this.eventEndTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        this.announcedWarnings.clear();

        // Broadcast messages
        List<String> startBroadcast = configManager.getMessageBroadcastStart();
        if (startBroadcast != null) {
            long durationMinutes = Math.max(1, durationSeconds / 60);
            for (String line : startBroadcast) {
                String replaced = line
                        .replace("{duration}", String.valueOf(durationMinutes))
                        .replace("{drop_mult}", String.valueOf(configManager.getDropMultiplier()))
                        .replace("{exp_mult}", String.valueOf(configManager.getExpMultiplier()))
                        .replace("{dmg_mult}", String.valueOf(configManager.getDamageMultiplier()));
                TextUtil.broadcast(replaced);
            }
        }

        // Send Titles
        if (configManager.isTitleStartEnabled()) {
            Title.Times times = Title.Times.times(
                    Duration.ofMillis(configManager.getTitleStartFadeIn() * 50L),
                    Duration.ofMillis(configManager.getTitleStartStay() * 50L),
                    Duration.ofMillis(configManager.getTitleStartFadeOut() * 50L)
            );
            Title title = Title.title(
                    TextUtil.parse(configManager.getTitleStartTitle()),
                    TextUtil.parse(configManager.getTitleStartSubtitle()),
                    times
            );
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showTitle(title);
            }
        }

        // Play Start Sound
        if (configManager.isSoundStartEnabled() && configManager.getSoundStartSound() != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(
                        player.getLocation(),
                        configManager.getSoundStartSound(),
                        configManager.getSoundStartVolume(),
                        configManager.getSoundStartPitch()
                );
            }
        }

        // Initialize BossBar
        createBossBar();
        updateBossBar(durationSeconds);

        saveState();
        return true;
    }

    public boolean stopEvent(boolean naturally) {
        if (!eventActive) {
            return false;
        }

        this.eventActive = false;
        removeBossBar();

        scheduleNextEvent();

        if (naturally) {
            // Broadcast End
            List<String> endBroadcast = configManager.getMessageBroadcastEnd();
            if (endBroadcast != null) {
                String nextTimeFormatted = TextUtil.formatDuration(getSecondsUntilNextEvent());
                for (String line : endBroadcast) {
                    String replaced = line.replace("{next_event_time}", nextTimeFormatted);
                    TextUtil.broadcast(replaced);
                }
            }

            // End Titles
            if (configManager.isTitleEndEnabled()) {
                Title.Times times = Title.Times.times(
                        Duration.ofMillis(configManager.getTitleEndFadeIn() * 50L),
                        Duration.ofMillis(configManager.getTitleEndStay() * 50L),
                        Duration.ofMillis(configManager.getTitleEndFadeOut() * 50L)
                );
                Title title = Title.title(
                        TextUtil.parse(configManager.getTitleEndTitle()),
                        TextUtil.parse(configManager.getTitleEndSubtitle()),
                        times
                );
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showTitle(title);
                }
            }

            // End Sound
            if (configManager.isSoundEndEnabled() && configManager.getSoundEndSound() != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(
                            player.getLocation(),
                            configManager.getSoundEndSound(),
                            configManager.getSoundEndVolume(),
                            configManager.getSoundEndPitch()
                    );
                }
            }
        }

        saveState();
        return true;
    }

    public void scheduleNextEvent() {
        this.nextEventTime = System.currentTimeMillis() + (configManager.getIntervalSeconds() * 1000L);
        this.announcedWarnings.clear();
        saveState();
    }

    private void checkCountdownWarnings(long now) {
        if (!configManager.isCountdownWarningsEnabled()) {
            return;
        }

        long secondsUntilNext = Math.max(0, (nextEventTime - now) / 1000L);

        for (long warningSec : configManager.getCountdownWarningSeconds()) {
            if (secondsUntilNext <= warningSec && !announcedWarnings.contains(warningSec)) {
                announcedWarnings.add(warningSec);

                String formattedTime = TextUtil.formatDuration(warningSec);
                String msg = configManager.getMessageBroadcastCountdown()
                        .replace("{time_left}", formattedTime);
                TextUtil.broadcast(msg);

                // Tick sound
                if (configManager.isSoundTickEnabled() && configManager.getSoundTickSound() != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(
                                player.getLocation(),
                                configManager.getSoundTickSound(),
                                configManager.getSoundTickVolume(),
                                configManager.getSoundTickPitch()
                        );
                    }
                }
                break;
            }
        }
    }

    private void createBossBar() {
        if (!configManager.isBossBarEnabled()) {
            return;
        }
        removeBossBar();

        bossBar = Bukkit.createBossBar(
                "",
                configManager.getBossBarColor(),
                configManager.getBossBarStyle()
        );
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
        bossBar.setVisible(true);
    }

    private void updateBossBar(long remainingSeconds) {
        if (bossBar == null || !configManager.isBossBarEnabled()) {
            return;
        }

        double progress = totalDurationSeconds > 0
                ? Math.min(1.0, Math.max(0.0, (double) remainingSeconds / totalDurationSeconds))
                : 0.0;
        bossBar.setProgress(progress);

        String formattedTime = TextUtil.formatDigitalClock(remainingSeconds);
        String title = configManager.getBossBarTitle()
                .replace("{time_left}", formattedTime)
                .replace("{drop_mult}", String.valueOf(configManager.getDropMultiplier()))
                .replace("{exp_mult}", String.valueOf(configManager.getExpMultiplier()))
                .replace("{dmg_mult}", String.valueOf(configManager.getDamageMultiplier()));

        // Bukkit BossBar title expects string with color codes or legacy
        bossBar.setTitle(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(TextUtil.parse(title)));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    public void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
            bossBar = null;
        }
    }

    public void onPlayerJoin(Player player) {
        if (eventActive && bossBar != null) {
            bossBar.addPlayer(player);
        }
    }

    public void onPlayerQuit(Player player) {
        if (bossBar != null) {
            bossBar.removePlayer(player);
        }
    }

    public void loadState() {
        if (!configManager.isSaveStateOnRestart() || !dataFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        boolean wasActive = config.getBoolean("active", false);
        long savedEndTime = config.getLong("endTime", 0L);
        long savedTotalDuration = config.getLong("totalDuration", 0L);
        long savedNextTime = config.getLong("nextTime", 0L);
        long now = System.currentTimeMillis();

        if (wasActive && now < savedEndTime) {
            this.eventActive = true;
            this.eventEndTime = savedEndTime;
            this.totalDurationSeconds = savedTotalDuration;
            createBossBar();
            updateBossBar((savedEndTime - now) / 1000L);
            plugin.getLogger().info("Event boost yang sedang berjalan dipulihkan dari state sebelumnya.");
        } else if (savedNextTime > now) {
            this.nextEventTime = savedNextTime;
            plugin.getLogger().info("Countdown event berikutnya dipulihkan: " + TextUtil.formatDuration((savedNextTime - now) / 1000L));
        }
    }

    public void saveState() {
        if (!configManager.isSaveStateOnRestart()) {
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.set("active", eventActive);
        config.set("endTime", eventEndTime);
        config.set("totalDuration", totalDurationSeconds);
        config.set("nextTime", nextEventTime);

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan event state ke data.yml: " + e.getMessage());
        }
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public long getRemainingSeconds() {
        if (!eventActive) return 0L;
        return Math.max(0, (eventEndTime - System.currentTimeMillis()) / 1000L);
    }

    public long getSecondsUntilNextEvent() {
        if (eventActive) return 0L;
        return Math.max(0, (nextEventTime - System.currentTimeMillis()) / 1000L);
    }

    public long getNextEventTime() {
        return nextEventTime;
    }

    public void setNextEventTime(long nextEventTime) {
        this.nextEventTime = nextEventTime;
        saveState();
    }
}
