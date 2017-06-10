package ua.gwm.sponge_plugin.crates.preview.previews;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class FirstGuiPreview extends Preview {

    public static final HashMap<Container, Pair<FirstGuiPreview, Manager>> FIRST_GUI_CONTAINERS = new HashMap<Container, Pair<FirstGuiPreview, Manager>>();

    private Optional<Text> display_name = Optional.empty();
    private List<ItemStack> decorative_items;
    private int scroll_delay;

    public FirstGuiPreview(Optional<Text> display_name, List<ItemStack> decorative_items, int scroll_delay) {
        super();
        this.display_name = display_name;
        this.decorative_items = decorative_items;
        this.scroll_delay = scroll_delay;
    }

    public FirstGuiPreview(ConfigurationNode node) {
        super(node);
        ConfigurationNode display_name_node = node.getNode("DISPLAY_NAME");
        ConfigurationNode decorative_items_node = node.getNode("DECORATIVE_ITEMS");
        ConfigurationNode scroll_delay_node = node.getNode("SCROLL_DELAY");
        if (!display_name_node.isVirtual()) {
            display_name = Optional.of(TextSerializers.FORMATTING_CODE.deserialize(display_name_node.getString()));
        }
        if (decorative_items_node.isVirtual()) {
            throw new RuntimeException("DECORATIVE_ITEMS node does not exist!");
        }
        decorative_items = new ArrayList<ItemStack>();
        for (ConfigurationNode decorative_item_node : decorative_items_node.getChildrenList()) {
            decorative_items.add(GWMCratesUtils.parseItem(decorative_item_node));
        }
        if (decorative_items.size() != 20) {
            throw new RuntimeException("DECORATIVE_ITEMS size must be 20 instead of " + decorative_items.size() + "!");
        }
        scroll_delay = scroll_delay_node.getInt(10);
    }

    @Override
    public void preview(Player player, Manager manager) {
        Inventory inventory = display_name.map(text -> Inventory.builder().of(InventoryArchetypes.CHEST).
                property(InventoryTitle.PROPERTY_NAME, new InventoryTitle(text)).
                build(GWMCrates.getInstance())).orElseGet(() -> Inventory.builder().of(InventoryArchetypes.CHEST).
                build(GWMCrates.getInstance()));
        OrderedInventory ordered = inventory.query(OrderedInventory.class);
        for (int i = 0; i < 10; i++) {
            ordered.getSlot(new SlotIndex(i)).get().set(decorative_items.get(i));
        }
        List<Drop> drops = manager.getDrop();
        int index = 0;
        for (int i = 10; i < 17; i++) {
            if (index > drops.size() - 1) {
                index = 0;
            }
            ordered.getSlot(new SlotIndex(i)).get().set(drops.get(index).getDropItem());
            index++;
        }
        for (int i = 17; i < 27; i++) {
            ordered.getSlot(new SlotIndex(i)).get().set(decorative_items.get(i - 7));
        }
        Container container = player.openInventory(inventory, GWMCrates.getInstance().getDefaultCause()).get();
        FIRST_GUI_CONTAINERS.put(container, new Pair<FirstGuiPreview, Manager>(this, manager));
        DropChangeRunnable runnable = new DropChangeRunnable(container, drops, index);
        Sponge.getScheduler().createTaskBuilder().delayTicks(scroll_delay).execute(runnable).submit(GWMCrates.getInstance());
    }

    public class DropChangeRunnable implements Runnable {

        private Container container;
        private OrderedInventory inventory;
        private List<Drop> drops;
        private int index;

        public DropChangeRunnable(Container container, List<Drop> drops, int index) {
            this.container = container;
            this.inventory = container.first();
            this.drops = drops;
            this.index = index;
        }

        @Override
        public void run() {
            if (!FIRST_GUI_CONTAINERS.containsKey(container)) {
                return;
            }
            for (int i = 10; i < 16; i++) {
                inventory.getSlot(new SlotIndex(i)).get().set(inventory.query(new SlotIndex(i + 1)).peek().orElse(ItemStack.empty()));
            }
            if (index == drops.size()) {
                index = 0;
            }
            inventory.query(new SlotIndex(16)).set(drops.get(index).getDropItem());
            index++;
            Sponge.getScheduler().createTaskBuilder().delayTicks(scroll_delay).execute(this).submit(GWMCrates.getInstance());
        }
    }
}
