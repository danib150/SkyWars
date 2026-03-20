package com.gmail.filoghost.skywars.world.utils;

import com.gmail.filoghost.skywars.world.IntVector;
import com.gmail.filoghost.skywars.world.Region;
import org.bukkit.World;

public class ArenaCopyUtils {

    public static void copy(String arenaName, Region fromRegion, World toWorld, IntVector toPosition, Runnable onComplete) {
        long start = System.currentTimeMillis();

        SimpleClipboard clipboard = BlockCopyUtils.copyRegion(fromRegion);
        clipboard.paste(toWorld, toPosition, false);

        long end = System.currentTimeMillis();
        System.out.println("Time spent pasting arena " + arenaName + ": " + (end - start) + " ms");

        if (onComplete != null) {
            onComplete.run();
        }
    }

    public static void removeAllBlocks(Region region) {
        long start = System.currentTimeMillis();

        BlockCopyUtils.clearRegion(region);

        long end = System.currentTimeMillis();
        System.out.println("Time spent deleting cage: " + (end - start) + " ms");
    }
}