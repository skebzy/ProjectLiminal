package dev.skebzy.projectLiminal.levels.engine.layout.strategies;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;
import dev.skebzy.projectLiminal.levels.engine.layout.grid.BackroomsRoomGrid;
import dev.skebzy.projectLiminal.levels.engine.layout.grid.BackroomsSectorMath;

public class NoiseLayout implements LayoutStrategy {

    private int cachedSectorX = Integer.MIN_VALUE;
    private int cachedSectorZ = Integer.MIN_VALUE;
    private long cachedSeed = Long.MIN_VALUE;
    private BackroomsRoomGrid.SectorPlan cachedPlan;

    public NoiseLayout() {
    }

    public NoiseLayout(double threshold, int scale) {
        this();
    }

    @Override
    public boolean isOpen(int x, int z, long seed) {
        int sectorX = BackroomsSectorMath.sector(x);
        int sectorZ = BackroomsSectorMath.sector(z);

        refreshCache(sectorX, sectorZ, seed);

        return cachedPlan.isOpenLocal(
                BackroomsSectorMath.local(x),
                BackroomsSectorMath.local(z)
        );
    }

    private void refreshCache(int sectorX, int sectorZ, long seed) {
        if (cachedPlan != null
                && cachedSectorX == sectorX
                && cachedSectorZ == sectorZ
                && cachedSeed == seed) {
            return;
        }

        cachedSectorX = sectorX;
        cachedSectorZ = sectorZ;
        cachedSeed = seed;
        cachedPlan = BackroomsRoomGrid.plan(sectorX, sectorZ, seed);
    }
}
