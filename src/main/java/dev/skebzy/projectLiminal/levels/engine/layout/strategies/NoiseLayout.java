package dev.skebzy.projectLiminal.levels.engine.layout.strategies;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;
public class NoiseLayout implements LayoutStrategy {

    private static final int SECTOR_SIZE = BackroomsSectorMath.SECTOR_SIZE;
    private static final long BOUNDARY_SALT = 0x245A1F3DL;

    private final double threshold;
    private final int scale;

    public NoiseLayout(double threshold, int scale) {
        this.threshold = threshold;
        this.scale = scale;
    }

    @Override
    public boolean isOpen(int x, int z, long seed) {
        int sectorX = BackroomsSectorMath.sector(x);
        int sectorZ = BackroomsSectorMath.sector(z);
        int localX = BackroomsSectorMath.local(x);
        int localZ = BackroomsSectorMath.local(z);

        if (isPerimeter(localX, localZ)) {
            return hasPerimeterOpening(localX, localZ, sectorX, sectorZ, seed);
        }

        if (isDividerWall(localX, localZ, sectorX, sectorZ, seed)) {
            return isDividerOpening(localX, localZ, sectorX, sectorZ, seed);
        }

        if (isConnectorSpace(localX, localZ, sectorX, sectorZ, seed)) {
            return true;
        }

        return true;
    }

    private boolean isPerimeter(int localX, int localZ) {
        return localX == 0 || localZ == 0
                || localX == SECTOR_SIZE - 1 || localZ == SECTOR_SIZE - 1;
    }

    private boolean hasPerimeterOpening(int localX, int localZ,
                                        int sectorX, int sectorZ,
                                        long seed) {
        if (localX == 0) {
            return isWithinVerticalOpening(localZ, sectorX, sectorZ, seed)
                    || isWithinSecondaryVerticalOpening(localZ, sectorX, sectorZ, seed);
        }

        if (localX == SECTOR_SIZE - 1) {
            return isWithinVerticalOpening(localZ, sectorX + 1, sectorZ, seed)
                    || isWithinSecondaryVerticalOpening(localZ, sectorX + 1, sectorZ, seed);
        }

        if (localZ == 0) {
            return isWithinHorizontalOpening(localX, sectorX, sectorZ, seed)
                    || isWithinSecondaryHorizontalOpening(localX, sectorX, sectorZ, seed);
        }

        return isWithinHorizontalOpening(localX, sectorX, sectorZ + 1, seed)
                || isWithinSecondaryHorizontalOpening(localX, sectorX, sectorZ + 1, seed);
    }

    private boolean isDividerWall(int localX, int localZ,
                                  int sectorX, int sectorZ,
                                  long seed) {
        int verticalDivider = BackroomsSectorMath.verticalDivider(sectorX, sectorZ, seed);
        int horizontalDivider = BackroomsSectorMath.horizontalDivider(sectorX, sectorZ, seed);

        boolean verticalEnabled = BackroomsSectorMath.hasVerticalDivider(sectorX, sectorZ, seed);
        boolean horizontalEnabled = BackroomsSectorMath.hasHorizontalDivider(sectorX, sectorZ, seed);

        return (verticalEnabled && localX == verticalDivider)
                || (horizontalEnabled && localZ == horizontalDivider);
    }

