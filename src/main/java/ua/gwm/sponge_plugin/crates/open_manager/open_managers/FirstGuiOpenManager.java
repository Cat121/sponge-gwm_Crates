package ua.gwm.sponge_plugin.crates.open_manager.open_managers;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.type.OrderedInventory;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;
import ua.gwm.sponge_plugin.crates.util.LanguageUtils;
import ua.gwm.sponge_plugin.crates.util.Pair;

import java.util.*;

public class FirstGuiOpenManager extends OpenManager {

    public static final HashMap<Container, Pair<FirstGuiOpenManager, Manager>> FIRST_GUI_CONTAINERS = new HashMap<Container, Pair<FirstGuiOpenManager, Manager>>();

    private List<ItemStack> decorative_items;
    private List<Integer> scroll_cooldowns;
    private boolean clear_decorative_items;
    private boolean clear_other_items;
    private int close_cooldown;
    private boolean forbid_close;

    public FirstGuiOpenManager(ConfigurationNode node) {
        super(node);
        ConfigurationNode decorative_items_node = node.getNode("DECORATIVE_ITEMS");
        ConfigurationNode scroll_cooldowns_node = node.getNode("SCROLL_COOLDOWNS");
        ConfigurationNode clear_decorative_items_node = node.getNode("CLEAR_DECORATIVE_ITEMS");
        ConfigurationNode clear_other_items_node = node.getNode("CLEAR_OTHER_ITEMS");
        ConfigurationNode close_cooldown_node = node.getNode("CLOSE_COOLDOWN");
        ConfigurationNode forbid_clode_node = node.getNode("FORBID_CLOSE");
        try {
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
            if (scroll_cooldowns_node.isVirtual()) {
                throw new RuntimeException("SCROLL_COOLDOWNS node does not exist!");
            }
            scroll_cooldowns = scroll_cooldowns_node.getList(TypeToken.of(Integer.class));
            clear_decorative_items = clear_decorative_items_node.getBoolean(false);
            clear_other_items = clear_other_items_node.getBoolean(true);
            if (close_cooldown_node.isVirtual()) {
                throw new RuntimeException("CLOSE_COOLDOWN node does not exist!");
            }
            close_cooldown = close_cooldown_node.getInt();
            forbid_close = forbid_clode_node.getBoolean(true);
        } catch (Exception e) {
            throw new RuntimeException("Exception creating FIRST GUI OPEN MANAGER!", e);
        }
    }

    public FirstGuiOpenManager(List<ItemStack> decorative_items, List<Integer> scroll_cooldowns,
                               boolean clear_decorative_items, boolean clear_other_items, int close_cooldown) {
        super();
        if (decorative_items.size() != 20) {
            throw new RuntimeException("DECORATIVE_ITEMS size must be 20 instead of " + decorative_items.size() + "!");
        }
        this.decorative_items = decorative_items;
        this.scroll_cooldowns = scroll_cooldowns;
        this.clear_decorative_items = clear_decorative_items;
        this.clear_other_items = clear_other_items;
        this.close_cooldown = close_cooldown;
    }

