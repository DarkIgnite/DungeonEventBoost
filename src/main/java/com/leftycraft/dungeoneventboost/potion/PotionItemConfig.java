package com.leftycraft.dungeoneventboost.potion;

import com.leftycraft.dungeoneventboost.event.EventType;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.List;

public class PotionItemConfig {

    private final EventType eventType;
    private final Material material;
    private final Color potionColor;
    private final int customModelData;
    private final boolean glow;
    private final long defaultDurationSeconds;
    private final String name;
    private final List<String> lore;

    public PotionItemConfig(EventType eventType,
                            Material material,
                            Color potionColor,
                            int customModelData,
                            boolean glow,
                            long defaultDurationSeconds,
                            String name,
                            List<String> lore) {
        this.eventType = eventType;
        this.material = material != null ? material : Material.POTION;
        this.potionColor = potionColor;
        this.customModelData = customModelData;
        this.glow = glow;
        this.defaultDurationSeconds = defaultDurationSeconds;
        this.name = name;
        this.lore = lore;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Material getMaterial() {
        return material;
    }

    public Color getPotionColor() {
        return potionColor;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public boolean isGlow() {
        return glow;
    }

    public long getDefaultDurationSeconds() {
        return defaultDurationSeconds;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }
}
