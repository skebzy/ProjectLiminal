package dev.skebzy.projectLiminal.levels.engine.paint.decorators;

import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class DecayDecorator {

    public static void apply(World world,
                             int startX, int startZ,
                             int size, int height,
                             long seed,
                             Palette palette) {

        Random r = new Random(seed);

        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {

                if (r.nextInt(20) != 0) continue;

                int wx = startX + x;
                int wz = startZ + z;

                world.getBlockAt(wx, 1, wz)
                        .setType(Material.AIR); // broken wall spot
            }
        }
    }
}