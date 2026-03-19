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
package com.gmail.filoghost.skywars.arena.scoreboard;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.player.Team;
import com.google.common.collect.Maps;

import lombok.Getter;
import wild.api.WildConstants;
import wild.api.sidebar.DynamicSidebarLine;
import wild.api.sidebar.ScoreboardSidebarManager;

public class ArenaScoreboard {
	
	@Getter private Scoreboard scoreboard;
	private ScoreboardSidebarManager sidebar;
	
	private DynamicSidebarLine lobbyCountdownLine;
	private DynamicSidebarLine gameCountdownLine;
	private DynamicSidebarLine killsLine;
	private DynamicSidebarLine totalPlayersLine;
	private Map<Team, DynamicSidebarLine> teamsLines;
	
	private Map<UUID, Integer> kills;
	private Map<UUID, Integer> deaths;
	
	public ArenaScoreboard() {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.kills = Maps.newHashMap();
		this.deaths = Maps.newHashMap();
		
		lobbyCountdownLine = null;
		gameCountdownLine = null;
		killsLine = null;
		teamsLines = null;
	}
	
	private void resetSidebar() {
		this.sidebar = new ScoreboardSidebarManager(scoreboard, SkyWars.SIDEBAR_TITLE);
		sidebar.setLine(0, WildConstants.Messages.getSidebarIP());
		sidebar.setLine(1, "");
	}
	
	public void displayLobby() {
		resetSidebar();
		
		lobbyCountdownLine = sidebar.setDynamicLine(2, "?");
		sidebar.setLine(3, "");
		unsetLobbyCountdown();
	}
	
	public void setLobbyCountdown(int seconds) {
		int minutes = seconds / 60;
		seconds = seconds % 60;
		lobbyCountdownLine.updateAll(ChatColor.WHITE + "Inizio partita: " + ChatColor.GRAY + String.format("%d:%02d", minutes, seconds));
	}
	
	public void unsetLobbyCountdown() {
		lobbyCountdownLine.updateAll(ChatColor.WHITE + "In attesa...");
	}
	

	public void displayGame(boolean hasTeams, Collection<Team> teams) {
		resetSidebar();
		
		int index = 2;
		
		for (Team team : teams) {
			org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.registerNewTeam(team.getId());
			if (hasTeams) {
				scoreboardTeam.setPrefix(team.getChatColor().toString());
			}
			scoreboardTeam.setCanSeeFriendlyInvisibles(true);
			scoreboardTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		}
		
		killsLine = sidebar.setDynamicLine(index++, ""); // Così gli spettatori vedono righe vuote
		sidebar.setLine(index++, "");
		
		if (hasTeams) {
			teamsLines = Maps.newHashMap();
			for (Team team : teams) {
				DynamicSidebarLine teamLine = sidebar.setDynamicLine(index++, team.getChatColor() + team.getNamePluralCapitalized() + ":" + ChatColor.WHITE + " ?");
				teamsLines.put(team, teamLine);
			}
		} else {
			totalPlayersLine = sidebar.setDynamicLine(index++, "");
		}
		
		sidebar.setLine(index++, "");
		gameCountdownLine = sidebar.setDynamicLine(index++, "");
		sidebar.setLine(index++, "");
	}
	
	public void setChestRefillCountdown(int seconds) {
		int minutes = seconds / 60;
		seconds = seconds % 60;
		gameCountdownLine.updateAll((minutes == 0 && seconds <= 10 ? ChatColor.YELLOW : ChatColor.WHITE) + "Refill casse: " + ChatColor.GRAY + String.format("%d:%02d", minutes, seconds));
	}
	
	public void updateTeamCount(Team team, int playersCount) {
		if (teamsLines == null) {
			return;
		}
		
		ChatColor teamColor = playersCount > 0 ? team.getChatColor() : ChatColor.DARK_GRAY;
		ChatColor playersCountColor = playersCount > 0 ? ChatColor.WHITE : ChatColor.DARK_GRAY;

		teamsLines.get(team).updateAll(teamColor + team.getNamePluralCapitalized() + ": " + playersCountColor + playersCount);
	}
	
	public void updateTotalCount(int playersCount) {
		if (totalPlayersLine == null) {
			return;
		}

		totalPlayersLine.updateAll("Giocatori: " + ChatColor.GRAY + playersCount);
	}
	
	public void addKill(Player killer) {
		int newKillsValue = kills.merge(killer.getUniqueId(), 1, Integer::sum);
		displayKills(killer, newKillsValue);
	}
	
	public void displayKills(Player player, int kills) {
		killsLine.update(player, ChatColor.WHITE + "Uccisioni: " + ChatColor.GRAY + kills);
	}
	
	public void addTeamColor(Player player, Team team) {
		scoreboard.getTeam(team.getId()).addEntry(player.getName());
	}

	public void removeTeamColor(Player player, Team team) {
		scoreboard.getTeam(team.getId()).removeEntry(player.getName());
	}

	public void untrackPlayer(Player player) {
		kills.remove(player.getUniqueId());
		deaths.remove(player.getUniqueId());
	}
	
}
