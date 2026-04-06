package dev.skebzy.projectLiminal.levels.engine.layout.strategies;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;

public class GridLayout implements LayoutStrategy {

    private static final int GRID_SIZE = 16;

    @Override
    public boolean isOpen(int x, int z, long seed) {
        return (x % GRID_SIZE != 0 && z % GRID_SIZE != 0);
    }
}
