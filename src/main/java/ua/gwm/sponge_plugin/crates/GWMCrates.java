package ua.gwm.sponge_plugin.crates;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import ua.gwm.sponge_plugin.crates.caze.Case;
import ua.gwm.sponge_plugin.crates.caze.cases.*;
import ua.gwm.sponge_plugin.crates.command.GWMCratesCommand;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.drop.drops.CommandDrop;
import ua.gwm.sponge_plugin.crates.drop.drops.ItemDrop;
import ua.gwm.sponge_plugin.crates.drop.drops.MultiDrop;
import ua.gwm.sponge_plugin.crates.event.GWMCratesRegistrationEvent;
import ua.gwm.sponge_plugin.crates.key.Key;
import ua.gwm.sponge_plugin.crates.key.keys.EmptyKey;
import ua.gwm.sponge_plugin.crates.key.keys.ItemKey;
import ua.gwm.sponge_plugin.crates.key.keys.TimedKey;
import ua.gwm.sponge_plugin.crates.key.keys.VirtualKey;
import ua.gwm.sponge_plugin.crates.listener.*;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.open_manager.open_managers.FirstGuiOpenManager;
import ua.gwm.sponge_plugin.crates.open_manager.open_managers.NoGuiOpenManager;
import ua.gwm.sponge_plugin.crates.open_manager.open_managers.SecondGuiOpenManager;
import ua.gwm.sponge_plugin.crates.preview.Preview;
import ua.gwm.sponge_plugin.crates.preview.previews.FirstGuiPreview;
import ua.gwm.sponge_plugin.crates.preview.previews.SecondGuiPreview;
import ua.gwm.sponge_plugin.crates.util.Config;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Plugin(
        id = "gwm_crates",
        name = "GWMCrates",
        version = "1.1",
        description = "Universal crates plugin for your server!",
        authors = {"GWM"/*My contacts: Skype(nk_gwm), Discord(GWM#2192)*/})
public class GWMCrates {

    private static GWMCrates instance;

    public static GWMCrates getInstance() {
        if (instance == null) {
            throw new RuntimeException("GWMCrates not initialized!");
        }
        return instance;
    }

    private Cause default_cause = Cause.of(NamedCause.source(this));

    @Inject
    @ConfigDir(sharedRoot = false)
    private File config_directory;
    private File managers_directory;

    @Inject
    private Logger logger;

    @Inject
    private PluginContainer plugin_container;

    private HashMap<String, Class<? extends Case>> cases = new HashMap<String, Class<? extends Case>>();
    private HashMap<String, Class<? extends Key>> keys = new HashMap<String, Class<? extends Key>>();
    private HashMap<String, Class<? extends Drop>> drops = new HashMap<String, Class<? extends Drop>>();
    private HashMap<String, Class<? extends OpenManager>> open_managers = new HashMap<String, Class<? extends OpenManager>>();
    private HashMap<String, Class<? extends Preview>> previews = new HashMap<String, Class<? extends Preview>>();

    private HashSet<Manager> created_managers = new HashSet<Manager>();

    private Optional<EconomyService> optional_economy_service = Optional.empty();

    private Config config;
    private Config language_config;
    private Config virtual_cases_config;
    private Config virtual_keys_config;
    private Config timed_cases_cooldowns_config;
    private Config timed_keys_cooldowns_config;

