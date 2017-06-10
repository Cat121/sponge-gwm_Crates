package ua.gwm.sponge_plugin.crates.manager;

import ninja.leaping.configurate.ConfigurationNode;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.caze.Case;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.key.Key;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.preview.Preview;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;

import java.lang.reflect.Constructor;
import java.util.*;

public class Manager {

    private String id;
    private String name;
    private Case caze;
    private Key key;
    private OpenManager open_manager;
    private List<Drop> drops;
    private Optional<Preview> preview = Optional.empty();

    public Manager(ConfigurationNode node) {
        ConfigurationNode id_node = node.getNode("ID");
        ConfigurationNode name_node = node.getNode("NAME");
        ConfigurationNode case_node = node.getNode("CASE");
        ConfigurationNode key_node = node.getNode("KEY");
        ConfigurationNode open_manager_node = node.getNode("OPEN_MANAGER");
        ConfigurationNode drops_node = node.getNode("DROPS");
        ConfigurationNode preview_node = node.getNode("PREVIEW");
        if (id_node.isVirtual()) {
            throw new RuntimeException("ID node does not exist!");
        }
        if (name_node.isVirtual()) {
            throw new RuntimeException("NAME node does not exist!");
        }
        if (case_node.isVirtual()) {
            throw new RuntimeException("CASE node does not exist!");
        }
        if (key_node.isVirtual()) {
            throw new RuntimeException("KEY node does not exist!");
        }
        if (open_manager_node.isVirtual()) {
            throw new RuntimeException("OPEN_MANGER node does not exist!");
        }
        if (drops_node.isVirtual()) {
            throw new RuntimeException("DROPS node does not exist!");
        }
        id = id_node.getString();
        name = name_node.getString();
        //Loading case
        ConfigurationNode case_type_node = case_node.getNode("TYPE");
        if (case_type_node.isVirtual()) {
            throw new RuntimeException("TYPE node for Case does not exist!");
        }
        String case_type = case_type_node.getString();
        if (!GWMCrates.getInstance().getCases().containsKey(case_type)) {
            throw new RuntimeException("Case type \"" + case_type + "\" not found!");
        }
        try {
            Class<? extends Case> case_class = GWMCrates.getInstance().getCases().get(case_type);
            Constructor<? extends Case> case_constructor = case_class.getConstructor(ConfigurationNode.class);
            caze = case_constructor.newInstance(case_node);
        } catch (Exception e) {
            throw new RuntimeException("Exception creating Case!", e);
        }
        //Loading key
        ConfigurationNode key_type_node = key_node.getNode("TYPE");
        if (key_type_node.isVirtual()) {
            throw new RuntimeException("TYPE node for Key does not exist!");
        }
        String key_type = key_type_node.getString();
        if (!GWMCrates.getInstance().getKeys().containsKey(key_type)) {
            throw new RuntimeException("Key type \"" + case_type + "\" not found!");
        }
        try {
            Class<? extends Key> key_class = GWMCrates.getInstance().getKeys().get(key_type);
            Constructor<? extends Key> key_constructor = key_class.getConstructor(ConfigurationNode.class);
            key = key_constructor.newInstance(key_node);
        } catch (Exception e) {
            throw new RuntimeException("Exception creating Key!", e);
        }
        //Loading open manager
        ConfigurationNode open_manager_type_node = open_manager_node.getNode("TYPE");
        if (open_manager_type_node.isVirtual()) {
            throw new RuntimeException("TYPE node for Open Manager does not exist!");
        }
        String open_manager_type = open_manager_type_node.getString();
        if (!GWMCrates.getInstance().getOpenManagers().containsKey(open_manager_type)) {
            throw new RuntimeException("Open Manager type \"" + case_type + "\" not found!");
        }
        try {
            Class<? extends OpenManager> open_manager_class = GWMCrates.getInstance().getOpenManagers().get(open_manager_type);
            Constructor<? extends OpenManager> open_manager_constructor = open_manager_class.getConstructor(ConfigurationNode.class);
            open_manager = open_manager_constructor.newInstance(open_manager_node);
        } catch (Exception e) {
            throw new RuntimeException("Exception creating Open Manager!", e);
        }
        //Loading drops
        drops = new ArrayList<Drop>();
        for (ConfigurationNode drop_node : drops_node.getChildrenList()) {
            ConfigurationNode drop_type_node = drop_node.getNode("TYPE");
            if (drop_type_node.isVirtual()) {
                throw new RuntimeException("TYPE node for Drop does not exist!");
            }
            String drop_type = drop_type_node.getString();
            if (!GWMCrates.getInstance().getDrops().containsKey(drop_type)) {
                throw new RuntimeException("Drop type \"" + drop_type + "\" not found!");
            }
            try {
                Class<? extends Drop> drop_class = GWMCrates.getInstance().getDrops().get(drop_type);
                Constructor<? extends Drop> drop_constructor = drop_class.getConstructor(ConfigurationNode.class);
                drops.add(drop_constructor.newInstance(drop_node));
            } catch (Exception e) {
                throw new RuntimeException("Exception creating Drop!", e);
            }
        }
        //Loading preview
        if (!preview_node.isVirtual()) {
            ConfigurationNode preview_type_node = preview_node.getNode("TYPE");
            if (preview_type_node.isVirtual()) {
                throw new RuntimeException("TYPE node for Preview does not exist!");
            }
            String preview_type = preview_type_node.getString();
            if (!GWMCrates.getInstance().getPreviews().containsKey(preview_type)) {
                throw new RuntimeException("Preview type \"" + preview_type + "\" not found!");
            }
            try {
                Class<? extends Preview> preview_class = GWMCrates.getInstance().getPreviews().get(preview_type);
                Constructor<? extends Preview> preview_constructor = preview_class.getConstructor(ConfigurationNode.class);
                preview = Optional.of(preview_constructor.newInstance(preview_node));
            } catch (Exception e) {
                throw new RuntimeException("Exception creating Preview!", e);
            }
        }
    }

