package dev.skebzy.projectLiminal.levels.engine.paint.surfaces;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;
import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;

public class RoomPainter {

    public static void paint(ChunkGenerator.ChunkData chunk,
                             int chunkX, int chunkZ,
                             int size, int height,
                             long seed,
                             LayoutStrategy layout,
                             Palette palette) {

        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int worldX = baseX + x;
                int worldZ = baseZ + z;

                boolean open = layout.isOpen(worldX, worldZ, seed);

                for (int y = 0; y < height; y++) {

                    if (y == 0) {
                        chunk.setBlock(x, y, z, palette.floor());
                        continue;
                    }

                    if (y == height - 1) {
                        chunk.setBlock(x, y, z, palette.ceiling());
                        continue;
                    }

                    if (open) {
                        chunk.setBlock(x, y, z, Material.AIR);
                        continue;
                    }

                    Material wallMaterial = wallMaterialFor(
                            palette,
                            worldX,
                            worldZ,
                            y,
                            height,
                            seed
                    );

                    chunk.setBlock(x, y, z, wallMaterial);
                }
            }
        }
    }

    private static Material wallMaterialFor(Palette palette,
                                            int worldX,
                                            int worldZ,
                                            int y,
                                            int height,
                                            long seed) {
        if (y == 1) {
            return palette.wallTrim();
        }

        if (y == 2 && hasWallpaperVariation(worldX, worldZ, seed)) {
            return palette.wallAccent();
        }

        return palette.wall();
    }

    private static boolean hasWallpaperVariation(int worldX, int worldZ, long seed) {
        int blockX = Math.floorDiv(worldX, 3);
        int blockZ = Math.floorDiv(worldZ, 3);
        return accentSample(blockX, blockZ, seed, 17) < 4;
    }

    private static int accentSample(int worldX, int worldZ, long seed, long salt) {
        long mixed = seed ^ salt;
        mixed ^= worldX * 0x9E3779B97F4A7C15L;
        mixed ^= worldZ * 0xC2B2AE3D27D4EB4FL;
        mixed ^= (mixed >>> 33);
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= (mixed >>> 33);
        return Math.floorMod((int) mixed, 100);
    }
}
