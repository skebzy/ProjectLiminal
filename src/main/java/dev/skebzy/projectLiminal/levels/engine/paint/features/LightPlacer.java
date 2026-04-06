package dev.skebzy.projectLiminal.levels.engine.paint.features;

import dev.skebzy.projectLiminal.levels.engine.layout.grid.BackroomsRoomGrid;
import dev.skebzy.projectLiminal.levels.engine.layout.grid.BackroomsSectorMath;
import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;

public class LightPlacer {

    private static final int CHUNK_SIZE = 16;
    private static final int CHUNK_LAST = CHUNK_SIZE - 1;
    private static final int ROOM_MIN_SPAN = 5;
    private static final int ROOM_MARGIN = 2;
    private static final int HALLWAY_MARGIN = 1;
    private static final int HALLWAY_FIXTURE_SPACING = 6;
    private static final int ROOM_FIXTURE_SPACING = 6;
    private static final int LARGE_ROOM_FIXTURE_SPACING = 7;
    private static final int MAX_FIXTURES_PER_AXIS = 8;
    private static final int LONG_HALL = 18;
    private static final int LARGE_ROOM = 20;
    private static final int SHORT_LIGHT = 2;
    private static final int LONG_LIGHT = 3;
    private static final int MID_LANE_BREAK = 12;
    private static final int WIDE_LANE_BREAK = 20;
    private static final int AIR_CHECK_Y = 2;

    public static void apply(ChunkGenerator.ChunkData chunk,
                             int chunkX,
                             int chunkZ,
                             int height,
                             long seed,
                             Palette palette) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minSectorX = BackroomsSectorMath.sector(baseX) - 1;
        int maxSectorX = BackroomsSectorMath.sector(baseX + CHUNK_LAST) + 1;
        int minSectorZ = BackroomsSectorMath.sector(baseZ) - 1;
        int maxSectorZ = BackroomsSectorMath.sector(baseZ + CHUNK_LAST) + 1;
        BlockData lightBlock = palette.lightBlockData();

