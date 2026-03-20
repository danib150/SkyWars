package com.gmail.filoghost.skywars.world.utils;

import com.gmail.filoghost.skywars.world.IntVector;
import com.gmail.filoghost.skywars.world.Region;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockCopyUtils {

    public static SimpleClipboard copyRegion(Region region) {
        IntVector min = region.getMinCorner();
        IntVector max = region.getMaxCorner();
        World world = region.getWorld();

        int width = max.getX() - min.getX() + 1;
        int height = max.getY() - min.getY() + 1;
        int length = max.getZ() - min.getZ() + 1;

        SimpleClipboard clipboard = new SimpleClipboard(width, height, length);

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {

                    Block block = world.getBlockAt(x, y, z);

                    clipboard.addBlock(new SavedBlock(
                            x - min.getX(),
                            y - min.getY(),
                            z - min.getZ(),
                            block.getType(),
                            block.getData()
                    ));
                }
            }
        }

        return clipboard;
    }

    public static void clearRegion(Region region) {
        IntVector min = region.getMinCorner();
        IntVector max = region.getMaxCorner();
        World world = region.getWorld();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    world.getBlockAt(x, y, z).setType(org.bukkit.Material.AIR, false);
                }
            }
        }
    }
}