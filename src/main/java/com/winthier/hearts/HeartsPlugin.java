package com.winthier.hearts;

import com.winthier.custom.CustomPlugin;
import com.winthier.custom.event.CustomRegisterEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
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
    ChatColor fullHeartColor = ChatColor.LIGHT_PURPLE;
    ChatColor halfHeartColor = ChatColor.DARK_PURPLE;
    ChatColor emptyHeartColor = ChatColor.BLACK;
    ChatColor numbersColor = ChatColor.RED;
    ChatColor slashColor = ChatColor.DARK_GRAY;
    boolean showNumericalHealth, showNumericalMaxHealth, dropHealth;

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        fullHeartColor = ChatColor.valueOf(getConfig().getString("colors.FullHeart").toUpperCase());
        halfHeartColor = ChatColor.valueOf(getConfig().getString("colors.HalfHeart").toUpperCase());
        emptyHeartColor = ChatColor.valueOf(getConfig().getString("colors.EmptyHeart").toUpperCase());
        numbersColor = ChatColor.valueOf(getConfig().getString("colors.Numbers").toUpperCase());
        slashColor = ChatColor.valueOf(getConfig().getString("colors.Slash").toUpperCase());
        showNumericalHealth = getConfig().getBoolean("ShowNumericalHealth");
        showNumericalMaxHealth = getConfig().getBoolean("ShowNumericalMaxHealth");
        dropHealth = getConfig().getBoolean("DropHealth");
    }

    @EventHandler
    public void onCustomRegister(CustomRegisterEvent event) {
        event.addEntity(new HealthBarEntity(this));
        event.addEntity(new HealthDropEntity(this));
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
        if (dropHealth && event.getEntity() instanceof LivingEntity && event.getEntity().getType() != EntityType.ARMOR_STAND) {
            switch (event.getCause()) {
            case FIRE_TICK:
            case POISON:
            case STARVATION:
            case THORNS:
            case HOT_FLOOR:
            case MELTING:
            case VOID:
            case WITHER:
            case SUFFOCATION:
            case CRAMMING:
            case DROWNING:
                break;
            default:
                int dmg = (int)Math.round(event.getFinalDamage());
                if (dmg >= 1) {
                    LivingEntity living = (LivingEntity)event.getEntity();
                    HealthDropEntity.Watcher watcher = (HealthDropEntity.Watcher)CustomPlugin.getInstance().getEntityManager().spawnEntity(living.getLocation().add(0, living.getHeight(), 0), HealthDropEntity.CUSTOM_ID);
                    watcher.setDamage(dmg);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        onEvent(event);
        if (dropHealth && event.getEntity() instanceof LivingEntity && event.getEntity().getType() != EntityType.ARMOR_STAND) {
            switch (event.getRegainReason()) {
            case SATIATED:
            case ENDER_CRYSTAL:
            case MAGIC_REGEN:
                break;
            default:
                LivingEntity living = (LivingEntity)event.getEntity();
                int health = Math.min((int)Math.round(living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - living.getHealth()), (int)Math.round(event.getAmount()));
                if (health >= 1) {
                    HealthDropEntity.Watcher watcher = (HealthDropEntity.Watcher)CustomPlugin.getInstance().getEntityManager().spawnEntity(living.getLocation().add(0, living.getHeight(), 0), HealthDropEntity.CUSTOM_ID);
                    watcher.setHealing(health);
                }
            }
        }
    }
}
