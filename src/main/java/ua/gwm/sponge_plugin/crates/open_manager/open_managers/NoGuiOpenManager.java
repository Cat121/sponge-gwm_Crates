package ua.gwm.sponge_plugin.crates.open_manager.open_managers;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.util.LanguageUtils;
import ua.gwm.sponge_plugin.crates.util.Pair;

public class NoGuiOpenManager extends OpenManager {

    public NoGuiOpenManager(ConfigurationNode node) {
        super(node);
    }

    public NoGuiOpenManager() {
        super();
    }

    @Override
    public void open(Player player, Manager manager) {
        Drop drop = manager.getRandomDrop();
        drop.apply(player);
        player.sendMessage(LanguageUtils.getText("SUCCESSFULLY_OPENED_MANAGER",
                new Pair<String, String>("%MANAGER%", manager.getName())));
    }

    @Override
    public boolean canOpen(Player player, Manager manager) {
        return true;
    }
}
