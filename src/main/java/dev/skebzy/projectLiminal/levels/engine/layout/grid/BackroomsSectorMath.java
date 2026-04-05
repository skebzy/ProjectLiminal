package dev.skebzy.projectLiminal.levels.engine.layout.grid;

import dev.skebzy.projectLiminal.levels.engine.math.CellMath;
import dev.skebzy.projectLiminal.levels.engine.noise.NoiseField;

public final class BackroomsSectorMath {

    public static final int SECTOR_SIZE = 24;
    public static final int MIN_DIVIDER_OFFSET = 6;
    public static final int MAX_DIVIDER_OFFSET = SECTOR_SIZE - 7;

    private static final long DIVIDER_SALT = 0x61C88647L;
    private static final long BLACKOUT_SALT = 0x4C1F3A25L;

    private BackroomsSectorMath() {
    }

    public static int sector(int coordinate) {
        return CellMath.cell(coordinate, SECTOR_SIZE);
    }

    public static int local(int coordinate) {
        return CellMath.local(coordinate, SECTOR_SIZE);
    }

    public static int verticalDivider(int sectorX, int sectorZ, long seed) {
        return dividerOffset(sectorX, sectorZ, seed, 3);
    }

    public static int horizontalDivider(int sectorX, int sectorZ, long seed) {
        return dividerOffset(sectorX, sectorZ, seed, 7);
    }

    public static boolean hasVerticalDivider(int sectorX, int sectorZ, long seed) {
        return sample(sectorX, sectorZ, seed, 11) > 0.08;
    }

    public static boolean hasHorizontalDivider(int sectorX, int sectorZ, long seed) {
        return sample(sectorX, sectorZ, seed, 19) > 0.14;
    }

    public static boolean isBlackoutSector(int sectorX, int sectorZ, long seed) {
        return sample(sectorX, sectorZ, seed ^ BLACKOUT_SALT, 23) > 0.86;
    }

    public static int hashRange(int x, int z, long seed, long salt, int bound) {
        return Math.floorMod(mix(x, z, seed, salt), bound);
    }

    public static double sample(int x, int z, long seed, long salt) {
        return NoiseField.sample(x + (int) salt, z - (int) salt, seed);
    }

    private static int dividerOffset(int sectorX, int sectorZ, long seed, long salt) {
        return MIN_DIVIDER_OFFSET
                + hashRange(
                sectorX,
                sectorZ,
                seed ^ DIVIDER_SALT,
                salt,
                MAX_DIVIDER_OFFSET - MIN_DIVIDER_OFFSET + 1
        );
    }

    private static int mix(int x, int z, long seed, long salt) {
        long mixed = seed ^ salt;
        mixed ^= x * 0x9E3779B97F4A7C15L;
        mixed ^= z * 0xC2B2AE3D27D4EB4FL;
        mixed ^= (mixed >>> 33);
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= (mixed >>> 33);
        mixed *= 0xC4CEB9FE1A85EC53L;
        mixed ^= (mixed >>> 33);
        return (int) mixed;
    }
}
