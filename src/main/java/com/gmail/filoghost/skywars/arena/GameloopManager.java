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

import java.util.Collections;
import java.util.List;

import com.gmail.filoghost.skywars.world.utils.ArenaCopyUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.kit.Kit;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.arena.player.Team;
import com.gmail.filoghost.skywars.database.PlayerData;
import com.gmail.filoghost.skywars.database.SQLManager;
import com.gmail.filoghost.skywars.settings.MainSettings;
import com.gmail.filoghost.skywars.timer.CountdownTimer;
import com.gmail.filoghost.skywars.utils.Utils;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wild.api.WildCommons;
import wild.api.scheduler.Countdowns;
import wild.api.util.UnitFormatter;

@RequiredArgsConstructor
public class GameloopManager {
	
	private static final Vector VECTOR_ZERO = new Vector(0, 0, 0);
	
	private final Arena arena;
	
	@Getter private CountdownTimer lobbyCountdownTimer;
	@Getter private CountdownTimer combatCountdownTimer;
	
	@Getter private long startTime;

	public void forceStart() {
		if (arena.getArenaStatus() != ArenaStatus.LOBBY) {
			return;
		}

		cancelLobbyCountdown();
		finishLobbyCountdown();
	}

	private void finishNoWinner() {
		arena.setArenaStatus(ArenaStatus.ENDING);
		cancelCombatCountdown();
		arena.broadcast(ChatColor.GRAY + "La partita è terminata senza vincitori.");
		startWinCountdown();
	}

	public void checkWinners() {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			Utils.reportAnomaly("checking winners in wrong arena status", this, arena.getArenaStatus());
			return;
		}
		
		Team winnerTeam = null;
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (!arena.isFightingPlayerStatus(playerStatus)) {
				continue;
			}
			
