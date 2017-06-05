package ua.gwm.sponge_plugin.crates.key.keys;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.key.Key;

import java.math.BigDecimal;
import java.util.Optional;

public class TimedKey extends Key {

    protected String virtual_name;
    protected long cooldown;

    public TimedKey(ConfigurationNode node) {
        super(node);
        ConfigurationNode virtual_name_node = node.getNode("VIRTUAL_NAME");
        ConfigurationNode cooldown_node = node.getNode("COOLDOWN");
        if (virtual_name_node.isVirtual()) {
            throw new RuntimeException("VIRTUAL_NAME node does not exist!");
        }
        if (cooldown_node.isVirtual()) {
            throw new RuntimeException("COOLDOWN node does not exist!");
        }
        virtual_name = virtual_name_node.getString();
        cooldown = cooldown_node.getLong();
    }

    public TimedKey(Optional<BigDecimal> price, String virtual_name, long cooldown) {
        super(price);
        this.virtual_name = virtual_name;
        this.cooldown = cooldown;
    }

    @Override
    public void add(Player player, int amount) {
        ConfigurationNode cooldown_node = GWMCrates.getInstance().getTimedKeysCooldownsConfig().
                getNode(player.getUniqueId().toString(), virtual_name);
        if (amount > 0) {
            cooldown_node.setValue(null);
        } else if (amount < 0) {
            cooldown_node.setValue(System.currentTimeMillis() + cooldown);
        }
    }

    @Override
    public int get(Player player) {
        ConfigurationNode cooldown_node = GWMCrates.getInstance().getTimedKeysCooldownsConfig().
                getNode(player.getUniqueId().toString(), virtual_name);
        if (cooldown_node.isVirtual()) {
            return Integer.MAX_VALUE;
        }
        long cooldown = cooldown_node.getLong();
        if (System.currentTimeMillis() >= cooldown) {
            cooldown_node.setValue(null);
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }
    }

    public String getVirtualName() {
        return virtual_name;
    }

    public void setVirtualName(String virtual_name) {
        this.virtual_name = virtual_name;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }
}
