package pl.noflyzone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class NoFlyZonePlugin extends JavaPlugin {

    private boolean debug;
    private boolean zoneEnabled;
    private String zoneWorld;
    private double zoneRadius;
    private double zoneX;
    private double zoneZ;
    private boolean blockElytraFlight;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadZoneFromConfig();

        getServer().getPluginManager().registerEvents(new ElytraListener(this), this);

        var command = getCommand("noflyzone");
        if (command != null) {
            var setZoneCommand = new SetZoneCommand(this);
            command.setExecutor(setZoneCommand);
            command.setTabCompleter(setZoneCommand);
        } else {
            getLogger().severe("Command 'noflyzone' not found in plugin.yml!");
        }

        logZoneConfiguration();
        getLogger().info("NoFlyZone has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NoFlyZone has been disabled.");
    }

    public void loadZoneFromConfig() {
        debug = getConfig().getBoolean("debug", false);
        zoneEnabled = getConfig().getBoolean("zone.enabled", false);
        zoneWorld = getConfig().getString("zone.world", "world");
        zoneRadius = getConfig().getDouble("zone.radius", 100);
        zoneX = getConfig().getDouble("zone.x", 0);
        zoneZ = getConfig().getDouble("zone.z", 0);
        blockElytraFlight = getConfig().getBoolean("zone.block-elytra-flight", false);
    }

    public void saveZoneToConfig(boolean enabled, double radius, double x, double z) {
        zoneEnabled = enabled;
        zoneRadius = radius;
        zoneX = x;
        zoneZ = z;

        getConfig().set("zone.enabled", enabled);
        getConfig().set("zone.radius", radius);
        getConfig().set("zone.x", x);
        getConfig().set("zone.z", z);
        saveConfig();
    }

    public void setZoneEnabled(boolean enabled) {
        zoneEnabled = enabled;
        getConfig().set("zone.enabled", enabled);
        saveConfig();
    }

    public void saveZoneWorld(String world) {
        zoneWorld = world;
        getConfig().set("zone.world", world);
        saveConfig();
    }

    public void saveBlockElytraFlight(boolean enabled) {
        blockElytraFlight = enabled;
        getConfig().set("zone.block-elytra-flight", enabled);
        saveConfig();
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    public boolean isZoneEnabled() {
        return zoneEnabled;
    }

    public boolean isBlockElytraFlightEnabled() {
        return blockElytraFlight;
    }

    public double getZoneRadius() {
        return zoneRadius;
    }

    public double getZoneX() {
        return zoneX;
    }

    public double getZoneZ() {
        return zoneZ;
    }

    public String getZoneWorldName() {
        return zoneWorld;
    }

    public boolean isInsideZone(String worldName, double x, double z) {
        if (!zoneEnabled || !zoneWorld.equals(worldName)) {
            return false;
        }

        double dx = x - zoneX;
        double dz = z - zoneZ;
        return (dx * dx + dz * dz) <= (zoneRadius * zoneRadius);
    }

    public void logAdminAction(CommandSender sender, String action) {
        getLogger().info("[" + sender.getName() + "] " + action);
    }

    public void logDebug(String message) {
        if (debug) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public void logZoneConfiguration() {
        String status = zoneEnabled ? "ENABLED" : "DISABLED";
        getLogger().info(String.format(
                Locale.ROOT,
                "Zone %s | world=%s | radius=%.1f | center=(%.1f, %.1f) | elytra-block=%s | debug=%s",
                status,
                zoneWorld,
                zoneRadius,
                zoneX,
                zoneZ,
                blockElytraFlight ? "ON" : "OFF",
                debug ? "ON" : "OFF"
        ));

        if (zoneEnabled && Bukkit.getWorld(zoneWorld) == null) {
            getLogger().warning("Configured world '" + zoneWorld + "' is not loaded. The zone will not work until that world exists.");
        }
    }
}
