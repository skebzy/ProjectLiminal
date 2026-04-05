package dev.skebzy.projectLiminal;

import dev.skebzy.projectLiminal.levels.level0.Level0ChunkGenerator;
import dev.skebzy.projectLiminal.listener.JoinListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProjectLiminal extends JavaPlugin {

    private static final String LEVEL0_NAME = "level0";

    @Override
    public void onEnable() {

        createLevel0();

        //TODO: This is for testing implement actual ways to go later
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);

        getLogger().info("Level 0 ready.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down ProjectLiminal");
    }

    private void createLevel0() {

        World existing = Bukkit.getWorld(LEVEL0_NAME);

        if (existing != null) {
            getLogger().info("Level 0 already exists, loading...");
            return;
        }

        WorldCreator creator = new WorldCreator(LEVEL0_NAME);
        creator.generator(new Level0ChunkGenerator());

        World world = Bukkit.createWorld(creator);

        if (world != null) {
            world.setSpawnLocation(0, 5, 0);
            getLogger().info("Level 0 world created successfully.");
        } else {
            getLogger().severe("Failed to create Level 0 world.");
        }
    }
}