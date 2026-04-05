package dev.skebzy.projectLiminal.levels.engine.paint.decorators;

import dev.skebzy.projectLiminal.levels.engine.layout.strategies.BackroomsSectorMath;
import dev.skebzy.projectLiminal.levels.engine.layout.strategies.GridRoomMath;
import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator;

public class LightPlacer {

    private static final int ROOM_MARGIN = 2;
    private static final int HALLWAY_MAX_WIDTH = 6;

    public static void apply(ChunkGenerator.ChunkData chunk,
                             int chunkX,
                             int chunkZ,
                             int height,
                             long seed,
                             Palette palette) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minSectorX = BackroomsSectorMath.sector(baseX) - 1;
        int maxSectorX = BackroomsSectorMath.sector(baseX + 15) + 1;
        int minSectorZ = BackroomsSectorMath.sector(baseZ) - 1;
        int maxSectorZ = BackroomsSectorMath.sector(baseZ + 15) + 1;
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
        int sectorStartX = sectorX * BackroomsSectorMath.SECTOR_SIZE;
        int sectorStartZ = sectorZ * BackroomsSectorMath.SECTOR_SIZE;

        int[] xRanges = GridRoomMath.xRanges(sectorX, sectorZ, seed);
        int[] zRanges = GridRoomMath.zRanges(sectorX, sectorZ, seed);

