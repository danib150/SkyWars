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
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.gmail.filoghost.skywars.Perms;
import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.arena.entities.EntityOwnership;
import com.gmail.filoghost.skywars.utils.PlayerDamageSource;
import com.gmail.filoghost.skywars.utils.Utils;

import wild.api.WildCommons;

public class PlayerListener implements Listener {
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFish(PlayerFishEvent event) {
		if (event.getCaught() instanceof Player) {
			event.setCancelled(true);
			
			Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
				FishHook fishingHook = WildCommons.getFishingHook(event.getPlayer());
				if (fishingHook != null && fishingHook.getTicksLived() > 0) {
					WildCommons.removeFishingHook(fishingHook);
				}
			});
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onLiquidFlow(BlockFromToEvent event) {
		Block from = event.getBlock();
		if (!from.isLiquid()) {
			event.setCancelled(true);
			return;
		}
		
		for (Arena arena : ArenasManager.getArenas()) {
			if (arena.getRegion().isInside(from)) {
				arena.getEvents().onFlow(event, event.getToBlock());
				return;
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBucketFill(PlayerBucketFillEvent event) {
		WorldModifyManager.onPlayerModifyBlock(event, event.getPlayer(), event.getBlockClicked());
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		WorldModifyManager.onPlayerModifyBlock(event, event.getPlayer(), event.getBlockClicked().getRelative(event.getBlockFace()));
//		if (!event.isCancelled()) {
//			Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
//				event.getPlayer().getInventory().remove(Material.BUCKET); // Rimuove il secchio dopo l'utilizzo
//			});
//		}
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		WorldModifyManager.onPlayerModifyBlock(event, event.getPlayer(), event.getBlock());
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		WorldModifyManager.onPlayerModifyBlock(event, player, block);
	}


	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onExplosion(EntityExplodeEvent event) {		
		EntityOwnership entityOwnership = EntityOwnership.get(event.getEntity());
		if (entityOwnership != null) {
			entityOwnership.getArena().getEvents().onExplosion(event, event.blockList());
		} else {
			// Metodo alternativo, cerca in base alla posizione
			Location location = event.getEntity().getLocation();
			
			for (Arena arena : ArenasManager.getArenas()) {
				if (arena.getRegion().isInside(location)) {
					arena.getEvents().onExplosion(event, event.blockList());
					return;
				}
			}
		}
	}
	
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBeforeExplosion(ExplosionPrimeEvent event) {
		if (!(event.getEntity() instanceof TNTPrimed)) {
			return;
		}
		
		TNTPrimed tnt = (TNTPrimed) event.getEntity();
		PlayerDamageSource playerSource = Utils.getPlayerDamageSource(tnt.getSource());
		
		if (playerSource == null) {
			return;
		}
		
		Arena arena = ArenasManager.getArenaByPlayer(playerSource.getPlayer());
		if (arena == null) {
			event.setCancelled(true);
			return;
		}
		
		boolean success = arena.getSpawningManager().onTntBeforeExplode(playerSource.getPlayer(), tnt);
		
		if (!success) {
			// Gli spettatori non possono accendere tnt
			event.setCancelled(true);
			return;
		}
	}
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHanging(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Hanging)) {
			return;
		}
			
		if (ArenasManager.isArenaWorld(event.getEntity().getWorld())) {
			return;
		}
		
		// Nel mondo principale si possono distruggere i quadri solo se è stato un giocatore con permessi
		PlayerDamageSource playerDamager = Utils.getPlayerDamageSource(event.getDamager());
		if (playerDamager == null || !playerDamager.getPlayer().hasPermission(Perms.BUILD)) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHangingBreak(HangingBreakEvent event) {
		if (ArenasManager.isArenaWorld(event.getEntity().getWorld())) {
			return;
		}
		
		// Nel mondo principale si possono distruggere i quadri solo se è stato un giocatore con permessi
		if (!(event instanceof HangingBreakByEntityEvent)) {
			event.setCancelled(true);
			return;
		}
		
		PlayerDamageSource playerBreaker = Utils.getPlayerDamageSource(((HangingBreakByEntityEvent) event).getRemover());
		if (playerBreaker == null || !playerBreaker.getPlayer().hasPermission(Perms.BUILD)) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHangingPlace(HangingPlaceEvent event) {
		WorldModifyManager.onPlayerModifyWorld(event, event.getPlayer(), event.getEntity().getWorld());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		WorldModifyManager.onPlayerModifyWorld(event, event.getPlayer(), event.getRightClicked().getWorld());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerManipulateArmorstand(PlayerArmorStandManipulateEvent event) {
		WorldModifyManager.onPlayerModifyWorld(event, event.getPlayer(), event.getRightClicked().getWorld());
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCraft(CraftItemEvent event) {
		if (!ArenasManager.isFightingPlayer((Player) event.getWhoClicked())) {
			event.setCancelled(true);
		}
	}

}
