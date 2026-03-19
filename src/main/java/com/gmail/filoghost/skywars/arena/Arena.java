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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.PlayerInventory;

import com.gmail.filoghost.skywars.Constants;
import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.entities.SpawningManager;
import com.gmail.filoghost.skywars.arena.kit.Kit;
import com.gmail.filoghost.skywars.arena.menu.ItemShopMenu;
import com.gmail.filoghost.skywars.arena.menu.TeamSelectorMenu;
import com.gmail.filoghost.skywars.arena.menu.TeleporterMenu;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.arena.player.Team;
import com.gmail.filoghost.skywars.arena.scoreboard.ArenaScoreboard;
import com.gmail.filoghost.skywars.arena.shop.ShopManager;
import com.gmail.filoghost.skywars.database.PlayerData;
import com.gmail.filoghost.skywars.timer.RefillChestsTimer;
import com.gmail.filoghost.skywars.utils.Format;
import com.gmail.filoghost.skywars.utils.Utils;
import com.gmail.filoghost.skywars.world.BlockPosition;
import com.gmail.filoghost.skywars.world.Region;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import wild.api.WildCommons;
import wild.api.WildConstants;
import wild.api.sound.EasySound;
import wild.api.translation.Translation;
import wild.api.util.UnitFormatter;

public class Arena {
	
	// Settings
	@Getter private final String name;
	@Getter private final Region region;
	@Getter private final Location lobby;
	@Getter private final Region cageRegion;
	private final Block sign;
	@Getter private final int minPlayers;
	@Getter private final int maxPlayers;
	@Getter private final int maxPlayersPerTeam;
	@Getter private final boolean teamsMode;
	private final List<Team> teams;

	// Statuses
	@Getter @Setter private ArenaStatus arenaStatus;
	private final Map<Player, PlayerStatus> playerStatuses;
	
	// Dynamic objects
	@Getter private TeamSelectorMenu teamSelectorMenu;
	@Getter private TeleporterMenu teleporterMenu;
	private Set<BlockPosition> chestPositions;
	
	// Managers
	@Getter private final ArenaScoreboard scoreboard;
	@Getter private final GameloopManager gameloop;
	@Getter private final EventManager events;
	@Getter private final SpawningManager spawningManager;
	@Getter private final ShopManager shopManager;
	
	protected Arena(ArenaModel model, Region cageRegion) {
		this.name = model.getName();
		this.region = model.getRegion();
		this.lobby = model.getLobby();
		this.cageRegion = cageRegion;
		this.sign = model.getSign();
		this.maxPlayersPerTeam = model.getMaxPlayersPerTeam();
		this.teamsMode = model.isTeamsMode();
		this.maxPlayers = model.getMaxPlayers();
		this.minPlayers = model.getMinPlayers();
		this.teams = model.getTeams();
		
		this.scoreboard = new ArenaScoreboard();
		this.gameloop = new GameloopManager(this);
		this.events = new EventManager(this);
		this.spawningManager = new SpawningManager(this);
		this.shopManager = new ShopManager(this, new ItemShopMenu(this));

		this.playerStatuses = Maps.newConcurrentMap();
		this.chestPositions = Sets.newHashSet();
		
		arenaStatus = ArenaStatus.LOBBY;
		
		// Menu e scoreboard
		teamSelectorMenu = new TeamSelectorMenu(this);
		teleporterMenu = new TeleporterMenu(this);
		scoreboard.displayLobby();
		
		refreshSign();
	}
	
