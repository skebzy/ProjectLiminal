package dev.skebzy.projectLiminal.levels.level0;

import dev.skebzy.projectLiminal.levels.engine.layout.LayoutStrategy;
import dev.skebzy.projectLiminal.levels.engine.layout.strategies.NoiseLayout;

public class Level0Config {

    public static final int HEIGHT = 5;
    public static final long LAYOUT_SEED_SALT = 0x6C6576656C304CL;
    public static final long FEATURE_SEED_SALT = 0x726F6F6D734630L;

    public static final LayoutStrategy LAYOUT =
            new NoiseLayout(0.35, 4);

    public static final boolean LIGHTS = true;
    public static final boolean PILLARS = false;
    public static final boolean DECAY = false;
}
