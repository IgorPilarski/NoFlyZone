package pl.noflyzone;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElytraListener implements Listener {

    private static final long MESSAGE_COOLDOWN_MS = 3000L;

    private static final Component FIREWORK_BLOCK_MESSAGE = Component.text(
            "Using firework rockets with elytra in the city zone is forbidden! Take the train instead.",
            NamedTextColor.RED
    );

    private static final Component ELYTRA_BLOCK_MESSAGE = Component.text(
            "Elytra flight in the city zone is forbidden! Take the train instead.",
            NamedTextColor.RED
    );

    private final NoFlyZonePlugin plugin;
    private final Map<UUID, Long> lastMessageAt = new ConcurrentHashMap<>();

    public ElytraListener(NoFlyZonePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onElytraBoost(PlayerElytraBoostEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        if (!plugin.isInsideZone(location.getWorld().getName(), location.getX(), location.getZ())) {
            return;
        }

        event.setCancelled(true);
        event.setShouldConsume(false);

        Firework firework = event.getFirework();
        if (firework != null && !firework.isDead()) {
            firework.remove();
        }

        sendMessage(player, FIREWORK_BLOCK_MESSAGE);
        plugin.logDebug(String.format(
                Locale.ROOT,
                "Blocked firework boost for %s at (%.1f, %.1f, %.1f) in %s",
                player.getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getWorld().getName()
        ));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStartGliding(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!event.isGliding() || !plugin.isBlockElytraFlightEnabled()) {
            return;
        }

        Location location = player.getLocation();
        if (!plugin.isInsideZone(location.getWorld().getName(), location.getX(), location.getZ())) {
            return;
        }

        event.setCancelled(true);
        sendMessage(player, ELYTRA_BLOCK_MESSAGE);
        plugin.logDebug(String.format(
                Locale.ROOT,
                "Blocked elytra glide start for %s at (%.1f, %.1f, %.1f) in %s",
                player.getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getWorld().getName()
        ));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMoveWhileGliding(PlayerMoveEvent event) {
        if (!plugin.isBlockElytraFlightEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        if (!plugin.isInsideZone(to.getWorld().getName(), to.getX(), to.getZ())) {
            return;
        }

        player.setGliding(false);
        sendMessage(player, ELYTRA_BLOCK_MESSAGE);
        plugin.logDebug(String.format(
                Locale.ROOT,
                "Stopped elytra glide for %s at (%.1f, %.1f, %.1f) in %s",
                player.getName(),
                to.getX(),
                to.getY(),
                to.getZ(),
                to.getWorld().getName()
        ));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastMessageAt.remove(event.getPlayer().getUniqueId());
    }

    private void sendMessage(Player player, Component message) {
        long now = System.currentTimeMillis();
        Long lastSent = lastMessageAt.get(player.getUniqueId());

        if (lastSent != null && now - lastSent < MESSAGE_COOLDOWN_MS) {
            return;
        }

        lastMessageAt.put(player.getUniqueId(), now);
        player.sendMessage(message);
    }
}
