package dev.skebzy.projectLiminal.levels.engine.layout.grid;

import java.util.ArrayList;
import java.util.List;

public final class BackroomsRoomGrid {

    private static final int INTERIOR_MIN = 1;
    private static final int INTERIOR_MAX = BackroomsSectorMath.SECTOR_SIZE - 2;
    private static final int MIN_ROOM_SPAN = 6;
    private static final int MAX_SPLIT_DEPTH = 2;
    private static final long BOUNDARY_SALT = 0x245A1F3DL;

    private BackroomsRoomGrid() {
    }

    public static SectorPlan plan(int sectorX, int sectorZ, long seed) {
        boolean[][] openMap = new boolean[BackroomsSectorMath.SECTOR_SIZE][BackroomsSectorMath.SECTOR_SIZE];
        List<LocalRoom> localRooms = new ArrayList<>();

        fillInterior(openMap);
        partition(
                openMap,
                localRooms,
                new LocalRoom(INTERIOR_MIN, INTERIOR_MAX, INTERIOR_MIN, INTERIOR_MAX),
                sectorX,
                sectorZ,
                seed,
                0
        );
        carvePerimeterOpenings(openMap, sectorX, sectorZ, seed);

        return new SectorPlan(openMap, toWorldRooms(localRooms, sectorX, sectorZ));
    }

    private static void fillInterior(boolean[][] openMap) {
        for (int localX = INTERIOR_MIN; localX <= INTERIOR_MAX; localX++) {
            for (int localZ = INTERIOR_MIN; localZ <= INTERIOR_MAX; localZ++) {
                openMap[localX][localZ] = true;
            }
        }
    }

    private static void partition(boolean[][] openMap,
                                  List<LocalRoom> rooms,
                                  LocalRoom room,
                                  int sectorX,
                                  int sectorZ,
                                  long seed,
                                  int depth) {
        if (!shouldSplit(room, sectorX, sectorZ, seed, depth)) {
            rooms.add(room);
            return;
        }

        boolean splitVertical = chooseSplitOrientation(room, sectorX, sectorZ, seed, depth);
        if (splitVertical && canSplitVertically(room)) {
            int splitX = verticalSplit(room, sectorX, sectorZ, seed, depth);
            drawVerticalWall(openMap, room, splitX);
            carveVerticalOpenings(openMap, room, splitX, sectorX, sectorZ, seed, depth);

            partition(openMap, rooms, room.leftOf(splitX), sectorX, sectorZ, seed, depth + 1);
            partition(openMap, rooms, room.rightOf(splitX), sectorX, sectorZ, seed, depth + 1);
            return;
        }

        if (canSplitHorizontally(room)) {
            int splitZ = horizontalSplit(room, sectorX, sectorZ, seed, depth);
            drawHorizontalWall(openMap, room, splitZ);
            carveHorizontalOpenings(openMap, room, splitZ, sectorX, sectorZ, seed, depth);

            partition(openMap, rooms, room.above(splitZ), sectorX, sectorZ, seed, depth + 1);
            partition(openMap, rooms, room.below(splitZ), sectorX, sectorZ, seed, depth + 1);
            return;
        }

        rooms.add(room);
    }

    private static boolean shouldSplit(LocalRoom room,
                                       int sectorX,
                                       int sectorZ,
                                       long seed,
                                       int depth) {
        if (depth >= MAX_SPLIT_DEPTH) {
            return false;
        }

        boolean canSplitVertically = canSplitVertically(room);
        boolean canSplitHorizontally = canSplitHorizontally(room);
        if (!canSplitVertically && !canSplitHorizontally) {
            return false;
        }

        if (depth == 0) {
            return true;
        }

        int largerSpan = Math.max(room.width(), room.length());
        if (largerSpan >= 18) {
            return true;
        }

        if (largerSpan >= 14) {
            return hash(room, sectorX, sectorZ, seed, 13 + depth, 100) < 42;
        }

        return false;
    }

    private static boolean chooseSplitOrientation(LocalRoom room,
                                                  int sectorX,
                                                  int sectorZ,
                                                  long seed,
                                                  int depth) {
        if (!canSplitHorizontally(room)) {
            return true;
        }

        if (!canSplitVertically(room)) {
            return false;
        }

        int width = room.width();
        int length = room.length();
        if (width - length >= 4) {
            return true;
        }
        if (length - width >= 4) {
            return false;
        }

        return hash(room, sectorX, sectorZ, seed, 29 + depth, 2) == 0;
    }

