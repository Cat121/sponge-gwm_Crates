package ua.gwm.sponge_plugin.crates.key.keys;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import ua.gwm.sponge_plugin.crates.key.Key;

import java.util.Optional;

public class EmptyKey extends Key {

    public EmptyKey(ConfigurationNode node) {
        super(node);
    }

    public EmptyKey() {
        super(Optional.empty());
    }

    @Override
    public void add(Player player, int amount) {
    }

    @Override
    public int get(Player player) {
        return Integer.MAX_VALUE;
    }
}
