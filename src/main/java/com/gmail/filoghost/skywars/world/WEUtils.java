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

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.boydti.fawe.object.clipboard.WorldCopyClipboard;
import com.boydti.fawe.object.schematic.Schematic;
import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.regions.CuboidRegion;

public class WEUtils {
	
	@SuppressWarnings("resource")
	public static void copy(String arenaName, Region fromRegion, World toWorld, IntVector toPosition, Runnable onComplete) {
		TaskManager.IMP.async(() -> {
			long start = System.currentTimeMillis();
			EditSession copySession = getSession(fromRegion.getWorld());
			EditSession pasteSession = getSession(toWorld);
			CuboidRegion copyRegion = new CuboidRegion(toWEVector(fromRegion.getMinCorner()), toWEVector(fromRegion.getMaxCorner()));
			BlockArrayClipboard lazyCopy;
			
			try {
				// Metodo più personalizzabile
				boolean copyEntities = true;
				boolean copyBiomes = false;
				
				WorldCopyClipboard faweClipboard = new WorldCopyClipboard(copySession, copyRegion, copyEntities, copyBiomes);
				lazyCopy = new BlockArrayClipboard(copyRegion, faweClipboard);
				lazyCopy.setOrigin(copyRegion.getMinimumPoint());
			} catch (Throwable t) {
				t.printStackTrace();
				
				// Metodo alternativo, con entità
				lazyCopy = copySession.lazyCopy(copyRegion);
			}
			
			Schematic schematic = new Schematic(lazyCopy);
			boolean pasteAir = false;
			schematic.paste(pasteSession, toWEVector(toPosition), pasteAir);
			pasteSession.flushQueue();
			long end = System.currentTimeMillis();
			System.out.println("Time spent pasting arena " + arenaName + ": " + (end - start) + " ms");
			
			TaskManager.IMP.taskNowMain(onComplete);
		});
	}
	
	@SuppressWarnings("deprecation")
	public static void removeAllBlocks(Region region) {
		TaskManager.IMP.async(() -> {
			long start = System.currentTimeMillis();
			EditSession deleteSession = getSession(region.getWorld());
			CuboidRegion deleteRegion = new CuboidRegion(toWEVector(region.getMinCorner()), toWEVector(region.getMaxCorner()));
			
			deleteSession.setBlocks(deleteRegion, new BaseBlock(BlockID.AIR));
			deleteSession.flushQueue();
			long end = System.currentTimeMillis();
			System.out.println("Time spent deleting cage: " + (end - start) + " ms (is primary thread: " + Bukkit.isPrimaryThread() + ")");
		});
	}
	
	private static EditSession getSession(World world) {
		return new EditSessionBuilder(world.getName())
			.autoQueue(false)
			.limitUnlimited()
			.checkMemory(false)
			.changeSetNull()
			.fastmode(true)
			.build();
	}
	
	private static Vector toWEVector(IntVector intVector) {
		return new Vector(intVector.getX(), intVector.getY(), intVector.getZ());
	}

}
