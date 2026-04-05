package dev.skebzy.projectLiminal.levels.level0;

import dev.skebzy.projectLiminal.levels.engine.palette.Palette;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;

public class Level0Palette implements Palette {

    @Override
    public Material wall() {
        return Material.STRIPPED_BAMBOO_BLOCK;
    }

    @Override
    public Material wallTrim() {
        return Material.BIRCH_PLANKS;
    }

    @Override
    public Material floor() {
        return Material.HAY_BLOCK;
    }

    @Override
    public Material ceiling() {
        return Material.POLISHED_DIORITE;
    }

    @Override
    public Material light() {
        return Material.REDSTONE_LAMP;
    }

    @Override
    public BlockData lightBlockData() {
        BlockData lightData = light().createBlockData();
        if (lightData instanceof Lightable lightable) {
            lightable.setLit(true);
        }
        return lightData;
    }
}