    public Manager(String id, String name, Case caze, Key key, List<Drop> drops, OpenManager open_manager) {
        this.id = id;
        this.name = name;
        this.caze = caze;
        this.key = key;
        this.drops = drops;
        this.open_manager = open_manager;
    }

    public Drop getRandomDrop() {
        int max_level = getMaxLevel();
        Drop drop;
        while ((drop = getDropByLevel(GWMCratesUtils.getRandomIntLevel(1, max_level))) == null) {
        }
        return drop;
    }

    private Drop getDropByLevel(int level) {
        List<Drop> drop_with_level = new ArrayList<Drop>();
        for (Drop drop : drops) {
            if (drop.getLevel() == level) {
                drop_with_level.add(drop);
            }
        }
        if (drop_with_level.size() == 0) {
            return null;
        } else if (drop_with_level.size() == 1) {
            return drop_with_level.get(0);
        } else {
            return drop_with_level.get(new Random().nextInt(drop_with_level.size()));
        }
    }

    private int getMaxLevel() {
        int max = 1;
        for (Drop drop : drops) {
            int level = drop.getLevel();
            if (level > max) {
                max = level;
            }
        }
        return max;
    }

    public Optional<Drop> getDropById(String id) {
        for (Drop drop : drops) {
            if (drop.getId().equalsIgnoreCase(id)) {
                return Optional.of(drop);
            }
        }
        return Optional.empty();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Case getCase() {
        return caze;
    }

    public void setCase(Case caze) {
        this.caze = caze;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public List<Drop> getDrop() {
        return drops;
    }

    public void setDrop(List<Drop> drops) {
        this.drops = drops;
    }

    public OpenManager getOpenManager() {
        return open_manager;
    }

    public void setOpenManager(OpenManager open_manager) {
        this.open_manager = open_manager;
    }

    public Optional<Preview> getPreview() {
        return preview;
    }

    public void setPreview(Optional<Preview> preview) {
        this.preview = preview;
    }
}
