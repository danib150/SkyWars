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
package com.gmail.filoghost.skywars.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.gmail.filoghost.skywars.Constants;
import com.gmail.filoghost.skywars.Perms;
import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.arena.entities.EntityOwnership;
import com.gmail.filoghost.skywars.arena.kit.KitsManager;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.arena.reward.KillReward;
import com.gmail.filoghost.skywars.utils.Utils;

import wild.api.WildCommons;
import wild.api.WildConstants;

public class InteractListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) // false perché RIGHT_CLICK_AIR è sempre cancellato di default
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();
		
		if (event.hasBlock() && event.hasItem() && action == Action.RIGHT_CLICK_BLOCK) {
			if (event.getItem().getType() == Material.MONSTER_EGG) {
				Arena arena = ArenasManager.getArenaByPlayer(player);
				
				if (arena != null && arena.isFightingPlayer(player)) {
					EntityType eggType = WildCommons.getEggType(event.getItem());
					
					if (eggType != null && arena.getSpawningManager().spawnMob(player, eggType, event.getClickedBlock(), event.getBlockFace())) {
						event.setCancelled(true);
						Utils.consumeOneItemInHand(player, event.getHand());
						return;
					}
				}
			}
		}
		
		if (event.hasBlock() && action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) event.getClickedBlock().getState();
			Arena arena = ArenasManager.getArenaByName(ChatColor.stripColor(sign.getLine(0)));
			if (arena != null) {
				event.setCancelled(true);
				arena.tryAddPlayer(player);
				return; // Non gestire le cose successive
			}
		}
			
		if (event.hasItem() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
			
			if (Constants.ITEM_TEAM_PICKER.isSimilar(event.getItem())) {
				Arena arena = ArenasManager.getArenaByPlayer(player);
				if (arena != null) {
					event.setCancelled(true);
					arena.getTeamSelectorMenu().open(player);
					return;
				}
				
			} else if (Constants.ITEM_KIT_PICKER.isSimilar(event.getItem())) {
				Arena arena = ArenasManager.getArenaByPlayer(player);
				if (arena != null) {
					event.setCancelled(true);
					KitsManager.openSelectorMenu(player);
					return;
				}
				
			} else if (Constants.ITEM_ARENA_PICKER.isSimilar(event.getItem())) {
				event.setCancelled(true);
				SkyWars.getArenaSelector().open(player);
				return;
				
			} else if (WildConstants.Spectator.TELEPORTER.isSimilar(event.getItem())) {
				Arena arena = ArenasManager.getArenaByPlayer(player);
				if (arena != null) {
					event.setCancelled(true);
					arena.getTeleporterMenu().open(player);
					return;
				}
				
			} else if (WildConstants.Spectator.QUIT_SPECTATING.isSimilar(event.getItem())) {
				Arena arena = ArenasManager.getArenaByPlayer(player);
				if (arena != null && arena.getPlayerStatus(player).getTeam() == null) {
					event.setCancelled(true);
					arena.removePlayerAndEventuallyProcessDeath(player);
					SkyWars.setupToLobby(player);
					player.sendMessage(ChatColor.GREEN + "Sei andato allo spawn.");
					return;
				}
				
			} else if (event.getItem().getType() == Material.FIREBALL) {
				Arena arena = ArenasManager.getArenaByPlayer(player);
				if (arena != null) {
					if (arena.getSpawningManager().throwFireball(player)) {
						event.setCancelled(true);
						Utils.consumeOneItemInHand(player, event.getHand());
						return;
					}
				}
				
			} else if (KillReward.isActivatorItem(event.getItem())) {
				if (ArenasManager.isFightingPlayer(player)) {
					event.setCancelled(true);
					Utils.consumeOneItemInHand(player, event.getHand());
					KillReward.giveRandomReward(player);
					return;
				}
			}
		}
		
		if (event.hasBlock()) {
			Arena arena = ArenasManager.getArenaByPlayer(player);
			if (arena != null) {
				arena.getEvents().onBlockInteract(event, player, event.getClickedBlock());
			} else {
				if (!player.hasPermission(Perms.BUILD)) {
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player) {
			Player shooterPlayer = (Player) event.getEntity().getShooter();
			Arena arena = ArenasManager.getArenaByPlayer(shooterPlayer);
			
			if (arena == null) {
				event.setCancelled(true);
				return;
			}
			
			PlayerStatus shooterStatus = arena.getFightingPlayerStatus(shooterPlayer);
			if (shooterStatus == null) {
				event.setCancelled(true);
				return;
			}
			
			EntityOwnership.set(event.getEntity(), arena, shooterStatus.getTeam(), shooterPlayer);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		Arena arena = ArenasManager.getArenaByPlayer(player);
		if (arena != null) {
			arena.getEvents().onInventoryOpen(event, player, event.getInventory());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemPickup(PlayerPickupItemEvent event) {
		WorldModifyManager.onPlayerModifyWorld(event, event.getPlayer(), event.getItem().getWorld());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		WorldModifyManager.onPlayerModifyWorld(event, event.getPlayer(), event.getItemDrop().getWorld());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemCreative(InventoryCreativeEvent event) {
		WorldModifyManager.onPlayerModifyWorld(event, (Player) event.getWhoClicked(), event.getWhoClicked().getWorld());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() == Material.POTION) {
			Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
				event.getPlayer().getInventory().remove(Material.GLASS_BOTTLE);
			});
		}
	}


}
