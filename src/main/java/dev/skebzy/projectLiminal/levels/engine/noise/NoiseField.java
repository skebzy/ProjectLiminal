package dev.skebzy.projectLiminal.levels.engine.noise;

import java.util.Random;

public class NoiseField {

    private static final long X_MULTIPLIER = 49_632L;
    private static final long Z_MULTIPLIER = 325_176L;

    public static double sample(int x, int z, long seed) {
        long mixedSeed = (x * X_MULTIPLIER) + (z * Z_MULTIPLIER) + seed;
        Random random = new Random(mixedSeed);
        return random.nextDouble();
    }
}
