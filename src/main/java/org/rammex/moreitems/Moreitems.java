package org.rammex.moreitems;

import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.rammex.moreitems.commands.MoreItemsCommand;
import org.rammex.moreitems.events.FunnelPickupEvent;

import java.io.File;
import java.io.IOException;

public final class Moreitems extends JavaPlugin {
    private File dir = getDataFolder();

    @Getter
    private FileConfiguration itemConf;
    @Getter
    private FileConfiguration messageConf;
    @Getter
    private File itemFile;
    @Getter
    private File messageFile;

    @Override
    public void onEnable() {
        loadMessages();
        loadFiles();
        this.getCommand("moreitems").setExecutor(new MoreItemsCommand(this));
        getServer().getPluginManager().registerEvents(new FunnelPickupEvent(this), this);

    }

    @Override
    public void onDisable() {
    }

    private void loadMessages() {
        getLogger().info("\\033[35m-----------");
        getLogger().info("\\033[35mPlugin Created by .rammex");
        getLogger().info("\\033[35mVersion 1.0");
        getLogger().info("\\033[35m-----------");
    }

    private void loadFiles() {
        loadFile("items");
        loadFile("messages");
    }

    private void loadFile(String fileName) {
        File file = new File(getDataFolder(), fileName + ".yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveResource(fileName + ".yml", false);
        }

        FileConfiguration fileConf = new YamlConfiguration();
        try {
            fileConf.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        switch (fileName) {
            case "messages":
                messageConf = fileConf;
                break;
            case "items":
                itemConf = fileConf;
                break;
        }
    }

    public void reloadItemsConfig() {
        File file = new File(getDataFolder(), "items.yml");
        FileConfiguration fileConf = new YamlConfiguration();
        try {
            fileConf.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        itemConf = fileConf;
    }
}
