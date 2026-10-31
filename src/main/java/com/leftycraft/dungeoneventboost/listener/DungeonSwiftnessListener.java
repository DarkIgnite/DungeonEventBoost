package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DungeonSwiftnessListener implements Listener {

    private final DungeonEventBoost plugin;
    private final ConfigManager config;
    private final EventManager eventManager;

    public DungeonSwiftnessListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDungeonCombat(EntityDamageByEntityEvent event) {
        if (!config.isSwiftnessBoostEnabled()) {
            return;
        }

        Player player = null;
        ActiveMob activeMob = null;

        // Check if player attacked mob
        Player damagerPlayer = getPlayerDamager(event.getDamager());
        if (damagerPlayer != null) {
            player = damagerPlayer;
            activeMob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(event.getEntity());
        } else if (event.getEntity() instanceof Player victimPlayer) {
            player = victimPlayer;
            activeMob = getMythicMobDamager(event.getDamager());
        }

        if (player == null || activeMob == null) {
            return;
        }

        if (!eventManager.isBoostActive(player, EventType.SWIFTNESS)) {
            return;
        }

        if (!isDungeonMob(activeMob)) {
            return;
        }

        int durationTicks = config.getSwiftnessDurationSeconds() * 20;
        int speedAmp = config.getSwiftnessSpeedAmplifier();
        int hasteAmp = config.getSwiftnessHasteAmplifier();

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, speedAmp, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, durationTicks, hasteAmp, false, true, true));

        // Berserk particle effect & sound once in a while
        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_BLINDNESS, 0.7f, 1.4f);
        }
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1.0, 0), 2, 0.2, 0.2, 0.2, 0.05);
    }

    private Player getPlayerDamager(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }

    private ActiveMob getMythicMobDamager(Entity damager) {
        if (damager == null) return null;
        ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(damager);
        if (mob != null) return mob;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Entity shooter) {
            return MythicBukkit.inst().getAPIHelper().getMythicMobInstance(shooter);
        }
        return null;
    }

    private boolean isDungeonMob(ActiveMob activeMob) {
        if (config.isDamageAllMythicMobs()) {
            return true;
        }
        if (config.isDamageFilterByFaction() && activeMob.hasFaction()) {
            String faction = activeMob.getFaction().toLowerCase();
            if (config.getDamageAllowedFactions().contains(faction)) {
                return true;
            }
        }
        if (config.isDamageFilterByMobTypes()) {
            String mobType = activeMob.getMobType() != null ? activeMob.getMobType().toLowerCase() : "";
            if (config.getDamageAllowedMobTypes().contains(mobType)) {
                return true;
            }
        }
        return false;
    }
}
