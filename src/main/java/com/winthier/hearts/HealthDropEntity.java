package com.winthier.hearts;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.entity.CustomEntity;
import com.winthier.custom.entity.EntityContext;
import com.winthier.custom.entity.EntityWatcher;
import com.winthier.custom.entity.TickableEntity;
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
                as.setVelocity(new Vector(0, 0.3, 0));
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

        void onTick() {
            if (ticks <= 0) {
                entity.remove();
            } else {
                ticks -= 1;
            }
        }

        void setDamage(double health) {
            int h = (int)Math.round(health);
            ChatColor color;
            boolean bold = false;
            if (h < 5) {
                color = ChatColor.DARK_GRAY;
            } else if (h < 10) {
                color = ChatColor.GRAY;
            } else if (h < 20) {
                color = ChatColor.WHITE;
            } else if (h < 30) {
                color = ChatColor.YELLOW;
            } else if (h < 40) {
                color = ChatColor.GOLD;
            } else if (h < 50) {
                color = ChatColor.RED;
            } else {
                color = ChatColor.RED;
                bold = true;
            }
            if (bold) {
                entity.setCustomName("" + color + ChatColor.BOLD + h);
            } else {
                entity.setCustomName("" + color + h);
            }
            entity.setCustomNameVisible(true);
            ticks = 60;
        }
    }
}
