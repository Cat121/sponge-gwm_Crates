package ua.gwm.sponge_plugin.crates.drop.drops;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;

import java.math.BigDecimal;
import java.util.Optional;

public class ItemDrop extends Drop {

    protected ItemStack item;

    public ItemDrop(ConfigurationNode node) {
        super(node);
        ConfigurationNode item_node = node.getNode("ITEM");
        if (item_node.isVirtual()) {
            throw new RuntimeException("ITEM node does not exist!");
        }
        item = GWMCratesUtils.parseItem(item_node);
    }

    public ItemDrop(String id, Optional<BigDecimal> price, Optional<ItemStack> drop_item, int level,
                    ItemStack item) {
        super(id, price, drop_item, level);
        this.item = item;
    }

    @Override
    public void apply(Player player) {
        player.getInventory().offer(item.copy());
    }

    @Override
    public ItemStack getDropItem() {
        return drop_item.orElse(item);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
