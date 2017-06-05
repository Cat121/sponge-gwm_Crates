package ua.gwm.sponge_plugin.crates.key;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;

import java.math.BigDecimal;
import java.util.Optional;

public abstract class Key {

    protected Optional<BigDecimal> price = Optional.empty();

    protected Key(Optional<BigDecimal> price) {
        this.price = price;
    }

    public Key(ConfigurationNode node) {
        ConfigurationNode price_node = node.getNode("PRICE");
        if (!price_node.isVirtual()) {
            price = Optional.of(new BigDecimal(price_node.getString("0")));
        }
    }

    public abstract void add(Player player, int amount);

    public abstract int get(Player player);

    public Optional<BigDecimal> getPrice() {
        return price;
    }

    public void setPrice(Optional<BigDecimal> price) {
        this.price = price;
    }
}
