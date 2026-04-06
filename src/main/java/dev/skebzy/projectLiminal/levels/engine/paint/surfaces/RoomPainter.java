package dev.skebzy.projectLiminal.levels.engine.paint.surfaces;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;
import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;

public class RoomPainter {

    private static final int CHUNK_SIZE = 16;
    private static final int FLOOR_Y = 0;
    private static final int WALL_TRIM_Y = 1;
    private static final int WALL_ACCENT_Y = 2;
    private static final int ACCENT_GRID = 3;
    private static final int ACCENT_SALT = 17;
    private static final int ACCENT_RANGE = 100;
    private static final int ACCENT_RATE = 4;
    private static final int MIX_SHIFT = 33;
    private static final long X_MIX = 0x9E3779B97F4A7C15L;
    private static final long Z_MIX = 0xC2B2AE3D27D4EB4FL;
    private static final long MIX_MULTIPLIER = 0xFF51AFD7ED558CCDL;

    public static void paint(ChunkGenerator.ChunkData chunk,
                             int chunkX, int chunkZ,
                             int size, int height,
                             long seed,
                             LayoutStrategy layout,
                             Palette palette) {

        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {

                int worldX = baseX + x;
                int worldZ = baseZ + z;

                boolean open = layout.isOpen(worldX, worldZ, seed);

                for (int y = 0; y < height; y++) {

                    if (y == FLOOR_Y) {
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
                                            long seed) {
        if (y == WALL_TRIM_Y) {
            return palette.wallTrim();
        }

        if (y == WALL_ACCENT_Y && hasWallpaperVariation(worldX, worldZ, seed)) {
            return palette.wallAccent();
        }

        return palette.wall();
    }

    private static boolean hasWallpaperVariation(int worldX, int worldZ, long seed) {
        int blockX = Math.floorDiv(worldX, ACCENT_GRID);
        int blockZ = Math.floorDiv(worldZ, ACCENT_GRID);
        return accentSample(blockX, blockZ, seed, ACCENT_SALT) < ACCENT_RATE;
    }

    private static int accentSample(int worldX, int worldZ, long seed, long salt) {
        long mixed = seed ^ salt;
        mixed ^= worldX * X_MIX;
        mixed ^= worldZ * Z_MIX;
        mixed ^= (mixed >>> MIX_SHIFT);
        mixed *= MIX_MULTIPLIER;
        mixed ^= (mixed >>> MIX_SHIFT);
        return Math.floorMod((int) mixed, ACCENT_RANGE);
    }
}
