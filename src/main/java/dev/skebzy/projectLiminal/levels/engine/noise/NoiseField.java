package dev.skebzy.projectLiminal.levels.engine.noise;

import java.util.Random;

public class NoiseField {

    public static double sample(int x, int z, long seed) {
        long s = x * 49632L + z * 325176L + seed;
        Random r = new Random(s);
        return r.nextDouble();
    }
}