			if (winnerTeam == null) {
				winnerTeam = playerStatus.getTeam();
			} else {
				if (winnerTeam != playerStatus.getTeam()) {
					// Almeno un team diverso, si sta ancora combattendo
					return;
				}
			}
		}

		if (winnerTeam == null) {
			finishNoWinner();
			return;
		}
		
		// Un team ha vinto se siamo arrivati qui!
		long duration = System.currentTimeMillis() - startTime;
		SQLManager.insertAnalyticsAsync("duration", String.valueOf(duration), arena);
		
		arena.setArenaStatus(ArenaStatus.ENDING);
		
		if (arena.isTeamsMode()) {
			arena.broadcast(ChatColor.GRAY + "Il team " + winnerTeam.getChatColor() + winnerTeam.getNameSingular() + ChatColor.GRAY + " ha vinto la partita!");
		} else {
			for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
				Player player = playerStatus.getPlayer();
				
				if (playerStatus.getTeam() == winnerTeam) {
					arena.broadcast(arena.formatPlayer(player, winnerTeam) + ChatColor.GRAY + " ha vinto la partita!");
				}
			}
		}
			
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			Player player = playerStatus.getPlayer();
			
			if (playerStatus.getTeam() == winnerTeam) {
				PlayerData playerData = SkyWars.getOnlinePlayerData(player);
				playerData.addWin(arena);
				arena.addPositiveScore(player, playerData, MainSettings.score_win_byTeamsAmount, MainSettings.score_win_base);
			}
			
			if (player.isDead()) {
				WildCommons.respawn(player); // Respawna i giocatori morti per evitare crash del client
			}
		}
		
		cancelCombatCountdown();
		startWinCountdown();
	}
	
	
	/*
	 * 
	 *			 _         ____    ____    ____   __     __
	 *			| |       / __ \  |  _ \  |  _ \  \ \   / /
	 *			| |      | |  | | | |_) | | |_) |  \ \_/ /
	 *			| |      | |  | | |  _ <  |  _ <    \   /
	 *			| |____  | |__| | | |_) | | |_) |    | |
	 *			|______|  \____/  |____/  |____/     |_|
	 * 
	 * 
	 */
	public void startLobbyCountdown() {
		lobbyCountdownTimer = new CountdownTimer(MainSettings.countdown_start, this::onLobbyCountdown, this::finishLobbyCountdown).start();
	}
	
	public void cancelLobbyCountdown() {
		if (lobbyCountdownTimer != null) {
			lobbyCountdownTimer.cancel();
			lobbyCountdownTimer = null;
		}
		arena.getScoreboard().unsetLobbyCountdown();
	}
	
	private void onLobbyCountdown(int seconds) {
		if (Countdowns.shouldAnnounceCountdown(seconds)) {
			Countdowns.announceStartingCountdown(SkyWars.PREFIX, arena.getPlayers(), seconds);
        }
		arena.getScoreboard().setLobbyCountdown(seconds);
	}
	
	private void finishLobbyCountdown() {
		lobbyCountdownTimer = null;
		
		// Segna i giocatori senza team
		List<PlayerStatus> noTeamPlayers = Lists.newArrayList();
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (playerStatus.getTeam() == null) {
				noTeamPlayers.add(playerStatus);
			}
		}
		Collections.shuffle(noTeamPlayers);
		
		// Assegna un team a tutti
		for (PlayerStatus playerStatus : noTeamPlayers) {
			playerStatus.setTeam(arena.findLowestPlayersTeam());
		}
		
		arena.getScoreboard().displayGame(arena.isTeamsMode(), arena.getTeams());
		for (Team team : arena.getTeams()) {
			arena.getScoreboard().updateTeamCount(team, arena.countPlayersByTeam(team));
		}
		arena.getScoreboard().updateTotalCount(arena.countPlayingPlayers());
		
		// Manda i giocatori ai rispettivi spawn (prima di iniziare la partita)
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			Player player = playerStatus.getPlayer();
			Team team = playerStatus.getTeam();
			
			player.setFallDistance(0.0f);
			player.setVelocity(VECTOR_ZERO);
			player.teleport(team.getSpawnPoint(), TeleportCause.PLUGIN);
		}
		
		// Imposta lo stato su combat
		arena.setArenaStatus(ArenaStatus.COMBAT);
		System.out.println("Arena started: " + arena.getName());
		
		// Prepara i giocatori
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			Player player = playerStatus.getPlayer();
			Team team = playerStatus.getTeam();
			Kit kit = playerStatus.getSelectedKit();
			
			arena.giveEquip(player, playerStatus);
			player.getEnderChest().clear();
			arena.getScoreboard().addTeamColor(player, team);
			arena.getScoreboard().displayKills(player, 0);
			if (player.getOpenInventory() != null) {
				player.getOpenInventory().close();
			}
			
			if (kit != null) {
				PlayerData playerData = SkyWars.getOnlinePlayerData(player);
				if (playerData.getScore() >= kit.getPrice()) {
					playerData.subtractScore(kit.getPrice());
					kit.apply(player);
				} else {
					player.sendMessage(ChatColor.RED + "Errore: non hai più i punti necessari per pagare il kit.");
				}
			}
		}
		
		arena.refreshSign();
		arena.getTeleporterMenu().update();
		
		arena.scanInitialChests();
		arena.refillChests();

		ArenaCopyUtils.removeAllBlocks(arena.getCageRegion());

		startTime = System.currentTimeMillis();
		Countdowns.announceEndedCountdown(SkyWars.PREFIX, arena.getPlayers());
		startCombatCountdown();
	}
	
	
	
	
	/*
	 * 
	 *			  _____    ____    __  __   ____               _______
	 *			 / ____|  / __ \  |  \/  | |  _ \      /\     |__   __|
	 *			| |      | |  | | | \  / | | |_) |    /  \       | |
	 *			| |      | |  | | | |\/| | |  _ <    / /\ \      | |
	 *			| |____  | |__| | | |  | | | |_) |  / ____ \     | |
	 *			 \_____|  \____/  |_|  |_| |____/  /_/    \_\    |_|
     * 
	 * 
	 */
	private void startCombatCountdown() {
		combatCountdownTimer = new CountdownTimer(MainSettings.countdown_game, this::onCombatCountdown, this::finishCombatCountdown).start();
	}
	
	private void cancelCombatCountdown() {
		if (combatCountdownTimer != null) {
			combatCountdownTimer.cancel();
			combatCountdownTimer = null;
		}
	}
	
	private void onCombatCountdown(int seconds) {
		boolean announce = false;
		if (seconds <= 60) {
			announce = seconds == 60 || seconds == 30 || seconds == 10; // 60, 30, 10 secondi
		} else if (seconds <= 5 * 60) {
			announce = seconds % 60 == 0; // Ogni minuto
		}
		if (announce) {
			arena.broadcast(ChatColor.YELLOW + "Le casse verranno riempite tra " + UnitFormatter.formatMinutesOrSeconds(seconds) + ".");
		}
		arena.getScoreboard().setChestRefillCountdown(seconds);
	}
	
	private void finishCombatCountdown() {
		combatCountdownTimer = null;
		startCombatCountdown();

		arena.broadcast(ChatColor.YELLOW + "Le casse sono state riempite!");
		arena.refillChests();
	}
	
	
	/*
	 * 
	 *		   	__          __  _____   _   _
	 *		   	\ \        / / |_   _| | \ | |
	 * 			 \ \  /\  / /    | |   |  \| |
	 * 			  \ \/  \/ /     | |   | . ` |
	 *  	       \  /\  /     _| |_  | |\  |
	 * 			  	\/  \/     |_____| |_| \_|
	 * 
	 * 
	 */
	private void startWinCountdown() {
		new CountdownTimer(MainSettings.countdown_end, this::onWinCountdown, this::finishWinCountdown).start();          
	}
	
	private void onWinCountdown(int seconds) {
		// Lancia fuochi di artificio			
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (!playerStatus.isSpectator() && playerStatus.getTeam() != null) {
				// Giocatore del team vincente
				Player player = playerStatus.getPlayer();
				Firework firework = (Firework) player.getWorld().spawnEntity(Utils.getRandomAround(player), EntityType.FIREWORK);
		        firework.setFireworkMeta(playerStatus.getTeam().getWinningFireworkMeta());
			}
		}
	}
	
	private void finishWinCountdown() {
		arena.delete();
	}

}
