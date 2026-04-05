package dev.skebzy.projectLiminal.levels.engine.layout.grid;

public final class GridRoomMath {

    private GridRoomMath() {
    }

    public static int[] xRanges(int sectorX, int sectorZ, long seed) {
        if (!BackroomsSectorMath.hasVerticalDivider(sectorX, sectorZ, seed)) {
            return new int[]{1, BackroomsSectorMath.SECTOR_SIZE - 1};
        }

        int divider = BackroomsSectorMath.verticalDivider(sectorX, sectorZ, seed);
        return new int[]{
                1, divider,
                divider + 1, BackroomsSectorMath.SECTOR_SIZE - 1
        };
    }

    public static int[] zRanges(int sectorX, int sectorZ, long seed) {
        if (!BackroomsSectorMath.hasHorizontalDivider(sectorX, sectorZ, seed)) {
            return new int[]{1, BackroomsSectorMath.SECTOR_SIZE - 1};
        }

        int divider = BackroomsSectorMath.horizontalDivider(sectorX, sectorZ, seed);
        return new int[]{
                1, divider,
                divider + 1, BackroomsSectorMath.SECTOR_SIZE - 1
        };
    }

    public static int dividerOpeningStart(int sectorX, int sectorZ, long seed, long salt) {
        return 3 + BackroomsSectorMath.hashRange(sectorX, sectorZ, seed, salt, BackroomsSectorMath.SECTOR_SIZE - 8);
    }

    public static int dividerOpeningWidth(int sectorX, int sectorZ, long seed, long salt) {
        return 4 + BackroomsSectorMath.hashRange(sectorX, sectorZ, seed, salt, 2);
    }

    public static int boundaryOpeningStart(int boundaryX, int boundaryZ, long seed, long salt) {
        return 3 + BackroomsSectorMath.hashRange(boundaryX, boundaryZ, seed, salt, BackroomsSectorMath.SECTOR_SIZE - 8);
    }

    public static int boundaryOpeningWidth(int boundaryX, int boundaryZ, long seed, long salt) {
        return 4 + BackroomsSectorMath.hashRange(boundaryX, boundaryZ, seed, salt, 2);
    }
}
