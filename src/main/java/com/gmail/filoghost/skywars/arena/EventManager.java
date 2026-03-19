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
package com.gmail.filoghost.skywars.arena;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.entities.EntityOwnership;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.arena.player.Team;
import com.gmail.filoghost.skywars.arena.reward.KillReward;
import com.gmail.filoghost.skywars.database.PlayerData;
import com.gmail.filoghost.skywars.settings.MainSettings;
import com.gmail.filoghost.skywars.utils.PlayerDamageSource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wild.api.WildCommons;

@RequiredArgsConstructor
public class EventManager {
	
	private final Arena arena;
	private Map<Player, LastPlayerDamageSource> lastPlayerDamagers = new WeakHashMap<>();
	
	
	public void onModify(Cancellable event, Player player, Block block) {
		if (!arena.isFightingPlayer(player)) {
			event.setCancelled(true);
			return;
		}
	}
	
	public void onFlow(Cancellable event, Block to) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
		}
	}
	
	
	public void onBlockInteract(Cancellable event, Player player, Block clickedBlock) {
		if (!arena.isFightingPlayer(player)) {
			event.setCancelled(true);
			return;
		}
		
		if (!arena.getRegion().isInside(clickedBlock)) {
			event.setCancelled(true);
			return;
		}
	}
	
	
	public void onExplosion(Cancellable event, Collection<Block> blocks) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
	}
	
	
	public void onInventoryOpen(Cancellable event, Player player, Inventory inventory) {
		// Blocca solo gli inventari collegati a dei blocchi
		if (inventory.getHolder() instanceof BlockState && !arena.isFightingPlayer(player)) {
			event.setCancelled(true);
			return;
		}
	}

	
	public void onPlayerDamagedByAnything(EntityDamageEvent event, Player defender) {
		if (!arena.isFightingPlayer(defender)) {
			event.setCancelled(true);
			return;
		}
		
		long now = System.currentTimeMillis();		
		if (now - arena.getGameloop().getStartTime() < 3000) {
			System.out.println("Early damage in " + arena.getName() + " to " + defender + ": " + event.getDamage() + " from " + event.getCause().name());
		}
	}
	
	
	public void onPlayerDamagedByPlayer(Cancellable event, Player defenderPlayer, PlayerDamageSource attackerPlayerSource) {		
		PlayerStatus defenderStatus = arena.getFightingPlayerStatus(defenderPlayer);
		if (defenderStatus == null) {
			event.setCancelled(true);
			return;
		}
		
		Player attackerPlayer = attackerPlayerSource.getPlayer();
		Entity attackerEntity = attackerPlayerSource.getDamagerEntity();
		
		boolean allowSelfDamage = !(attackerEntity instanceof LivingEntity); // Consente danno solo da entità come frecce, TNT, fireball
		boolean allowFriendlyFire = false;

		if (defenderPlayer == attackerPlayer) {
			if (!allowSelfDamage) {
				event.setCancelled(true);
				return;
			}
		} else {
			PlayerStatus attackerStatus = arena.getFightingPlayerStatus(attackerPlayer);
			if (attackerStatus == null) {
				event.setCancelled(true);
				return;
			}
		
			if (attackerStatus.getTeam() == defenderStatus.getTeam() && !allowFriendlyFire) {
				event.setCancelled(true);
				return;
			}
			
			// Solo se attacker e defender sono diversi
			lastPlayerDamagers.put(defenderPlayer, new LastPlayerDamageSource(attackerPlayer.getUniqueId(), attackerEntity.getType(), System.currentTimeMillis()));
		}
	}
	
	
	public void onPlayerTargetedByEntity(Cancellable event, Player defenderPlayer, Entity attackerEntity) {
		onPlayerDamagedByEntity(event, defenderPlayer, attackerEntity);
	}
	
	
	public void onPlayerDamagedByEntity(Cancellable event, Player defenderPlayer, Entity attackerEntity) {
		PlayerStatus defenderStatus = arena.getFightingPlayerStatus(defenderPlayer);
		if (defenderStatus == null) {
			event.setCancelled(true);
			return;
		}
		
		EntityOwnership attackerOwnership = EntityOwnership.get(attackerEntity);
		if (attackerOwnership == null) {
			// Non ha ownership, può avere come target chiunque
			return;
		}
		
		// Se ha ownership, non può avere come target i propri alleati del team
		if (defenderStatus.getTeam() == attackerOwnership.getTeam()) {
			event.setCancelled(true);
			return;
		}
	}
	
	
	public void onEntityDamagedByPlayer(Cancellable event, Entity defenderEntity, PlayerDamageSource attackerPlayerSource) {		
		PlayerStatus playerStatus = arena.getFightingPlayerStatus(attackerPlayerSource.getPlayer());
		if (playerStatus == null) {
			event.setCancelled(true);
			return;
		}
		
		// Consenti di danneggiare le proprie entità
	}

	
	/**
	 * Quando un giocatore viene eliminato dalla partita, oppure esce
	 */
	public void onDeath(Player victim, boolean victimStillInsideArena) {
		PlayerStatus victimStatus = arena.getFightingPlayerStatus(victim);
		if (victimStatus == null) {
			// Può capitare se il giocatore esce dal gioco nella lobby o quando è spettatore
			return;
		}
		
		// Salva il team che aveva prima di morire
		Team victimTeam = victimStatus.getTeam();
		Location victimLocation = victim.getLocation();
		
		// Imposta spettatore
		victimStatus.setTeam(null);
		victimStatus.setSpectator(victim, true);
		arena.getTeleporterMenu().update();
		
		// Aggiorna la scoreboard
		int remainingPlayers = arena.countPlayersByTeam(victimTeam);
		arena.getScoreboard().updateTeamCount(victimTeam, remainingPlayers);
		arena.getScoreboard().updateTotalCount(arena.countPlayingPlayers());
		arena.getScoreboard().removeTeamColor(victim, victimTeam);

		KillerInfo killerInfo = getKillerFromLastPlayerDamage(victim);
		PlayerStatus killerStatus = killerInfo != null ? arena.getPlayerStatus(killerInfo.getKiller()) : null;
		
		// Nota: se il killer è uno spettatore (cioè non ha un team) perché è morto anche lui, non prende niente (anche per evitare farming di punti tramite azioni kamikaze)
		if (killerStatus != null && killerStatus.getTeam() != null) {
			Player killer = killerStatus.getPlayer();
			broadcastDeathMessage(victim, victimTeam, killerInfo, killerStatus.getTeam());
			
			arena.getScoreboard().addKill(killer);
			KillReward.giveActivatorItem(killer);
			
			PlayerData killerData = SkyWars.getOnlinePlayerData(killer);
			killerData.addKill(arena);
			arena.addPositiveScore(killer, killerData, MainSettings.score_kill_byTeamsAmount, MainSettings.score_kill_base);
			
		} else {
			// Se non c'è un killer vivo e giocante, annuncia la morte solo se la vittima non è uscita dall'arena.
			// Altrimenti c'è già il messaggio che annuncia dell'uscita del giocatore.
			if (victimStillInsideArena) {
				broadcastDeathMessage(victim, victimTeam, victim.getLastDamageCause());
			}
		}
		
		// Aggiorna il conteggio delle morti
		PlayerData victimData = SkyWars.getOnlinePlayerData(victim);
		victimData.addDeath(arena);
		arena.addNegativeScore(victim, victimData, MainSettings.score_death);
		
		// Drop oggetti e esperienza
		for (ItemStack item : victim.getInventory().getContents()) {
			if (item != null && item.getType() != Material.AIR) {
				victim.getWorld().dropItemNaturally(victimLocation, item);
			}
		}
		ExperienceOrb exp = victim.getWorld().spawn(victimLocation, ExperienceOrb.class);
		exp.setExperience(victim.getTotalExperience());
		
		if (victimStillInsideArena) {
			// Hack: la vita viene ripristinata e viene impedita la morte
			victim.setHealth(victim.getMaxHealth());
			victim.setVelocity(new Vector(0, 0, 0));
			doFakeRespawn(victim);
			
			// Dopo, altrimenti vengono puliti gli effetti
			WildCommons.sendTitle(victim, 5, 30, 5, ChatColor.RED + "Sei morto!", "");
			victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 1, true, false));
			victim.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 2 * 20, 1, true, false));
		}

		arena.getGameloop().checkWinners();
	}
	
	
	private KillerInfo getKillerFromLastPlayerDamage(Player victim) {
		LastPlayerDamageSource lastPlayerDamage = lastPlayerDamagers.get(victim);
		
		if (lastPlayerDamage == null || System.currentTimeMillis() - lastPlayerDamage.getTimestamp() > 10000) {
			return null;
		}
		
		Player lastPlayerDamager = Bukkit.getPlayer(lastPlayerDamage.getDamagerPlayerUUID());
		if (lastPlayerDamager == null || !lastPlayerDamager.isOnline()) {
			return null;
		}
		
		return new KillerInfo(lastPlayerDamager, lastPlayerDamage.getDamagerEntityType());
	}
	
	
	private void broadcastDeathMessage(Player victim, Team victimTeam, KillerInfo killerInfo, Team killerTeam) {
		String killerString;
		if (killerInfo.getDamagerEntityType() == EntityType.PLAYER) {
			killerString = arena.formatPlayer(killerInfo.getKiller(), killerTeam);
		} else {
			killerString = arena.formatEntity(killerInfo.getDamagerEntityType(), killerInfo.getKiller().getName(), killerTeam);
		}
		arena.broadcast(arena.formatPlayer(victim, victimTeam) + ChatColor.GRAY + " è stato ucciso da " + killerString);
	}
	
	private void broadcastDeathMessage(Player victim, Team victimTeam, EntityDamageEvent deathCause) {
		String deathMessage = "è morto";
		
		// E' proprio l'ultima causa di danno subito dal giocatore
		if (deathCause != null) {
			if (deathCause instanceof EntityDamageByEntityEvent) {
				Entity killer = ((EntityDamageByEntityEvent) deathCause).getDamager();
				if (killer.getType() == EntityType.PLAYER) {
					Player killerPlayer = (Player) killer;
					PlayerStatus killerStatus = arena.getFightingPlayerStatus(killerPlayer);
					
					deathMessage = "è stato ucciso da " + arena.formatPlayer(
							killerPlayer,
							killerStatus != null ? killerStatus.getTeam() : null);
				} else {
					EntityOwnership ownership = EntityOwnership.get(killer);
					
					deathMessage = "è stato ucciso da " + arena.formatEntity(
							killer.getType(),
							ownership != null ? ownership.getPlayerName() : null,
							ownership != null ? ownership.getTeam() : null);
				}
			} else if (deathCause.getCause() == DamageCause.VOID) {
				deathMessage = "è caduto nel vuoto";
				
			} else if (deathCause.getCause() == DamageCause.FIRE || deathCause.getCause() == DamageCause.FIRE_TICK || deathCause.getCause() == DamageCause.LAVA) {
				deathMessage = "è morto bruciato";
			}
		}
		
		arena.broadcast(arena.formatPlayer(victim, victimTeam) + ChatColor.GRAY + " " + deathMessage);
	}
	

	public void onRespawn(PlayerRespawnEvent event, Player player) {
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		arena.giveEquip(player, playerStatus);
		
		if (arena.getArenaStatus() == ArenaStatus.COMBAT || arena.getArenaStatus() == ArenaStatus.ENDING) {
			if (playerStatus.isSpectator()) {
				event.setRespawnLocation(arena.getLobby());
			} else {
				event.setRespawnLocation(playerStatus.getTeam().getSpawnPoint());
			}
		} else {
			event.setRespawnLocation(arena.getLobby());
		}
	}
	
	
	private void doFakeRespawn(Player player) {
		PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(player, null, false);
		onRespawn(respawnEvent, player);
		player.teleport(respawnEvent.getRespawnLocation(), TeleportCause.PLUGIN);
	}
	
	
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	public static class LastPlayerDamageSource {
		
		private UUID damagerPlayerUUID;
		private EntityType damagerEntityType;
		private long timestamp;

	}
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter
	public static class KillerInfo {
		
		private Player killer;
		private EntityType damagerEntityType;

	}

}
