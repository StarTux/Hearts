package com.winthier.hearts;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.custom.entity.TickableEntity;
import java.util.Random;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;

public final class HealthDropEntity implements CustomEntity, TickableEntity {
    public static final String CUSTOM_ID = "hearts:health_drop";
    private final HeartsPlugin plugin;
    private Random random = new Random(System.currentTimeMillis());

    HealthDropEntity(HeartsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCustomId() {
        return CUSTOM_ID;
    }

    @Override
    public Entity spawnEntity(Location location) {
        return location.getWorld().spawn(location, ArmorStand.class, as -> {
                as.setVisible(false);
                as.setMarker(true);
                as.setSmall(true);
                as.setGravity(true);
                as.setVelocity(new Vector(random.nextDouble() * 0.2, 0.3, random.nextDouble() * 0.2));
            });
    }

    @Override
    public Watcher createEntityWatcher(Entity entity) {
        return new Watcher((ArmorStand)entity, this);
    }

    @Override
    public void onTick(EntityWatcher entityWatcher) {
        ((Watcher)entityWatcher).onTick();
    }

    @Override
    public void entityWillUnload(EntityWatcher entityWatcher) {
        CustomPlugin.getInstance().getEntityManager().removeEntityWatcher(entityWatcher);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event, EntityContext context) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event, EntityContext context) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityTeleport(EntityTeleportEvent event, EntityContext context) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityPortalEnter(EntityPortalEnterEvent event, EntityContext context) {
        CustomPlugin.getInstance().getEntityManager().removeEntityWatcher(context.getEntityWatcher());
    }

    @Getter @Setter @RequiredArgsConstructor
    public final class Watcher implements EntityWatcher {
        private final ArmorStand entity;
        private final HealthDropEntity customEntity;
        private int ticks = 0;
        private double damage;

        void onTick() {
            if (ticks <= 0) {
                entity.remove();
            } else {
                ticks -= 1;
            }
            if (ticks == 40) {
                int h = (int)Math.round(damage);
                entity.setCustomName("" + ChatColor.GRAY + h);
            } else if (ticks == 20) {
                int h = (int)Math.round(damage);
                entity.setCustomName("" + ChatColor.DARK_GRAY + h);
            }
        }

        void setDamage(double damage) {
            this.damage = damage;
            int h = (int)Math.round(damage);
            entity.setCustomName("" + ChatColor.WHITE + h);
            entity.setCustomNameVisible(true);
            ticks = 60;
        }
    }
}
