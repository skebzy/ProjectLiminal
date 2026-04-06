package dev.skebzy.projectLiminal.levels.engine.paint.features;

import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.generator.ChunkGenerator;

public class PillarDecorator {

    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_LAST = CHUNK_SIZE - 1;
    private static final int REGION_SIZE = 18;
    private static final int REGION_PADDING = 4;
    private static final int PILLAR_ROLL = 100;
    private static final int PILLAR_RATE = 24;
    private static final int MIN_WIDTH = 1;
    private static final int WIDTH_ROLL = 2;
    private static final int INTERIOR_MIN = 1;
    private static final int INTERIOR_MAX = CHUNK_SIZE - 2;
    private static final int FLOOR_Y = 1;
    private static final int CEILING_GAP = 2;
    private static final int SPAWN_SAFE_MIN = 4;
    private static final int SPAWN_SAFE_MAX = 12;
    private static final int HASH_MIX_SHIFT = 33;
    private static final long REGION_X_MIX = 0x9E3779B97F4A7C15L;
    private static final long REGION_Z_MIX = 0xC2B2AE3D27D4EB4FL;
    private static final long FIRST_MIX_MULTIPLIER = 0xFF51AFD7ED558CCDL;
    private static final long SECOND_MIX_MULTIPLIER = 0xC4CEB9FE1A85EC53L;

    public static void apply(ChunkGenerator.ChunkData chunk,
                             int chunkX,
                             int chunkZ,
                             int height,
                             long seed,
                             Palette palette) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minRegionX = Math.floorDiv(baseX, REGION_SIZE) - 1;
        int maxRegionX = Math.floorDiv(baseX + CHUNK_LAST, REGION_SIZE) + 1;
        int minRegionZ = Math.floorDiv(baseZ, REGION_SIZE) - 1;
        int maxRegionZ = Math.floorDiv(baseZ + CHUNK_LAST, REGION_SIZE) + 1;

        for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
            for (int regionZ = minRegionZ; regionZ <= maxRegionZ; regionZ++) {
                if (hash(regionX, regionZ, seed, 3, PILLAR_ROLL) >= PILLAR_RATE) {
                    continue;
                }

                int worldX = regionX * REGION_SIZE
                        + REGION_PADDING
                        + hash(regionX, regionZ, seed, 7, REGION_SIZE - (REGION_PADDING * 2));
                int worldZ = regionZ * REGION_SIZE
                        + REGION_PADDING
                        + hash(regionX, regionZ, seed, 11, REGION_SIZE - (REGION_PADDING * 2));

                if (worldX < baseX || worldX > baseX + CHUNK_LAST || worldZ < baseZ || worldZ > baseZ + CHUNK_LAST) {
                    continue;
                }

                int localX = worldX - baseX;
                int localZ = worldZ - baseZ;
                int width = MIN_WIDTH + hash(regionX, regionZ, seed, 13, WIDTH_ROLL);
                boolean extendX = hash(regionX, regionZ, seed, 17, WIDTH_ROLL) == 0;

                if (intersectsSpawnSafety(chunkX, chunkZ, localX, localZ, width, extendX)) {
                    continue;
                }

                if (!canPlace(chunk, localX, localZ, width, extendX, height)) {
                    continue;
                }

                for (int offset = 0; offset < width; offset++) {
                    int x = extendX ? localX + offset : localX;
                    int z = extendX ? localZ : localZ + offset;
                    for (int y = 1; y < height - 1; y++) {
                        chunk.setBlock(x, y, z, palette.wall());
                    }
                }
            }
        }
    }

    private static boolean intersectsSpawnSafety(int chunkX,
                                                 int chunkZ,
                                                 int localX,
                                                 int localZ,
                                                 int width,
                                                 boolean extendX) {
        if (chunkX != 0 || chunkZ != 0) {
            return false;
        }

        for (int offset = 0; offset < width; offset++) {
            int x = extendX ? localX + offset : localX;
            int z = extendX ? localZ : localZ + offset;
            if (x >= SPAWN_SAFE_MIN && x < SPAWN_SAFE_MAX && z >= SPAWN_SAFE_MIN && z < SPAWN_SAFE_MAX) {
                return true;
            }
        }

        return false;
    }

    private static boolean canPlace(ChunkGenerator.ChunkData chunk,
                                    int localX,
                                    int localZ,
                                    int width,
                                    boolean extendX,
                                    int height) {
        for (int offset = 0; offset < width; offset++) {
            int x = extendX ? localX + offset : localX;
            int z = extendX ? localZ : localZ + offset;

            if (x < INTERIOR_MIN || x > INTERIOR_MAX || z < INTERIOR_MIN || z > INTERIOR_MAX) {
                return false;
            }

            if (chunk.getType(x, FLOOR_Y, z) != org.bukkit.Material.AIR) {
                return false;
            }

            if (chunk.getType(x, height - CEILING_GAP, z) != org.bukkit.Material.AIR) {
                return false;
            }
        }

        return true;
    }

    private static int hash(int x, int z, long seed, long salt, int bound) {
        long mixed = seed ^ salt;
        mixed ^= x * REGION_X_MIX;
        mixed ^= z * REGION_Z_MIX;
        mixed ^= (mixed >>> HASH_MIX_SHIFT);
        mixed *= FIRST_MIX_MULTIPLIER;
        mixed ^= (mixed >>> HASH_MIX_SHIFT);
        mixed *= SECOND_MIX_MULTIPLIER;
        mixed ^= (mixed >>> HASH_MIX_SHIFT);
        return Math.floorMod((int) mixed, bound);
    }
}