	public void delete() {
		try {
			for (Player player : getPlayers()) {
				ArenasManager.getArenasByPlayers().remove(player);
				SkyWars.setupToLobby(player);
				player.sendMessage(ChatColor.GRAY + "Sei stato mandato allo spawn.");
			}
			playerStatuses.clear();
			
			// Così da farlo vedere sui cartelli
			arenaStatus = ArenaStatus.DELETED;		
			refreshSign();
			
		} finally {
			ArenasManager.onArenaDelete(this);
		}
		
		// Dopo l'arena delete perché non è necessario che venga fatto prima
		region.iterateChunks(chunk -> {
			// Pulizia oggetti e entità
			for (Entity entity : chunk.getEntities()) {
				if (entity.getType() != EntityType.PLAYER && region.isInside(entity.getLocation())) {
					entity.remove();
				}
			}
			// Unload dei chunk
			chunk.unload(false);
		});
	}

	
	public void refreshSign() {
		BlockState state = sign.getState();
		if (state instanceof Sign) {
			Sign sign = (Sign) state;
			sign.setLine(0, ChatColor.BOLD + name);
			sign.setLine(1, "----------------");
			if (gameloop.getLobbyCountdownTimer() != null) {
				sign.setLine(2, ChatColor.GREEN + "[" + UnitFormatter.formatMinutesOrSeconds(gameloop.getLobbyCountdownTimer().getCountdown()) + "]");
			} else {
				sign.setLine(2, (arenaStatus == ArenaStatus.LOBBY ? ChatColor.GREEN : ChatColor.RED) + arenaStatus.getName());
			}
			sign.setLine(3, ChatColor.DARK_GRAY + "[" + playerStatuses.size() + "/" + maxPlayers + "]");
			sign.update(true, false); // Force per ripristinare cartelli distrutti, e senza farli cadere
		}
		
		SkyWars.getArenaSelector().updateArena(this);
	}
	
	
	public void tryAddPlayer(Player joiner) {
		if (ArenasManager.getArenaByPlayer(joiner) != null) {
			Utils.reportAnomaly("player was trying to join while already inside arena", this, joiner);
			joiner.sendMessage(ChatColor.RED + "Sei già in un'arena.");
			return;
		}
		
		if (arenaStatus == ArenaStatus.LOBBY && playerStatuses.size() >= maxPlayers) {
			joiner.sendMessage(ChatColor.RED + "Questa arena è piena.");
			return;
		}
		
		PlayerStatus joinerStatus = new PlayerStatus(joiner);
		if (arenaStatus != ArenaStatus.LOBBY) {
			// Eccetto che nella lobby, si entra sempre come spettatori
			joinerStatus.setSpectator(joiner, true);
		} else {
			joinerStatus.setSpectator(joiner, false); // Per aggiornare i cosmetici
		}

		// Salva lo stato del giocatore
		playerStatuses.put(joiner, joinerStatus);
		ArenasManager.getArenasByPlayers().put(joiner, this);
		
		if (arenaStatus == ArenaStatus.LOBBY) {
			if (gameloop.getLobbyCountdownTimer() == null && playerStatuses.size() >= minPlayers) {
				gameloop.startLobbyCountdown();
			}
			broadcast(formatPlayer(joiner, (Team) null) + ChatColor.GRAY + " è entrato (" + ChatColor.WHITE + playerStatuses.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + maxPlayers + ChatColor.GRAY + ")");
		}
		
		refreshSign();
		joiner.teleport(lobby, TeleportCause.PLUGIN);
		giveEquip(joiner, joinerStatus);
		joiner.setScoreboard(scoreboard.getScoreboard());
	}
	

