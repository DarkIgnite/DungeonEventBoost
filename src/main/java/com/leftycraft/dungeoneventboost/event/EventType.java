package com.leftycraft.dungeoneventboost.event;

import org.bukkit.boss.BarColor;

public enum EventType {
    DROPS("drops", "2x Dungeon Mob Drops", "<green>2x Mob Drops</green>", BarColor.GREEN),
    EXP_SKILLS("exp_skills", "2x AuraSkills EXP", "<aqua>2x AuraSkills EXP</aqua>", BarColor.BLUE),
    DAMAGE_DEALT("damage", "2x Damage ke Mob Dungeon", "<red>2x Damage Dealt</red>", BarColor.RED),
    DEFENSE("defense", "50% Damage Reduction Mob Dungeon", "<yellow>50% Defense Boost</yellow>", BarColor.YELLOW),
    ECONOMY("economy", "2x Jobs & Economy Payment", "<gold>2x Jobs & Money</gold>", BarColor.YELLOW),
    EXP_VANILLA("exp_vanilla", "2x Vanilla EXP Orbs", "<light_purple>2x Vanilla EXP</light_purple>", BarColor.PURPLE),
    SWIFTNESS("swiftness", "Dungeon Berserk (Speed II & Haste II)", "<aqua>Speed II & Haste II</aqua>", BarColor.BLUE),
    MEGA_FRENZY("mega_frenzy", "⚡ MEGA ALL-STAR BOOST ⚡", "<gradient:#FFAA00:#FF5500><b>SEMUA BOOST AKTIF</b></gradient>", BarColor.PURPLE),
    KOKI_MBG_5X("koki_5x", "5x Money Koki MBG Boost", "<gradient:#FFAA00:#FFD700><bold>5x MONEY KOKI MBG</bold></gradient>", BarColor.YELLOW),
    KOKI_MBG_10X("koki_10x", "10x Money Koki MBG Boost", "<gradient:#FF0844:#FFB199><bold>10x MONEY KOKI MBG</bold></gradient>", BarColor.RED);

    private final String id;
    private final String displayName;
    private final String shortTag;
    private final BarColor defaultBarColor;

    EventType(String id, String displayName, String shortTag, BarColor defaultBarColor) {
        this.id = id;
        this.displayName = displayName;
        this.shortTag = shortTag;
        this.defaultBarColor = defaultBarColor;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortTag() {
        return shortTag;
    }

    public BarColor getDefaultBarColor() {
        return defaultBarColor;
    }

    public static EventType fromString(String str) {
        if (str == null || str.isEmpty()) return null;
        String clean = str.trim().toLowerCase().replace("-", "_");
        if (clean.equals("all") || clean.equals("frenzy") || clean.equals("all_star")) {
            return MEGA_FRENZY;
        }
        if (clean.equals("koki_5x") || clean.equals("koki5x") || clean.equals("mbg_5x") || clean.equals("mbg5x") || clean.equals("5x_koki") || clean.equals("5x")) {
            return KOKI_MBG_5X;
        }
        if (clean.equals("koki_10x") || clean.equals("koki10x") || clean.equals("mbg_10x") || clean.equals("mbg10x") || clean.equals("10x_koki") || clean.equals("10x")) {
            return KOKI_MBG_10X;
        }
        for (EventType type : values()) {
            if (type.name().equalsIgnoreCase(clean) || type.id.equalsIgnoreCase(clean)) {
                return type;
            }
        }
        return null;
    }
}
