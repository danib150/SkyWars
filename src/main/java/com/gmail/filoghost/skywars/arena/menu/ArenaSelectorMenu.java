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
package com.gmail.filoghost.skywars.arena.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenaModel;
import com.gmail.filoghost.skywars.arena.ArenaStatus;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.utils.Utils;

import wild.api.menu.Icon;
import wild.api.menu.PagedMenu;

public class ArenaSelectorMenu {

	private PagedMenu pagedMenu;
	Map<String, Icon> iconsByArenaName;

	public ArenaSelectorMenu() {
		this.pagedMenu = new PagedMenu("Arene", 5);
		this.iconsByArenaName = new HashMap<>();
	}

	public void updateAll() {
		pagedMenu.clearIcons();
		iconsByArenaName.clear();

		// Sort per dimensione squadre, poi per giocatori totali
		List<ArenaModel> orderedArenaModels = new ArrayList<ArenaModel>(ArenasManager.getModels());
		Collections.sort(orderedArenaModels, (arenaModel1, arenaModel2) -> {
			int diff = arenaModel1.getMaxPlayersPerTeam() - arenaModel2.getMaxPlayersPerTeam();
			if (diff != 0) {
				return diff;
			}

			diff = arenaModel1.getMaxPlayers() - arenaModel2.getMaxPlayers();
			if (diff != 0) {
				return diff;
			}
			return arenaModel1.getName().compareTo(arenaModel2.getName());
		});

		for (ArenaModel arenaModel : orderedArenaModels) {
			Icon icon = createIcon(arenaModel);
			Arena arena = ArenasManager.getArenaByName(arenaModel.getName());
			
			if (arena != null) {
				updateIcon(arena, icon);
			} else {
				icon.setName(ChatColor.WHITE + arenaModel.getName());
				icon.setLore(ChatColor.GRAY + "Caricamento...");
			}
			
			pagedMenu.addIcon(icon);
			iconsByArenaName.put(arenaModel.getName(), icon);
		}

		pagedMenu.update();
	}
	
	
	public void updateArena(Arena arena) {
		Icon icon = iconsByArenaName.get(arena.getName());
		if (icon != null) {
			updateIcon(arena, icon);
			pagedMenu.refresh(icon);
		} else {
			Utils.reportAnomaly("couldn't get arena icon", arena);
		}
	}

	
	private Icon createIcon(ArenaModel arenaModel) {
		Icon icon = new Icon(Material.STAINED_CLAY);
		icon.setClickHandler(clicker -> {
			Arena arena = ArenasManager.getArenaByName(arenaModel.getName());
			if (arena != null) {
				arena.tryAddPlayer(clicker);
			}
		});
		return icon;
	}

	
	private void updateIcon(Arena arena, Icon icon) {
		int players = arena.getPlayerStatuses().size();
		int maxPlayers = arena.getMaxPlayers();

		StainedClayColor iconColor;
		ChatColor nameColor;
		String status = null;

		if (arena.getArenaStatus() == ArenaStatus.LOBBY) {
			if (arena.getGameloop().getLobbyCountdownTimer() == null) {
				status = ChatColor.DARK_GRAY + "In attesa...";
			} else {
				status = ChatColor.DARK_GRAY + "Inizio in corso...";
			}

			if (players > 0) {
				if (players < maxPlayers) {
					nameColor = ChatColor.YELLOW;
					iconColor = StainedClayColor.YELLOW;
				} else {
					nameColor = ChatColor.GOLD;
					iconColor = StainedClayColor.ORANGE;
				}
			} else {
				nameColor = ChatColor.GREEN;
				iconColor = StainedClayColor.GREEN;
			}
		} else {
			nameColor = ChatColor.RED;
			iconColor = StainedClayColor.RED;
			status = ChatColor.RED + "In corso.";
		}

		icon.setName(nameColor + arena.getName());
		icon.setAmount(players > 1 ? players : 1);
		icon.setDataValue(iconColor.getData());

		String teamSizeLine = ChatColor.GRAY + "" + ChatColor.ITALIC + (arena.getMaxPlayersPerTeam() > 1 ? "Squadre da " + arena.getMaxPlayersPerTeam() + " giocatori" : "Tutti contro tutti");
		String playersLine = ChatColor.WHITE + "Giocatori" + ChatColor.GRAY + ": " + ChatColor.WHITE + players + ChatColor.GRAY + "/" + ChatColor.WHITE + maxPlayers;

		icon.setLore(teamSizeLine, "", playersLine, "", status);
	}

	public void open(Player player) {
		pagedMenu.open(player);
	}

}