        for (int xIndex = 0; xIndex < xRanges.length; xIndex += 2) {
            for (int zIndex = 0; zIndex < zRanges.length; zIndex += 2) {
                int roomMinX = sectorStartX + xRanges[xIndex];
                int roomMaxX = sectorStartX + xRanges[xIndex + 1] - 1;
                int roomMinZ = sectorStartZ + zRanges[zIndex];
                int roomMaxZ = sectorStartZ + zRanges[zIndex + 1] - 1;

                int width = roomMaxX - roomMinX + 1;
                int length = roomMaxZ - roomMinZ + 1;
                if (width < 5 || length < 5) {
                    continue;
                }

                RoomBounds bounds = inset(roomMinX, roomMaxX, roomMinZ, roomMaxZ, roomMargin(width, length));
                if (bounds == null) {
                    continue;
                }

                long signature = roomSignature(roomMinX, roomMinZ, width, length, seed);
                if (isHallway(width, length)) {
                    placeHallwayLights(chunk, baseX, baseZ, height, lightBlock, bounds, width, length, signature);
                } else {
                    placeRoomLights(chunk, baseX, baseZ, height, lightBlock, bounds, width, length, signature);
                }
            }
        }
    }

    private static void placeHallwayLights(ChunkGenerator.ChunkData chunk,
                                           int baseX,
                                           int baseZ,
                                           int height,
                                           BlockData lightBlock,
                                           RoomBounds bounds,
                                           int width,
                                           int length,
                                           long signature) {
        boolean horizontal = width >= length;
        int centerX = centerOf(bounds.minX, bounds.maxX);
        int centerZ = centerOf(bounds.minZ, bounds.maxZ);
        int spacing = 5 + hash(signature, 3, 2);

        if (horizontal) {
            int startX = centeredStart(bounds.minX, bounds.maxX, spacing, centerX);
            for (int worldX = startX; worldX <= bounds.maxX; worldX += spacing) {
                placeLight(chunk, baseX, baseZ, height, lightBlock, worldX, centerZ);
            }
            return;
        }

        int startZ = centeredStart(bounds.minZ, bounds.maxZ, spacing, centerZ);
        for (int worldZ = startZ; worldZ <= bounds.maxZ; worldZ += spacing) {
            placeLight(chunk, baseX, baseZ, height, lightBlock, centerX, worldZ);
        }
    }

    private static void placeRoomLights(ChunkGenerator.ChunkData chunk,
                                        int baseX,
                                        int baseZ,
                                        int height,
                                        BlockData lightBlock,
                                        RoomBounds bounds,
                                        int width,
                                        int length,
                                        long signature) {
        boolean horizontal = width >= length;
        int centerX = centerOf(bounds.minX, bounds.maxX);
        int centerZ = centerOf(bounds.minZ, bounds.maxZ);

        int major = Math.max(width, length);
        int minor = Math.min(width, length);
        int laneCount = laneCount(width, length, signature);
        int majorSpacing = majorSpacing(major, signature);
        int minorSpacing = minorSpacing(minor, signature);

        if (laneCount == 1) {
            placeSingleLane(
                    chunk, baseX, baseZ, height, lightBlock,
                    bounds, horizontal, centerX, centerZ, majorSpacing
            );
            return;
        }

        int[] laneCenters = laneCenters(horizontal ? centerZ : centerX, laneCount, minorSpacing,
                horizontal ? bounds.minZ : bounds.minX,
                horizontal ? bounds.maxZ : bounds.maxX);

        for (int laneCenter : laneCenters) {
            if (horizontal) {
                placeSingleLane(chunk, baseX, baseZ, height, lightBlock, bounds, true, centerX, laneCenter, majorSpacing);
            } else {
                placeSingleLane(chunk, baseX, baseZ, height, lightBlock, bounds, false, laneCenter, centerZ, majorSpacing);
            }
        }
    }

    private static void placeSingleLane(ChunkGenerator.ChunkData chunk,
                                        int baseX,
                                        int baseZ,
                                        int height,
                                        BlockData lightBlock,
                                        RoomBounds bounds,
                                        boolean horizontal,
                                        int centerX,
                                        int centerZ,
                                        int spacing) {
        if (horizontal) {
            int startX = centeredStart(bounds.minX, bounds.maxX, spacing, centerX);
            for (int worldX = startX; worldX <= bounds.maxX; worldX += spacing) {
                placeLight(chunk, baseX, baseZ, height, lightBlock, worldX, centerZ);
            }
            return;
        }

        int startZ = centeredStart(bounds.minZ, bounds.maxZ, spacing, centerZ);
        for (int worldZ = startZ; worldZ <= bounds.maxZ; worldZ += spacing) {
            placeLight(chunk, baseX, baseZ, height, lightBlock, centerX, worldZ);
        }
    }

    private static int laneCount(int width, int length, long signature) {
        int minor = Math.min(width, length);
        if (minor >= 16) {
            return 3;
        }
        if (minor >= 11) {
            return 2 + hash(signature, 11, 2);
        }
        if (minor >= 8 && hash(signature, 17, 100) < 35) {
            return 2;
        }
        return 1;
    }

    private static int majorSpacing(int major, long signature) {
        if (major >= 20) {
            return 5 + hash(signature, 23, 2);
        }
        if (major >= 12) {
            return 4 + hash(signature, 29, 2);
        }
        return 4;
    }

    private static int minorSpacing(int minor, long signature) {
        if (minor >= 16) {
            return 4 + hash(signature, 31, 2);
        }
        return 3 + hash(signature, 37, 2);
    }

    private static int[] laneCenters(int center, int count, int spacing, int min, int max) {
        if (count <= 1) {
            return new int[]{clamp(center, min, max)};
        }
        if (count == 2) {
            return new int[]{
                    clamp(center - (spacing / 2), min, max),
                    clamp(center + (spacing - (spacing / 2)), min, max)
            };
        }
        return new int[]{
                clamp(center - spacing, min, max),
                clamp(center, min, max),
                clamp(center + spacing, min, max)
        };
    }

    private static void placeLight(ChunkGenerator.ChunkData chunk,
                                   int baseX,
                                   int baseZ,
                                   int height,
                                   BlockData lightBlock,
                                   int worldX,
                                   int worldZ) {
        if (worldX < baseX || worldX > baseX + 15 || worldZ < baseZ || worldZ > baseZ + 15) {
            return;
        }

        int localX = worldX - baseX;
        int localZ = worldZ - baseZ;
        if (chunk.getType(localX, height - 2, localZ) != Material.AIR) {
            return;
        }

        chunk.setBlock(localX, height - 1, localZ, lightBlock);
    }

    private static boolean isHallway(int width, int length) {
        int smaller = Math.min(width, length);
        int larger = Math.max(width, length);
        return smaller <= HALLWAY_MAX_WIDTH && larger >= 9;
    }

    private static int roomMargin(int width, int length) {
        return Math.min(width, length) >= 11 ? ROOM_MARGIN : 1;
    }

    private static RoomBounds inset(int minX, int maxX, int minZ, int maxZ, int margin) {
        int safeMinX = minX + margin;
        int safeMaxX = maxX - margin;
        int safeMinZ = minZ + margin;
        int safeMaxZ = maxZ - margin;
        if (safeMinX > safeMaxX || safeMinZ > safeMaxZ) {
            return null;
        }
        return new RoomBounds(safeMinX, safeMaxX, safeMinZ, safeMaxZ);
    }

    private static int centeredStart(int min, int max, int spacing, int center) {
        int steps = Math.floorDiv(center - min, spacing);
        return center - (steps * spacing);
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

    private static int hash(long signature, long salt, int bound) {
        long mixed = signature ^ salt;
        mixed ^= (mixed >>> 33);
        mixed *= 0xFF51AFD7ED558CCDL;
        mixed ^= (mixed >>> 33);
        return Math.floorMod((int) mixed, bound);
    }

    private static long roomSignature(int roomMinX, int roomMinZ, int width, int length, long seed) {
        long signature = seed;
        signature ^= roomMinX * 73428767L;
        signature ^= roomMinZ * 912931L;
        signature ^= width * 19349663L;
        signature ^= length * 83492791L;
        return signature;
    }
    private record RoomBounds(int minX, int maxX, int minZ, int maxZ) {
    }
}
