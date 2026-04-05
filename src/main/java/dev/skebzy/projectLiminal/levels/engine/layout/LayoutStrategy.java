package dev.skebzy.projectLiminal.levels.engine.layout;

public interface LayoutStrategy {
    boolean isOpen(int x, int z, long seed);
}