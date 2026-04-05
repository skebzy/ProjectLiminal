package dev.skebzy.projectLiminal.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinListener implements Listener {

    private final JavaPlugin plugin;
    private static final String LEVEL0_NAME = "level0";

    public JoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        World world = Bukkit.getWorld(LEVEL0_NAME);
        if (world == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.getPlayer().teleport(world.getSpawnLocation());
        }, 1L);
    }
}