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
package com.gmail.filoghost.skywars.arena.entities;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpawningManager {
	
	private final Arena arena;
	
	
	public boolean spawnMob(Player player, EntityType entityType, Block clickedBlock, BlockFace clickedFace) {
		PlayerStatus playerStatus = arena.getFightingPlayerStatus(player);
		if (playerStatus == null) {
			return false;
		}
		
		Block targetBlock = clickedBlock.getRelative(clickedFace);
		if (targetBlock.getType().isSolid()) {
			return false;
		}
		
		Entity entity = targetBlock.getWorld().spawnEntity(targetBlock.getLocation().add(0.5, 0, 0.5), entityType);
		EntityOwnership.set(entity, arena, playerStatus.getTeam(), player);
		entity.setCustomName(arena.formatEntity(entity.getType(), player.getName(), playerStatus.getTeam()));
		entity.setCustomNameVisible(true);
		return true;
	}
	
	
	public boolean onTntBeforeExplode(Player player, TNTPrimed tnt) {
		PlayerStatus playerStatus = arena.getFightingPlayerStatus(player);
		if (playerStatus == null) {
			return false;
		}
		
		EntityOwnership.set(tnt, arena, playerStatus.getTeam(), player);
		return true;
	}
	
	
	public boolean throwFireball(Player player) {
		PlayerStatus playerStatus = arena.getFightingPlayerStatus(player);
		if (playerStatus == null) {
			return false;
		}
		
		Fireball fireball = player.launchProjectile(Fireball.class);
		fireball.setBounce(false);
		fireball.setIsIncendiary(false);
		EntityOwnership.set(fireball, arena, playerStatus.getTeam(), player);
		return true;
	}


}
