package dev.skebzy.projectLiminal.levels.engine.paint;

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

                    Material wallMaterial =
                            (y == 1 || y == height - 2)
                                    ? palette.wallTrim()
                                    : palette.wall();

                    chunk.setBlock(x, y, z, wallMaterial);
                }
            }
        }
    }
}
