/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.gmail.filoghost.skywars.world;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.gmail.filoghost.skywars.settings.objects.ArenaConfig;
import com.gmail.filoghost.skywars.settings.objects.BlockConfig;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Region {
	
	@Getter @NonNull private World world;
	@Getter private IntVector minCorner, maxCorner;
	
	public static Region fromMinMaxCorners(World world, IntVector minCorner, IntVector maxCorner) {
		return new Region(world, minCorner, maxCorner);
	}
	
	public static Region fromRandomCorners(World world, BlockConfig corner1, BlockConfig corner2) {
		IntVector minCorner = new IntVector(Math.min(corner1.x, corner2.x), Math.min(corner1.y, corner2.y), Math.min(corner1.z, corner2.z));
		IntVector maxCorner = new IntVector(Math.max(corner1.x, corner2.x), Math.max(corner1.y, corner2.y), Math.max(corner1.z, corner2.z));
		return new Region(world, minCorner, maxCorner);
	}
	
	public static Region fromConfig(ArenaConfig config) {
		World world = Bukkit.getWorld(config.corner1.worldName);
		IntVector minCorner = new IntVector(Math.min(config.corner1.x, config.corner2.x), Math.min(config.corner1.y, config.corner2.y), Math.min(config.corner1.z, config.corner2.z));
		IntVector maxCorner = new IntVector(Math.max(config.corner1.x, config.corner2.x), Math.max(config.corner1.y, config.corner2.y), Math.max(config.corner1.z, config.corner2.z));
		return new Region(world, minCorner, maxCorner);
	}
	
	public void addPadding(int padding) {
		this.minCorner = minCorner.subtractAll(padding);
		this.maxCorner = maxCorner.addAll(padding);
	}
	
	/**
	 * Nota: si occupa già di caricare i chunk
	 */
	public void iterateChunks(Consumer<Chunk> consumer) {
		int minChunkX = minCorner.getX() >> 4;
		int minChunkZ = minCorner.getZ() >> 4;
		int maxChunkX = maxCorner.getX() >> 4;
		int maxChunkZ = maxCorner.getZ() >> 4;
		
		for (int x = minChunkX; x <= maxChunkX; x++) {
			for (int z = minChunkZ; z <= maxChunkZ; z++) {
				consumer.accept(world.getChunkAt(x, z)); // Nota: questo carica il chunk
			}
		}
	}
	
	
	public boolean isInside(Location loc) {
		return loc.getWorld() == world &&
			minCorner.getX() <= loc.getX() && loc.getX() <= maxCorner.getX() + 1 &&
			minCorner.getY() <= loc.getY() && loc.getY() <= maxCorner.getY() + 1 &&
			minCorner.getZ() <= loc.getZ() && loc.getZ() <= maxCorner.getZ() + 1;
	}

	
	public boolean isInside(Block block) {
		return block.getWorld() == world &&
			minCorner.getX() <= block.getX() && block.getX() <= maxCorner.getX() &&
			minCorner.getY() <= block.getY() && block.getY() <= maxCorner.getY() &&
			minCorner.getZ() <= block.getZ() && block.getZ() <= maxCorner.getZ();
	}
	
	public boolean isInside(BlockState tileEntity) {
		return tileEntity.getWorld() == world &&
			minCorner.getX() <= tileEntity.getX() && tileEntity.getX() <= maxCorner.getX() &&
			minCorner.getY() <= tileEntity.getY() && tileEntity.getY() <= maxCorner.getY() &&
			minCorner.getZ() <= tileEntity.getZ() && tileEntity.getZ() <= maxCorner.getZ();
	}
	
	public IntVector getSize() {
		return maxCorner.subtract(minCorner);
	}
	
	public int getMinX() {
		return minCorner.getX();
	}
	
	public int getMinY() {
		return minCorner.getY();
	}
	
	public int getMinZ() {
		return minCorner.getZ();
	}
	
	public int getMaxX() {
		return maxCorner.getX();
	}
	
	public int getMaxY() {
		return maxCorner.getY();
	}
	
	public int getMaxZ() {
		return maxCorner.getZ();
	}

}