	public void removePlayerAndEventuallyProcessDeath(Player leaver) {
		// Rimuove il player dalle collezioni
		if (!playerStatuses.containsKey(leaver)) {
			Utils.reportAnomaly("removing player but wasn't in arena", this, leaver);
			return;
		}
		
		PlayerStatus leaverStatus = playerStatuses.get(leaver);
		Team leaverTeam = leaverStatus.getTeam();
		int newPlayersAmount = playerStatuses.size() - 1;
		
		// Broadcast messaggio e eventuale morte forzata
		if (arenaStatus == ArenaStatus.LOBBY) {
			broadcast(formatPlayer(leaver, leaverTeam) + ChatColor.GRAY + " è uscito (" + ChatColor.WHITE + newPlayersAmount + ChatColor.GRAY + "/" + ChatColor.WHITE + maxPlayers + ChatColor.GRAY + ")");
			
			if (gameloop.getLobbyCountdownTimer() != null && newPlayersAmount < minPlayers) {
				gameloop.cancelLobbyCountdown();
				broadcast(ChatColor.YELLOW + "Il conto alla rovescia è stato interrotto.");
			}
			
		} else if (arenaStatus == ArenaStatus.COMBAT) {
			if (leaverTeam != null) {
				broadcast(formatPlayer(leaver, leaverTeam) + ChatColor.GRAY + " è uscito");
				
				// Considera l'evento come se fosse una morte, perché il leaver stava giocando.
				// Questo ovviamente controlla anche se ci sono vincitori.
				// Va eseguito prima di togliere lo status della vittima.
				events.onDeath(leaver, false);
			}
		}

		// Ora possiamo toglierlo dall'arena
		playerStatuses.remove(leaver);
		scoreboard.untrackPlayer(leaver);
		ArenasManager.getArenasByPlayers().remove(leaver);
		
		if (leaverTeam != null) {
			if (teamsMode) {
				teamSelectorMenu.updateCount(leaverTeam, countPlayersByTeam(leaverTeam), maxPlayersPerTeam);
			}
			teleporterMenu.update();
		}
		
		refreshSign();
		
		// Rimuove l'eventuale vanish
		leaverStatus.setSpectator(leaver, false);
	}
	
	
	public void trySetPlayerTeam(Player player, @NonNull Team team) {
		if (!teamsMode) {
			Utils.reportAnomaly("trying to set team not in team mode", this, player, arenaStatus);
			player.sendMessage(ChatColor.RED + "Non sono disponibili i team in questa arena.");
		}
		
		if (arenaStatus != ArenaStatus.LOBBY) {
			Utils.reportAnomaly("trying to set team not in lobby", this, player, arenaStatus);
			player.sendMessage(ChatColor.RED + "Non puoi cambiare team ora.");
			return;
		}
		
		PlayerStatus playerStatus = getPlayerStatus(player);
		
		if (playerStatus == null) {
			Utils.reportAnomaly("trying to set team on external player", this, player);
			player.sendMessage(ChatColor.RED + "Non sei all'interno della partita.");
			return;
		}
		
		if (!teams.contains(team)) {
			Utils.reportAnomaly("trying to set team that is not present in arena", this, player);
			player.sendMessage(ChatColor.RED + "Errore interno.");
			return;
		}
		
		if (playerStatus.getTeam() == team) {
			player.sendMessage(ChatColor.RED + "Sei già nel team " + team.getNameSingular() + ".");
			return;
		}
		
		
		if (countPlayersByTeam(team) >= maxPlayersPerTeam) {
			player.sendMessage(ChatColor.RED + "Il team " + team.getNameSingular() + " è al completo.");
			return;
		}
		
		Team previousTeam = playerStatus.getTeam();
		playerStatus.setTeam(team);
		
		if (teamsMode) {
			if (previousTeam != null) {
				teamSelectorMenu.updateCount(previousTeam, countPlayersByTeam(previousTeam), maxPlayersPerTeam);
			}
			teamSelectorMenu.updateCount(team, countPlayersByTeam(team), maxPlayersPerTeam);
		}
		teleporterMenu.update();
		player.sendMessage(SkyWars.PREFIX + ChatColor.GRAY + "Hai scelto il team " + team.getChatColor() + team.getNameSingular() + ChatColor.GRAY + ".");
		EasySound.quickPlay(player, Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE);
	}
	
	
	public void trySetKit(Player player, Kit kit) {
		if (arenaStatus != ArenaStatus.LOBBY) {
			Utils.reportAnomaly("selecting kit not in lobby", this, player);
			return;
		}
		
		PlayerStatus playerStatus = getPlayerStatus(player);
		
		if (playerStatus == null) {
			Utils.reportAnomaly("trying to set kit on external player", this, player);
			player.sendMessage(ChatColor.RED + "Non sei all'interno della partita.");
			return;
		}
		
		PlayerData playerData = SkyWars.getOnlinePlayerData(player);
		if (playerData.getScore() < kit.getPrice()) {
			player.sendMessage(ChatColor.RED + "Non hai abbastanza punti per questo kit.");
			return;
		}
		
		playerStatus.setSelectedKit(kit);
		player.sendMessage(SkyWars.PREFIX + ChatColor.GRAY + "Hai scelto il kit " + Kit.NAME_COLOR + kit.getName() + ChatColor.GRAY + ".");
		EasySound.quickPlay(player, Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE);
	}
	
	
	public void scanInitialChests() {
		this.chestPositions.clear();
		
		region.iterateChunks(chunk -> {
			for (BlockState tileEntity : chunk.getTileEntities()) {
				if (!region.isInside(tileEntity)) {
					continue;
				}
				
				if (!(tileEntity instanceof Chest)) {
					continue;
				}
				
				BlockPosition chestPosition = new BlockPosition(tileEntity);
				chestPositions.add(chestPosition);
			}
		});
	}
	
	
	public void refillChests() {
		new RefillChestsTimer(region.getWorld(), new ArrayDeque<>(chestPositions)).start();
	}
	
	
	/*
	 * 
	 *			 _    _   _______   _____   _        _____   _______  __     __
	 *			| |  | | |__   __| |_   _| | |      |_   _| |__   __| \ \   / /
	 *			| |  | |    | |      | |   | |        | |      | |     \ \_/ /
	 *			| |  | |    | |      | |   | |        | |      | |      \   /
	 *			| |__| |    | |     _| |_  | |____   _| |_     | |       | |
	 *			 \____/     |_|    |_____| |______| |_____|    |_|       |_|
	 *
	 *	
	 */
	public String formatPlayer(Player player, Team team) {
		if (team != null && isTeamsMode()) {
			return team.getChatColor() + player.getName();
		} else {
			return ChatColor.WHITE + player.getName();
		}
	}
	
	public String formatEntity(EntityType entityType, @Nullable String playerOwnerName, @Nullable Team team) {
		if (playerOwnerName != null) {
			if (team != null && isTeamsMode()) {
				return team.getChatColor() + Translation.of(entityType) + " di " + playerOwnerName;
			} else {
				return ChatColor.WHITE + Translation.of(entityType) + " di " + playerOwnerName;
			}
		} else {
			return ChatColor.WHITE + Translation.of(entityType);
		}
	}
	