    private boolean isDividerOpening(int localX, int localZ,
                                     int sectorX, int sectorZ,
                                     long seed) {
        int verticalDivider = BackroomsSectorMath.verticalDivider(sectorX, sectorZ, seed);
        int horizontalDivider = BackroomsSectorMath.horizontalDivider(sectorX, sectorZ, seed);

        if (localX == verticalDivider) {
            int openingA = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 13);
            int openingB = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 17);
            int width = GridRoomMath.dividerOpeningWidth(sectorX, sectorZ, seed, 23);
            return within(localZ, openingA, width) || within(localZ, openingB, width);
        }

        if (localZ == horizontalDivider) {
            int openingA = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 29);
            int openingB = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 31);
            int width = GridRoomMath.dividerOpeningWidth(sectorX, sectorZ, seed, 37);
            return within(localX, openingA, width) || within(localX, openingB, width);
        }

        return false;
    }

    private boolean isConnectorSpace(int localX, int localZ,
                                     int sectorX, int sectorZ,
                                     long seed) {
        return isDividerConnector(localX, localZ, sectorX, sectorZ, seed)
                || isPerimeterConnector(localX, localZ, sectorX, sectorZ, seed);
    }

    private boolean isDividerConnector(int localX, int localZ,
                                       int sectorX, int sectorZ,
                                       long seed) {
        int verticalDivider = BackroomsSectorMath.verticalDivider(sectorX, sectorZ, seed);
        int horizontalDivider = BackroomsSectorMath.horizontalDivider(sectorX, sectorZ, seed);

        if (BackroomsSectorMath.hasVerticalDivider(sectorX, sectorZ, seed)) {
            int startA = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 13);
            int startB = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 17);
            int width = GridRoomMath.dividerOpeningWidth(sectorX, sectorZ, seed, 23);

            if (matchesConnectorAlongVerticalDivider(localX, localZ, sectorX, sectorZ, seed, verticalDivider, startA, width, 41)) {
                return true;
            }

            if (matchesConnectorAlongVerticalDivider(localX, localZ, sectorX, sectorZ, seed, verticalDivider, startB, width, 43)) {
                return true;
            }
        }

        if (BackroomsSectorMath.hasHorizontalDivider(sectorX, sectorZ, seed)) {
            int startA = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 29);
            int startB = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 31);
            int width = GridRoomMath.dividerOpeningWidth(sectorX, sectorZ, seed, 37);

            if (matchesConnectorAlongHorizontalDivider(localX, localZ, sectorX, sectorZ, seed, horizontalDivider, startA, width, 47)) {
                return true;
            }

            if (matchesConnectorAlongHorizontalDivider(localX, localZ, sectorX, sectorZ, seed, horizontalDivider, startB, width, 53)) {
                return true;
            }
        }

        return false;
    }

    private boolean isPerimeterConnector(int localX, int localZ,
                                         int sectorX, int sectorZ,
                                         long seed) {
        if (matchesBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, 0, true, 61)) {
            return true;
        }

        if (matchesSecondaryBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, 0, true, 63)) {
            return true;
        }

        if (matchesBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, SECTOR_SIZE - 1, true, 67)) {
            return true;
        }

        if (matchesSecondaryBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, SECTOR_SIZE - 1, true, 69)) {
            return true;
        }

        if (matchesBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, 0, false, 71)) {
            return true;
        }

        if (matchesSecondaryBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, 0, false, 73)) {
            return true;
        }

        if (matchesBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, SECTOR_SIZE - 1, false, 79)) {
            return true;
        }

        return matchesSecondaryBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, SECTOR_SIZE - 1, false, 81);
    }

    private boolean matchesConnectorAlongVerticalDivider(int localX, int localZ,
                                                         int sectorX, int sectorZ,
                                                         long seed,
                                                         int divider,
                                                         int openingStart,
                                                         int openingWidth,
                                                         long salt) {
        if (!within(localZ, openingStart, openingWidth)) {
            return false;
        }

        int depth = 4 + hashRange(sectorX, sectorZ, seed, salt, 4);
        int hallWidth = openingWidth + 2 + hashRange(sectorX, sectorZ, seed, salt + 1, 2);
        int hallStart = openingStart - ((hallWidth - openingWidth) / 2);

        return within(localX, divider - depth + 1, (depth * 2) - 1)
                && within(localZ, hallStart, hallWidth);
    }

    private boolean matchesConnectorAlongHorizontalDivider(int localX, int localZ,
                                                           int sectorX, int sectorZ,
                                                           long seed,
                                                           int divider,
                                                           int openingStart,
                                                           int openingWidth,
                                                           long salt) {
        if (!within(localX, openingStart, openingWidth)) {
            return false;
        }

        int depth = 4 + hashRange(sectorX, sectorZ, seed, salt, 4);
        int hallWidth = openingWidth + 2 + hashRange(sectorX, sectorZ, seed, salt + 1, 2);
        int hallStart = openingStart - ((hallWidth - openingWidth) / 2);

        return within(localZ, divider - depth + 1, (depth * 2) - 1)
                && within(localX, hallStart, hallWidth);
    }

    private boolean matchesBoundaryConnector(int localX, int localZ,
                                             int sectorX, int sectorZ,
                                             long seed,
                                             int boundary,
                                             boolean verticalBoundary,
                                             long salt) {
        int openingStart;
        int openingWidth;

        if (verticalBoundary) {
            int boundaryX = boundary == 0 ? sectorX : sectorX + 1;
            openingStart = 3 + boundaryHash(boundaryX, sectorZ, seed ^ BOUNDARY_SALT, 97, SECTOR_SIZE - 8);
            openingWidth = 3 + boundaryHash(boundaryX, sectorZ, seed ^ BOUNDARY_SALT, 101, 2);
            if (!within(localZ, openingStart, openingWidth)) {
                return false;
            }

            int inwardStart = boundary == 0 ? 0 : boundary - (4 + boundaryHash(boundaryX, sectorZ, seed, salt, 3));
            int inwardLength = 6 + boundaryHash(boundaryX, sectorZ, seed, salt + 1, 4);
            int hallWidth = openingWidth + 3;
            return within(localX, inwardStart, inwardLength)
                    && within(localZ, openingStart - 1, hallWidth);
        }

        int boundaryZ = boundary == 0 ? sectorZ : sectorZ + 1;
        openingStart = 3 + boundaryHash(sectorX, boundaryZ, seed ^ BOUNDARY_SALT, 103, SECTOR_SIZE - 8);
        openingWidth = 3 + boundaryHash(sectorX, boundaryZ, seed ^ BOUNDARY_SALT, 107, 2);
        if (!within(localX, openingStart, openingWidth)) {
            return false;
        }

        int inwardStart = boundary == 0 ? 0 : boundary - (4 + boundaryHash(sectorX, boundaryZ, seed, salt, 3));
        int inwardLength = 6 + boundaryHash(sectorX, boundaryZ, seed, salt + 1, 4);
        int hallWidth = openingWidth + 3;
        return within(localZ, inwardStart, inwardLength)
                && within(localX, openingStart - 1, hallWidth);
    }

    private boolean isWithinVerticalOpening(int localZ, int boundaryX, int sectorZ, long seed) {
        int start = GridRoomMath.boundaryOpeningStart(boundaryX, sectorZ, seed ^ BOUNDARY_SALT, 97);
        int width = GridRoomMath.boundaryOpeningWidth(boundaryX, sectorZ, seed ^ BOUNDARY_SALT, 101);
        return within(localZ, start, width);
    }

    private boolean isWithinHorizontalOpening(int localX, int sectorX, int boundaryZ, long seed) {
        int start = GridRoomMath.boundaryOpeningStart(sectorX, boundaryZ, seed ^ BOUNDARY_SALT, 103);
        int width = GridRoomMath.boundaryOpeningWidth(sectorX, boundaryZ, seed ^ BOUNDARY_SALT, 107);
        return within(localX, start, width);
    }

    private boolean isWithinSecondaryVerticalOpening(int localZ, int boundaryX, int sectorZ, long seed) {
        if (boundaryHash(boundaryX, sectorZ, seed, 151, 100) < 60) {
            return false;
        }

        int start = 2 + boundaryHash(boundaryX, sectorZ, seed ^ BOUNDARY_SALT, 157, SECTOR_SIZE - 6);
        int width = 3 + boundaryHash(boundaryX, sectorZ, seed ^ BOUNDARY_SALT, 163, 2);
        return within(localZ, start, width);
    }

    private boolean isWithinSecondaryHorizontalOpening(int localX, int sectorX, int boundaryZ, long seed) {
        if (boundaryHash(sectorX, boundaryZ, seed, 167, 100) < 60) {
            return false;
        }

        int start = 2 + boundaryHash(sectorX, boundaryZ, seed ^ BOUNDARY_SALT, 173, SECTOR_SIZE - 6);
        int width = 3 + boundaryHash(sectorX, boundaryZ, seed ^ BOUNDARY_SALT, 179, 2);
        return within(localX, start, width);
    }

    private int hashRange(int x, int z, long seed, long salt, int bound) {
        return BackroomsSectorMath.hashRange(x, z, seed, salt, bound);
    }

    private int boundaryHash(int x, int z, long seed, long salt, int bound) {
        return BackroomsSectorMath.hashRange(x, z, seed, salt ^ 0x27D4EB2DL, bound);
    }

    private boolean matchesSecondaryBoundaryConnector(int localX, int localZ,
                                                      int sectorX, int sectorZ,
                                                      long seed,
                                                      int boundary,
                                                      boolean verticalBoundary,
                                                      long salt) {
        if (verticalBoundary) {
            int boundaryX = boundary == 0 ? sectorX : sectorX + 1;
            if (boundaryHash(boundaryX, sectorZ, seed, 151, 100) < 60) {
                return false;
            }
            return matchesBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, boundary, true, salt);
        }

        int boundaryZ = boundary == 0 ? sectorZ : sectorZ + 1;
        if (boundaryHash(sectorX, boundaryZ, seed, 167, 100) < 60) {
            return false;
        }
        return matchesBoundaryConnector(localX, localZ, sectorX, sectorZ, seed, boundary, false, salt);
    }

    private boolean isNearVerticalConnection(int localX, int localZ,
                                             int sectorX, int sectorZ,
                                             long seed) {
        int verticalDivider = BackroomsSectorMath.verticalDivider(sectorX, sectorZ, seed);
        int primaryA = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 13);
        int primaryB = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 17);
        int primaryWidth = GridRoomMath.dividerOpeningWidth(sectorX, sectorZ, seed, 23);

        return within(localX, verticalDivider - 4, 9)
                && (within(localZ, primaryA - 3, primaryWidth + 6)
                || within(localZ, primaryB - 3, primaryWidth + 6));
    }

    private boolean isNearHorizontalConnection(int localX, int localZ,
                                               int sectorX, int sectorZ,
                                               long seed) {
        int horizontalDivider = BackroomsSectorMath.horizontalDivider(sectorX, sectorZ, seed);
        int primaryA = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 29);
        int primaryB = GridRoomMath.dividerOpeningStart(sectorX, sectorZ, seed, 31);
        int primaryWidth = GridRoomMath.dividerOpeningWidth(sectorX, sectorZ, seed, 37);

        return within(localZ, horizontalDivider - 4, 9)
                && (within(localX, primaryA - 3, primaryWidth + 6)
                || within(localX, primaryB - 3, primaryWidth + 6));
    }

    private boolean within(int value, int start, int length) {
        return value >= start && value < start + length;
    }
}
