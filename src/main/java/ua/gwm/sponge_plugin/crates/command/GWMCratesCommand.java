package ua.gwm.sponge_plugin.crates.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import ua.gwm.sponge_plugin.crates.GWMCrates;
import ua.gwm.sponge_plugin.crates.caze.Case;
import ua.gwm.sponge_plugin.crates.drop.Drop;
import ua.gwm.sponge_plugin.crates.key.Key;
import ua.gwm.sponge_plugin.crates.manager.Manager;
import ua.gwm.sponge_plugin.crates.open_manager.OpenManager;
import ua.gwm.sponge_plugin.crates.preview.Preview;
import ua.gwm.sponge_plugin.crates.util.LanguageUtils;
import ua.gwm.sponge_plugin.crates.util.Pair;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GWMCratesCommand implements CommandCallable {

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.split(" ");
        Optional<Player> optional_player = source instanceof Player ? Optional.of((Player) source) : Optional.empty();
        if (args.length == 0) {
            return CommandResult.empty();
        }
        switch (args[0].toLowerCase()) {
            case "save": {
                if (!source.hasPermission("gwm_crates.command.save")) {
                    source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                    return CommandResult.success();
                }
                GWMCrates.getInstance().save();
                source.sendMessage(LanguageUtils.getText("SUCCESSFULLY_SAVED"));
                return CommandResult.success();
            }
            case "reload": {
                if (!source.hasPermission("gwm_crates.command.reload")) {
                    source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                    return CommandResult.success();
                }
                GWMCrates.getInstance().reload();
                source.sendMessage(LanguageUtils.getText("SUCCESSFULLY_RELOADED"));
                return CommandResult.success();
            }
            case "open": {
                if (args.length != 2) {
                    return CommandResult.empty();
                }
                String manager_id = args[1].toLowerCase();
                if (!optional_player.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("COMMAND_CAN_BE_EXECUTED_ONLY_BY_PLAYER"));
                    return CommandResult.success();
                }
                Player player = optional_player.get();
                Optional<Manager> optional_manager = GWMCrates.getInstance().getManagerById(manager_id);
                if (!optional_manager.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("MANAGER_NOT_EXIST",
                            new Pair<String, String>("%MANAGER_ID%", manager_id)));
                    return CommandResult.success();
                }
                Manager manager = optional_manager.get();
                if (!player.hasPermission("gwm_crates.open." + manager_id) ||
                        !player.hasPermission("gwm_crates.command_open." + manager_id)) {
                    source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                    return CommandResult.success();
                }
                OpenManager open_manager = manager.getOpenManager();
                if (!open_manager.canOpen(player, manager)) {
                    source.sendMessage(LanguageUtils.getText("CAN_NOT_OPEN_MANAGER"));
                    return CommandResult.success();
                }
                Case caze = manager.getCase();
                Key key = manager.getKey();
                if (caze.get(player) < 1) {
                    source.sendMessage(LanguageUtils.getText("HAVE_NOT_CASE"));
                    return CommandResult.success();
                }
                if (key.get(player) < 1) {
                    source.sendMessage(LanguageUtils.getText("HAVE_NOT_KEY"));
                    return CommandResult.success();
                }
                caze.add(player, -1);
                key.add(player, -1);
                open_manager.open(player, manager);
                return CommandResult.success();
            }
            case "force": {
                if (args.length < 2 || args.length > 3) {
                    return CommandResult.empty();
                }
                Optional<Player> optional_target = Optional.empty();
                if (args.length == 3) {
                    String target_name = args[2];
                    optional_target = Sponge.getServer().getPlayer(target_name);
                    if (!optional_target.isPresent()) {
                        source.sendMessage(LanguageUtils.getText("PLAYER_NOT_EXIST",
                                new Pair<String, String>("%PLAYER%", target_name)));
                        return CommandResult.success();
                    }
                }
                String manager_id = args[1].toLowerCase();
                if (!optional_target.isPresent() && !optional_player.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("COMMAND_CAN_BE_EXECUTED_ONLY_BY_PLAYER"));
                    return CommandResult.success();
                }
                Optional<Manager> optional_manager = GWMCrates.getInstance().getManagerById(manager_id);
                if (!optional_manager.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("MANAGER_NOT_EXIST",
                            new Pair<String, String>("%MANAGER_ID%", manager_id)));
                    return CommandResult.success();
                }
                Manager manager = optional_manager.get();
                if ((!optional_target.isPresent() && !source.hasPermission("gwm_crates.open." + manager_id) ||
                        !source.hasPermission("gwm_crates.command_open." + manager_id) ||
                        !source.hasPermission("gwm_crates.force_open." + manager_id)) ||
                        optional_target.isPresent() && !source.hasPermission("gwm_crates.force_open_other." + manager_id)) {
                    source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                    return CommandResult.success();
                }
                OpenManager open_manager = manager.getOpenManager();
                if (!optional_target.isPresent() && optional_player.isPresent() &&
                        !open_manager.canOpen(optional_player.get(), manager)) {
                    source.sendMessage(LanguageUtils.getText("CAN_NOT_OPEN_MANAGER"));
                    return CommandResult.success();
                } else if (optional_target.isPresent() && !open_manager.canOpen(optional_target.get(), manager)) {
                    source.sendMessage(LanguageUtils.getText("PLAYER_CAN_NOT_OPEN_MANAGER",
                            new Pair<String, String>("%PLAYER%", optional_target.get().getName())));
                    return CommandResult.success();
                }
                if (!optional_target.isPresent()) {
                    open_manager.open(optional_player.get(), manager);
                    source.sendMessage(LanguageUtils.getText("CRATE_FORCE_OPENED",
                            new Pair<String, String>("%MANAGER%", manager.getName())));
                } else {
                    Player target = optional_target.get();
                    open_manager.open(target, manager);
                    source.sendMessage(LanguageUtils.getText("CRATE_FORCE_OPENED_FOR_PLAYER",
                            new Pair<String, String>("%MANAGER%", manager.getName()),
                            new Pair<String, String>("%PLAYER%", target.getName())));
                    if (GWMCrates.getInstance().getConfig().getNode("TELL_FORCE_CRATE_OPEN_INFO").getBoolean(true)) {
                        target.sendMessage(LanguageUtils.getText("CRATE_FORCE_OPENED_BY_PLAYER",
                                new Pair<String, String>("%PLAUER%", optional_player.get().getName())));
                    }
                }
                return CommandResult.success();
            }
            case "preview": {
                if (args.length != 2) {
                    return CommandResult.empty();
                }
                String manager_id = args[1].toLowerCase();
                if (!optional_player.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("COMMAND_CAN_BE_EXECUTED_ONLY_BY_PLAYER"));
                    return CommandResult.success();
                }
                Player player = optional_player.get();
                Optional<Manager> optional_manager = GWMCrates.getInstance().getManagerById(manager_id);
                if (!optional_manager.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("MANAGER_NOT_EXIST",
                            new Pair<String, String>("%MANAGER_ID%", manager_id)));
                    return CommandResult.success();
                }
                Manager manager = optional_manager.get();
                Optional<Preview> optional_preview = manager.getPreview();
                if (!optional_preview.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("PREVIEW_NOT_AVAILABLE",
                            new Pair<String, String>("%MANAGER%", manager.getName())));
                    return CommandResult.success();
                }
                Preview preview = optional_preview.get();
                if (!player.hasPermission("gwm_crates.preview." + manager_id)) {
                    source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                    return CommandResult.success();
                }
                preview.preview(player, manager);
                player.sendMessage(LanguageUtils.getText("PREVIEW_STARTED",
                        new Pair<String, String>("%MANAGER%", manager.getName())));
                return CommandResult.success();
            }
            case "buy": {
                if (args.length < 3 || args.length > 4) {
                    return CommandResult.empty();
                }
                if (!optional_player.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("COMMAND_CAN_BE_EXECUTED_ONLY_BY_PLAYER"));
                    return CommandResult.success();
                }
                Player player = optional_player.get();
                UUID uuid = player.getUniqueId();
                Optional<EconomyService> optional_economy_service = GWMCrates.getInstance().getEconomyService();
                if (!optional_economy_service.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("ECONOMY_SERVICE_NOT_FOUND"));
                    return CommandResult.success();
                }
                EconomyService economy_service = optional_economy_service.get();
                Optional<UniqueAccount> optional_player_account = economy_service.getOrCreateAccount(uuid);
                if (!optional_economy_service.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("ECONOMY_ACCOUNT_NOT_FOUND"));
                    return CommandResult.success();
                }
                UniqueAccount player_account = optional_player_account.get();
                Currency currency = economy_service.getDefaultCurrency();
                BigDecimal money = player_account.getBalance(currency);
                String manager_id = args[2].toLowerCase();
                Optional<Manager> optional_manager = GWMCrates.getInstance().getManagerById(manager_id);
                if (!optional_manager.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("MANAGER_NOT_EXIST",
                            new Pair<String, String>("%MANAGER_ID%", manager_id)));
                    return CommandResult.success();
                }
                Manager manager = optional_manager.get();
                switch (args[1].toLowerCase()) {
                    case "case": {
                        if (args.length > 3) {
                            return CommandResult.empty();
                        }
                        Case caze = manager.getCase();
                        Optional<BigDecimal> optional_price = caze.getPrice();
                        if (!optional_price.isPresent()) {
                            source.sendMessage(LanguageUtils.getText("CASE_NOT_FOR_SALE"));
                            return CommandResult.success();
                        }
                        if (!source.hasPermission("gwm_crates.buy.manager." + manager_id + ".case")) {
                            source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                            return CommandResult.success();
                        }
                        BigDecimal price = optional_price.get();
                        if (price.compareTo(money) > 0) {
                            source.sendMessage(LanguageUtils.getText("NOT_ENOUGH_MONEY"));
                            return CommandResult.success();
                        }
                        player_account.withdraw(currency, price, GWMCrates.getInstance().getDefaultCause());
                        caze.add(player, 1);
                        player.sendMessage(LanguageUtils.getText("SUCCESSFULLY_BOUGHT_CASE",
                                new Pair<String, String>("%MANAGER%", manager.getName())));
                        return CommandResult.success();
                    }
                    case "key": {
                        if (args.length > 3) {
                            return CommandResult.empty();
                        }
                        Key key = manager.getKey();
                        Optional<BigDecimal> optional_price = key.getPrice();
                        if (!optional_price.isPresent()) {
                            source.sendMessage(LanguageUtils.getText("KEY_NOT_FOR_SALE"));
                            return CommandResult.success();
                        }
                        if (!source.hasPermission("gwm_crates.buy.manager." + manager_id + ".key")) {
                            source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                            return CommandResult.success();
                        }
                        BigDecimal price = optional_price.get();
                        if (price.compareTo(money) > 0) {
                            source.sendMessage(LanguageUtils.getText("NOT_ENOUGH_MONEY"));
                            return CommandResult.success();
                        }
                        player_account.withdraw(currency, price, GWMCrates.getInstance().getDefaultCause());
                        key.add(player, 1);
                        player.sendMessage(LanguageUtils.getText("SUCCESSFULLY_BOUGHT_KEY",
                                new Pair<String, String>("%MANAGER%", manager.getName())));
                        return CommandResult.success();
                    }
                    case "drop": {
                        if (args.length != 4) {
                            return CommandResult.empty();
                        }
                        String drop_id = args[3].toLowerCase();
                        Optional<Drop> optional_drop = manager.getDropById(drop_id);
                        if (!optional_drop.isPresent()) {
                            source.sendMessage(LanguageUtils.getText("DROP_NOT_EXIST",
                                    new Pair<String, String>("%DROP_ID%", drop_id)));
                            return CommandResult.success();
                        }
                        if (!source.hasPermission("gwm_crates.buy.manager." + manager_id + ".drop." + drop_id)) {
                            source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                            return CommandResult.success();
                        }
                        Drop drop = optional_drop.get();
                        Optional<BigDecimal> optional_price = drop.getPrice();
                        if (!optional_price.isPresent()) {
                            source.sendMessage(LanguageUtils.getText("DROP_NOT_FOR_SALE"));
                            return CommandResult.success();
                        }
                        BigDecimal price = optional_price.get();
                        if (price.compareTo(money) > 0) {
                            source.sendMessage(LanguageUtils.getText("NOT_ENOUGH_MONEY"));
                            return CommandResult.success();
                        }
                        player_account.withdraw(currency, price, GWMCrates.getInstance().getDefaultCause());
                        drop.apply(player);
                        player.sendMessage(LanguageUtils.getText("SUCCESSFULLY_BOUGHT_DROP",
                                new Pair<String, String>("%DROP_ID%", drop.getId()),
                                new Pair<String, String>("%MANAGER%", manager.getName())));
                        return CommandResult.success();
                    }
                    default: {
                        return CommandResult.empty();
                    }
                }
            }
            case "give": {
                if (args.length < 4 || args.length > 5) {
                    return CommandResult.empty();
                }
                String manager_id = args[3].toLowerCase();
                Optional<Manager> optional_manager = GWMCrates.getInstance().getManagerById(manager_id);
                if (!optional_manager.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("MANAGER_NOT_EXIST",
                            new Pair<String, String>("%MANAGER_ID%", manager_id)));
                    return CommandResult.success();
                }
                Manager manager = optional_manager.get();
                String target_name = args[1];
                Optional<Player> optional_target = Sponge.getServer().getPlayer(target_name);
                if (!optional_target.isPresent()) {
                    source.sendMessage(LanguageUtils.getText("PLAYER_NOT_EXIST",
                            new Pair<String, String>("%PLAYER%", target_name)));
                    return CommandResult.success();
                }
                Player target = optional_target.get();
                switch (args[2].toLowerCase()) {
                    case "case": {
                        if (args.length > 4) {
                            return CommandResult.empty();
                        }
                        Case caze = manager.getCase();
                        if (!source.hasPermission("gwm_crates.give." + manager_id + ".case")) {
                            source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                            return CommandResult.success();
                        }
                        caze.add(target, 1);
                        source.sendMessage(LanguageUtils.getText("SUCCESSFULLY_GIVE_CASE",
                                new Pair<String, String>("%MANAGER%", manager.getName()),
                                new Pair<String, String>("%PLAYER%", target_name)));
                        if (GWMCrates.getInstance().getConfig().getNode("TELL_GIVE_INFO").getBoolean(true)) {
                            source.sendMessage(LanguageUtils.getText("GET_CASE_BY_PLAYER",
                                    new Pair<String, String>("%MANAGER%", manager.getName()),
                                    new Pair<String, String>("%PLAYER%", source.getName())));
                        }
                        return CommandResult.success();
                    }
                    case "key": {
                        if (args.length > 4) {
                            return CommandResult.empty();
                        }
                        Key key = manager.getKey();
                        if (!source.hasPermission("gwm_crates.give." + manager_id + ".key")) {
                            source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                            return CommandResult.success();
                        }
                        key.add(target, 1);
                        source.sendMessage(LanguageUtils.getText("SUCCESSFULLY_GIVE_KEY",
                                new Pair<String, String>("%MANAGER%", manager.getName()),
                                new Pair<String, String>("%PLAYER%", target_name)));
                        if (GWMCrates.getInstance().getConfig().getNode("TELL_GIVE_INFO").getBoolean(true)) {
                            source.sendMessage(LanguageUtils.getText("GET_KEY_BY_PLAYER",
                                    new Pair<String, String>("%MANAGER%", manager.getName()),
                                    new Pair<String, String>("%PLAYER%", source.getName())));
                        }
                        return CommandResult.success();
                    }
                    case "drop": {
                        if (args.length != 5) {
                            return CommandResult.empty();
                        }
                        String drop_id = args[4].toLowerCase();
                        Optional<Drop> optional_drop = manager.getDropById(drop_id);
                        if (!optional_drop.isPresent()) {
                            source.sendMessage(LanguageUtils.getText("DROP_NOT_EXIST",
                                    new Pair<String, String>("%DROP_ID%", drop_id)));
                            return CommandResult.success();
                        }
                        Drop drop = optional_drop.get();
                        if (!source.hasPermission("gwm_crates.give." + manager_id + ".drop." + drop_id)) {
                            source.sendMessage(LanguageUtils.getText("HAVE_NOT_PERMISSION"));
                            return CommandResult.success();
                        }
                        drop.apply(target);
                        source.sendMessage(LanguageUtils.getText("SUCCESSFULLY_GIVE_DROP",
                                new Pair<String, String>("%DROP_ID%", drop_id),
                                new Pair<String, String>("%MANAGER%", manager.getName()),
                                new Pair<String, String>("%PLAYER%", target_name)));
                        if (GWMCrates.getInstance().getConfig().getNode("TELL_GIVE_INFO").getBoolean(true)) {
                            source.sendMessage(LanguageUtils.getText("GET_DROP_BY_PLAYER",
                                    new Pair<String, String>("%DROP_ID%", drop_id),
                                    new Pair<String, String>("%MANAGER%", manager.getName()),
                                    new Pair<String, String>("%PLAYER%", source.getName())));
                        }
                        return CommandResult.success();
                    }
                    default: {
                        return CommandResult.empty();
                    }
                }
            }
            default: {
                return CommandResult.empty();
            }
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> location) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of();
    }
}
