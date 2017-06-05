package ua.gwm.sponge_plugin.crates.caze.cases;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import ua.gwm.sponge_plugin.crates.caze.Case;
import ua.gwm.sponge_plugin.crates.util.GWMCratesUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class BlockCase extends Case {

    protected Collection<Location<World>> locations;

    public BlockCase(ConfigurationNode node) {
        super(node);
        ConfigurationNode locations_node = node.getNode("LOCATIONS");
        if (locations_node.isVirtual()) {
            throw new RuntimeException("LOCATIONS node does not exist!");
        }
        locations = new HashSet<Location<World>>();
        for (ConfigurationNode location_node : locations_node.getChildrenList()) {
            locations.add(GWMCratesUtils.parseLocation(location_node));
        }
    }

    public BlockCase(Optional<BigDecimal> price, Collection<Location<World>> locations) {
        super(price);
        this.locations = locations;
    }

    @Override
    public void add(Player player, int amount) {
    }

    @Override
    public int get(Player player) {
        return Integer.MAX_VALUE;
    }

    public Collection<Location<World>> getLocations() {
        return locations;
    }

    public void setLocations(Collection<Location<World>> locations) {
        this.locations = locations;
    }
}
