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
import java.util.*;

public class EventManager {

    private final DungeonEventBoost plugin;
    private final ConfigManager configManager;

    private boolean eventActive = false;
    private EventType activeEventType = null;
    private long eventEndTime = 0L;
    private long totalDurationSeconds = 0L;
    private long nextEventTime = 0L;
    private int rotationIndex = 0;

    private BossBar bossBar;
    private final File dataFile;
    private final Random random = new Random();

    // Performance optimizations: state tracking
    private long lastRemainingSeconds = -1L;
    private int nextWarningIndex = 0;

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

    /**
     * Highly optimized 1-second tick loop.
     * Takes < 0.001ms CPU time.
     */
    public void tick() {
        long now = System.currentTimeMillis();

        if (eventActive) {
            if (now >= eventEndTime) {
                stopEvent(true);
                return;
            }

            long remainingSeconds = Math.max(0, (eventEndTime - now) / 1000L);
            if (remainingSeconds != lastRemainingSeconds) {
                lastRemainingSeconds = remainingSeconds;
                updateBossBar(remainingSeconds);
            }
        } else {
            if (nextEventTime > 0L) {
                if (now >= nextEventTime) {
                    EventType nextType = pickNextEventType();
                    startEvent(nextType, configManager.getDurationSeconds());
                    return;
                }

                checkCountdownWarnings(now);
            }
        }
    }

    public boolean isBoostActive(EventType type) {
        if (!eventActive || activeEventType == null) {
            return false;
        }
        return activeEventType == type || activeEventType == EventType.MEGA_FRENZY;
    }

    public EventType pickNextEventType() {
        List<EventType> enabled = configManager.getEnabledEvents();
        if (enabled.isEmpty()) {
            return EventType.DROPS;
        }

        if (configManager.getSelectionMode().equalsIgnoreCase("RANDOM")) {
            if (random.nextDouble() < configManager.getMegaFrenzyChance() && enabled.contains(EventType.MEGA_FRENZY)) {
                return EventType.MEGA_FRENZY;
            }
            List<EventType> regular = new ArrayList<>(enabled);
            regular.remove(EventType.MEGA_FRENZY);
            if (regular.isEmpty()) return EventType.MEGA_FRENZY;
            return regular.get(random.nextInt(regular.size()));
        } else {
            // ROTATION mode
            if (rotationIndex >= enabled.size()) {
                rotationIndex = 0;
            }
            EventType chosen = enabled.get(rotationIndex);
            rotationIndex = (rotationIndex + 1) % enabled.size();
            saveState();
            return chosen;
        }
    }

