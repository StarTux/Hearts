package com.winthier.hearts;

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
        entityWatcher.getEntity().remove();
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
        context.getEntity().remove();
    }

    @Getter @Setter @RequiredArgsConstructor
    public final class Watcher implements EntityWatcher {
        private final ArmorStand entity;
        private final HealthDropEntity customEntity;
        private int ticks = 0;
        private int health;
        private boolean isHealing;

        void onTick() {
            if (ticks <= 0) {
                entity.remove();
            } else {
                ticks -= 1;
            }
            if (ticks > 50) {
                if (isHealing) {
                    entity.teleport(entity.getLocation().add(0, 0.1, 0));
                }
            } else if (ticks == 40) {
                if (isHealing) {
                    entity.setCustomName("" + ChatColor.DARK_GREEN + health);
                    entity.setVelocity(new Vector(0, 0, 0));
                } else {
                    entity.setCustomName("" + ChatColor.GRAY + health);
                    entity.setGravity(false);
                }
            } else if (ticks == 20) {
                entity.setCustomName("" + ChatColor.DARK_GRAY + health);
            }
        }

        void setDamage(int h) {
            this.health = h;
            this.isHealing = false;
            entity.setCustomName("" + ChatColor.WHITE + h);
            entity.setCustomNameVisible(true);
            entity.setVelocity(new Vector(random.nextDouble() * 0.4 - 0.2, 0.3, random.nextDouble() * 0.4 - 0.2));
            ticks = 60;
        }

        void setHealing(int h) {
            this.health = h;
            this.isHealing = true;
            entity.setCustomName("" + ChatColor.GREEN + h);
            entity.setCustomNameVisible(true);
            entity.setGravity(false);
            entity.setVelocity(new Vector(0, 10, 0));
            ticks = 60;
        }
    }
}
