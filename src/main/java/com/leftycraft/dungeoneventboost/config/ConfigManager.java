package com.leftycraft.dungeoneventboost.config;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final DungeonEventBoost plugin;

    // Event Timing
    private double intervalHours;
    private double durationMinutes;
    private boolean autoStartTimer;
    private boolean saveStateOnRestart;

    // MythicMobs Drops
    private boolean dropBoostEnabled;
    private double dropMultiplier;
    private boolean dropFilterByFaction;
    private Set<String> dropAllowedFactions;
    private boolean dropFilterByMobTypes;
    private Set<String> dropAllowedMobTypes;
    private boolean dropAllMythicMobs;
    private boolean dropRequirePlayerKiller;

    // AuraSkills EXP
    private boolean expBoostEnabled;
    private double expMultiplier;
    private boolean expFilterBySkills;
    private Set<String> expAllowedSkills;

    // Dungeon Damage
    private boolean damageBoostEnabled;
    private double damageMultiplier;
    private boolean damageFilterByFaction;
    private Set<String> damageAllowedFactions;
    private boolean damageFilterByMobTypes;
    private Set<String> damageAllowedMobTypes;
    private boolean damageAllMythicMobs;
    private Particle damageParticle;
    private int damageParticleCount;
    private boolean damagePlaySound;
    private Sound damageSound;
    private float damageSoundVolume;
    private float damageSoundPitch;

    // Worlds Whitelist
    private boolean worldWhitelistEnabled;
    private Set<String> allowedWorlds;

    // BossBar
    private boolean bossBarEnabled;
    private String bossBarTitle;
    private BarColor bossBarColor;
    private BarStyle bossBarStyle;

    // Countdown Warnings
    private boolean countdownWarningsEnabled;
    private List<Long> countdownWarningSeconds;

    // Titles
    private boolean titleStartEnabled;
    private String titleStartTitle;
    private String titleStartSubtitle;
    private int titleStartFadeIn;
    private int titleStartStay;
    private int titleStartFadeOut;

    private boolean titleEndEnabled;
    private String titleEndTitle;
    private String titleEndSubtitle;
    private int titleEndFadeIn;
    private int titleEndStay;
    private int titleEndFadeOut;

    // Sounds
    private boolean soundStartEnabled;
    private Sound soundStartSound;
    private float soundStartVolume;
    private float soundStartPitch;

    private boolean soundEndEnabled;
    private Sound soundEndSound;
    private float soundEndVolume;
    private float soundEndPitch;

    private boolean soundTickEnabled;
    private Sound soundTickSound;
    private float soundTickVolume;
    private float soundTickPitch;

    // Messages
    private String messagePrefix;
    private String messageNoPermission;
    private String messageReloaded;
    private String messageAlreadyActive;
    private String messageNotActive;
    private String messageStartedManual;
    private String messageStoppedManual;
    private List<String> messageBroadcastStart;
    private List<String> messageBroadcastEnd;
    private String messageBroadcastCountdown;
    private List<String> messageStatusActive;
    private List<String> messageStatusInactive;
    private List<String> messageHelp;

    public ConfigManager(DungeonEventBoost plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Event Timing
        intervalHours = config.getDouble("event.interval-hours", 6.0);
        durationMinutes = config.getDouble("event.duration-minutes", 10.0);
        autoStartTimer = config.getBoolean("event.auto-start-timer", true);
        saveStateOnRestart = config.getBoolean("event.save-state-on-restart", true);

        // Boost 1: MythicMobs Drops
        dropBoostEnabled = config.getBoolean("boosts.mythicmobs-drops.enabled", true);
        dropMultiplier = config.getDouble("boosts.mythicmobs-drops.multiplier", 2.0);
        dropFilterByFaction = config.getBoolean("boosts.mythicmobs-drops.filter-by-faction", true);
        dropAllowedFactions = toLowerSet(config.getStringList("boosts.mythicmobs-drops.allowed-factions"));
        dropFilterByMobTypes = config.getBoolean("boosts.mythicmobs-drops.filter-by-mob-types", false);
        dropAllowedMobTypes = toLowerSet(config.getStringList("boosts.mythicmobs-drops.allowed-mob-types"));
        dropAllMythicMobs = config.getBoolean("boosts.mythicmobs-drops.all-mythic-mobs", false);
        dropRequirePlayerKiller = config.getBoolean("boosts.mythicmobs-drops.require-player-killer", false);

        // Boost 2: AuraSkills EXP
        expBoostEnabled = config.getBoolean("boosts.auraskills-exp.enabled", true);
        expMultiplier = config.getDouble("boosts.auraskills-exp.multiplier", 2.0);
        expFilterBySkills = config.getBoolean("boosts.auraskills-exp.filter-by-skills", false);
        expAllowedSkills = toLowerSet(config.getStringList("boosts.auraskills-exp.allowed-skills"));

        // Boost 3: Dungeon Damage
        damageBoostEnabled = config.getBoolean("boosts.dungeon-damage.enabled", true);
        damageMultiplier = config.getDouble("boosts.dungeon-damage.multiplier", 2.0);
        damageFilterByFaction = config.getBoolean("boosts.dungeon-damage.filter-by-faction", true);
        damageAllowedFactions = toLowerSet(config.getStringList("boosts.dungeon-damage.allowed-factions"));
        damageFilterByMobTypes = config.getBoolean("boosts.dungeon-damage.filter-by-mob-types", false);
        damageAllowedMobTypes = toLowerSet(config.getStringList("boosts.dungeon-damage.allowed-mob-types"));
        damageAllMythicMobs = config.getBoolean("boosts.dungeon-damage.all-mythic-mobs", false);

        String particleStr = config.getString("boosts.dungeon-damage.effects.particle", "CRIT");
        damageParticle = parseParticle(particleStr, Particle.CRIT);
        damageParticleCount = config.getInt("boosts.dungeon-damage.effects.particle-count", 8);
        damagePlaySound = config.getBoolean("boosts.dungeon-damage.effects.play-sound", true);
        damageSound = parseSound(config.getString("boosts.dungeon-damage.effects.sound", "ENTITY_PLAYER_ATTACK_CRIT"), Sound.ENTITY_PLAYER_ATTACK_CRIT);
        damageSoundVolume = (float) config.getDouble("boosts.dungeon-damage.effects.sound-volume", 0.8);
        damageSoundPitch = (float) config.getDouble("boosts.dungeon-damage.effects.sound-pitch", 1.2);

        // Worlds Whitelist
        worldWhitelistEnabled = config.getBoolean("worlds.whitelist-enabled", false);
        allowedWorlds = toLowerSet(config.getStringList("worlds.allowed-worlds"));

        // BossBar
        bossBarEnabled = config.getBoolean("bossbar.enabled", true);
        bossBarTitle = config.getString("bossbar.title", "<gradient:#FFAA00:#FF5500><bold>⚡ EVENT BOOST AKTIF! ⚡</bold></gradient> <gray>(<white>{time_left}</white>)</gray>");
        bossBarColor = parseBarColor(config.getString("bossbar.color", "YELLOW"));
        bossBarStyle = parseBarStyle(config.getString("bossbar.style", "SOLID"));

        // Countdown Warnings
        countdownWarningsEnabled = config.getBoolean("countdown-warnings.enabled", true);
        List<Long> secondsList = new ArrayList<>();
        for (int sec : config.getIntegerList("countdown-warnings.times-in-seconds")) {
            secondsList.add((long) sec);
        }
        Collections.sort(secondsList, Collections.reverseOrder());
        countdownWarningSeconds = secondsList;

        // Titles
        titleStartEnabled = config.getBoolean("titles.start.enabled", true);
        titleStartTitle = config.getString("titles.start.title", "<gradient:#FFAA00:#FF5500><bold>⚡ EVENT BOOST AKTIF! ⚡</bold></gradient>");
        titleStartSubtitle = config.getString("titles.start.subtitle", "<green><bold>2x DROPS</bold></green> <dark_gray>•</dark_gray> <aqua><bold>2x SKILLS EXP</bold></aqua> <dark_gray>•</dark_gray> <red><bold>2x DAMAGE</bold></red>");
        titleStartFadeIn = config.getInt("titles.start.fade-in", 10);
        titleStartStay = config.getInt("titles.start.stay", 70);
        titleStartFadeOut = config.getInt("titles.start.fade-out", 20);

        titleEndEnabled = config.getBoolean("titles.end.enabled", true);
        titleEndTitle = config.getString("titles.end.title", "<red><bold>⚡ EVENT TELAH SELESAI ⚡</bold></red>");
        titleEndSubtitle = config.getString("titles.end.subtitle", "<gray>Event boost telah berakhir. Terima kasih!</gray>");
        titleEndFadeIn = config.getInt("titles.end.fade-in", 10);
        titleEndStay = config.getInt("titles.end.stay", 60);
        titleEndFadeOut = config.getInt("titles.end.fade-out", 20);

        // Sounds
        soundStartEnabled = config.getBoolean("sounds.start.enabled", true);
        soundStartSound = parseSound(config.getString("sounds.start.sound", "UI_TOAST_CHALLENGE_COMPLETE"), Sound.UI_TOAST_CHALLENGE_COMPLETE);
        soundStartVolume = (float) config.getDouble("sounds.start.volume", 1.0);
        soundStartPitch = (float) config.getDouble("sounds.start.pitch", 1.0);

        soundEndEnabled = config.getBoolean("sounds.end.enabled", true);
        soundEndSound = parseSound(config.getString("sounds.end.sound", "BLOCK_BEACON_DEACTIVATE"), Sound.BLOCK_BEACON_DEACTIVATE);
        soundEndVolume = (float) config.getDouble("sounds.end.volume", 1.0);
        soundEndPitch = (float) config.getDouble("sounds.end.pitch", 0.8);

        soundTickEnabled = config.getBoolean("sounds.countdown-tick.enabled", true);
        soundTickSound = parseSound(config.getString("sounds.countdown-tick.sound", "BLOCK_NOTE_BLOCK_PLING"), Sound.BLOCK_NOTE_BLOCK_PLING);
        soundTickVolume = (float) config.getDouble("sounds.countdown-tick.volume", 0.8);
        soundTickPitch = (float) config.getDouble("sounds.countdown-tick.pitch", 1.5);

        // Messages
        messagePrefix = config.getString("messages.prefix", "<gradient:#FFAA00:#FF5500><bold>[BOOST]</bold></gradient> ");
        messageNoPermission = config.getString("messages.no-permission", "<red>Kamu tidak memiliki izin untuk menggunakan perintah ini!</red>");
        messageReloaded = config.getString("messages.reloaded", "<green>Konfigurasi berhasil dimuat ulang!</green>");
        messageAlreadyActive = config.getString("messages.already-active", "<red>Event boost sedang berlangsung!</red>");
        messageNotActive = config.getString("messages.not-active", "<red>Tidak ada event boost yang sedang aktif.</red>");
        messageStartedManual = config.getString("messages.started-manual", "<green>Event boost dimulai secara manual!</green>");
        messageStoppedManual = config.getString("messages.stopped-manual", "<yellow>Event boost dihentikan.</yellow>");
        messageBroadcastStart = config.getStringList("messages.broadcast-start");
        messageBroadcastEnd = config.getStringList("messages.broadcast-end");
        messageBroadcastCountdown = config.getString("messages.broadcast-countdown", "<yellow>⚡ Event Boost akan dimulai dalam <gold>{time_left}</gold>!</yellow>");
        messageStatusActive = config.getStringList("messages.status-active");
        messageStatusInactive = config.getStringList("messages.status-inactive");
        messageHelp = config.getStringList("messages.help");
    }

    private Set<String> toLowerSet(List<String> list) {
        Set<String> set = new HashSet<>();
        if (list != null) {
            for (String item : list) {
                if (item != null) {
                    set.add(item.trim().toLowerCase());
                }
            }
        }
        return set;
    }

    private Sound parseSound(String name, Sound def) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("NONE")) {
            return def;
        }
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown sound name in config: " + name + ", fallback to " + def.name());
            return def;
        }
    }

    private Particle parseParticle(String name, Particle def) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("NONE")) {
            return null;
        }
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown particle name in config: " + name + ", fallback to " + def.name());
            return def;
        }
    }

    private BarColor parseBarColor(String name) {
        try {
            return BarColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarColor.YELLOW;
        }
    }

    private BarStyle parseBarStyle(String name) {
        try {
            return BarStyle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return BarStyle.SOLID;
        }
    }

    // Getters
    public double getIntervalHours() { return intervalHours; }
    public long getIntervalSeconds() { return (long) (intervalHours * 3600); }
    public double getDurationMinutes() { return durationMinutes; }
    public long getDurationSeconds() { return (long) (durationMinutes * 60); }
    public boolean isAutoStartTimer() { return autoStartTimer; }
    public boolean isSaveStateOnRestart() { return saveStateOnRestart; }

    public boolean isDropBoostEnabled() { return dropBoostEnabled; }
    public double getDropMultiplier() { return dropMultiplier; }
    public boolean isDropFilterByFaction() { return dropFilterByFaction; }
    public Set<String> getDropAllowedFactions() { return dropAllowedFactions; }
    public boolean isDropFilterByMobTypes() { return dropFilterByMobTypes; }
    public Set<String> getDropAllowedMobTypes() { return dropAllowedMobTypes; }
    public boolean isDropAllMythicMobs() { return dropAllMythicMobs; }
    public boolean isDropRequirePlayerKiller() { return dropRequirePlayerKiller; }

    public boolean isExpBoostEnabled() { return expBoostEnabled; }
    public double getExpMultiplier() { return expMultiplier; }
    public boolean isExpFilterBySkills() { return expFilterBySkills; }
    public Set<String> getExpAllowedSkills() { return expAllowedSkills; }

    public boolean isDamageBoostEnabled() { return damageBoostEnabled; }
    public double getDamageMultiplier() { return damageMultiplier; }
    public boolean isDamageFilterByFaction() { return damageFilterByFaction; }
    public Set<String> getDamageAllowedFactions() { return damageAllowedFactions; }
    public boolean isDamageFilterByMobTypes() { return damageFilterByMobTypes; }
    public Set<String> getDamageAllowedMobTypes() { return damageAllowedMobTypes; }
    public boolean isDamageAllMythicMobs() { return damageAllMythicMobs; }
    public Particle getDamageParticle() { return damageParticle; }
    public int getDamageParticleCount() { return damageParticleCount; }
    public boolean isDamagePlaySound() { return damagePlaySound; }
    public Sound getDamageSound() { return damageSound; }
    public float getDamageSoundVolume() { return damageSoundVolume; }
    public float getDamageSoundPitch() { return damageSoundPitch; }

    public boolean isWorldWhitelistEnabled() { return worldWhitelistEnabled; }
    public Set<String> getAllowedWorlds() { return allowedWorlds; }

    public boolean isBossBarEnabled() { return bossBarEnabled; }
    public String getBossBarTitle() { return bossBarTitle; }
    public BarColor getBossBarColor() { return bossBarColor; }
    public BarStyle getBossBarStyle() { return bossBarStyle; }

    public boolean isCountdownWarningsEnabled() { return countdownWarningsEnabled; }
    public List<Long> getCountdownWarningSeconds() { return countdownWarningSeconds; }

    public boolean isTitleStartEnabled() { return titleStartEnabled; }
    public String getTitleStartTitle() { return titleStartTitle; }
    public String getTitleStartSubtitle() { return titleStartSubtitle; }
    public int getTitleStartFadeIn() { return titleStartFadeIn; }
    public int getTitleStartStay() { return titleStartStay; }
    public int getTitleStartFadeOut() { return titleStartFadeOut; }

    public boolean isTitleEndEnabled() { return titleEndEnabled; }
    public String getTitleEndTitle() { return titleEndTitle; }
    public String getTitleEndSubtitle() { return titleEndSubtitle; }
    public int getTitleEndFadeIn() { return titleEndFadeIn; }
    public int getTitleEndStay() { return titleEndStay; }
    public int getTitleEndFadeOut() { return titleEndFadeOut; }

    public boolean isSoundStartEnabled() { return soundStartEnabled; }
    public Sound getSoundStartSound() { return soundStartSound; }
    public float getSoundStartVolume() { return soundStartVolume; }
    public float getSoundStartPitch() { return soundStartPitch; }

    public boolean isSoundEndEnabled() { return soundEndEnabled; }
    public Sound getSoundEndSound() { return soundEndSound; }
    public float getSoundEndVolume() { return soundEndVolume; }
    public float getSoundEndPitch() { return soundEndPitch; }

    public boolean isSoundTickEnabled() { return soundTickEnabled; }
    public Sound getSoundTickSound() { return soundTickSound; }
    public float getSoundTickVolume() { return soundTickVolume; }
    public float getSoundTickPitch() { return soundTickPitch; }

    public String getMessagePrefix() { return messagePrefix; }
    public String getMessageNoPermission() { return messageNoPermission; }
    public String getMessageReloaded() { return messageReloaded; }
    public String getMessageAlreadyActive() { return messageAlreadyActive; }
    public String getMessageNotActive() { return messageNotActive; }
    public String getMessageStartedManual() { return messageStartedManual; }
    public String getMessageStoppedManual() { return messageStoppedManual; }
    public List<String> getMessageBroadcastStart() { return messageBroadcastStart; }
    public List<String> getMessageBroadcastEnd() { return messageBroadcastEnd; }
    public String getMessageBroadcastCountdown() { return messageBroadcastCountdown; }
    public List<String> getMessageStatusActive() { return messageStatusActive; }
    public List<String> getMessageStatusInactive() { return messageStatusInactive; }
    public List<String> getMessageHelp() { return messageHelp; }
}
