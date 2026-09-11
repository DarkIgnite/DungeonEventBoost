package com.leftycraft.dungeoneventboost.listener;

import com.leftycraft.dungeoneventboost.DungeonEventBoost;
import com.leftycraft.dungeoneventboost.config.ConfigManager;
import com.leftycraft.dungeoneventboost.event.EventManager;
import dev.aurelium.auraskills.api.event.skill.XpGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AuraSkillsExpListener implements Listener {

    private final DungeonEventBoost plugin;
    private final ConfigManager config;
    private final EventManager eventManager;

    public AuraSkillsExpListener(DungeonEventBoost plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.eventManager = plugin.getEventManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onXpGain(XpGainEvent event) {
        if (!config.isExpBoostEnabled() || !eventManager.isEventActive()) {
            return;
        }

        Player player = event.getPlayer();
        if (player != null && config.isWorldWhitelistEnabled()) {
            if (!config.getAllowedWorlds().contains(player.getWorld().getName().toLowerCase())) {
                return;
            }
        }

        // Check skill filter if enabled
        if (config.isExpFilterBySkills()) {
            String skillName = event.getSkill() != null ? event.getSkill().name().toLowerCase() : "";
            if (!config.getExpAllowedSkills().contains(skillName)) {
                return;
            }
        }

        double multiplier = config.getExpMultiplier();
        if (multiplier <= 1.0) {
            return;
        }

        double originalAmount = event.getAmount();
        event.setAmount(originalAmount * multiplier);
    }
}