    @Listener
    public void onConstruct(GameConstructionEvent event) {
        instance = this;
    }

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        managers_directory = new File(config_directory, "managers");
        if (!config_directory.exists()) {
            try {
                config_directory.mkdirs();
            } catch (Exception e) {
                logger.warn("Failed creating config directory!", e);
            }
        }
        if (!managers_directory.exists()) {
            try {
                managers_directory.mkdirs();
            } catch (Exception e) {
                logger.warn("Failed creating managers config directory!", e);
            }
        }
        config = new Config("config.conf", false);
        language_config = new Config("language.conf", false);
        virtual_cases_config = new Config("virtual_cases.conf", true);
        virtual_keys_config = new Config("virtual_keys.conf", true);
        timed_cases_cooldowns_config = new Config("timed_cases_cooldowns.conf", true);
        timed_keys_cooldowns_config = new Config("timed_keys_cooldowns.conf", true);
        logger.info("PreInitialization complete!");
    }

    @Listener
    public void onInitialize(GameInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new ItemCaseListener());
        Sponge.getEventManager().registerListeners(this, new BlockCaseListener());
        Sponge.getEventManager().registerListeners(this, new FirstGuiOpenManagerListener());
        Sponge.getEventManager().registerListeners(this, new SecondGuiOpenManagerListener());
        Sponge.getEventManager().registerListeners(this, new PreviewListener());
        Sponge.getCommandManager().register(this, new GWMCratesCommand(), "gwmcrates", "crates");
        register();
        logger.info("Initialization complete!");
    }

    @Listener
    public void onStarting(GameStartingServerEvent event) {
        createManagers();
        loadEconomy();
    }

    @Listener
    public void reloadListener(GameReloadEvent event) {
        reload();
    }

    public void save() {
        config.save();
        language_config.save();
        virtual_cases_config.save();
        virtual_keys_config.save();
        timed_cases_cooldowns_config.save();
        timed_keys_cooldowns_config.save();
        logger.info("All plugin configs has been reloaded!");
    }

    public void reload() {
        config.reload();
        language_config.reload();
        virtual_cases_config.reload();
        virtual_keys_config.reload();
        timed_cases_cooldowns_config.reload();
        timed_keys_cooldowns_config.reload();
        cases.clear();
        keys.clear();
        open_managers.clear();
        drops.clear();
        previews.clear();
        register();
        created_managers.clear();
        createManagers();
        optional_economy_service = Optional.empty();
        loadEconomy();
        logger.info("Plugin has been reloaded.");
    }

    private void register() {
        GWMCratesRegistrationEvent registration_event = new GWMCratesRegistrationEvent();
        registration_event.getCases().put("ITEM", ItemCase.class);
        registration_event.getCases().put("BLOCK", BlockCase.class);
        registration_event.getCases().put("VIRTUAL", VirtualCase.class);
        registration_event.getCases().put("TIMED", TimedCase.class);
        registration_event.getCases().put("EMPTY", EmptyCase.class);
        registration_event.getKeys().put("ITEM", ItemKey.class);
        registration_event.getKeys().put("VIRTUAL", VirtualKey.class);
        registration_event.getKeys().put("TIMED", TimedKey.class);
        registration_event.getKeys().put("EMPTY", EmptyKey.class);
        registration_event.getDrops().put("ITEM", ItemDrop.class);
        registration_event.getDrops().put("COMMAND", CommandDrop.class);
        registration_event.getDrops().put("MULTI", MultiDrop.class);
        registration_event.getOpenManagers().put("NO-GUI", NoGuiOpenManager.class);
        registration_event.getOpenManagers().put("FIRST", FirstGuiOpenManager.class);
        registration_event.getOpenManagers().put("SECOND", SecondGuiOpenManager.class);
        registration_event.getPreviews().put("FIRST", FirstGuiPreview.class);
        registration_event.getPreviews().put("SECOND", SecondGuiPreview.class);
        Sponge.getEventManager().post(registration_event);
        for (Map.Entry<String, Class<? extends Case>> entry : registration_event.getCases().entrySet()) {
            String name = entry.getKey();
            Class<? extends Case> case_class = entry.getValue();
            String class_name = case_class.getSimpleName();
            if (cases.containsKey(name)) {
                logger.warn("Trying to add Case type " + name + " (" + class_name + ".class) which already exist!");
            } else {
                cases.put(name, case_class);
                logger.info("Successfully added Case type " + name + " (" + class_name + ".class)!");
            }
        }
        for (Map.Entry<String, Class<? extends Key>> entry : registration_event.getKeys().entrySet()) {
            String name = entry.getKey();
            Class<? extends Key> key_class = entry.getValue();
            String class_name = key_class.getSimpleName();
            if (keys.containsKey(name)) {
                logger.warn("Trying to add Key type " + name + " (" + class_name + ".class) which already exist!");
            } else {
                keys.put(name, key_class);
                logger.info("Successfully added Key type " + name + " (" + class_name + ".class)!");
            }
        }
        for (Map.Entry<String, Class<? extends Drop>> entry : registration_event.getDrops().entrySet()) {
            String name = entry.getKey();
            Class<? extends Drop> drop_class = entry.getValue();
            String class_name = drop_class.getSimpleName();
            if (drops.containsKey(name)) {
                logger.warn("Trying to add Drop type " + name + " (" + class_name + ".class) which already exist!");
            } else {
                drops.put(name, drop_class);
                logger.info("Successfully added Drop type " + name + " (" + class_name + ".class)!");
            }
        }
        for (Map.Entry<String, Class<? extends OpenManager>> entry : registration_event.getOpenManagers().entrySet()) {
            String name = entry.getKey();
            Class<? extends OpenManager> open_manager_class = entry.getValue();
            String class_name = open_manager_class.getSimpleName();
            if (open_managers.containsKey(name)) {
                logger.warn("Trying to add Open Manager type " + name + " (" + class_name + ".class) which already exist!");
            } else {
                open_managers.put(name, open_manager_class);
                logger.info("Successfully added Open Manager type " + name + " (" + class_name + ".class)!");
            }
        }
        for (Map.Entry<String, Class<? extends Preview>> entry : registration_event.getPreviews().entrySet()) {
            String name = entry.getKey();
            Class<? extends Preview> preview_class = entry.getValue();
            String class_name = preview_class.getSimpleName();
            if (previews.containsKey(name)) {
                logger.warn("Trying to add Preview type " + name + " (" + class_name + ".class) which already exist!");
            } else {
                previews.put(name, preview_class);
                logger.info("Successfully added Preview type " + name + " (" + class_name + ".class)!");
            }
        }
        logger.info("Registration complete!");
    }

    private void createManagers() {
        File[] managers_files = managers_directory.listFiles();
        if (managers_files.length == 0) {
            logger.warn("No one manager was found.");
        } else {
            for (File manager_file : managers_files) {
                try {
                    ConfigurationLoader<CommentedConfigurationNode> manager_configuration_loader =
                            HoconConfigurationLoader.builder().setFile(manager_file).build();
                    ConfigurationNode manager_node = manager_configuration_loader.load();
                    Manager manager = new Manager(manager_node);
                    created_managers.add(manager);
                    logger.info("Manager \"" + manager.getName() + "\" successfully created!");
                } catch (Exception e) {
                    logger.info("Exception creating manager " + manager_file.getName() + "!", e);
                }
            }
            logger.info("All managers created!");
        }
    }

    private void loadEconomy() {
        optional_economy_service = Sponge.getServiceManager().provide(EconomyService.class);
        if (optional_economy_service.isPresent()) {
            logger.info("Economy Service found!");
        } else {
            logger.info("Economy Service does not found!");
        }
    }

    public Optional<Manager> getManagerById(String manager_id) {
        for (Manager manager : created_managers) {
            if (manager.getId().equalsIgnoreCase(manager_id)) {
                return Optional.of(manager);
            }
        }
        return Optional.empty();
    }

    public Cause getDefaultCause() {
        return default_cause;
    }

    public File getConfigDirectory() {
        return config_directory;
    }

    public Logger getLogger() {
        return logger;
    }

    public Optional<EconomyService> getEconomyService() {
        return optional_economy_service;
    }

    public PluginContainer getPluginContainer() {
        return plugin_container;
    }

    public HashMap<String, Class<? extends Case>> getCases() {
        return cases;
    }

    public HashMap<String, Class<? extends Key>> getKeys() {
        return keys;
    }

    public HashMap<String, Class<? extends Drop>> getDrops() {
        return drops;
    }

    public HashMap<String, Class<? extends OpenManager>> getOpenManagers() {
        return open_managers;
    }

    public HashMap<String, Class<? extends Preview>> getPreviews() {
        return previews;
    }

    public HashSet<Manager> getCreatedManagers() {
        return created_managers;
    }

    public Config getConfig() {
        return config;
    }

    public Config getLanguageConfig() {
        return language_config;
    }

    public Config getVirtualCasesConfig() {
        return virtual_cases_config;
    }

    public Config getVirtualKeysConfig() {
        return virtual_keys_config;
    }

    public Config getTimedCasesCooldownsConfig() {
        return timed_cases_cooldowns_config;
    }

    public Config getTimedKeysCooldownsConfig() {
        return timed_keys_cooldowns_config;
    }

    public boolean isDebugEnabled() {
        return config.getNode("DEBUG").getBoolean(true);
    }
}