    private static boolean canSplitVertically(LocalRoom room) {
        return room.width() >= (MIN_ROOM_SPAN * 2) + 1;
    }

    private static boolean canSplitHorizontally(LocalRoom room) {
        return room.length() >= (MIN_ROOM_SPAN * 2) + 1;
    }

    private static int verticalSplit(LocalRoom room,
                                     int sectorX,
                                     int sectorZ,
                                     long seed,
                                     int depth) {
        int min = room.minX + MIN_ROOM_SPAN;
        int max = room.maxX - MIN_ROOM_SPAN;
        return selectSplitCoordinate(min, max, room.width(), hash(room, sectorX, sectorZ, seed, 41 + depth, 3));
    }

    private static int horizontalSplit(LocalRoom room,
                                       int sectorX,
                                       int sectorZ,
                                       long seed,
                                       int depth) {
        int min = room.minZ + MIN_ROOM_SPAN;
        int max = room.maxZ - MIN_ROOM_SPAN;
        return selectSplitCoordinate(min, max, room.length(), hash(room, sectorX, sectorZ, seed, 47 + depth, 3));
    }

    private static void drawVerticalWall(boolean[][] openMap, LocalRoom room, int splitX) {
        for (int localZ = room.minZ; localZ <= room.maxZ; localZ++) {
            openMap[splitX][localZ] = false;
        }
    }

    private static void drawHorizontalWall(boolean[][] openMap, LocalRoom room, int splitZ) {
        for (int localX = room.minX; localX <= room.maxX; localX++) {
            openMap[localX][splitZ] = false;
        }
    }

    private static void carveVerticalOpenings(boolean[][] openMap,
                                              LocalRoom room,
                                              int splitX,
                                              int sectorX,
                                              int sectorZ,
                                              long seed,
                                              int depth) {
        int openingCount = internalOpeningCount(room.length(), room, sectorX, sectorZ, seed, depth);
        carveSpanOpenings(
                openMap,
                splitX,
                room.minZ,
                room.maxZ,
                openingCount,
                openingWidth(room.length(), room, sectorX, sectorZ, seed, 61 + depth),
                true,
                room,
                sectorX,
                sectorZ,
                seed,
                71 + depth
        );
    }

    private static void carveHorizontalOpenings(boolean[][] openMap,
                                                LocalRoom room,
                                                int splitZ,
                                                int sectorX,
                                                int sectorZ,
                                                long seed,
                                                int depth) {
        int openingCount = internalOpeningCount(room.width(), room, sectorX, sectorZ, seed, depth);
        carveSpanOpenings(
                openMap,
                splitZ,
                room.minX,
                room.maxX,
                openingCount,
                openingWidth(room.width(), room, sectorX, sectorZ, seed, 79 + depth),
                false,
                room,
                sectorX,
                sectorZ,
                seed,
                89 + depth
        );
    }

    private static int internalOpeningCount(int span,
                                            LocalRoom room,
                                            int sectorX,
                                            int sectorZ,
                                            long seed,
                                            int depth) {
        if (span < 14) {
            return 1;
        }

        return span >= 18 ? 2 : 1;
    }

    private static int openingWidth(int span,
                                    LocalRoom room,
                                    int sectorX,
                                    int sectorZ,
                                    long seed,
                                    long salt) {
        int base = span >= 18 ? 5 : 4;
        return Math.min(base + hash(room, sectorX, sectorZ, seed, salt, 2), 6);
    }

    private static void carvePerimeterOpenings(boolean[][] openMap,
                                               int sectorX,
                                               int sectorZ,
                                               long seed) {
        carveBoundary(openMap, 0, sectorX, sectorZ, seed, true, false);
        carveBoundary(openMap, BackroomsSectorMath.SECTOR_SIZE - 1, sectorX + 1, sectorZ, seed, true, true);
        carveBoundary(openMap, 0, sectorX, sectorZ, seed, false, false);
        carveBoundary(openMap, BackroomsSectorMath.SECTOR_SIZE - 1, sectorX, sectorZ + 1, seed, false, true);
    }

