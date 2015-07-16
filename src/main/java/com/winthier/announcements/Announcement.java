package com.winthier.announcements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

public class Announcement
{
    @Getter private final String jsonString;
    @Getter private final String debugString;

    private Announcement(Object json) {
        this.jsonString = JSONValue.toJSONString(json);
        this.debugString = jsonToString(json);
    }

    static Announcement withPrefixAndEntry(Object prefix, Object entry)
    {
        if (prefix instanceof String) {
            prefix = ChatColor.translateAlternateColorCodes('&', (String)prefix);
        }
        List<Object> list = new ArrayList<>();
        list.add(prefix);
        if (entry instanceof List) {
            list.addAll((List)entry);
        } else if (entry instanceof String) {
            list.add(ChatColor.translateAlternateColorCodes('&', (String)entry));
        } else {
            list.add(entry);
        }
        return new Announcement(list);
    }

    void announce(Player player)
    {
        String command = String.format("minecraft:tellraw %s %s", player.getName(), jsonString);
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

    static String jsonToString(Object json) {
        if (json instanceof String) {
            return (String)json;
        } else if (json instanceof Map) {
            Map map = (Map)json;
            Object result;
            if (null != (result = map.get("text"))) return (String)result;
            if (null != (result = map.get("extra"))) return jsonToString(result);
            return "";
        } else if (json instanceof List) {
            StringBuilder sb = new StringBuilder();
            for (Object o : (List)json) {
                sb.append(jsonToString(o));
            }
            return sb.toString();
        } else {
            return json.toString();
        }
    }
}
