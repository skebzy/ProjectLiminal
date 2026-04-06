package dev.skebzy.projectLiminal.levels.engine.paint.features;

import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class DecayDecorator {

    private static final int DECAY_CHANCE = 20;
    private static final int BROKEN_WALL_Y = 1;

    public static void apply(World world,
                             int startX, int startZ,
                             int size, int height,
                             long seed,
                             Palette palette) {

        Random r = new Random(seed);

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {

                if (r.nextInt(DECAY_CHANCE) != 0) continue;

                int wx = startX + x;
                int wz = startZ + z;

                world.getBlockAt(wx, BROKEN_WALL_Y, wz)
                        .setType(Material.AIR); // broken wall spot
            }
        }
    }
}
