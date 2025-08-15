package me.primimity.sleepSkip;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this,this);

        System.out.println("\n" +
                getDescription().getName() + " v" + getDescription().getVersion() + "\n" +
                "Created by " + getDescription().getAuthors() + "\n" +
                "Need a custom plugin? Discord me @primimity\n");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // SkipSleep
    private final Map<World, BukkitTask> pendingSkips = new HashMap<>();
    private boolean isEligible(Player p) {
        return p.isOnline() && p.getGameMode() != GameMode.SPECTATOR && !p.isDead();
    }
    private boolean isNight(World w) {
        long t = w.getTime() % 24000L;
        return t > 12542L && t < 23460L;
    }
    private void checkSkip(World world) {
        if (!isNight(world)) {
            cancelSkip(world);
            return;
        }

        int eligibleAmt = (int) world.getPlayers().stream().filter(this::isEligible).count();
        if (eligibleAmt == 0) {
            cancelSkip(world);
            return;
        }

        int sleepingAmt = (int) world.getPlayers().stream().filter(this::isEligible).filter(Player::isSleeping).count();
        double percentRequired = Math.max(0.0, Math.min(1.0, (getConfig().getDouble("settings.percentRequired"))));
        int neededAmt = (int) Math.max(1, (int) Math.ceil(percentRequired * eligibleAmt));
        int sleepDelay = getConfig().getInt("settings.sleepDelay");

        if (sleepingAmt >= neededAmt) {
            if (!pendingSkips.containsKey(world)) {
                // Action bar 'skipping' message
                String msg = getConfig().getString("messages.skipped", "&fSleeping through this night")
                        .replace("{sleeping}", String.valueOf(sleepingAmt))
                        .replace("{needed}", String.valueOf(neededAmt)).
                        replace("{total}", String.valueOf(eligibleAmt));
                String actionbar = ChatColor.translateAlternateColorCodes('&', msg);
                world.getPlayers().forEach(p ->
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbar))
                );

                // Schedule Skip
                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        int stillSleeping = (int) world.getPlayers().stream()
                                .filter(Main.this::isEligible)
                                .filter(Player::isSleeping)
                                .count();
                        if (stillSleeping >= neededAmt && isNight(world)) {
                            world.setTime(getConfig().getLong("settings.timeOfDay"));
                            world.setStorm(false);
                            world.setThundering(false);
                            world.setWeatherDuration(0);
                            world.setThunderDuration(0);

                            world.getPlayers().forEach(p -> {
                                if (p.isSleeping()) p.wakeup(false);
                            });
                        }
                        pendingSkips.remove(world);
                    }
                }.runTaskLater(this, (sleepDelay * 20L) + 1L);
                pendingSkips.put(world, task);
            }
        } else {
            cancelSkip(world);
            // Action bar 'progress' message
            String msg = getConfig().getString("messages.progress", "&f{sleeping}/{total} players sleeping")
                    .replace("{sleeping}", String.valueOf(sleepingAmt))
                    .replace("{needed}", String.valueOf(neededAmt))
                    .replace("{total}", String.valueOf(eligibleAmt));
            String actionbar = ChatColor.translateAlternateColorCodes('&', msg);
            world.getPlayers().forEach(p -> {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbar));
            });
        }
    }
    private void cancelSkip(World world) {
        BukkitTask task = pendingSkips.remove(world);
        if (task != null) task.cancel();
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        if (e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        Bukkit.getScheduler().runTask(this, () -> checkSkip(e.getPlayer().getWorld()));
    }
    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent e) {
        Bukkit.getScheduler().runTask(this, () -> checkSkip(e.getPlayer().getWorld()));
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Bukkit.getScheduler().runTask(this, () -> checkSkip(e.getPlayer().getWorld()));
    }
}