    public boolean startEvent(EventType type, long durationSeconds) {
        if (eventActive) {
            return false;
        }

        if (type == null) {
            type = EventType.DROPS;
        }

        this.eventActive = true;
        this.activeEventType = type;
        this.totalDurationSeconds = durationSeconds;
        this.eventEndTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        this.lastRemainingSeconds = -1L;
        this.nextWarningIndex = 0;

        ConfigManager.EventDisplayInfo display = configManager.getDisplayInfo(type);

        // Broadcast messages
        List<String> startBroadcast = configManager.getMessageBroadcastStart();
        if (startBroadcast != null) {
            long durationMinutes = Math.max(1, durationSeconds / 60);
            for (String line : startBroadcast) {
                String replaced = line
                        .replace("{event_name}", display.getName())
                        .replace("{event_tag}", display.getShortTag())
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
                    TextUtil.parse(display.getTitle()),
                    TextUtil.parse(display.getSubtitle()),
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
        createBossBar(display);
        updateBossBar(durationSeconds);

        saveState();
        return true;
    }

    public boolean stopEvent(boolean naturally) {
        if (!eventActive) {
            return false;
        }

        EventType endedType = this.activeEventType;
        this.eventActive = false;
        this.activeEventType = null;
        this.lastRemainingSeconds = -1L;
        removeBossBar();

        scheduleNextEvent();

        if (naturally) {
            ConfigManager.EventDisplayInfo display = configManager.getDisplayInfo(endedType != null ? endedType : EventType.DROPS);

            // Broadcast End
            List<String> endBroadcast = configManager.getMessageBroadcastEnd();
            if (endBroadcast != null) {
                String nextTimeFormatted = TextUtil.formatDuration(getSecondsUntilNextEvent());
                for (String line : endBroadcast) {
                    String replaced = line
                            .replace("{event_name}", display.getName())
                            .replace("{next_event_time}", nextTimeFormatted);
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
        resetCountdownPointer();
        saveState();
    }

    private void resetCountdownPointer() {
        long secondsUntilNext = getSecondsUntilNextEvent();
        List<Long> warnings = configManager.getCountdownWarningSeconds();
        this.nextWarningIndex = 0;
        // Skip any warnings that are already past
        while (nextWarningIndex < warnings.size() && secondsUntilNext < warnings.get(nextWarningIndex)) {
            nextWarningIndex++;
        }
    }

    /**
     * Optimized O(1) countdown checker using index pointer.
     */
    private void checkCountdownWarnings(long now) {
        if (!configManager.isCountdownWarningsEnabled()) {
            return;
        }

        List<Long> warnings = configManager.getCountdownWarningSeconds();
        if (nextWarningIndex >= warnings.size()) {
            return;
        }

        long secondsUntilNext = Math.max(0, (nextEventTime - now) / 1000L);
        long targetWarningSec = warnings.get(nextWarningIndex);

        if (secondsUntilNext <= targetWarningSec) {
            nextWarningIndex++;

            String formattedTime = TextUtil.formatDuration(targetWarningSec);
            String msg = configManager.getMessageBroadcastCountdown()
                    .replace("{time_left}", formattedTime);
            TextUtil.broadcast(msg);

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
        }
    }

    private void createBossBar(ConfigManager.EventDisplayInfo display) {
        if (!configManager.isBossBarEnabled()) {
            return;
        }
        removeBossBar();

        bossBar = Bukkit.createBossBar(
                "",
                display.getBarColor(),
                configManager.getBossBarStyle()
        );

        // Add all currently online players once
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
        bossBar.setVisible(true);
    }

    /**
     * Efficiently updates BossBar progress and title.
     * Zero player collection lookups.
     */
    private void updateBossBar(long remainingSeconds) {
        if (bossBar == null || !configManager.isBossBarEnabled() || activeEventType == null) {
            return;
        }

        ConfigManager.EventDisplayInfo display = configManager.getDisplayInfo(activeEventType);

        double progress = totalDurationSeconds > 0
                ? Math.min(1.0, Math.max(0.0, (double) remainingSeconds / totalDurationSeconds))
                : 0.0;
        bossBar.setProgress(progress);

        String formattedTime = TextUtil.formatDigitalClock(remainingSeconds);
        String rawTitle = display.getBossBarTitle()
                .replace("{time_left}", formattedTime)
                .replace("{event_name}", display.getName())
                .replace("{drop_mult}", String.valueOf(configManager.getDropMultiplier()))
                .replace("{exp_mult}", String.valueOf(configManager.getExpMultiplier()))
                .replace("{dmg_mult}", String.valueOf(configManager.getDamageMultiplier()));

        bossBar.setTitle(TextUtil.toLegacySection(TextUtil.parse(rawTitle)));
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
        String typeStr = config.getString("eventType", "");
        long savedEndTime = config.getLong("endTime", 0L);
        long savedTotalDuration = config.getLong("totalDuration", 0L);
        long savedNextTime = config.getLong("nextTime", 0L);
        this.rotationIndex = config.getInt("rotationIndex", 0);
        long now = System.currentTimeMillis();

        EventType type = EventType.fromString(typeStr);
        if (wasActive && type != null && now < savedEndTime) {
            this.eventActive = true;
            this.activeEventType = type;
            this.eventEndTime = savedEndTime;
            this.totalDurationSeconds = savedTotalDuration;
            this.lastRemainingSeconds = -1L;
            createBossBar(configManager.getDisplayInfo(type));
            updateBossBar((savedEndTime - now) / 1000L);
            plugin.getLogger().info("Event boost [" + type.name() + "] dipulihkan dari state sebelumnya.");
        } else if (savedNextTime > now) {
            this.nextEventTime = savedNextTime;
            resetCountdownPointer();
            plugin.getLogger().info("Countdown event berikutnya dipulihkan: " + TextUtil.formatDuration((savedNextTime - now) / 1000L));
        }
    }

    public void saveState() {
        if (!configManager.isSaveStateOnRestart()) {
            return;
        }

        YamlConfiguration config = new YamlConfiguration();
        config.set("active", eventActive);
        config.set("eventType", activeEventType != null ? activeEventType.name() : "");
        config.set("endTime", eventEndTime);
        config.set("totalDuration", totalDurationSeconds);
        config.set("nextTime", nextEventTime);
        config.set("rotationIndex", rotationIndex);

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Gagal menyimpan event state ke data.yml: " + e.getMessage());
        }
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public EventType getActiveEventType() {
        return activeEventType;
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
        resetCountdownPointer();
        saveState();
    }
}