    @Override
    public void open(Player player, Manager manager) {
        Inventory inventory = Inventory.builder().of(InventoryArchetypes.CHEST).
                build(GWMCrates.getInstance());
        ArrayList<Drop> drop_list = new ArrayList<Drop>();
        OrderedInventory ordered = inventory.query(OrderedInventory.class);
        for (int i = 0; i < 10; i++) {
            ordered.getSlot(new SlotIndex(i)).get().set(decorative_items.get(i));
        }
        for (int i = 10; i < 17; i++) {
            Drop new_drop = manager.getRandomDrop();
            drop_list.add(new_drop);
            ordered.getSlot(new SlotIndex(i)).get().set(new_drop.getDropItem());
        }
        for (int i = 17; i < 27; i++) {
            ordered.getSlot(new SlotIndex(i)).get().set(decorative_items.get(i - 7));
        }
        Container container = player.openInventory(inventory, GWMCrates.getInstance().getDefaultCause()).get();
        FIRST_GUI_CONTAINERS.put(container, new Pair<FirstGuiOpenManager, Manager>(this, manager));
        int wait_time = 0;
        for (int i = 0; i < scroll_cooldowns.size() - 1; i++) {
            wait_time += scroll_cooldowns.get(i);
            Sponge.getScheduler().createTaskBuilder().delayTicks(wait_time).execute(() -> {
                for (int j = 10; j < 16; j++) {
                    ordered.getSlot(new SlotIndex(j)).get().set(inventory.query(new SlotIndex(j + 1)).peek().orElse(ItemStack.empty()));
                }
                Drop new_drop = manager.getRandomDrop();
                drop_list.add(new_drop);
                ordered.getSlot(new SlotIndex(16)).get().set(new_drop.getDropItem());
            }).submit(GWMCrates.getInstance());
        }
        Sponge.getScheduler().createTaskBuilder().delayTicks(wait_time + scroll_cooldowns.get(scroll_cooldowns.size() - 1)).execute(() -> {
            Drop drop = drop_list.get(drop_list.size() - 4);
            drop.apply(player);
            if (clear_decorative_items) {
                for (int i = 0; i < 10; i++) {
                    ordered.getSlot(new SlotIndex(i)).get().set(ItemStack.empty());
                }
                for (int i = 17; i < 17; i++) {
                    ordered.getSlot(new SlotIndex(i)).get().set(ItemStack.empty());
                }
            }
            if (clear_other_items) {
                for (int i = 10; i < 13; i++) {
                    ordered.getSlot(new SlotIndex(i)).get().set(ItemStack.empty());
                }
                for (int i = 14; i < 17; i++) {
                    ordered.getSlot(new SlotIndex(i)).get().set(ItemStack.empty());
                }
            }
            player.sendMessage(LanguageUtils.getText("SUCCESSFULLY_OPENED_MANAGER",
                    new Pair<String, String>("%MANAGER%", manager.getName())));
        }).submit(GWMCrates.getInstance());
        Sponge.getScheduler().createTaskBuilder().delayTicks(wait_time + scroll_cooldowns.get(scroll_cooldowns.size() - 1) + close_cooldown).execute(() -> {
            Optional<Container> optional_open_inventory = player.getOpenInventory();
            if (optional_open_inventory.isPresent() && container.equals(optional_open_inventory.get())) {
                player.closeInventory(GWMCrates.getInstance().getDefaultCause());
            }
            FIRST_GUI_CONTAINERS.remove(container);
        }).submit(GWMCrates.getInstance());
    }

    @Override
    public boolean canOpen(Player player, Manager manager) {
        return true;
    }

    public List<ItemStack> getDecorativeItems() {
        return decorative_items;
    }

    public void setDecorativeItems(List<ItemStack> decorative_items) {
        this.decorative_items = decorative_items;
    }

    public List<Integer> getScrollCooldowns() {
        return scroll_cooldowns;
    }

    public void setScrollCooldowns(List<Integer> scroll_cooldowns) {
        this.scroll_cooldowns = scroll_cooldowns;
    }

    public boolean isClearDecorativeItems() {
        return clear_decorative_items;
    }

    public void setClearDecorativeItems(boolean clear_decorative_items) {
        this.clear_decorative_items = clear_decorative_items;
    }

    public boolean isClearOtherItems() {
        return clear_other_items;
    }

    public void setClearOtherItems(boolean clear_other_items) {
        this.clear_other_items = clear_other_items;
    }

    public int getCloseCooldown() {
        return close_cooldown;
    }

    public void setCloseCooldown(int close_cooldown) {
        this.close_cooldown = close_cooldown;
    }

    public boolean isForbidClose() {
        return forbid_close;
    }

    public void setForbidClose(boolean forbid_close) {
        this.forbid_close = forbid_close;
    }
}
