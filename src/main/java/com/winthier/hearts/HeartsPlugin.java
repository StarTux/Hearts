package com.winthier.hearts;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.event.CustomRegisterEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

@Getter
public final class HeartsPlugin extends JavaPlugin implements Listener {
    private final Map<UUID, HealthBarEntity.Watcher> healthBars = new HashMap<>();
    boolean showNumericalHealth, showNumericalMaxHealth;

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        showNumericalHealth = getConfig().getBoolean("ShowNumericalHealth");
        showNumericalMaxHealth = getConfig().getBoolean("ShowNumericalMaxHealth");
    }

    @EventHandler
    public void onCustomRegister(CustomRegisterEvent event) {
        event.addEntity(new HealthBarEntity(this));
    }

    void onEvent(EntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (event.getEntity() instanceof org.bukkit.entity.Player) return;
        if (event.getEntity() instanceof org.bukkit.entity.ArmorStand) return;
        LivingEntity living = (LivingEntity)event.getEntity();
        if (living.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        UUID uuid = living.getUniqueId();
        HealthBarEntity.Watcher watcher = healthBars.get(uuid);
        if (watcher == null) {
            watcher = (HealthBarEntity.Watcher)CustomPlugin.getInstance().getEntityManager().spawnEntity(living.getEyeLocation().add(0, 0.4, 0), HealthBarEntity.CUSTOM_ID);
            healthBars.put(uuid, watcher);
            watcher.setLiving(living);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        onEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        onEvent(event);
    }
}
