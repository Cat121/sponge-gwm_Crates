package ua.gwm.sponge_plugin.crates.preview.previews;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.preview.Preview;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;
import ua.gwm.sponge_plugin.crates.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;

public class SecondGuiPreview extends Preview {

    public static final HashMap<Container, Pair<SecondGuiPreview, Manager>> SECOND_GUI_CONTAINERS = new HashMap<Container, Pair<SecondGuiPreview, Manager>>();

    private Optional<Text> display_name = Optional.empty();
    private boolean show_only_drops_with_drop_item = true;

    public SecondGuiPreview(Optional<Text> display_name, boolean show_only_drops_with_drop_item) {
        super();
        this.display_name = display_name;
        this.show_only_drops_with_drop_item = show_only_drops_with_drop_item;
    }

    public SecondGuiPreview(ConfigurationNode node) {
        super(node);
        ConfigurationNode display_name_node = node.getNode("DISPLAY_NAME");
        ConfigurationNode show_only_drops_with_drop_item_node = node.getNode("SHOW_ONLY_DROPS_WITH_DROP_ITEM_NODE");
        if (display_name_node.isVirtual()) {
            throw new RuntimeException("DISPLAY_NAME node does not exist!");
        }
        display_name = Optional.of(TextSerializers.FORMATTING_CODE.deserialize(display_name_node.getString()));
        show_only_drops_with_drop_item = show_only_drops_with_drop_item_node.getBoolean(true);
    }

    @Override
    public void preview(Player player, Manager manager) {
        InventoryDimension dimension = new InventoryDimension(9, GWMCratesUtils.getInventoryHeight(manager.getDrop().size()));
        Inventory inventory = display_name.map(text -> Inventory.builder().of(InventoryArchetypes.CHEST).
                property(InventoryDimension.PROPERTY_NAME, dimension).
                property(InventoryTitle.PROPERTY_NAME, new InventoryTitle(text)).
                build(GWMCrates.getInstance())).orElseGet(() -> Inventory.builder().of(InventoryArchetypes.CHEST).
                property(InventoryDimension.PROPERTY_NAME, dimension).
                build(GWMCrates.getInstance()));
        OrderedInventory ordered = inventory.query(OrderedInventory.class);
        Iterator<Drop> drop_iterator = manager.getDrop().iterator();
        int size = 9 * dimension.getRows();
        for (int i = 0; i < size && drop_iterator.hasNext();) {
            Drop next = drop_iterator.next();
            ItemStack drop_item = next.getDropItem();
            if (drop_item.equalTo(ItemStack.empty())) {
                continue;
            }
            ordered.getSlot(new SlotIndex(i)).get().set(drop_item);
            i++;
        }
        Container container = player.openInventory(inventory, GWMCrates.getInstance().getDefaultCause()).get();
        SECOND_GUI_CONTAINERS.put(container, new Pair<SecondGuiPreview, Manager>(this, manager));
    }
}
