package dev.skebzy.projectLiminal.levels.engine.layout.grid;

import dev.skebzy.projectLiminal.levels.engine.math.CellMath;
import dev.skebzy.projectLiminal.levels.engine.noise.NoiseField;

public final class BackroomsSectorMath {

    public static final int SECTOR_SIZE = 24;
    public static final int MIN_DIVIDER_OFFSET = 6;
    public static final int MAX_DIVIDER_OFFSET = SECTOR_SIZE - 7;
    private static final int DIVIDER_RANGE = MAX_DIVIDER_OFFSET - MIN_DIVIDER_OFFSET + 1;
    private static final int MIX_SHIFT = 33;
    private static final long NOISE_SALT = 0x165667919E3779F9L;
    private static final double HASH_NOISE_WEIGHT = 0.08D;
    private static final long HASH_NOISE_SPAN = 1_000_000L;
    private static final long X_MIX = 0x9E3779B97F4A7C15L;
    private static final long Z_MIX = 0xC2B2AE3D27D4EB4FL;
    private static final long FIRST_MIX_MULTIPLIER = 0xFF51AFD7ED558CCDL;
    private static final long SECOND_MIX_MULTIPLIER = 0xC4CEB9FE1A85EC53L;

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
                DIVIDER_RANGE
        );
    }

    private static int mix(int x, int z, long seed, long salt) {
        long mixed = seed ^ salt;
        mixed ^= x * X_MIX;
        mixed ^= z * Z_MIX;
        mixed += noiseOffset(x, z, seed, salt);
        mixed ^= (mixed >>> MIX_SHIFT);
        mixed *= FIRST_MIX_MULTIPLIER;
        mixed ^= (mixed >>> MIX_SHIFT);
        mixed *= SECOND_MIX_MULTIPLIER;
        mixed ^= (mixed >>> MIX_SHIFT);
        return (int) mixed;
    }

    private static long noiseOffset(int x, int z, long seed, long salt) {
        double noise = NoiseField.hashNoise(x + (int) salt, z - (int) salt, seed ^ NOISE_SALT);
        double centered = noise - 0.5D;
        return Math.round(centered * HASH_NOISE_WEIGHT * HASH_NOISE_SPAN);
    }
}
