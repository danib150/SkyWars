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
package com.gmail.filoghost.skywars.timer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;

public class SpectatorLocationCheckTimer extends BukkitRunnable {
	
	private static final int MAX_TOLERANCE = 50;
	private static final int BOUNCE_BACK_DISTANCE = 10;

	
	public SpectatorLocationCheckTimer start() {
		this.runTaskTimer(SkyWars.get(), 6, 50);
		return this;
	}
	
	@Override
	public void run() {
		for (Arena arena : ArenasManager.getArenas()) {
			int minX = 0, minZ = 0, maxX = 0, maxZ = 0;
			boolean initializedBorders = false;
			
			for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
				if (playerStatus.isSpectator()) {
					Player player = playerStatus.getPlayer();
					Location location = player.getLocation();
					boolean shouldTeleportBack = false;
					
					if (!initializedBorders) {
						minX = arena.getRegion().getMinX() - MAX_TOLERANCE;
						minZ = arena.getRegion().getMinZ() - MAX_TOLERANCE;
						maxX = arena.getRegion().getMaxX() + MAX_TOLERANCE;
						maxZ = arena.getRegion().getMaxZ() + MAX_TOLERANCE;
						initializedBorders = true;
					}
					
					if (location.getX() < minX) {
						shouldTeleportBack = true;
						location.setX(minX + BOUNCE_BACK_DISTANCE);
					} else if (location.getX() > maxX) {
						shouldTeleportBack = true;
						location.setX(maxX - BOUNCE_BACK_DISTANCE);
					}
					
					if (location.getZ() < minZ) {
						shouldTeleportBack = true;
						location.setZ(minZ + BOUNCE_BACK_DISTANCE);
					} else if (location.getZ() > maxZ) {
						shouldTeleportBack = true;
						location.setZ(maxZ - BOUNCE_BACK_DISTANCE);
					}
					
					if (shouldTeleportBack) {
						player.teleport(location, TeleportCause.PLUGIN);
						player.sendMessage(ChatColor.RED + "Non puoi allontanarti dall'arena.");
					}
				}
			}
		}
	}
	
}
