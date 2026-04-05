package dev.skebzy.projectLiminal.levels.engine.palette;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public interface Palette {

    Material wall();

    default Material wallTrim() {
        return wall();
    }

    Material floor();
    Material ceiling();
    Material light();

    default BlockData lightBlockData() {
        return light().createBlockData();
    }
}
