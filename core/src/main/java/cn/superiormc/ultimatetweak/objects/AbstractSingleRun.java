package cn.superiormc.ultimatetweak.objects;


import cn.superiormc.ultimatetweak.managers.ConfigManager;
import cn.superiormc.ultimatetweak.utils.CommonUtil;
import cn.superiormc.ultimatetweak.utils.MathUtil;
import cn.superiormc.ultimatetweak.utils.TextUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSingleRun {

    protected ConfigurationSection section;

    protected String rankSetting;

    public AbstractSingleRun(ConfigurationSection section) {
        this.section = section;
        rankSetting = section.getString("rank-limit", null);
    }

    protected String replacePlaceholder(String content, Player player) {
        if (content == null) {
            return "";
        }

        content = CommonUtil.modifyString(player, content,
                "world", player.getWorld().getName(),
                "player-x", String.valueOf(player.getLocation().getX()),
                "player-y", String.valueOf(player.getLocation().getY()),
                "player-z", String.valueOf(player.getLocation().getZ()),
                "player-pitch", String.valueOf(player.getLocation().getPitch()),
                "player-yaw", String.valueOf(player.getLocation().getYaw()),
                "player", player.getName());
        String[] extraArgs = getExtraArgs();
        if (extraArgs.length > 0) {
            content = CommonUtil.modifyString(player, content, extraArgs);
        }
        return content;
    }

    protected String[] getExtraArgs() {
        return new String[0];
    }

    public String getString(String path) {
        return section.getString(path);
    }

    public List<String> getStringList(String path, Player player) {
        List<String> rawList = section.getStringList(path);
        if (rawList.isEmpty()) {
            return rawList;
        }

        List<String> tempVal1 = new ArrayList<>();
        for (String line : rawList) {
            tempVal1.add(replacePlaceholder(line, player));
        }
        return tempVal1;
    }

    public int getInt(String path) {
        return section.getInt(path);
    }

    public int getInt(String path, int defaultValue) {
        return section.getInt(path, defaultValue);
    }

    public double getDouble(String path) {
        return MathUtil.doCalculate(section.getString(path));
    }

    public double getDouble(String path, Player player) {
        return MathUtil.doCalculate(replacePlaceholder(section.getString(path), player));
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return section.getBoolean(path, defaultValue);
    }

    public String getString(String path, Player player) {
        return replacePlaceholder(section.getString(path), player);
    }

    public ConfigurationSection getSection() {
        return section;
    }
}
