package ua.gwm.sponge_plugin.crates.drop;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;

import java.math.BigDecimal;
import java.util.Optional;

public abstract class Drop {

    protected String id;
    protected Optional<BigDecimal> price = Optional.empty();
    protected Optional<ItemStack> drop_item = Optional.empty();
    protected int level;

    protected Drop(String id, Optional<BigDecimal> price, Optional<ItemStack> drop_item, int level) {
        this.id = id;
        this.price = price;
        this.drop_item = drop_item;
        this.level = level;
    }

    public Drop(ConfigurationNode node) {
        ConfigurationNode id_node = node.getNode("ID");
        ConfigurationNode price_node = node.getNode("PRICE");
        ConfigurationNode drop_item_node = node.getNode("DROP_ITEM");
        ConfigurationNode level_node = node.getNode("LEVEL");
        if (id_node.isVirtual()) {
            throw new RuntimeException("ID node does not exist!");
        }
        if (!price_node.isVirtual()) {
            price = Optional.of(new BigDecimal(price_node.getString("0")));
        }
        if (!drop_item_node.isVirtual()) {
            drop_item = Optional.of(GWMCratesUtils.parseItem(drop_item_node));
        }
        level = level_node.getInt(1);
    }

    public abstract void apply(Player player);

    public String getId() {
        return id;
    }

    public Optional<BigDecimal> getPrice() {
        return price;
    }

    public void setPrice(Optional<BigDecimal> price) {
        this.price = price;
    }

    public ItemStack getDropItem() {
        return drop_item.orElseGet(ItemStack::empty);
    }

    public void setDropItem(Optional<ItemStack> drop_item) {
        this.drop_item = drop_item;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
