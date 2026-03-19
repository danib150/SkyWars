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
package com.gmail.filoghost.skywars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import wild.api.WildConstants;
import wild.api.sidebar.DynamicSidebarLine;
import wild.api.sidebar.ScoreboardSidebarManager;

public class SpawnScoreboard {
	
	private Scoreboard scoreboard;
	private ScoreboardSidebarManager sidebarManager;
	private Team noCollisionsTeam;
	
	private DynamicSidebarLine scoreLine;
	private DynamicSidebarLine winsLine;
	private DynamicSidebarLine killsLine;
	private DynamicSidebarLine deathsLine;
	
	public SpawnScoreboard() {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.noCollisionsTeam = scoreboard.registerNewTeam("noCollisions");
		//this.noCollisionsTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		this.noCollisionsTeam.setCanSeeFriendlyInvisibles(false);
		
		this.sidebarManager = new ScoreboardSidebarManager(scoreboard, SkyWars.SIDEBAR_TITLE);
		
		sidebarManager.setLine(6, "");
		scoreLine = sidebarManager.setDynamicLine(5, "?");
		winsLine = sidebarManager.setDynamicLine(4, "?");
		killsLine = sidebarManager.setDynamicLine(3, "?");
		deathsLine = sidebarManager.setDynamicLine(2, "?");
		
		sidebarManager.setLine(1, "");
		sidebarManager.setLine(0, WildConstants.Messages.getSidebarIP());
	}
	
	public void setActiveScoreboard(Player player) {
		player.setScoreboard(scoreboard);
		noCollisionsTeam.addEntry(player.getName());
	}
	
	public boolean hasActiveScoreboard(Player player) {
		return player.getScoreboard() == this.scoreboard;
	}
	
	public void onQuit(Player quitter) {
		noCollisionsTeam.removeEntry(quitter.getName());
	}
	
	public void setWins(Player player, int wins) {
		winsLine.update(player, ChatColor.WHITE + "Vittorie: " + ChatColor.GRAY + wins);
	}
	
	public void setKills(Player player, int kills) {
		killsLine.update(player, ChatColor.WHITE + "Uccisioni: " + ChatColor.GRAY + kills);
	}
	
	public void setDeaths(Player player, int deaths) {
		deathsLine.update(player, ChatColor.WHITE + "Morti: " + ChatColor.GRAY + deaths);
	}
	
	public void setScore(Player player, int score) {
		scoreLine.update(player, ChatColor.WHITE + "Punti: " + ChatColor.GRAY + score);
	}
	
}
