package dev.skebzy.projectLiminal.levels.engine.palette;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public interface Palette {

    Material wall();

    default Material wallTrim() {
        return wall();
    }

    default Material wallAccent() {
        return wall();
    }

    Material floor();

    default Material floorEdge() {
        return floor();
    }

    Material ceiling();

    default Material ceilingEdge() {
        return ceiling();
    }

    Material light();

    default BlockData lightBlockData() {
        return light().createBlockData();
    }
}
