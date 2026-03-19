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

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class NatureListener implements Listener {
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void mobSpawn(CreatureSpawnEvent event) {
		// I mob non spawnano in modo naturale, solo tramite uova
		switch (event.getSpawnReason()) {
			case CUSTOM:
			case DISPENSE_EGG:
			case SPAWNER_EGG:
				return;
			case DEFAULT:
				if (event.getEntityType() == EntityType.ARMOR_STAND) {
					// Consenti caso eccezionale
					return;
				}
			default:
				event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void mobCombust(EntityCombustEvent event) {
		if (event instanceof EntityCombustByBlockEvent || event instanceof EntityCombustByEntityEvent) {
			// Da un blocco o da un'entità, va bene
			return;
		}
		
		if (event.getEntity() instanceof Projectile) {
			// I proiettili si possono incendiare
			return;
		}
		
		// Disabilita il danno del sole ai non morti (e forse altre cose di cui non sono a conoscenza)
		event.setCancelled(true);
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockPhysics(BlockPhysicsEvent event) {
		if (event.getBlock().isLiquid()) {
			// I liquidi devono scorrere
			return;
		}
		
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void foodLevelChange(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockBurn(BlockBurnEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockDispense(BlockDispenseEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockFade(BlockFadeEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockForm(BlockFormEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockGrow(BlockGrowEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockIgnite(BlockIgniteEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockSpread(BlockSpreadEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockFormByEntity(EntityBlockFormEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void blockDecay(LeavesDecayEvent event) {
		WorldModifyManager.onWorldChange(event, event.getBlock().getWorld());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void weather(WeatherChangeEvent event) {
		// Il meteo è disabilitato ovunque
		event.setCancelled(true);
	}

}
