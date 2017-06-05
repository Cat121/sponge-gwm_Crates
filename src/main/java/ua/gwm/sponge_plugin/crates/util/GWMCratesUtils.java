package ua.gwm.sponge_plugin.crates.util;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import ua.gwm.sponge_plugin.crates.drop.drops.CommandDrop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class GWMCratesUtils {

    public static boolean itemStacksEquals(ItemStack item, ItemStack other) {
        ItemStack copy1 = item.copy();
        ItemStack copy2 = other.copy();
        copy1.setQuantity(1);
        copy2.setQuantity(1);
        return copy1.equalTo(copy2);
    }

    public static int getRandomIntLevel(int min, int max) {
        Random random = new Random();
        while (random.nextBoolean()) {
            min++;
        }
        return min > max ? max : min;
    }

    public static Location<World> parseLocation(ConfigurationNode node) {
        ConfigurationNode x_node = node.getNode("X");
        ConfigurationNode y_node = node.getNode("Y");
        ConfigurationNode z_node = node.getNode("Z");
        ConfigurationNode world_node = node.getNode("WORLD_NAME");
        if (x_node.isVirtual()) {
            throw new RuntimeException("X node does not exist!");
        }
        if (y_node.isVirtual()) {
            throw new RuntimeException("Y node does not exist!");
        }
        if (z_node.isVirtual()) {
            throw new RuntimeException("Z node does not exist!");
        }
        if (world_node.isVirtual()) {
            throw new RuntimeException("WORLD_NAME node does not exist!");
        }
        double x = x_node.getDouble();
        double y = y_node.getDouble();
        double z = z_node.getDouble();
        String world_name = world_node.getString();
        Optional<World> optional_world = Sponge.getServer().getWorld(world_name);
        if (!optional_world.isPresent()) {
            throw new RuntimeException("World \"" + world_name + "\" does not exist!");
        }
        World world = optional_world.get();
        return new Location<World>(world, x, y, z);
    }

    public static ItemStack parseItem(ConfigurationNode node) {
        try {
            ConfigurationNode item_type_node = node.getNode("ITEM_TYPE");
            ConfigurationNode quantity_node = node.getNode("QUANTITY");
            ConfigurationNode sub_id_node = node.getNode("SUB_ID");
            ConfigurationNode durability_node = node.getNode("DURABILITY");
            ConfigurationNode display_name_node = node.getNode("DISPLAY_NAME");
            ConfigurationNode lore_node = node.getNode("LORE");
            ConfigurationNode enchantments_node = node.getNode("ENCHANTMENTS");
            if (item_type_node.isVirtual()) {
                throw new RuntimeException("ITEM_TYPE node does not exist!");
            }
            ItemStack item = ItemStack.of(item_type_node.getValue(TypeToken.of(ItemType.class)), quantity_node.getInt(1));
            if (!sub_id_node.isVirtual()) {
                DataContainer container = item.toContainer();
                container.set(DataQuery.of("UnsafeDamage"), sub_id_node.getInt(0));
                item.setRawData(container);
            }
            if (!durability_node.isVirtual()) {
                int durability = durability_node.getInt();
                item.offer(Keys.ITEM_DURABILITY, durability);
            }
            if (!display_name_node.isVirtual()) {
                Text display_name = TextSerializers.FORMATTING_CODE.deserialize(display_name_node.getString());
                item.offer(Keys.DISPLAY_NAME, display_name);
            }
            if (!lore_node.isVirtual()) {
                List<Text> lore = lore_node.getList(TypeToken.of(String.class)).stream().
                        map(TextSerializers.FORMATTING_CODE::deserialize).
                        collect(Collectors.toList());
                item.offer(Keys.ITEM_LORE, lore);
            }
            if (!enchantments_node.isVirtual()) {
                List<ItemEnchantment> item_enchantments = new ArrayList<ItemEnchantment>();
                for (ConfigurationNode enchantment_node : enchantments_node.getChildrenList()) {
                    item_enchantments.add(parseEnchantments(enchantment_node));
                }
                item.offer(Keys.ITEM_ENCHANTMENTS, item_enchantments);
            }
            return item;
        } catch (Exception e) {
            throw new RuntimeException("Exception parsing item!", e);
        }
    }

    public static ItemEnchantment parseEnchantments(ConfigurationNode node) {
        ConfigurationNode enchantment_node = node.getNode("ENCHANTMENT");
        ConfigurationNode level_node = node.getNode("LEVEL");
        if (enchantment_node.isVirtual()) {
            throw new RuntimeException("ENCHANTMENT node does not exist!");
        }
        try {
            Enchantment enchantment = enchantment_node.getValue(TypeToken.of(Enchantment.class));
            int level = level_node.getInt(1);
            return new ItemEnchantment(enchantment, level);
        } catch (Exception e) {
            throw new RuntimeException("Exception parsing enchantment!", e);
        }
    }

    public static CommandDrop.Command parseCommand(ConfigurationNode node) {
        ConfigurationNode cmd_node = node.getNode("CMD");
        ConfigurationNode console_node = node.getNode("CONSOLE");
        if (cmd_node.isVirtual()) {
            throw new RuntimeException("CMD node does not exist!");
        }
        String cmd = cmd_node.getString();
        boolean console = console_node.getBoolean(true);
        return new CommandDrop.Command(cmd, console);
    }
}