    private static void carveBoundary(boolean[][] openMap,
                                      int boundaryCoordinate,
                                      int boundaryX,
                                      int boundaryZ,
                                      long seed,
                                      boolean verticalBoundary,
                                      boolean farSide) {
        int min = INTERIOR_MIN;
        int max = INTERIOR_MAX;
        int primaryWidth = 4 + boundaryHash(boundaryX, boundaryZ, seed, 101, 2);
        int primaryCenter = boundaryOpeningCenter(min, max, primaryWidth, boundaryX, boundaryZ, seed, 107);

        carveBoundaryOpening(openMap, boundaryCoordinate, primaryCenter, primaryWidth, verticalBoundary);

        int secondChance = farSide ? 34 : 26;
        if (spanHash(boundaryX, boundaryZ, seed, 113, 100) >= secondChance) {
            return;
        }

        int secondaryWidth = 3 + boundaryHash(boundaryX, boundaryZ, seed, 127, 2);
        int secondaryCenter = boundaryOpeningCenter(min, max, secondaryWidth, boundaryX, boundaryZ, seed, 131);

        if (Math.abs(primaryCenter - secondaryCenter) < 6) {
            return;
        }

        carveBoundaryOpening(openMap, boundaryCoordinate, secondaryCenter, secondaryWidth, verticalBoundary);
    }

    private static void carveBoundaryOpening(boolean[][] openMap,
                                             int boundaryCoordinate,
                                             int center,
                                             int width,
                                             boolean verticalBoundary) {
        int start = center - ((width - 1) / 2);
        for (int offset = 0; offset < width; offset++) {
            int spanCoordinate = start + offset;
            if (spanCoordinate < 1 || spanCoordinate >= BackroomsSectorMath.SECTOR_SIZE - 1) {
                continue;
            }

            if (verticalBoundary) {
                openMap[boundaryCoordinate][spanCoordinate] = true;
                continue;
            }

            openMap[spanCoordinate][boundaryCoordinate] = true;
        }
    }

    private static int boundaryOpeningCenter(int min,
                                             int max,
                                             int width,
                                             int boundaryX,
                                             int boundaryZ,
                                             long seed,
                                             long salt) {
        int available = (max - min + 1) - width + 1;
        int start = min + boundaryHash(boundaryX, boundaryZ, seed ^ BOUNDARY_SALT, salt, available);
        return start + ((width - 1) / 2);
    }

    private static void carveSpanOpenings(boolean[][] openMap,
                                          int wallCoordinate,
                                          int spanMin,
                                          int spanMax,
                                          int count,
                                          int width,
                                          boolean verticalWall,
                                          LocalRoom room,
                                          int sectorX,
                                          int sectorZ,
                                          long seed,
                                          long salt) {
        int[] centers = distributedCenters(spanMin + 2, spanMax - 2, count);

        for (int index = 0; index < centers.length; index++) {
            int openingCenter = clamp(centers[index], spanMin + 2, spanMax - 2);
            int start = openingCenter - ((width - 1) / 2);

            for (int offset = 0; offset < width; offset++) {
                int spanCoordinate = start + offset;
                if (spanCoordinate < spanMin || spanCoordinate > spanMax) {
                    continue;
                }

                if (verticalWall) {
                    openMap[wallCoordinate][spanCoordinate] = true;
                    continue;
                }

                openMap[spanCoordinate][wallCoordinate] = true;
            }
        }
    }

    private static int[] distributedCenters(int min, int max, int count) {
        if (count <= 1) {
            return new int[]{centerOf(min, max)};
        }

        int[] centers = new int[count];
        int span = max - min + 1;
        for (int index = 0; index < count; index++) {
            double ratio = (index + 1D) / (count + 1D);
            int coordinate = min + (int) Math.round((span - 1) * ratio);
            centers[index] = clamp(coordinate, min, max);
        }
        return centers;
    }

    private static int selectSplitCoordinate(int min, int max, int span, int variant) {
        int center = centerOf(min, max);
        if (span < 18) {
            return clamp(center, min, max);
        }

        int offset = Math.max(1, span / 6);
        if (variant == 0) {
            return clamp(center - offset, min, max);
        }
        if (variant == 1) {
            return clamp(center, min, max);
        }
        return clamp(center + offset, min, max);
    }

