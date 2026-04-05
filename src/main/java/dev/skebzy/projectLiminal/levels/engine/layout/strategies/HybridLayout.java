package dev.skebzy.projectLiminal.levels.engine.layout.strategies;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;

public class HybridLayout implements LayoutStrategy {

    private final LayoutStrategy a;
    private final LayoutStrategy b;

    public HybridLayout(LayoutStrategy a, LayoutStrategy b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean isOpen(int x, int z, long seed) {
        return a.isOpen(x, z, seed) && b.isOpen(x, z, seed);
    }
}