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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public final class HealthBarEntity implements CustomEntity, TickableEntity {
    public static final String CUSTOM_ID = "hearts:health_bar";
    private final String heart = "\u2764";
    private final HeartsPlugin plugin;

    HealthBarEntity(HeartsPlugin plugin) {
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
                as.setGravity(false);
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
        ((Watcher)entityWatcher).remove();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event, EntityContext context) {
        event.setCancelled(true);
        ((Watcher)context.getEntityWatcher()).remove();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event, EntityContext context) {
        event.setCancelled(true);
        ((Watcher)context.getEntityWatcher()).remove();
    }

    @Getter @Setter @RequiredArgsConstructor
    public final class Watcher implements EntityWatcher {
        private final ArmorStand entity;
        private final HealthBarEntity customEntity;
        private LivingEntity living;
        private double health;
        private int ticks;

        void onTick() {
            if (living == null || !living.isValid()) {
                remove();
            } else {
                if (living.getCustomName() == null) {
                    entity.teleport(living.getEyeLocation().add(0, 0.35, 0));
                } else {
                    entity.teleport(living.getEyeLocation().add(0, 0.5, 0));
                }
                double newHealth = living.getHealth();
                if (newHealth != health) {
                    ticks = 0;
                    updateCustomName();
                } else {
                    ticks += 1;
                    if (ticks > 100) remove();
                }
            }
        }

        void remove() {
            entity.remove();
            CustomPlugin.getInstance().getEntityManager().removeEntityWatcher(this);
            if (living != null) plugin.getHealthBars().remove(living.getUniqueId());
        }

        void updateCustomName() {
            health = living.getHealth();
            double maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            int fullHearts, emptyHearts, halfHeart;
            int totalHearts = (int)maxHealth / 2;
            if (totalHearts > 10) {
                double tmp = (health / maxHealth) * 10.0;
                fullHearts = (int)tmp;
                halfHeart = tmp - (double)fullHearts >= 0.5 ? 1 : 0;
                emptyHearts = 10 - fullHearts - halfHeart;
            } else {
                fullHearts = (int)health / 2;
                halfHeart = fullHearts * 4 < (int)health * 2 ? 1 : 0;
                emptyHearts = (totalHearts - fullHearts - halfHeart);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(ChatColor.RED).append((int)health).append(ChatColor.DARK_GRAY).append("/").append(ChatColor.RED).append((int)maxHealth).append(" ");
            if (fullHearts > 0) {
                sb.append(ChatColor.RED);
                for (int i = 0; i < fullHearts; i += 1) sb.append(heart);
            }
            if (halfHeart > 0) {
                sb.append(ChatColor.DARK_RED);
                sb.append(heart);
            }
            if (emptyHearts > 0) {
                sb.append(ChatColor.DARK_GRAY);
                for (int i = 0; i < emptyHearts; i += 1) sb.append(heart);
            }
            entity.setCustomName(sb.toString());
            entity.setCustomNameVisible(true);
        }
    }
}
