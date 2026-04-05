package dev.skebzy.projectLiminal.levels.engine.layout.strategies;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;

public class GridLayout implements LayoutStrategy {

    @Override
    public boolean isOpen(int x, int z, long seed) {
        return (x % 16 != 0 && z % 16 != 0);
    }
}