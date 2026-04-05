package dev.skebzy.projectLiminal.levels.engine.math;

public class CellMath {

    public static int cell(int coord, int size) {
        return Math.floorDiv(coord, size);
    }

    public static int local(int coord, int size) {
        return Math.floorMod(coord, size);
    }

    public static long seed(int x, int z) {
        return (x * 73428767L) ^ (z * 912931L);
    }
}