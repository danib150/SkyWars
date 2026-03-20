package com.gmail.filoghost.skywars.world.utils;

public class SavedBlock {

    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;
    private final org.bukkit.Material material;
    private final byte data;

    public SavedBlock(int offsetX, int offsetY, int offsetZ, org.bukkit.Material material, byte data) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.material = material;
        this.data = data;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    public org.bukkit.Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }
}