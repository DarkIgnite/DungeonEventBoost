package com.leftycraft.dungeoneventboost.config;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.event.EventType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final DungeonEventBoost plugin;

    // Event Scheduling
    private double intervalHours;
    private double durationMinutes;
    private boolean autoStartTimer;
    private boolean saveStateOnRestart;
    private String selectionMode; // ROTATION or RANDOM
    private List<EventType> enabledEvents;
    private double megaFrenzyChance;

    // Boost 1: Drops
    private boolean dropBoostEnabled;
    private double dropMultiplier;
    private boolean dropFilterByFaction;
    private Set<String> dropAllowedFactions;
    private boolean dropFilterByMobTypes;
    private Set<String> dropAllowedMobTypes;
    private boolean dropAllMythicMobs;
    private boolean dropRequirePlayerKiller;

    // Boost 2: AuraSkills EXP
    private boolean expBoostEnabled;
    private double expMultiplier;
    private boolean expFilterBySkills;
    private Set<String> expAllowedSkills;

    // Boost 3: Dungeon Damage
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

    // Boost 4: Dungeon Defense (Damage Reduction)
    private boolean defenseBoostEnabled;
    private double defenseMultiplier;

    // Boost 5: Economy (Jobs & Vault)
    private boolean economyBoostEnabled;
    private double economyMultiplier;
    private boolean economyDoublePoints;

    // Boost 6: Vanilla EXP
    private boolean vanillaExpBoostEnabled;
    private double vanillaExpMultiplier;

    // Boost 7: Swiftness (Speed + Haste)
    private boolean swiftnessBoostEnabled;
    private int swiftnessDurationSeconds;
    private int swiftnessSpeedAmplifier;
    private int swiftnessHasteAmplifier;

    // Worlds Whitelist
    private boolean worldWhitelistEnabled;
    private Set<String> allowedWorlds;

    // BossBar & Warnings
    private boolean bossBarEnabled;
    private BarStyle bossBarStyle;
    private boolean countdownWarningsEnabled;
    private List<Long> countdownWarningSeconds;

    // Titles general
    private boolean titleStartEnabled;
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

    // Event Info Map
    private final Map<EventType, EventDisplayInfo> eventDisplayMap = new EnumMap<>(EventType.class);

    public ConfigManager(DungeonEventBoost plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Event Timing & Mode
        intervalHours = config.getDouble("event.interval-hours", 6.0);
        durationMinutes = config.getDouble("event.duration-minutes", 10.0);
        autoStartTimer = config.getBoolean("event.auto-start-timer", true);
        saveStateOnRestart = config.getBoolean("event.save-state-on-restart", true);
        selectionMode = config.getString("event.selection-mode", "ROTATION").toUpperCase();
        megaFrenzyChance = config.getDouble("event.mega-frenzy-chance", 0.1);

        List<String> eventNames = config.getStringList("event.enabled-events");
        List<EventType> enabledList = new ArrayList<>();
        for (String name : eventNames) {
            EventType type = EventType.fromString(name);
            if (type != null && !enabledList.contains(type)) {
                enabledList.add(type);
            }
        }
        if (enabledList.isEmpty()) {
            enabledList.addAll(Arrays.asList(EventType.values()));
        }
        this.enabledEvents = enabledList;

        // Boost 1: Drops
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

        // Boost 4: Defense
        defenseBoostEnabled = config.getBoolean("boosts.dungeon-defense.enabled", true);
        defenseMultiplier = config.getDouble("boosts.dungeon-defense.multiplier", 0.5);

        // Boost 5: Economy
        economyBoostEnabled = config.getBoolean("boosts.economy-jobs.enabled", true);
        economyMultiplier = config.getDouble("boosts.economy-jobs.multiplier", 2.0);
        economyDoublePoints = config.getBoolean("boosts.economy-jobs.double-points", true);

        // Boost 6: Vanilla EXP
        vanillaExpBoostEnabled = config.getBoolean("boosts.vanilla-exp.enabled", true);
        vanillaExpMultiplier = config.getDouble("boosts.vanilla-exp.multiplier", 2.0);

        // Boost 7: Swiftness
        swiftnessBoostEnabled = config.getBoolean("boosts.dungeon-swiftness.enabled", true);
        swiftnessDurationSeconds = config.getInt("boosts.dungeon-swiftness.duration-seconds", 20);
        swiftnessSpeedAmplifier = config.getInt("boosts.dungeon-swiftness.speed-amplifier", 1);
        swiftnessHasteAmplifier = config.getInt("boosts.dungeon-swiftness.haste-amplifier", 1);

        // Worlds Whitelist
        worldWhitelistEnabled = config.getBoolean("worlds.whitelist-enabled", false);
        allowedWorlds = toLowerSet(config.getStringList("worlds.allowed-worlds"));

        // BossBar & Warnings
        bossBarEnabled = config.getBoolean("bossbar.enabled", true);
        bossBarStyle = parseBarStyle(config.getString("bossbar.style", "SOLID"));

        countdownWarningsEnabled = config.getBoolean("countdown-warnings.enabled", true);
        List<Long> secondsList = new ArrayList<>();
        for (int sec : config.getIntegerList("countdown-warnings.times-in-seconds")) {
            secondsList.add((long) sec);
        }
        Collections.sort(secondsList, Collections.reverseOrder());
        countdownWarningSeconds = secondsList;

        // Titles general
        titleStartEnabled = config.getBoolean("titles.start.enabled", true);
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
        messageStartedManual = config.getString("messages.started-manual", "<green>Event boost <white>{event_name}</white> dimulai secara manual selama <white>{duration}</white> menit!</green>");
        messageStoppedManual = config.getString("messages.stopped-manual", "<yellow>Event boost dihentikan.</yellow>");
        messageBroadcastStart = config.getStringList("messages.broadcast-start");
        messageBroadcastEnd = config.getStringList("messages.broadcast-end");
        messageBroadcastCountdown = config.getString("messages.broadcast-countdown", "<yellow>⚡ Event Boost akan dimulai dalam <gold>{time_left}</gold>!</yellow>");
        messageStatusActive = config.getStringList("messages.status-active");
        messageStatusInactive = config.getStringList("messages.status-inactive");
        messageHelp = config.getStringList("messages.help");

        // Load Event Display Info
        loadEventDisplays(config);
    }

    private void loadEventDisplays(FileConfiguration config) {
        eventDisplayMap.clear();
        for (EventType type : EventType.values()) {
            String path = "events-info." + type.name();
            String name = config.getString(path + ".name", type.getDisplayName());
            String shortTag = config.getString(path + ".short-tag", type.getShortTag());
            String bossBar = config.getString(path + ".bossbar", "<gradient:#FFAA00:#FF5500><bold>⚡ " + name + " ⚡</bold></gradient> <gray>(<white>{time_left}</white>)</gray>");
            BarColor color = parseBarColor(config.getString(path + ".bar-color", type.getDefaultBarColor().name()));
            String title = config.getString(path + ".title", "<gradient:#FFAA00:#FF5500><bold>⚡ " + name + " ⚡</bold></gradient>");
            String subtitle = config.getString(path + ".subtitle", shortTag);

            eventDisplayMap.put(type, new EventDisplayInfo(name, shortTag, bossBar, color, title, subtitle));
        }
    }

    public EventDisplayInfo getDisplayInfo(EventType type) {
        return eventDisplayMap.getOrDefault(type, new EventDisplayInfo(type.getDisplayName(), type.getShortTag(), type.getDisplayName(), type.getDefaultBarColor(), type.getDisplayName(), type.getShortTag()));
    }

    private Set<String> toLowerSet(List<String> list) {
        Set<String> set = new HashSet<>();
        if (list != null) {
            for (String item : list) {
                if (item != null) set.add(item.trim().toLowerCase());
            }
        }
        return set;
    }

    private Sound parseSound(String name, Sound def) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("NONE")) return def;
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    private Particle parseParticle(String name, Particle def) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase("NONE")) return null;
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
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
    public String getSelectionMode() { return selectionMode; }
    public List<EventType> getEnabledEvents() { return enabledEvents; }
    public double getMegaFrenzyChance() { return megaFrenzyChance; }

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

    public boolean isDefenseBoostEnabled() { return defenseBoostEnabled; }
    public double getDefenseMultiplier() { return defenseMultiplier; }

    public boolean isEconomyBoostEnabled() { return economyBoostEnabled; }
    public double getEconomyMultiplier() { return economyMultiplier; }
    public boolean isEconomyDoublePoints() { return economyDoublePoints; }

    public boolean isVanillaExpBoostEnabled() { return vanillaExpBoostEnabled; }
    public double getVanillaExpMultiplier() { return vanillaExpMultiplier; }

    public boolean isSwiftnessBoostEnabled() { return swiftnessBoostEnabled; }
    public int getSwiftnessDurationSeconds() { return swiftnessDurationSeconds; }
    public int getSwiftnessSpeedAmplifier() { return swiftnessSpeedAmplifier; }
    public int getSwiftnessHasteAmplifier() { return swiftnessHasteAmplifier; }

    public boolean isWorldWhitelistEnabled() { return worldWhitelistEnabled; }
    public Set<String> getAllowedWorlds() { return allowedWorlds; }

    public boolean isBossBarEnabled() { return bossBarEnabled; }
    public BarStyle getBossBarStyle() { return bossBarStyle; }

    public boolean isCountdownWarningsEnabled() { return countdownWarningsEnabled; }
    public List<Long> getCountdownWarningSeconds() { return countdownWarningSeconds; }

    public boolean isTitleStartEnabled() { return titleStartEnabled; }
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

    public static class EventDisplayInfo {
        private final String name;
        private final String shortTag;
        private final String bossBarTitle;
        private final BarColor barColor;
        private final String title;
        private final String subtitle;

        public EventDisplayInfo(String name, String shortTag, String bossBarTitle, BarColor barColor, String title, String subtitle) {
            this.name = name;
            this.shortTag = shortTag;
            this.bossBarTitle = bossBarTitle;
            this.barColor = barColor;
            this.title = title;
            this.subtitle = subtitle;
        }

        public String getName() { return name; }
        public String getShortTag() { return shortTag; }
        public String getBossBarTitle() { return bossBarTitle; }
        public BarColor getBarColor() { return barColor; }
        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
    }
}
