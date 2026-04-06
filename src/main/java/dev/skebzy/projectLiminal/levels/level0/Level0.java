package dev.skebzy.projectLiminal.levels.level0;

import dev.skebzy.projectLiminal.levels.engine.paint.features.LightPlacer;
import dev.skebzy.projectLiminal.levels.engine.paint.features.PillarDecorator;
import dev.skebzy.projectLiminal.levels.engine.paint.surfaces.RoomPainter;
import org.bukkit.generator.ChunkGenerator;

public class Level0 {

    private static final int SIZE = 16;
    private static final int SPAWN_MIN = 4;
    private static final int SPAWN_MAX = 12;
    private static final int FLOOR_CLEARANCE = 1;
    private static final Level0Palette PALETTE = new Level0Palette();

    public static void generate(ChunkGenerator.ChunkData chunk,
                                int chunkX,
                                int chunkZ,
                                long worldSeed) {

        long layoutSeed = worldSeed ^ Level0Config.LAYOUT_SEED_SALT;
        long featureSeed = worldSeed ^ Level0Config.FEATURE_SEED_SALT;

        int height = Level0Config.HEIGHT;

        RoomPainter.paint(
                chunk,
                chunkX, chunkZ,
                SIZE,
                height,
                layoutSeed,
                Level0Config.LAYOUT,
                PALETTE
        );

        if (chunkX == 0 && chunkZ == 0) {
            carveSpawn(chunk, height);
        }

        if (Level0Config.LIGHTS) {
            LightPlacer.apply(chunk, chunkX, chunkZ, height, featureSeed, PALETTE);
        }

        if (Level0Config.PILLARS) {
            PillarDecorator.apply(chunk, chunkX, chunkZ, height, featureSeed, PALETTE);
        }
    }

    private static void carveSpawn(ChunkGenerator.ChunkData chunk, int height) {

        for (int x = SPAWN_MIN; x < SPAWN_MAX; x++) {
            for (int z = SPAWN_MIN; z < SPAWN_MAX; z++) {
                for (int y = FLOOR_CLEARANCE; y < height - 1; y++) {
                    chunk.setBlock(x, y, z, org.bukkit.Material.AIR);
                }
            }
        }
    }
}
