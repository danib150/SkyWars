package com.gmail.filoghost.skywars.world.utils;

import com.gmail.filoghost.skywars.world.IntVector;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class SimpleClipboard {

    private final List<SavedBlock> blocks = new ArrayList<>();
    private final int width;
    private final int height;
    private final int length;

    public SimpleClipboard(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;
    }

    public List<SavedBlock> getBlocks() {
        return blocks;
    }

    public void addBlock(SavedBlock block) {
        blocks.add(block);
    }

    public void paste(World world, IntVector origin, boolean pasteAir) {
        for (SavedBlock saved : blocks) {
            if (!pasteAir && saved.getMaterial() == Material.AIR) {
                continue;
            }

            Block block = world.getBlockAt(
                    origin.getX() + saved.getOffsetX(),
                    origin.getY() + saved.getOffsetY(),
                    origin.getZ() + saved.getOffsetZ()
            );

            block.setType(saved.getMaterial(), false);
            block.setData(saved.getData(), false);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }
}