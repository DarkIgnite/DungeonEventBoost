package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import com.leftycraft.dungeoneventboost.event.EventType;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DungeonDefenseListener implements Listener {

    private final DungeonEventBoost plugin;
    private final ConfigManager config;
    private final EventManager eventManager;

    public DungeonDefenseListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDungeonMobDamagePlayer(EntityDamageByEntityEvent event) {
        if (!config.isDefenseBoostEnabled() || !eventManager.isBoostActive(EventType.DEFENSE)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        World world = player.getWorld();
        if (config.isWorldWhitelistEnabled() && !config.getAllowedWorlds().contains(world.getName().toLowerCase())) {
            return;
        }

        ActiveMob activeMob = getMythicMobDamager(event.getDamager());
        if (activeMob == null) {
            return;
        }

        if (!isDungeonMob(activeMob)) {
            return;
        }

        double multiplier = config.getDefenseMultiplier(); // default 0.5 (50% reduction)
        if (multiplier >= 1.0) {
            return;
        }

        event.setDamage(event.getDamage() * multiplier);

        // Feedback sound & particles
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.6f, 1.3f);
        world.spawnParticle(Particle.ENCHANTED_HIT, player.getLocation().add(0, 1.0, 0), 6, 0.3, 0.3, 0.3, 0.05);
    }

    private ActiveMob getMythicMobDamager(Entity damager) {
        if (damager == null) return null;
        ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(damager);
        if (mob != null) return mob;

        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
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
