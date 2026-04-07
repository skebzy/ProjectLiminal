package dev.skebzy.projectLiminal.levels.engine.noise;

import java.util.Random;

public class NoiseField {

    private static final long X_MULTIPLIER = 49_632L;
    private static final long Z_MULTIPLIER = 325_176L;
    private static final double HASH_SCALE = 0.18D;
    private static final int HASH_DECIMALS = 2;
    private static final double DECIMAL_STEP = 100D;

    public static double sample(int x, int z, long seed) {
        long mixedSeed = (x * X_MULTIPLIER) + (z * Z_MULTIPLIER) + seed;
        Random random = new Random(mixedSeed);
        return random.nextDouble();
    }

    public static double hashNoise(int x, int z, long seed) {
        return quantize(sampleSmooth(x * HASH_SCALE, z * HASH_SCALE, seed), HASH_DECIMALS);
    }

    private static double sampleSmooth(double x, double z, long seed) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double localX = x - x0;
        double localZ = z - z0;
        double smoothX = fade(localX);
        double smoothZ = fade(localZ);

        double topLeft = sample(x0, z0, seed);
        double topRight = sample(x1, z0, seed);
        double bottomLeft = sample(x0, z1, seed);
        double bottomRight = sample(x1, z1, seed);

        double top = lerp(topLeft, topRight, smoothX);
        double bottom = lerp(bottomLeft, bottomRight, smoothX);
        return lerp(top, bottom, smoothZ);
    }

    private static double quantize(double value, int decimals) {
        double scale = decimals == HASH_DECIMALS ? DECIMAL_STEP : Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    private static double fade(double value) {
        return value * value * (3D - (2D * value));
    }

    private static double lerp(double start, double end, double t) {
        return start + ((end - start) * t);
    }
}
