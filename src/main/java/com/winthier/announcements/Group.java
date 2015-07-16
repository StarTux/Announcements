package com.winthier.announcements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Group extends BukkitRunnable {
    public final AnnouncementsPlugin plugin;
    public final String name;

    // Timings
    private int offset;
    private int interval;

    // Behavior
    private boolean logToConsole;
    private boolean randomize;
    private String permission;

    // Messages
    private final List<Announcement> announcements = new ArrayList<>();
    private List<Integer> randomQueue = null;

    // State
    private final Random random = new Random(System.currentTimeMillis());
    private int index = 0;

    public Group(AnnouncementsPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    // public void save(ConfigurationSection config) {
    //     config.set("Offset", offset);
    //     config.set("Interval", interval);
    //     config.set("LogToConsole", logToConsole);
    //     config.set("Randomize", randomize);
    //     config.set("Permission", permission);
    //     config.set("Prefix", prefix);
    //     config.set("Announcements", announcements);
    // }

    public void load(ConfigurationSection config) {
        // Set some defaults.
        config.addDefault("Permission", "");
        config.addDefault("Interval", 60);
        config.addDefault("Prefix", "[&eServer&r] ");
        config.addDefault("LogToConsole", true);
        config.addDefault("Randomize", true);
        config.addDefault("Announcements", new ArrayList<String>(0));
        // Load
        permission = config.getString("Permission");
        if (permission.length() == 0) permission = null;
        interval = config.getInt("Interval");
        Object prefix = config.get("Prefix");
        logToConsole = config.getBoolean("LogToConsole");
        randomize = config.getBoolean("Randomize");
        announcements.clear();
        randomQueue = null;
        for (Object entry : config.getList("Announcements")) {
            announcements.add(Announcement.withPrefixAndEntry(prefix, entry));
        }
    }

    public void onEnable() {
        final long period = (long)interval * 20L * 60L;
        final long delay = offset == 0 ? period : (long)offset * 20L * 60L;
        runTaskTimer(plugin, delay, period);
    }

    public void onDisable() {
        try {
            cancel();
        } catch (IllegalStateException e) {
            // ignore
        }
    }

    @Override
    public void run() {
        if (announcements.isEmpty()) return;
        if (index >= announcements.size()) index = 0;
        if (randomize) {
            if (randomQueue == null) {
                randomQueue = new ArrayList<>(announcements.size());
                for (int i = 0; i < announcements.size(); ++i) randomQueue.add(i);
                Collections.shuffle(randomQueue);
            }
            announce(randomQueue.get(index++));
        } else {
            announce(index++);
        }
    }

    public void announce(int index) {
        // This may throw
        Announcement announcement = announcements.get(index);
        if (logToConsole) plugin.getLogger().info("[" + name + "] " + announcement.getDebugString());
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (permission != null && !player.hasPermission(permission)) continue;
            announcement.announce(player);
        }
    }

    public String getName() {
        return name;
    }

    public int size() {
        return announcements.size();
    }

    /**
     */
    public Announcement getAnnouncement(int index) {
        return announcements.get(index);
    }
}
