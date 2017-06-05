package ua.gwm.sponge_plugin.crates.util;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import ua.gwm.sponge_plugin.crates.GWMCrates;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Config {

    public static final int AUTO_SAVE_INTERVAL = 600;

    private String name;
    private File file;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationNode node;

    public Config(String name, boolean auto_save) {
        this.name = name;
        try {
            file = new File(GWMCrates.getInstance().getConfigDirectory(), getName());
            loader = HoconConfigurationLoader.builder().setFile(getFile()).build();
            node = getLoader().load();
            if (!file.exists()) {
                file.createNewFile();
                URL defaultsURL = GWMCrates.class.getResource("/" + name);
                ConfigurationLoader<CommentedConfigurationNode> defaultsLoader = HoconConfigurationLoader.builder().setURL(defaultsURL).build();
                ConfigurationNode defaultsNode = defaultsLoader.load();
                getNode().mergeValuesFrom(defaultsNode);
                save();
            }
            if (auto_save) {
                Sponge.getScheduler().createTaskBuilder().async().execute(this::save).
                        interval(AUTO_SAVE_INTERVAL, TimeUnit.SECONDS).submit(GWMCrates.getInstance());
            }
        } catch (Exception e) {
            GWMCrates.getInstance().getLogger().warn("Failed initialize config \"" + getName() + "\"!", e);
        }
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public ConfigurationLoader<CommentedConfigurationNode> getLoader() {
        return loader;
    }

    public ConfigurationNode getNode() {
        return node;
    }

    public ConfigurationNode getNode(Object... objects) {
        return node.getNode(objects);
    }

    public void save() {
        try {
            getLoader().save(getNode());
            if (GWMCrates.getInstance().isDebugEnabled()) {
                GWMCrates.getInstance().getLogger().info("Config \"" + name + "\" successfully saved!");
            }
        } catch (Exception e) {
            GWMCrates.getInstance().getLogger().warn("Fail saving config \"" + name + "\"!", e);
        }
    }

    public void reload() {
        try {
            node = getLoader().load();
            if (GWMCrates.getInstance().isDebugEnabled()) {
                GWMCrates.getInstance().getLogger().info("Config \"" + name + "\" successfully reloaded!");
            }
        } catch (Exception e) {
            GWMCrates.getInstance().getLogger().warn("Fail reloading config \"" + name + "\"!", e);
        }
    }
}
