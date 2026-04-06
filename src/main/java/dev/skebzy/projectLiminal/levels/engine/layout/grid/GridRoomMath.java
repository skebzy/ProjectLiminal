package dev.skebzy.projectLiminal.levels.engine.layout.grid;

public final class GridRoomMath {

    private static final int INNER_MIN = 1;
    private static final int OPENING_MARGIN = 3;
    private static final int OPENING_BASE = 4;
    private static final int OPENING_VARIANTS = 2;
    private static final int OPENING_SPAN = 8;

    private GridRoomMath() {
    }

    public static int[] xRanges(int sectorX, int sectorZ, long seed) {
        if (!BackroomsSectorMath.hasVerticalDivider(sectorX, sectorZ, seed)) {
            return singleRange();
        }

        int divider = BackroomsSectorMath.verticalDivider(sectorX, sectorZ, seed);
        return new int[]{
                INNER_MIN, divider,
                divider + 1, BackroomsSectorMath.SECTOR_SIZE - 1
        };
    }

    public static int[] zRanges(int sectorX, int sectorZ, long seed) {
        if (!BackroomsSectorMath.hasHorizontalDivider(sectorX, sectorZ, seed)) {
            return singleRange();
        }

        int divider = BackroomsSectorMath.horizontalDivider(sectorX, sectorZ, seed);
        return new int[]{
                INNER_MIN, divider,
                divider + 1, BackroomsSectorMath.SECTOR_SIZE - 1
        };
    }

    public static int dividerOpeningStart(int sectorX, int sectorZ, long seed, long salt) {
        return openingStart(sectorX, sectorZ, seed, salt);
    }

    public static int dividerOpeningWidth(int sectorX, int sectorZ, long seed, long salt) {
        return openingWidth(sectorX, sectorZ, seed, salt);
    }

    public static int boundaryOpeningStart(int boundaryX, int boundaryZ, long seed, long salt) {
        return openingStart(boundaryX, boundaryZ, seed, salt);
    }

    public static int boundaryOpeningWidth(int boundaryX, int boundaryZ, long seed, long salt) {
        return openingWidth(boundaryX, boundaryZ, seed, salt);
    }

    private static int[] singleRange() {
        return new int[]{INNER_MIN, BackroomsSectorMath.SECTOR_SIZE - 1};
    }

    private static int openingStart(int x, int z, long seed, long salt) {
        return OPENING_MARGIN
                + BackroomsSectorMath.hashRange(x, z, seed, salt, BackroomsSectorMath.SECTOR_SIZE - OPENING_SPAN);
    }

    private static int openingWidth(int x, int z, long seed, long salt) {
        return OPENING_BASE
                + BackroomsSectorMath.hashRange(x, z, seed, salt, OPENING_VARIANTS);
    }
}