    private static RoomRegion[] toWorldRooms(List<LocalRoom> localRooms, int sectorX, int sectorZ) {
        int startX = sectorX * BackroomsSectorMath.SECTOR_SIZE;
        int startZ = sectorZ * BackroomsSectorMath.SECTOR_SIZE;
        RoomRegion[] rooms = new RoomRegion[localRooms.size()];

        for (int index = 0; index < localRooms.size(); index++) {
            LocalRoom room = localRooms.get(index);
            rooms[index] = new RoomRegion(
                    startX + room.minX,
                    startX + room.maxX,
                    startZ + room.minZ,
                    startZ + room.maxZ
            );
        }

        return rooms;
    }

    private static int boundaryHash(int x, int z, long seed, long salt, int bound) {
        return BackroomsSectorMath.hashRange(x, z, seed, salt ^ 0x27D4EB2DL, Math.max(1, bound));
    }

    private static int spanHash(int x, int z, long seed, long salt, int bound) {
        return BackroomsSectorMath.hashRange(x, z, seed ^ BOUNDARY_SALT, salt, bound);
    }

    private static int hash(LocalRoom room,
                            int sectorX,
                            int sectorZ,
                            long seed,
                            long salt,
                            int bound) {
        long signature = seed ^ salt;
        signature ^= sectorX * 0x9E3779B97F4A7C15L;
        signature ^= sectorZ * 0xC2B2AE3D27D4EB4FL;
        signature ^= room.minX * 73428767L;
        signature ^= room.maxX * 19349663L;
        signature ^= room.minZ * 912931L;
        signature ^= room.maxZ * 83492791L;
        signature ^= (signature >>> 33);
        signature *= 0xFF51AFD7ED558CCDL;
        signature ^= (signature >>> 33);
        return Math.floorMod((int) signature, bound);
    }

    private static int centerOf(int minInclusive, int maxInclusive) {
        return minInclusive + ((maxInclusive - minInclusive) / 2);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class SectorPlan {

        private final boolean[][] openMap;
        private final RoomRegion[] rooms;

        private SectorPlan(boolean[][] openMap, RoomRegion[] rooms) {
            this.openMap = openMap;
            this.rooms = rooms;
        }

        public boolean isOpenLocal(int localX, int localZ) {
            if (localX < 0 || localX >= BackroomsSectorMath.SECTOR_SIZE
                    || localZ < 0 || localZ >= BackroomsSectorMath.SECTOR_SIZE) {
                return false;
            }
            return openMap[localX][localZ];
        }

        public RoomRegion[] rooms() {
            return rooms.clone();
        }
    }

    public record RoomRegion(int minX, int maxX, int minZ, int maxZ) {

        public int width() {
            return maxX - minX + 1;
        }

        public int length() {
            return maxZ - minZ + 1;
        }

        public int centerX() {
            return centerOf(minX, maxX);
        }

        public int centerZ() {
            return centerOf(minZ, maxZ);
        }

        public RoomRegion inset(int margin) {
            int insetMinX = minX + margin;
            int insetMaxX = maxX - margin;
            int insetMinZ = minZ + margin;
            int insetMaxZ = maxZ - margin;
            if (insetMinX > insetMaxX || insetMinZ > insetMaxZ) {
                return null;
            }
            return new RoomRegion(insetMinX, insetMaxX, insetMinZ, insetMaxZ);
        }

        public boolean hallwayLike() {
            int smaller = Math.min(width(), length());
            int larger = Math.max(width(), length());
            return smaller <= 6 && larger >= 10;
        }
    }

    private record LocalRoom(int minX, int maxX, int minZ, int maxZ) {

        private int width() {
            return maxX - minX + 1;
        }

        private int length() {
            return maxZ - minZ + 1;
        }

        private LocalRoom leftOf(int splitX) {
            return new LocalRoom(minX, splitX - 1, minZ, maxZ);
        }

        private LocalRoom rightOf(int splitX) {
            return new LocalRoom(splitX + 1, maxX, minZ, maxZ);
        }

        private LocalRoom above(int splitZ) {
            return new LocalRoom(minX, maxX, minZ, splitZ - 1);
        }

        private LocalRoom below(int splitZ) {
            return new LocalRoom(minX, maxX, splitZ + 1, maxZ);
        }
    }
}
