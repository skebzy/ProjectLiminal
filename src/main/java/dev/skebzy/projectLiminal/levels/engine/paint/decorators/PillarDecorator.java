package dev.skebzy.projectLiminal.levels.engine.paint.decorators;

import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.generator.ChunkGenerator;

public class PillarDecorator {

    public static void apply(ChunkGenerator.ChunkData chunk,
                             int chunkX,
                             int chunkZ,
                             int height,
                             long seed,
                             Palette palette) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int regionSize = 18;
        int minRegionX = Math.floorDiv(baseX, regionSize) - 1;
        int maxRegionX = Math.floorDiv(baseX + 15, regionSize) + 1;
        int minRegionZ = Math.floorDiv(baseZ, regionSize) - 1;
        int maxRegionZ = Math.floorDiv(baseZ + 15, regionSize) + 1;

        for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
            for (int regionZ = minRegionZ; regionZ <= maxRegionZ; regionZ++) {
                if (hash(regionX, regionZ, seed, 3, 100) >= 24) {
                    continue;
                }

                int worldX = regionX * regionSize + 4 + hash(regionX, regionZ, seed, 7, regionSize - 8);
                int worldZ = regionZ * regionSize + 4 + hash(regionX, regionZ, seed, 11, regionSize - 8);

                if (worldX < baseX || worldX > baseX + 15 || worldZ < baseZ || worldZ > baseZ + 15) {
                    continue;
                }

                int localX = worldX - baseX;
                int localZ = worldZ - baseZ;
                int width = 1 + hash(regionX, regionZ, seed, 13, 2);
                boolean extendX = hash(regionX, regionZ, seed, 17, 2) == 0;

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
            if (x >= 4 && x < 12 && z >= 4 && z < 12) {
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

            if (x < 1 || x > 14 || z < 1 || z > 14) {
                return false;
            }

            if (chunk.getType(x, 1, z) != org.bukkit.Material.AIR) {
                return false;
            }

            if (chunk.getType(x, height - 2, z) != org.bukkit.Material.AIR) {
                return false;
            }
        }

        return true;
    }

    private static int hash(int x, int z, long seed, long salt, int bound) {
        long mixed = seed ^ salt;
        mixed ^= x * 0x9E3779B97F4A7C15L;
        mixed ^= z * 0xC2B2AE3D27D4EB4FL;
        mixed ^= (mixed >>> 33);
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= (mixed >>> 33);
        mixed *= 0xC4CEB9FE1A85EC53L;
        mixed ^= (mixed >>> 33);
        return Math.floorMod((int) mixed, bound);
    }
}
