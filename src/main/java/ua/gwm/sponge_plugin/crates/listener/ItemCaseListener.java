package ua.gwm.sponge_plugin.crates.listener;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.caze.Case;
import ua.gwm.sponge_plugin.crates.caze.cases.ItemCase;
import ua.gwm.sponge_plugin.crates.key.Key;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;
import ua.gwm.sponge_plugin.crates.util.LanguageUtils;

import java.util.Optional;

public class ItemCaseListener {

    @Listener(order = Order.LATE)
    public void openItemCase(InteractItemEvent.Secondary event) {
        Optional<Player> optional_player = event.getCause().first(Player.class);
        if (!optional_player.isPresent()) return;
        Player player = optional_player.get();
        ItemStack item = event.getItemStack().createStack();
        for (Manager manager : GWMCrates.getInstance().getCreatedManagers()) {
            Case caze = manager.getCase();
            if (!(caze instanceof ItemCase)) continue;
            ItemStack case_item = ((ItemCase) caze).getItem();
            if (!GWMCratesUtils.itemStacksEquals(item, case_item)) continue;
            event.setCancelled(true);
            OpenManager open_manager = manager.getOpenManager();
            if (!open_manager.canOpen(player, manager)) {
                player.sendMessage(LanguageUtils.getText("CAN_NOT_OPEN_CASE"));
                return;
            }
            Key key = manager.getKey();
            if (key.get(player) < 1) {
                player.sendMessage(LanguageUtils.getText("HAVE_NOT_KEY"));
                return;
            }
            caze.add(player, -1);
            key.add(player, -1);
            manager.getOpenManager().open(player, manager);
            break;
        }
    }
}
