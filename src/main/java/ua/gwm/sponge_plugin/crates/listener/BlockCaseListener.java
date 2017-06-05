package ua.gwm.sponge_plugin.crates.listener;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.caze.Case;
import ua.gwm.sponge_plugin.crates.caze.cases.BlockCase;
import ua.gwm.sponge_plugin.crates.key.Key;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.util.LanguageUtils;

import java.util.Collection;
import java.util.Optional;

public class BlockCaseListener {

    @Listener(order = Order.LATE)
    public void openCase(InteractBlockEvent.Secondary.MainHand event) {
        Optional<Player> optional_player = event.getCause().first(Player.class);
        if (!optional_player.isPresent()) return;
        Player player = optional_player.get();
        Optional<Location<World>> optional_location = event.getTargetBlock().getLocation();
        if (!optional_location.isPresent()) return;
        Location<World> location = optional_location.get();
        for (Manager manager : GWMCrates.getInstance().getCreatedManagers()) {
            Case caze = manager.getCase();
            if (!(caze instanceof BlockCase)) continue;
            Collection<Location<World>> locations = ((BlockCase) caze).getLocations();
            if (!locations.contains(location)) continue;
            event.setCancelled(true);
            OpenManager open_manager = manager.getOpenManager();
            if (!open_manager.canOpen(player, manager)) {
                player.sendMessage(LanguageUtils.getText("CAN_NOT_OPEN_CASE"));
                return;
            }
            Key key = manager.getKey();
            key.add(player, 10);
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