        for (int sectorX = minSectorX; sectorX <= maxSectorX; sectorX++) {
            for (int sectorZ = minSectorZ; sectorZ <= maxSectorZ; sectorZ++) {
                if (BackroomsSectorMath.isBlackoutSector(sectorX, sectorZ, seed)) {
                    continue;
                }

                placeSectorLights(chunk, baseX, baseZ, height, lightBlock, seed, sectorX, sectorZ);
            }
        }
    }

    private static void placeSectorLights(ChunkGenerator.ChunkData chunk,
                                          int baseX,
                                          int baseZ,
                                          int height,
                                          BlockData lightBlock,
                                          long seed,
                                          int sectorX,
                                          int sectorZ) {
        BackroomsRoomGrid.SectorPlan plan = BackroomsRoomGrid.plan(sectorX, sectorZ, seed);
        for (BackroomsRoomGrid.RoomRegion room : plan.rooms()) {
            if (room.width() < ROOM_MIN_SPAN || room.length() < ROOM_MIN_SPAN) {
                continue;
            }

            BackroomsRoomGrid.RoomRegion usableArea = room.inset(room.hallwayLike() ? HALLWAY_MARGIN : ROOM_MARGIN);
            if (usableArea == null) {
                continue;
            }

            if (room.hallwayLike()) {
                placeHallwayLights(chunk, baseX, baseZ, height, lightBlock, usableArea);
                continue;
            }

            placeRoomLights(chunk, baseX, baseZ, height, lightBlock, usableArea);
        }
    }

    private static void placeHallwayLights(ChunkGenerator.ChunkData chunk,
                                           int baseX,
                                           int baseZ,
                                           int height,
                                           BlockData lightBlock,
                                           BackroomsRoomGrid.RoomRegion room) {
        boolean horizontal = room.width() >= room.length();
        int majorSpan = horizontal ? room.width() : room.length();
        int fixtureCount = fixtureCount(majorSpan, HALLWAY_FIXTURE_SPACING, MAX_FIXTURES_PER_AXIS);
        int[] majorCenters = horizontal
                ? distributedCenters(room.minX(), room.maxX(), fixtureCount)
                : distributedCenters(room.minZ(), room.maxZ(), fixtureCount);
        int minorCenter = horizontal ? room.centerZ() : room.centerX();
        int fixtureLength = majorSpan >= LONG_HALL ? LONG_LIGHT : SHORT_LIGHT;

        if (horizontal) {
            for (int majorCenter : majorCenters) {
                placeFixture(chunk, baseX, baseZ, height, lightBlock, majorCenter, minorCenter, true, fixtureLength);
            }
            return;
        }

        for (int majorCenter : majorCenters) {
            placeFixture(chunk, baseX, baseZ, height, lightBlock, minorCenter, majorCenter, false, fixtureLength);
        }
    }

    private static void placeRoomLights(ChunkGenerator.ChunkData chunk,
                                        int baseX,
                                        int baseZ,
                                        int height,
                                        BlockData lightBlock,
                                        BackroomsRoomGrid.RoomRegion room) {
        boolean horizontal = room.width() >= room.length();
        int majorSpan = horizontal ? room.width() : room.length();
        int laneCount = laneCount(room);
        int fixtureCount = fixtureCount(
                majorSpan,
                majorSpan >= LARGE_ROOM ? LARGE_ROOM_FIXTURE_SPACING : ROOM_FIXTURE_SPACING,
                MAX_FIXTURES_PER_AXIS
        );
        int fixtureLength = majorSpan >= LARGE_ROOM ? LONG_LIGHT : SHORT_LIGHT;

        int[] laneCenters = horizontal
                ? distributedCenters(room.minZ(), room.maxZ(), laneCount)
                : distributedCenters(room.minX(), room.maxX(), laneCount);
        int[] majorCenters = horizontal
                ? distributedCenters(room.minX(), room.maxX(), fixtureCount)
                : distributedCenters(room.minZ(), room.maxZ(), fixtureCount);

        for (int laneIndex = 0; laneIndex < laneCenters.length; laneIndex++) {
            for (int fixtureIndex = 0; fixtureIndex < majorCenters.length; fixtureIndex++) {
                if (horizontal) {
                    placeFixture(
                            chunk,
                            baseX,
                            baseZ,
                            height,
                            lightBlock,
                            majorCenters[fixtureIndex],
                            laneCenters[laneIndex],
                            true,
                            fixtureLength
                    );
                    continue;
                }

                placeFixture(
                        chunk,
                        baseX,
                        baseZ,
                        height,
                        lightBlock,
                        laneCenters[laneIndex],
                        majorCenters[fixtureIndex],
                        false,
                        fixtureLength
                );
            }
        }
    }

    private static int laneCount(BackroomsRoomGrid.RoomRegion room) {
        int minorSpan = Math.min(room.width(), room.length());
        if (minorSpan >= WIDE_LANE_BREAK) {
            return 3;
        }
        if (minorSpan >= MID_LANE_BREAK) {
            return 2;
        }
        return 1;
    }

    private static int fixtureCount(int span, int spacing, int maxFixtures) {
        int count = Math.max(1, (span + (spacing - 1)) / spacing);
        return Math.min(count, maxFixtures);
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

    private static void placeFixture(ChunkGenerator.ChunkData chunk,
                                     int baseX,
                                     int baseZ,
                                     int height,
                                     BlockData lightBlock,
                                     int worldX,
                                     int worldZ,
                                     boolean horizontal,
                                     int length) {
        int startOffset = -((length - 1) / 2);
        for (int offset = 0; offset < length; offset++) {
            int fixtureX = horizontal ? worldX + startOffset + offset : worldX;
            int fixtureZ = horizontal ? worldZ : worldZ + startOffset + offset;
            placeLight(chunk, baseX, baseZ, height, lightBlock, fixtureX, fixtureZ);
        }
    }

    private static void placeLight(ChunkGenerator.ChunkData chunk,
                                   int baseX,
                                   int baseZ,
                                   int height,
                                   BlockData lightBlock,
                                   int worldX,
                                   int worldZ) {
        if (worldX < baseX || worldX > baseX + CHUNK_LAST || worldZ < baseZ || worldZ > baseZ + CHUNK_LAST) {
            return;
        }

        int localX = worldX - baseX;
        int localZ = worldZ - baseZ;
        if (chunk.getType(localX, height - AIR_CHECK_Y, localZ) != Material.AIR) {
            return;
        }

        chunk.setBlock(localX, height - 1, localZ, lightBlock);
    }

    private static int centerOf(int minInclusive, int maxInclusive) {
        int span = maxInclusive - minInclusive + 1;
        int midpoint = minInclusive + (span / 2);
        if ((span & 1) == 0) {
            midpoint -= 1;
        }
        return midpoint;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
