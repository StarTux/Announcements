package com.winthier.announcements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AnnouncementsPlugin extends JavaPlugin {
    private final Map<String, Group> groups = new HashMap<String, Group>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        loadAnnouncements();
    }

    @Override
    public void onDisable() {
        unloadAnnouncements();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        final Player player = sender instanceof Player ? (Player)sender : null;
        if (args.length == 0) {
            return false;
        } else if ("reload".equalsIgnoreCase(args[0]) && args.length == 1) {
            unloadAnnouncements();
            reloadConfig();
            loadAnnouncements();
            sender.sendMessage("Configuration reloaded.");
        } else if ("list".equalsIgnoreCase(args[0])) {
            if (args.length == 1) {
                sendTaggedMessage(sender, "&7Announcement Group List (&r%d&7)", groups.size());
                for (Group group : groups.values()) {
                    sendMessage(sender, "&b- &7%s (%d)", group.getName(), group.size());
                }
            }
            if (args.length == 2) {
                Group group = groups.get(args[1]);
                if (group == null) {
                    sendTaggedMessage(sender, "&cGroup not found: %s.", args[1]);
                    return true;
                }
                sendTaggedMessage(sender, "&7Announcements in Group %s (&r%d&7)", group.getName(), group.size());
                for (int i = 0; i < group.size(); ++i) {
                    sendMessage(sender, "&b%d: &7%s", i + 1, group.getAnnouncement(i).getDebugString());
                }
            }
        } else if ("announce".equalsIgnoreCase(args[0]) && args.length == 3) {
            String groupArg = args[1];
            String indexArg = args[2];
            Group group = groups.get(groupArg);
            if (group == null) {
                sendTaggedMessage(sender, "&cGroup not found: %s.", groupArg);
                return true;
            }
            int index = 0;
            try {
                index = Integer.parseInt(indexArg);
            } catch (NumberFormatException nfe) {}
            if (index <= 0 || index > group.size()) {
                sendTaggedMessage(sender, "&cInvalid index: %s.", indexArg);
                return true;
            }
            group.announce(index - 1);
        } else if ("show".equalsIgnoreCase(args[0]) && args.length == 3) {
            String groupArg = args[1];
            String indexArg = args[2];
            Group group = groups.get(groupArg);
            if (group == null) {
                sendTaggedMessage(sender, "&cGroup not found: %s.", groupArg);
                return true;
            }
            int index = 0;
            try {
                index = Integer.parseInt(indexArg);
            } catch (NumberFormatException nfe) {}
            if (index <= 0 || index > group.size()) {
                sendTaggedMessage(sender, "&cInvalid index: %s.", indexArg);
                return true;
            }
            group.getAnnouncement(index - 1).announce(player);
        } else {
            return false;
        }
        return true;
    }

    public void unloadAnnouncements() {
        for (Group group : groups.values()) {
            group.onDisable();
        }
        groups.clear();
    }

    public void loadAnnouncements() {
        ConfigurationSection config = getConfig().getConfigurationSection("groups");
        if (config == null) config = getConfig().createSection("groups");
        for (String name : config.getKeys(false)) {
            Group group = new Group(this, name);
            if (!config.isConfigurationSection(name)) {
                getLogger().warning("Not a ConfigurationSection: " + name);
                continue;
            }
            group.load(config.getConfigurationSection(name));
            group.onEnable();
            groups.put(name, group);
        }
    }

    public static String format(String string, Object... args) {
        string = ChatColor.translateAlternateColorCodes('&', string);
        string = String.format(string, args);
        return string;
    }

    public static void sendMessage(CommandSender sender, String string, Object... args) {
        string = format(string, args);
        sender.sendMessage(string);
    }

    public static void sendTaggedMessage(CommandSender sender, String string, Object... args) {
        sendMessage(sender, "&b[ann]&r " + string, args);
    }
}