	public void addPositiveScore(Player player, PlayerData playerData, Map<Integer, Integer> scoreByTeamsAmount, int scoreBase) {
		int scoreToAdd = scoreByTeamsAmount.getOrDefault(teams.size(), scoreBase);
		scoreToAdd = Math.abs(scoreToAdd);
		playerData.addScore(scoreToAdd);
		player.sendMessage(ChatColor.GOLD + "+ " + Format.formatPoints(scoreToAdd));
	}
	
	public void addNegativeScore(Player player, PlayerData playerData, int scoreToRemove) {
		if (scoreToRemove != 0) {
			scoreToRemove = Math.abs(scoreToRemove);
			playerData.subtractScore(scoreToRemove);
			player.sendMessage(ChatColor.GOLD + "- " + Format.formatPoints(scoreToRemove));
		}
	}
	
	public void broadcast(String message) {
		for (Player player : getPlayers()) {
			player.sendMessage(SkyWars.PREFIX + message);
		}
	}
	
	public void broadcastSound(Sound sound) {
		for (Player player : getPlayers()) {
			EasySound.quickPlay(player, sound);
		}
	}
	
	public void broadcastTeam(Team team, String message) {
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() == team) {
				playerStatus.getPlayer().sendMessage(SkyWars.PREFIX + message);
			}
		}
	}
	
	public void broadcastTeamTitle(Team team, String title, String subtitle, int ticksVisible) {
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() == team) {
				WildCommons.sendTitle(playerStatus.getPlayer(), 5, ticksVisible, 5, title, subtitle);
			}
		}
	}

	
	public void giveEquip(Player player, PlayerStatus playerStatus) {
		WildCommons.clearInventoryFully(player);
		WildCommons.removePotionEffects(player);
		PlayerInventory inventory = player.getInventory();
		
		if (arenaStatus == ArenaStatus.LOBBY) {
			player.setGameMode(GameMode.ADVENTURE);
			Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
				inventory.addItem(Constants.ITEM_KIT_PICKER);
				if (teamsMode) {
					inventory.addItem(Constants.ITEM_TEAM_PICKER);
				}
			});
		} else {
			if (playerStatus.isSpectator()) {
				player.setGameMode(GameMode.CREATIVE);
				player.setFlying(true);
				Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
					inventory.setItem(0, WildConstants.Spectator.TELEPORTER);
					if (playerStatus.getTeam() == null) {
						inventory.setItem(8, WildConstants.Spectator.QUIT_SPECTATING);
					}
				});
			} else {
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
	}
	
	
	public Team findLowestPlayersTeam() {
		Map<Team, Integer> counts = Maps.newHashMap();
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() != null) {
				counts.merge(playerStatus.getTeam(), 1, Integer::sum);
			}
		}
	
		Team smallestTeam = null;
		int smallestTeamCount = Integer.MAX_VALUE;
		
		for (Team team : teams) {
			int count = counts.getOrDefault(team, 0);
			if (count < smallestTeamCount) {
				smallestTeam = team;
				smallestTeamCount = count;
			}
		}
		
		return smallestTeam;
	}
	
	
	public int countPlayersByTeam(Team countTeam) {
		int count = 0;
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (countTeam == playerStatus.getTeam()) {
				count++;
			}
		}
		return count;
	}
	
	public int countPlayingPlayers() {
		int count = 0;
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() != null) {
				count++;
			}
		}
		return count;
	}
	
	public Collection<Player> getPlayers() {
		return playerStatuses.keySet();
	}
	
	public PlayerStatus getPlayerStatus(Player player) {
		return playerStatuses.get(player);
	}
	
	public PlayerStatus getFightingPlayerStatus(Player player) {
		if (arenaStatus != ArenaStatus.COMBAT) {
			return null;
		}
		
		PlayerStatus playerStatus = getPlayerStatus(player);
		if (playerStatus == null || playerStatus.isSpectator() || playerStatus.getTeam() == null) {
			return null;
		}
		
		return playerStatus;
	}
	
	public boolean isFightingPlayerStatus(PlayerStatus playerStatus) {
		if (arenaStatus != ArenaStatus.COMBAT) {
			return false;
		}
		
		if (playerStatus == null || playerStatus.isSpectator() || playerStatus.getTeam() == null) {
			return false;
		}
		
		return true;
	}
	
	public boolean isFightingPlayer(Player player) {
		return getFightingPlayerStatus(player) != null;
	}
	
	
	
	public Collection<PlayerStatus> getPlayerStatuses() {
		return playerStatuses.values();
	}
	
	public Collection<Team> getTeams() {
		return teams;
	}
	
}
