package pl.noflyzone;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SetZoneCommand implements CommandExecutor, TabCompleter {

    private final NoFlyZonePlugin plugin;

    public SetZoneCommand(NoFlyZonePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("noflyzone.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "help", "?" -> sendHelp(sender, label);
            case "set" -> handleSet(sender, label, args);
            case "info" -> sendInfo(sender);
            case "enable" -> {
                if (plugin.isZoneEnabled()) {
                    sender.sendMessage(Component.text("The no-fly zone is already enabled.", NamedTextColor.YELLOW));
                    return true;
                }
                plugin.setZoneEnabled(true);
                plugin.logAdminAction(sender, "enabled the no-fly zone");
                sender.sendMessage(Component.text(
                        String.format(
                                Locale.ROOT,
                                "No-fly zone enabled: radius %.1f, center (%.1f, %.1f), world '%s'.",
                                plugin.getZoneRadius(),
                                plugin.getZoneX(),
                                plugin.getZoneZ(),
                                plugin.getZoneWorldName()
                        ),
                        NamedTextColor.GREEN
                ));
            }
            case "disable" -> {
                if (!plugin.isZoneEnabled()) {
                    sender.sendMessage(Component.text("The no-fly zone is already disabled.", NamedTextColor.YELLOW));
                    return true;
                }
                plugin.setZoneEnabled(false);
                plugin.logAdminAction(sender, "disabled the no-fly zone");
                sender.sendMessage(Component.text("No-fly zone has been disabled.", NamedTextColor.YELLOW));
            }
            case "elytra" -> handleElytra(sender, label, args);
            case "world" -> handleWorld(sender, label, args);
            default -> sendHelp(sender, label);
        }

        return true;
    }

    private void handleSet(CommandSender sender, String label, String[] args) {
        if (args.length != 4) {
            sender.sendMessage(Component.text(
                    "Usage: /" + label + " set <radius> <x> <z>",
                    NamedTextColor.RED
            ));
            return;
        }

        double radius = parseDouble(args[1], sender);
        if (Double.isNaN(radius)) {
            return;
        }
        if (radius < 0) {
            sender.sendMessage(Component.text("Radius must be a non-negative number.", NamedTextColor.RED));
            return;
        }

        double x = parseDouble(args[2], sender);
        if (Double.isNaN(x)) {
            return;
        }

        double z = parseDouble(args[3], sender);
        if (Double.isNaN(z)) {
            return;
        }

        plugin.saveZoneToConfig(true, radius, x, z);
        plugin.logAdminAction(sender, String.format(
                Locale.ROOT,
                "set zone: radius=%.1f, center=(%.1f, %.1f), world='%s'",
                radius, x, z, plugin.getZoneWorldName()
        ));
        sender.sendMessage(Component.text(
                String.format(
                        Locale.ROOT,
                        "No-fly zone set: radius %.1f, center (%.1f, %.1f), world '%s'.",
                        radius, x, z, plugin.getZoneWorldName()
                ),
                NamedTextColor.GREEN
        ));
    }

    private void handleWorld(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text(
                    "Usage: /" + label + " world <world_name>",
                    NamedTextColor.RED
            ));
            return;
        }

        String worldName = args[1];
        if (Bukkit.getWorld(worldName) == null) {
            sender.sendMessage(Component.text(
                    "World '" + worldName + "' does not exist on this server.",
                    NamedTextColor.RED
            ));
            return;
        }

        plugin.saveZoneWorld(worldName);
        plugin.logAdminAction(sender, "set zone world to '" + worldName + "'");
        sender.sendMessage(Component.text(
                "Zone world set to: " + worldName,
                NamedTextColor.GREEN
        ));
    }

    private void handleElytra(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text(
                    "Usage: /" + label + " elytra <on|off>",
                    NamedTextColor.RED
            ));
            return;
        }

        String mode = args[1].toLowerCase(Locale.ROOT);
        switch (mode) {
            case "on", "true" -> {
                plugin.saveBlockElytraFlight(true);
                plugin.logAdminAction(sender, "enabled elytra flight block in the zone");
                sender.sendMessage(Component.text(
                        "Elytra flight block in the zone has been enabled.",
                        NamedTextColor.GREEN
                ));
            }
            case "off", "false" -> {
                plugin.saveBlockElytraFlight(false);
                plugin.logAdminAction(sender, "disabled elytra flight block in the zone");
                sender.sendMessage(Component.text(
                        "Elytra flight block in the zone has been disabled.",
                        NamedTextColor.YELLOW
                ));
            }
            default -> sender.sendMessage(Component.text(
                    "Usage: /" + label + " elytra <on|off>",
                    NamedTextColor.RED
            ));
        }
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage(Component.text(
                "No-fly zone: " + (plugin.isZoneEnabled() ? "ENABLED" : "DISABLED"),
                plugin.isZoneEnabled() ? NamedTextColor.GREEN : NamedTextColor.YELLOW
        ));
        sender.sendMessage(Component.text(
                String.format(
                        Locale.ROOT,
                        "Radius %.1f, center (%.1f, %.1f), world '%s'.",
                        plugin.getZoneRadius(),
                        plugin.getZoneX(),
                        plugin.getZoneZ(),
                        plugin.getZoneWorldName()
                ),
                NamedTextColor.AQUA
        ));
        sender.sendMessage(Component.text(
                "Elytra flight block: " + (plugin.isBlockElytraFlightEnabled() ? "ON" : "OFF"),
                plugin.isBlockElytraFlightEnabled() ? NamedTextColor.RED : NamedTextColor.GREEN
        ));
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Component.text("=== NoFlyZone - Help ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/" + label + " set <radius> <x> <z>", NamedTextColor.YELLOW)
                .append(Component.text(" - set the zone (2D distance)", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/" + label + " world <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - set zone world (default: world)", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/" + label + " elytra <on|off>", NamedTextColor.YELLOW)
                .append(Component.text(" - toggle full elytra flight block", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/" + label + " enable", NamedTextColor.YELLOW)
                .append(Component.text(" - enable zone without changing coordinates", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/" + label + " disable", NamedTextColor.YELLOW)
                .append(Component.text(" - disable the no-fly zone", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/" + label + " info", NamedTextColor.YELLOW)
                .append(Component.text(" - show current configuration", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/" + label + " help", NamedTextColor.YELLOW)
                .append(Component.text(" - show this help", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("Example: /" + label + " set 150 100 200", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("Command alias: /nfz", NamedTextColor.GRAY));
    }

    private double parseDouble(String value, CommandSender sender) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid number: " + value, NamedTextColor.RED));
            return Double.NaN;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("noflyzone.admin")) {
            return List.of();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String option : List.of("set", "world", "elytra", "enable", "disable", "info", "help")) {
                if (option.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    completions.add(option);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("world")) {
            String input = args[1].toLowerCase(Locale.ROOT);
            for (var world : Bukkit.getWorlds()) {
                String name = world.getName();
                if (name.toLowerCase(Locale.ROOT).startsWith(input)) {
                    completions.add(name);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("elytra")) {
            for (String option : List.of("on", "off")) {
                if (option.startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    completions.add(option);
                }
            }
        }

        return completions;
    }
}
