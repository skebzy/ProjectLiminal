package dev.skebzy.projectLiminal.levels.level0;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jspecify.annotations.NonNull;

import java.util.Random;

public class Level0ChunkGenerator extends ChunkGenerator {

    @Override
    public @NonNull ChunkData generateChunkData(World world,
                                                Random random,
                                                int chunkX,
                                                int chunkZ,
                                                BiomeGrid biome) {

        ChunkData chunk = createChunkData(world);

        Level0.generate(chunk, chunkX, chunkZ, world.getSeed());

        return chunk;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, 5, 0);
    }
}
