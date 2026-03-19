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

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;

import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.arena.entities.EntityOwnership;
import com.gmail.filoghost.skywars.utils.DummyCancellable;
import com.gmail.filoghost.skywars.utils.PlayerDamageSource;
import com.gmail.filoghost.skywars.utils.Utils;

import wild.api.WildCommons;

public class DamageListener implements Listener {
	
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER) {
			return;
		}
		
		// Il vuoto fa sempre il massimo danno possibile e non viene mai cancellato
		if (event.getCause() == DamageCause.VOID) {
			event.setDamage(10000);
			return;
		}
		
		// --------------------------------------------------------------------------
		// GIOCATORE danneggiato da QUALUNQUE cosa
		// --------------------------------------------------------------------------
		Player damagedPlayer = (Player) event.getEntity();
		Arena arena = ArenasManager.getArenaByPlayer(damagedPlayer);
		
		if (arena == null) {
			event.setCancelled(true);
			return;
		}
		
		arena.getEvents().onPlayerDamagedByAnything(event, damagedPlayer);
	}
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		processAnythingDamagedByAnything(event, event.getEntity(), event.getDamager());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		ThrownPotion thrownPotion = event.getPotion();
		boolean hasBadEffect = false;	
	
		for (PotionEffect effect : thrownPotion.getEffects()) {
			if (WildCommons.isBadPotionEffect(effect.getType())) {
				hasBadEffect = true;
				break;
			}
		}
		
		if (!hasBadEffect) {
			// Colpisce tutti sempre
			return;
		}
		
		for (LivingEntity affectedEntity : event.getAffectedEntities()) {
			DummyCancellable cancellableResult = new DummyCancellable();
			processAnythingDamagedByAnything(cancellableResult, affectedEntity, thrownPotion);
			if (cancellableResult.isCancelled()) {
				event.setIntensity(affectedEntity, 0.0);
			}
		}		
	}
	
	
	private void processAnythingDamagedByAnything(Cancellable event, Entity defenderEntity, Entity attackerEntity) {
		PlayerDamageSource playerDamageSource = Utils.getPlayerDamageSource(attackerEntity);
		
		if (defenderEntity.getType() == EntityType.PLAYER) {
			// GIOCATORE danneggiato
			Player defenderPlayer = (Player) defenderEntity;
			
			Arena defenderArena = ArenasManager.getArenaByPlayer(defenderPlayer);
			if (defenderArena == null) {
				// Il defender non sta combattendo
				event.setCancelled(true);
				return;
			}
			
			if (playerDamageSource != null) {
				// --------------------------------------------------------------------------
				// GIOCATORE danneggiato da GIOCATORE (anche tramite entità)
				// --------------------------------------------------------------------------
				Arena attackerArena = ArenasManager.getArenaByPlayer(playerDamageSource.getPlayer());
				if (defenderArena != attackerArena) {
					// Arene diverse!? Solo lo staff può causare questa condizione
					event.setCancelled(true);
					return;
				}

				defenderArena.getEvents().onPlayerDamagedByPlayer(event, defenderPlayer, playerDamageSource);

			} else {
				// --------------------------------------------------------------------------
				// GIOCATORE danneggiato da ENTITA' (indipendente o di giocatore uscito)
				// --------------------------------------------------------------------------
				defenderArena.getEvents().onPlayerDamagedByEntity(event, defenderPlayer, attackerEntity);
			}
		} else {
			// ENTITA' danneggiata

			if (playerDamageSource != null) {
				// --------------------------------------------------------------------------
				// ENTITA' danneggiata da GIOCATORE (anche tramite entità)
				// --------------------------------------------------------------------------
				EntityOwnership defenderOwnership = EntityOwnership.get(defenderEntity);
				if (defenderOwnership != null) {
					defenderOwnership.getArena().getEvents().onEntityDamagedByPlayer(event, defenderEntity, playerDamageSource);
				}
			} else {
				// --------------------------------------------------------------------------
				// ENTITA' danneggiata da ENTITA' (indipendente o di giocatore uscito)
				// --------------------------------------------------------------------------
				
				// Ok sempre, anche se sono dello stesso team
			}
		}
	}

	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityTargetEntity(EntityTargetLivingEntityEvent event) {
		Entity defenderEntity = event.getTarget();
		Entity attackerEntity = event.getEntity();
		
		EntityOwnership attackerOwnership = EntityOwnership.get(event.getEntity());
		if (attackerOwnership == null) {
			// Non ha ownership, può avere come target chiunque
			return;
		}
		
		// Se ha ownership, non può avere come target i propri alleati del team
		if (attackerOwnership != null) {
			if (defenderEntity instanceof Player) {
				// --------------------------------------------------------------------------
				// GIOCATORE preso di mira da ENTITA'
				// --------------------------------------------------------------------------
				attackerOwnership.getArena().getEvents().onPlayerTargetedByEntity(event, (Player) defenderEntity, attackerEntity);
			} else {
				// --------------------------------------------------------------------------
				// ENTITA' presa di mira da ENTITA' (indipendente o di giocatore uscito)
				// --------------------------------------------------------------------------
				
				// Ok sempre, anche se sono dello stesso team
			}
		}
	}
	
}
