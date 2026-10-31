package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DungeonDamageListener implements Listener {

    private final DungeonEventBoost plugin;
    private final ConfigManager config;
    private final EventManager eventManager;

    public DungeonDamageListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!config.isDamageBoostEnabled()) {
            return;
        }

        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        World world = target.getWorld();
        if (config.isWorldWhitelistEnabled() && !config.getAllowedWorlds().contains(world.getName().toLowerCase())) {
            return;
        }

        // Check if damager is a player or player-shot projectile
        Player player = getPlayerDamager(event.getDamager());
        if (player == null || !eventManager.isBoostActive(player, EventType.DAMAGE_DEALT)) {
            return;
        }

        // Check if target is a MythicMob
        ActiveMob activeMob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(target);
        if (activeMob == null) {
            return;
        }

        // Check dungeon criteria
        if (!isDungeonMob(activeMob)) {
            return;
        }

        double multiplier = eventManager.getEffectiveMultiplier(player, EventType.DAMAGE_DEALT, config.getDamageMultiplier());
        if (multiplier <= 1.0) {
            return;
        }

        // Apply damage boost
        double originalDamage = event.getDamage();
        event.setDamage(originalDamage * multiplier);

        // Visual and sound effects
        playHitEffects(livingTarget.getLocation().add(0, livingTarget.getHeight() / 2.0, 0), player);
    }

    private Player getPlayerDamager(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }

    private boolean isDungeonMob(ActiveMob activeMob) {
        if (config.isDamageAllMythicMobs()) {
            return true;
        }

        if (config.isDamageFilterByFaction()) {
            if (activeMob.hasFaction()) {
                String faction = activeMob.getFaction().toLowerCase();
                if (config.getDamageAllowedFactions().contains(faction)) {
                    return true;
                }
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

    private void playHitEffects(Location loc, Player player) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle particle = config.getDamageParticle();
        if (particle != null && config.getDamageParticleCount() > 0) {
            world.spawnParticle(
                    particle,
                    loc,
                    config.getDamageParticleCount(),
                    0.3, 0.3, 0.3,
                    0.1
            );
        }

        if (config.isDamagePlaySound() && config.getDamageSound() != null) {
            player.playSound(
                    loc,
                    config.getDamageSound(),
                    config.getDamageSoundVolume(),
                    config.getDamageSoundPitch()
            );
        }
    }
}
