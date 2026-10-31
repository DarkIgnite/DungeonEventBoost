package com.leftycraft.dungeoneventboost.potion;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import com.leftycraft.dungeoneventboost.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class BoostItemManager {

    private final DungeonEventBoost plugin;
    private final NamespacedKey keyType;
    private final NamespacedKey keyDuration;
    private final NamespacedKey keyMultiplier;

    private final Map<EventType, PotionItemConfig> itemConfigs = new EnumMap<>(EventType.class);

    public BoostItemManager(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.keyType = new NamespacedKey(plugin, "boost_type");
        this.keyDuration = new NamespacedKey(plugin, "boost_duration");
        this.keyMultiplier = new NamespacedKey(plugin, "boost_multiplier");
    }

    public void loadConfigs(FileConfiguration config) {
        itemConfigs.clear();
        ConfigurationSection section = config.getConfigurationSection("potion-items.items");

        for (EventType type : EventType.values()) {
            ConfigurationSection itemSec = section != null ? section.getConfigurationSection(type.name()) : null;

            Material material = Material.POTION;
            Color color = getDefaultColor(type);
            int customModelData = 0;
            boolean glow = true;
            long defaultDurationSeconds = 600L;
            String name = getDefaultName(type);
            List<String> lore = getDefaultLore(type);

            if (itemSec != null) {
                String matStr = itemSec.getString("material", "POTION");
                Material parsedMat = Material.matchMaterial(matStr);
                if (parsedMat != null) {
                    material = parsedMat;
                }

                String hexColor = itemSec.getString("potion-color");
                if (hexColor != null && !hexColor.isEmpty()) {
                    color = parseColor(hexColor, color);
                }

                customModelData = itemSec.getInt("custom-model-data", 0);
                glow = itemSec.getBoolean("glow", true);
                defaultDurationSeconds = itemSec.getLong("default-duration-minutes", 10) * 60L;
                name = itemSec.getString("name", name);
                List<String> customLore = itemSec.getStringList("lore");
                if (!customLore.isEmpty()) {
                    lore = customLore;
                }
            }

            PotionItemConfig itemConfig = new PotionItemConfig(
                    type, material, color, customModelData, glow, defaultDurationSeconds, name, lore
            );
            itemConfigs.put(type, itemConfig);
        }
    }

    public ItemStack createBoostPotion(EventType type, long durationSeconds, int amount) {
        PotionItemConfig cfg = itemConfigs.get(type);
        if (cfg == null) {
            cfg = new PotionItemConfig(type, Material.POTION, getDefaultColor(type), 0, true, 600L, getDefaultName(type), getDefaultLore(type));
        }

        if (durationSeconds <= 0) {
            durationSeconds = cfg.getDefaultDurationSeconds();
        }
        if (amount <= 0) {
            amount = 1;
        }

        ItemStack item = new ItemStack(cfg.getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        // Display Name
        meta.displayName(TextUtil.parse(cfg.getName()));

        // Lore
        String formattedDuration = TextUtil.formatDuration(durationSeconds);
        String digitalDuration = TextUtil.formatDigitalClock(durationSeconds);
        long minutes = Math.max(1, durationSeconds / 60);

        List<Component> loreComponents = new ArrayList<>();
        for (String line : cfg.getLore()) {
            String replaced = line
                    .replace("{duration}", formattedDuration)
                    .replace("{duration_digital}", digitalDuration)
                    .replace("{duration_minutes}", String.valueOf(minutes))
                    .replace("{event_name}", plugin.getConfigManager().getDisplayInfo(type).getName());
            loreComponents.add(TextUtil.parse(replaced));
        }
        meta.lore(loreComponents);

        // Potion Color & Flags
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.setColor(cfg.getPotionColor());
            potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        }

        // Glow
        if (cfg.isGlow()) {
            meta.setEnchantmentGlintOverride(true);
        }

        // Custom Model Data
        if (cfg.getCustomModelData() > 0) {
            meta.setCustomModelData(cfg.getCustomModelData());
        }

        // PersistentDataContainer
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(keyType, PersistentDataType.STRING, type.name());
        pdc.set(keyDuration, PersistentDataType.LONG, durationSeconds);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isBoostPotion(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(keyType, PersistentDataType.STRING);
    }

    public EventType getBoostType(ItemStack item) {
        if (!isBoostPotion(item)) {
            return null;
        }
        String typeStr = item.getItemMeta().getPersistentDataContainer().get(keyType, PersistentDataType.STRING);
        return EventType.fromString(typeStr);
    }

    public long getBoostDuration(ItemStack item) {
        if (!isBoostPotion(item)) {
            return 0L;
        }
        Long duration = item.getItemMeta().getPersistentDataContainer().get(keyDuration, PersistentDataType.LONG);
        return duration != null ? duration : 600L;
    }

    public double getBoostMultiplier(ItemStack item) {
        if (!isBoostPotion(item)) {
            return 0.0;
        }
        Double mult = item.getItemMeta().getPersistentDataContainer().get(keyMultiplier, PersistentDataType.DOUBLE);
        return mult != null ? mult : 0.0;
    }

    public boolean consumeBoost(Player player, ItemStack item, EquipmentSlot slot) {
        if (player == null || item == null || !isBoostPotion(item)) {
            return false;
        }

        EventType type = getBoostType(item);
        if (type == null) {
            return false;
        }

        long durationSeconds = getBoostDuration(item);
        double customMultiplier = getBoostMultiplier(item);

        PlayerBoostManager boostManager = plugin.getEventManager().getPlayerBoostManager();
        boolean extended = boostManager.hasDirectBoost(player.getUniqueId(), type);

        boolean success = boostManager.addBoost(player, type, durationSeconds, customMultiplier);
        if (!success) {
            // e.g. stack mode was DENY and effect is already active
            TextUtil.sendMessage(player, plugin.getConfigManager().getMessagePrefix() + "<red>Kamu masih memiliki efek ramuan ini yang sedang aktif!</red>");
            return false;
        }

        // Deduct item from inventory
        GameMode gm = player.getGameMode();
        if (gm != GameMode.CREATIVE) {
            boolean returnBottle = plugin.getConfigManager().isPotionReturnEmptyBottle() && item.getType() == Material.POTION;
            int amount = item.getAmount();

            if (amount <= 1) {
                if (returnBottle) {
                    item.setType(Material.GLASS_BOTTLE);
                    item.setItemMeta(null);
                } else {
                    if (slot == EquipmentSlot.HAND) {
                        player.getInventory().setItemInMainHand(null);
                    } else if (slot == EquipmentSlot.OFF_HAND) {
                        player.getInventory().setItemInOffHand(null);
                    } else {
                        item.setAmount(0);
                    }
                }
            } else {
                item.setAmount(amount - 1);
                if (returnBottle) {
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                    if (!leftover.isEmpty()) {
                        for (ItemStack drop : leftover.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), drop);
                        }
                    }
                }
            }
        }

        // Sound effects
        if (plugin.getConfigManager().isPotionSoundConsumeEnabled()) {
            Sound sound = plugin.getConfigManager().getPotionSoundConsume();
            if (sound != null) {
                player.playSound(
                        player.getLocation(),
                        sound,
                        plugin.getConfigManager().getPotionSoundConsumeVolume(),
                        plugin.getConfigManager().getPotionSoundConsumePitch()
                );
                // Also play to nearby players so drinking sound is audible in the world
                player.getWorld().playSound(
                        player.getLocation(),
                        sound,
                        0.7f,
                        plugin.getConfigManager().getPotionSoundConsumePitch()
                );
            }
        }

        if (plugin.getConfigManager().isPotionSoundConsumeSuccessEnabled()) {
            Sound successSound = plugin.getConfigManager().getPotionSoundConsumeSuccess();
            if (successSound != null) {
                player.playSound(
                        player.getLocation(),
                        successSound,
                        plugin.getConfigManager().getPotionSoundConsumeSuccessVolume(),
                        plugin.getConfigManager().getPotionSoundConsumeSuccessPitch()
                );
            }
        }

        // Particle effect
        if (plugin.getConfigManager().isPotionParticleConsumeEnabled()) {
            Particle particle = plugin.getConfigManager().getPotionParticleConsume();
            if (particle != null) {
                try {
                    Location loc = player.getLocation().add(0, 1.0, 0);
                    int count = plugin.getConfigManager().getPotionParticleConsumeCount();
                    Class<?> dataType = particle.getDataType();
                    if (dataType == Color.class) {
                        player.getWorld().spawnParticle(
                                particle,
                                loc,
                                count,
                                0.3, 0.5, 0.3, 0.05,
                                Color.fromRGB(80, 200, 255)
                        );
                    } else if (dataType == Particle.DustOptions.class) {
                        player.getWorld().spawnParticle(
                                particle,
                                loc,
                                count,
                                0.3, 0.5, 0.3, 0.05,
                                new Particle.DustOptions(Color.fromRGB(80, 200, 255), 1.0f)
                        );
                    } else if (dataType == Void.class) {
                        player.getWorld().spawnParticle(
                                particle,
                                loc,
                                count,
                                0.3, 0.5, 0.3, 0.05
                        );
                    } else {
                        player.getWorld().spawnParticle(particle, loc, count, 0.3, 0.5, 0.3, 0.05);
                    }
                } catch (Throwable t) {
                    plugin.getLogger().warning("Could not spawn particle " + particle + ": " + t.getMessage());
                }
            }
        }

        // If swiftness or mega frenzy, grant instant potion effect for responsiveness
        if (type == EventType.SWIFTNESS || type == EventType.MEGA_FRENZY) {
            int ticks = Math.min((int) (durationSeconds * 20), 20 * 60); // 1 min initial or full
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, plugin.getConfigManager().getSwiftnessSpeedAmplifier(), false, true, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, ticks, plugin.getConfigManager().getSwiftnessHasteAmplifier(), false, true, true));
        }

        // Notification messages
        ConfigManager.EventDisplayInfo display = plugin.getConfigManager().getDisplayInfo(type);
        PotionItemConfig cfg = itemConfigs.get(type);
        String potionName = cfg != null ? cfg.getName() : display.getName();

        if (extended) {
            long totalRemaining = boostManager.getRemainingSeconds(player.getUniqueId(), type);
            String extendedMsg = plugin.getConfigManager().getMessagePotionExtended()
                    .replace("{event_name}", display.getName())
                    .replace("{item_name}", potionName)
                    .replace("{duration}", TextUtil.formatDuration(totalRemaining));
            TextUtil.sendMessage(player, plugin.getConfigManager().getMessagePrefix() + extendedMsg);
        } else {
            String consumeMsg = plugin.getConfigManager().getMessagePotionConsumed()
                    .replace("{event_name}", display.getName())
                    .replace("{item_name}", potionName)
                    .replace("{duration}", TextUtil.formatDuration(durationSeconds));
            TextUtil.sendMessage(player, plugin.getConfigManager().getMessagePrefix() + consumeMsg);
        }

        return true;
    }

    public PotionItemConfig getConfig(EventType type) {
        return itemConfigs.get(type);
    }

    private Color getDefaultColor(EventType type) {
        return switch (type) {
            case DROPS -> Color.fromRGB(85, 255, 85);       // Emerald Green (#55FF55)
            case EXP_SKILLS -> Color.fromRGB(0, 210, 255);    // Sky Cyan (#00D2FF)
            case DAMAGE_DEALT -> Color.fromRGB(255, 60, 60);   // Crimson Red (#FF3C3C)
            case DEFENSE -> Color.fromRGB(255, 215, 0);       // Gold (#FFD700)
            case ECONOMY -> Color.fromRGB(255, 165, 0);       // Orange (#FFA500)
            case EXP_VANILLA -> Color.fromRGB(224, 86, 253);  // Magenta Purple (#E056FD)
            case SWIFTNESS -> Color.fromRGB(0, 255, 255);     // Electric Aqua (#00FFFF)
            case MEGA_FRENZY -> Color.fromRGB(255, 20, 147);  // Deep Pink (#FF1493)
            case KOKI_MBG_5X -> Color.fromRGB(243, 156, 18);  // Amber Gold (#F39C12)
            case KOKI_MBG_10X -> Color.fromRGB(231, 76, 60);  // Dark Red (#E74C3C)
        };
    }

    private String getDefaultName(EventType type) {
        return switch (type) {
            case DROPS -> "<gradient:#55FF55:#00AA00><bold>🧪 Ramuan 2x Dungeon Drops</bold></gradient>";
            case EXP_SKILLS -> "<gradient:#55FFFF:#00AAAA><bold>🧪 Ramuan 2x AuraSkills EXP</bold></gradient>";
            case DAMAGE_DEALT -> "<gradient:#FF5555:#AA0000><bold>🧪 Ramuan 2x Dungeon Damage</bold></gradient>";
            case DEFENSE -> "<gradient:#FFFF55:#FFAA00><bold>🧪 Ramuan 50% Dungeon Defense</bold></gradient>";
            case ECONOMY -> "<gradient:#FFAA00:#FF5500><bold>🧪 Ramuan 2x Jobs & Economy</bold></gradient>";
            case EXP_VANILLA -> "<gradient:#FF55FF:#AA00AA><bold>🧪 Ramuan 2x Vanilla EXP</bold></gradient>";
            case SWIFTNESS -> "<gradient:#55FFFF:#55FF55><bold>🧪 Ramuan Dungeon Berserk</bold></gradient>";
            case MEGA_FRENZY -> "<gradient:#FFAA00:#FF0055><bold>⚡ RAMUAN MEGA ALL-STAR ⚡</bold></gradient>";
            case KOKI_MBG_5X -> "<gradient:#FFAA00:#FFD700><bold>🧪 Ramuan 5x Uang Koki MBG</bold></gradient>";
            case KOKI_MBG_10X -> "<gradient:#FF0844:#FFB199><bold>🧪 Ramuan 10x Uang Koki MBG</bold></gradient>";
        };
    }

    private List<String> getDefaultLore(EventType type) {
        List<String> lore = new ArrayList<>();
        lore.add("<dark_gray><st>━━━━━━━━━━━━━━━━━━━━━━━━━━━━</st></dark_gray>");
        lore.add("<gray>Tipe: <yellow><bold>Ramuan Event Boost</bold></yellow></gray>");
        lore.add("");
        lore.add("<white>Efek Ramuan:</white>");

        switch (type) {
            case DROPS -> {
                lore.add("  <gold>»</gold> <green>Menggandakan seluruh item drop dari</green>");
                lore.add("    <green>mob dungeon MythicMobs!</green>");
            }
            case EXP_SKILLS -> {
                lore.add("  <gold>»</gold> <aqua>Meningkatkan perolehan EXP AuraSkills</aqua>");
                lore.add("    <aqua>sebesar 2x lipat untuk seluruh skill!</aqua>");
            }
            case DAMAGE_DEALT -> {
                lore.add("  <gold>»</gold> <red>Serangan fisik & panahmu ke mob</red>");
                lore.add("    <red>dungeon menghasilkan 2x lipat damage!</red>");
            }
            case DEFENSE -> {
                lore.add("  <gold>»</gold> <yellow>Damage yang kamu terima dari mob</yellow>");
                lore.add("    <yellow>dungeon berkurang sebesar 50%!</yellow>");
            }
            case ECONOMY -> {
                lore.add("  <gold>»</gold> <gold>Gaji pekerjaan Jobs dan drop uang</gold>");
                lore.add("    <gold>dari mob digandakan 2x lipat!</gold>");
            }
            case EXP_VANILLA -> {
                lore.add("  <gold>»</gold> <light_purple>Perolehan bola EXP vanilla dari mob</light_purple>");
                lore.add("    <light_purple>dan menambang digandakan 2x lipat!</light_purple>");
            }
            case SWIFTNESS -> {
                lore.add("  <gold>»</gold> <aqua>Otomatis memperoleh efek Speed II &</aqua>");
                lore.add("    <aqua>Haste II saat bertarung melawan dungeon mob!</aqua>");
            }
            case MEGA_FRENZY -> {
                lore.add("  <gold>»</gold> <gradient:#FFAA00:#FF0055><bold>SELURUH EFEK EVENT BOOST AKTIF</bold></gradient>");
                lore.add("    <yellow>Drops, Skills, Damage, Defense, Money,</yellow>");
                lore.add("    <yellow>Vanilla EXP, Swiftness & Koki MBG!</yellow>");
            }
            case KOKI_MBG_5X -> {
                lore.add("  <gold>»</gold> <gold>Pendapatan kasir Dapur Koki MBG kamu</gold>");
                lore.add("    <gold>dikalikan 5x lipat lebih besar!</gold>");
            }
            case KOKI_MBG_10X -> {
                lore.add("  <gold>»</gold> <red>Pendapatan kasir Dapur Koki MBG kamu</red>");
                lore.add("    <red>dikalikan 10x lipat lebih besar!</red>");
            }
        }

        lore.add("");
        lore.add("<gray>Durasi Efek: <yellow><bold>{duration}</bold></yellow></gray>");
        lore.add("<dark_gray><st>━━━━━━━━━━━━━━━━━━━━━━━━━━━━</st></dark_gray>");
        lore.add("<yellow>⚡ Klik Kanan untuk meminum ramuan ini!</yellow>");
        return lore;
    }

    private Color parseColor(String hex, Color def) {
        if (hex == null || hex.isEmpty()) return def;
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            int rgb = Integer.parseInt(clean, 16);
            return Color.fromRGB(rgb);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
