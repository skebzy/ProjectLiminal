package dev.skebzy.projectLiminal.levels.engine.math;

public final class CellMath {

    private static final long X_SEED_MULTIPLIER = 73_428_767L;
    private static final long Z_SEED_MULTIPLIER = 912_931L;

    private CellMath() {
    }

    public static int cell(int coord, int size) {
        return Math.floorDiv(coord, size);
    }

    public static int local(int coord, int size) {
        return Math.floorMod(coord, size);
    }

    public static long seed(int x, int z) {
        return (x * X_SEED_MULTIPLIER) ^ (z * Z_SEED_MULTIPLIER);
    }
}
