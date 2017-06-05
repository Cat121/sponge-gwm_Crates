package ua.gwm.sponge_plugin.crates.open_manager.open_managers;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;
import ua.gwm.sponge_plugin.crates.util.Pair;

import java.util.HashMap;

public class SecondGuiOpenManager extends OpenManager {

    public static final HashMap<Container, Pair<SecondGuiOpenManager, Manager>> SECOND_GUI_INVENTORIES = new HashMap<Container, Pair<SecondGuiOpenManager, Manager>>();

    private ItemStack hidden_item;
    private boolean increase_hidden_item_quantity;
    private int rows;
    private boolean show_other_items;
    private int close_delay;
    private boolean forbid_close;
    private boolean give_random_on_close;

    public SecondGuiOpenManager(ConfigurationNode node) {
        super(node);
        ConfigurationNode hidden_item_node = node.getNode("HIDDEN_ITEM");
        ConfigurationNode increase_hidden_item_quantity_node = node.getNode("INCREASE_HIDDEN_ITEM_QUANTITY");
        ConfigurationNode rows_node = node.getNode("ROWS");
        ConfigurationNode show_other_items_node = node.getNode("SHOW_OTHER_ITEMS");
        ConfigurationNode close_delay_node = node.getNode("CLOSE_DELAY");
        ConfigurationNode forbid_close_node = node.getNode("FORBID_CLOSE");
        ConfigurationNode give_random_on_close_node = node.getNode("GIVE_RANDOM_ON_CLOSE");
        try {
            if (hidden_item_node.isVirtual()) {
                throw new RuntimeException("HIDDEN_ITEM node does not exist!");
            }
            hidden_item = GWMCratesUtils.parseItem(hidden_item_node);
            increase_hidden_item_quantity = increase_hidden_item_quantity_node.getBoolean(true);
            rows = rows_node.getInt(3);
            if (rows < 1 || rows > 6) {
                GWMCrates.getInstance().getLogger().info("ROWS value more than 6 or less than 1! Force set it to 3!");
                rows = 3;
            }
            show_other_items = show_other_items_node.getBoolean(true);
            close_delay = close_delay_node.getInt(20);
            forbid_close = forbid_close_node.getBoolean(true);
            give_random_on_close = give_random_on_close_node.getBoolean(true);
        } catch (Exception e) {
            throw new RuntimeException("Exception creating SECOND GUI OPEN MANAGER!", e);
        }
    }

    public SecondGuiOpenManager(ItemStack hidden_item, boolean increase_hidden_item_quantity, int rows,
                                boolean show_other_items, int close_delay, boolean forbid_close,
                                boolean give_random_on_close) {
        super();
        this.hidden_item = hidden_item;
        this.increase_hidden_item_quantity = increase_hidden_item_quantity;
        if (rows < 1 || rows > 6) {
            GWMCrates.getInstance().getLogger().info("ROWS value more than 6 or less than 1! Force set it to 3!");
            rows = 3;
        }
        this.rows = rows;
        this.show_other_items = show_other_items;
        this.close_delay = close_delay;
        this.forbid_close = forbid_close;
        this.give_random_on_close = give_random_on_close;
    }

    @Override
    public void open(Player player, Manager manager) {
        Inventory inventory = Inventory.builder().
                property(InventoryDimension.PROPERTY_NAME, new InventoryDimension(9, rows)).
                build(GWMCrates.getInstance());
        OrderedInventory ordered = inventory.query(OrderedInventory.class);
        int hidden_item_quantity = hidden_item.getQuantity();
        for (int i = 0; i < 9 * rows; i++) {
            int quantity = increase_hidden_item_quantity ? i + 1 : hidden_item_quantity;
            ItemStack copy = hidden_item.copy();
            copy.setQuantity(quantity);
            ordered.getSlot(new SlotIndex(i)).get().set(copy);
        }
        Container container = player.openInventory(inventory, GWMCrates.getInstance().getDefaultCause()).get();
        SECOND_GUI_INVENTORIES.put(container, new Pair<SecondGuiOpenManager, Manager>(this, manager));
    }

    @Override
    public boolean canOpen(Player player, Manager manager) {
        return true;
    }

    public ItemStack getHiddenItem() {
        return hidden_item;
    }

    public void setHiddenItem(ItemStack hidden_item) {
        this.hidden_item = hidden_item;
    }

    public boolean isIncreaseHiddenItemQuantity() {
        return increase_hidden_item_quantity;
    }

    public void setIncreaseHiddenItemQuantity(boolean increase_hidden_item_quantity) {
        this.increase_hidden_item_quantity = increase_hidden_item_quantity;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public boolean isShowOtherItems() {
        return show_other_items;
    }

    public void setShowOtherItems(boolean show_other_items) {
        this.show_other_items = show_other_items;
    }

    public int getCloseDelay() {
        return close_delay;
    }

    public void setCloseDelay(int close_delay) {
        this.close_delay = close_delay;
    }

    public boolean isForbidClose() {
        return forbid_close;
    }

    public void setForbidClose(boolean forbid_close) {
        this.forbid_close = forbid_close;
    }

    public boolean isGiveRandomOnClose() {
        return give_random_on_close;
    }

    public void setGiveRandomOnClose(boolean give_random_on_close) {
        this.give_random_on_close = give_random_on_close;
    }
}