package dev.velmax.velkoth.command;

import dev.velmax.velkoth.VelKothPlugin;
import dev.velmax.velkoth.arena.Arena;
import dev.velmax.velkoth.arena.region.CuboidRegion;
import dev.velmax.velkoth.config.PluginConfig;
import dev.velmax.velkoth.manager.WandManager;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.ConsoleSource;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main /koth command handler using Incendo Cloud (v2).
 */
public final class KothCommand {

    private final VelKothPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final PaperCommandManager<Source> manager;

    public KothCommand(VelKothPlugin plugin) {
        this.plugin = plugin;
        this.manager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);

        registerCommands();
    }

    private void registerCommands() {
        var base = manager.commandBuilder("koth").permission("velkoth.use");

        manager.command(base
                .handler(ctx -> handleHelp(ctx.sender().source())));

        manager.command(base.literal("help")
                .handler(ctx -> handleHelp(ctx.sender().source())));

        SuggestionProvider<Source> arenaSuggestions = (ctx,
                input) -> CompletableFuture.completedFuture(plugin.getArenaManager().getArenaIds().stream()
                        .map(Suggestion::suggestion)
                        .toList());

        manager.command(base.literal("create")
                .permission("velkoth.admin")
                .senderType(PlayerSource.class)
                .required("name", StringParser.stringParser())
                .handler(ctx -> handleCreate(ctx.sender().source(), ctx.get("name"))));

        manager.command(base.literal("delete")
                .permission("velkoth.admin")
                .required("name", StringParser.stringParser(), arenaSuggestions)
                .handler(ctx -> handleDelete(ctx.sender().source(), ctx.get("name"))));

        manager.command(base.literal("start")
                .permission("velkoth.admin")
                .required("name", StringParser.stringParser(), arenaSuggestions)
                .handler(ctx -> handleStart(ctx.sender().source(), ctx.get("name"))));

        manager.command(base.literal("stop")
                .permission("velkoth.admin")
                .required("name", StringParser.stringParser(), arenaSuggestions)
                .handler(ctx -> handleStop(ctx.sender().source(), ctx.get("name"))));

        manager.command(base.literal("pause")
                .permission("velkoth.admin")
                .required("name", StringParser.stringParser(), arenaSuggestions)
                .handler(ctx -> handlePause(ctx.sender().source(), ctx.get("name"))));

        manager.command(base.literal("resume")
                .permission("velkoth.admin")
                .required("name", StringParser.stringParser(), arenaSuggestions)
                .handler(ctx -> handleResume(ctx.sender().source(), ctx.get("name"))));

        manager.command(base.literal("wand")
                .permission("velkoth.admin")
                .senderType(PlayerSource.class)
                .handler(ctx -> handleWand(ctx.sender().source())));

        manager.command(base.literal("list")
                .permission("velkoth.admin")
                .handler(ctx -> handleList(ctx.sender().source())));

        manager.command(base.literal("next")
                .required("name", StringParser.stringParser(), arenaSuggestions)
                .handler(ctx -> handleNext(ctx.sender().source(), ctx.get("name"))));

        manager.command(base.literal("stats")
                .senderType(PlayerSource.class)
                .handler(ctx -> handleStats(ctx.sender().source())));

        manager.command(base.literal("reload")
                .permission("velkoth.admin")
                .handler(ctx -> handleReload(ctx.sender().source())));
    }

    // ── Create ──

    private void handleCreate(Player player, String nameLowercase) {
        String name = nameLowercase.toLowerCase();
        if (plugin.getArenaManager().arenaExists(name)) {
            sendPrefixed(player, plugin.getMessages().getArenaExists().replace("<arena>", name));
            return;
        }

        WandManager.Selection selection = plugin.getWandManager().getSelection(player);
        if (selection == null || !selection.isComplete()) {
            sendPrefixed(player, plugin.getMessages().getSelectionIncomplete());
            return;
        }

        var pos1 = selection.getPos1();
        var pos2 = selection.getPos2();

        CuboidRegion region = new CuboidRegion(
                pos1.getWorld(),
                pos1.getX(), pos1.getY(), pos1.getZ(),
                pos2.getX(), pos2.getY(), pos2.getZ());

        PluginConfig.CaptureDefaults defaults = plugin.getPluginConfig().getCaptureDefaults();
        Arena arena = new Arena(
                name, name, region,
                defaults.getCaptureTime(),
                Arena.CaptureMode.valueOf(defaults.getCaptureMode()),
                defaults.getGracePeriod(),
                defaults.getMaxScore());

        plugin.getArenaManager().addArena(arena);
        plugin.getWandManager().clearSelection(player);
        sendPrefixed(player, plugin.getMessages().getArenaCreated().replace("<arena>", name));
    }

    // ── Delete ──

    private void handleDelete(CommandSender sender, String nameLowercase) {
        String name = nameLowercase.toLowerCase();
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotFound().replace("<arena>", name));
            return;
        }

        if (arena.state() == Arena.ArenaState.ACTIVE) {
            plugin.getCaptureManager().stopArena(arena);
        }

        plugin.getArenaManager().removeArena(name);
        sendPrefixed(sender, plugin.getMessages().getArenaDeleted().replace("<arena>", name));
    }

    // ── Start ──

    private void handleStart(CommandSender sender, String nameLowercase) {
        String name = nameLowercase.toLowerCase();
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotFound().replace("<arena>", name));
            return;
        }
        if (arena.state() != Arena.ArenaState.IDLE) {
            sendPrefixed(sender, plugin.getMessages().getArenaAlreadyActive().replace("<arena>", name));
            return;
        }

        plugin.getCaptureManager().startArena(arena);
    }

    // ── Stop ──

    private void handleStop(CommandSender sender, String nameLowercase) {
        String name = nameLowercase.toLowerCase();
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotFound().replace("<arena>", name));
            return;
        }
        if (arena.state() == Arena.ArenaState.IDLE) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotActive().replace("<arena>", name));
            return;
        }

        plugin.getCaptureManager().stopArena(arena);
    }

    // ── Pause / Resume ──

    private void handlePause(CommandSender sender, String nameLowercase) {
        String name = nameLowercase.toLowerCase();
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotFound().replace("<arena>", name));
            return;
        }
        if (arena.state() != Arena.ArenaState.ACTIVE) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotActive().replace("<arena>", name));
            return;
        }

        plugin.getCaptureManager().pauseArena(arena);
    }

    private void handleResume(CommandSender sender, String nameLowercase) {
        String name = nameLowercase.toLowerCase();
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotFound().replace("<arena>", name));
            return;
        }
        if (arena.state() != Arena.ArenaState.PAUSED) {
            sendPrefixed(sender, "<red>Arena <gold>" + name + "</gold> is not paused.");
            return;
        }

        plugin.getCaptureManager().resumeArena(arena);
    }

    // ── Wand ──

    private void handleWand(Player player) {

        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = wand.getItemMeta();
        meta.displayName(Component.text("KoTH Wand", NamedTextColor.GOLD, TextDecoration.BOLD));
        meta.lore(List.of(
                Component.text("Left-click → Set Position 1", NamedTextColor.GRAY),
                Component.text("Right-click → Set Position 2", NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, WandManager.WAND_TAG),
                PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);

        player.getInventory().addItem(wand);
        sendPrefixed(player, plugin.getMessages().getWandGiven());
    }

    // ── List ──

    private void handleList(CommandSender sender) {
        var arenas = plugin.getArenaManager().getArenas();
        if (arenas.isEmpty()) {
            sendPrefixed(sender, "<gray>No arenas defined.");
            return;
        }

        sendPrefixed(sender, "<gold><bold>KoTH Arenas</bold></gold> <dark_gray>(" + arenas.size() + ")</dark_gray>");
        for (Arena arena : arenas) {
            String stateColor = switch (arena.state()) {
                case ACTIVE -> "<green>";
                case PAUSED -> "<yellow>";
                case IDLE -> "<gray>";
            };
            sendPrefixed(sender, " <dark_gray>•</dark_gray> <gold>" + arena.id()
                    + "</gold> " + stateColor + "[" + arena.state().name() + "]"
                    + " <dark_gray>(" + arena.captureMode().name() + ", " + arena.captureTime() + "s)</dark_gray>");
        }
    }

    // ── Next ──

    private void handleNext(CommandSender sender, String nameLowercase) {
        String name = nameLowercase.toLowerCase();
        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena == null) {
            sendPrefixed(sender, plugin.getMessages().getArenaNotFound().replace("<arena>", name));
            return;
        }

        Long delayMs = plugin.getSchedulerManager().getNextStartTime(name);
        if (delayMs == null) {
            sendPrefixed(sender, plugin.getMessages().getNoNextScheduled().replace("<arena>", name));
        } else {
            String timeStr = formatDuration(delayMs);
            sendPrefixed(sender, plugin.getMessages().getNextScheduled()
                    .replace("<arena>", name)
                    .replace("<time>", timeStr));
        }
    }

    // ── Stats ──

    private void handleStats(Player player) {

        plugin.getStatsManager().getStats(player.getUniqueId()).thenAccept(stats -> {
            sendPrefixed(player, "<gold><bold>Your KoTH Stats</bold></gold>");
            sendPrefixed(player, " <dark_gray>•</dark_gray> <gray>Total Wins:</gray> <white>" + stats.totalWins());

            plugin.getStatsManager().getDailyWins(player.getUniqueId()).thenAccept(
                    daily -> sendPrefixed(player,
                            " <dark_gray>•</dark_gray> <gray>Today:</gray> <white>" + daily));

            plugin.getStatsManager().getWeeklyWins(player.getUniqueId()).thenAccept(
                    weekly -> sendPrefixed(player,
                            " <dark_gray>•</dark_gray> <gray>This Week:</gray> <white>" + weekly));
        });
    }

    // ── Reload ──

    private void handleReload(CommandSender sender) {
        plugin.reloadAllConfigs();
        sendPrefixed(sender, plugin.getMessages().getReloaded());
    }

    // ── Help ──

    private void handleHelp(CommandSender sender) {
        sender.sendMessage(Component.empty());
        sendPrefixed(sender, "<gold><bold>VelKoth Commands</bold></gold>");
        sendPrefixed(sender, " <gold>/koth create <name></gold> <dark_gray>-</dark_gray> <gray>Create a new arena");
        sendPrefixed(sender, " <gold>/koth delete <name></gold> <dark_gray>-</dark_gray> <gray>Delete an arena");
        sendPrefixed(sender, " <gold>/koth start <name></gold> <dark_gray>-</dark_gray> <gray>Start a KoTH event");
        sendPrefixed(sender, " <gold>/koth stop <name></gold> <dark_gray>-</dark_gray> <gray>Stop a KoTH event");
        sendPrefixed(sender, " <gold>/koth pause <name></gold> <dark_gray>-</dark_gray> <gray>Pause a KoTH event");
        sendPrefixed(sender, " <gold>/koth resume <name></gold> <dark_gray>-</dark_gray> <gray>Resume a paused event");
        sendPrefixed(sender, " <gold>/koth wand</gold> <dark_gray>-</dark_gray> <gray>Get the selection wand");
        sendPrefixed(sender, " <gold>/koth list</gold> <dark_gray>-</dark_gray> <gray>List all arenas");
        sendPrefixed(sender,
                " <gold>/koth next <name></gold> <dark_gray>-</dark_gray> <gray>Check when an arena starts next");
        sendPrefixed(sender, " <gold>/koth stats</gold> <dark_gray>-</dark_gray> <gray>View your stats");
        sendPrefixed(sender, " <gold>/koth reload</gold> <dark_gray>-</dark_gray> <gray>Reload configuration");
    }

    // ── Helpers ──

    private void sendPrefixed(CommandSender sender, String message) {
        var prefix = miniMessage.deserialize(plugin.getMessages().getPrefix());
        sender.sendMessage(prefix.append(miniMessage.deserialize(message)));
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = ((seconds % 86400) % 3600) / 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0)
            sb.append(days).append("d ");
        if (hours > 0)
            sb.append(hours).append("h ");
        if (minutes > 0)
            sb.append(minutes).append("m ");
        if (sb.isEmpty())
            sb.append("< 1m");
        return sb.toString().trim();
    }
}
