package ua.gwm.sponge_plugin.crates.listener;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Container;
import ua.gwm.sponge_plugin.crates.preview.previews.FirstGuiPreview;
import ua.gwm.sponge_plugin.crates.preview.previews.SecondGuiPreview;

public class PreviewListener {

    @Listener
    public void cancelClick(ClickInventoryEvent event) {
        Container container = event.getTargetInventory();
        if (FirstGuiPreview.FIRST_GUI_CONTAINERS.containsKey(container) ||
                SecondGuiPreview.SECOND_GUI_CONTAINERS.containsKey(container)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void manageClose(InteractInventoryEvent.Close event) {
        Container container = event.getTargetInventory();
        if (FirstGuiPreview.FIRST_GUI_CONTAINERS.containsKey(container)) {
            FirstGuiPreview.FIRST_GUI_CONTAINERS.remove(container);
        } else if (SecondGuiPreview.SECOND_GUI_CONTAINERS.containsKey(container)) {
            SecondGuiPreview.SECOND_GUI_CONTAINERS.remove(container);
        }
    }
}
