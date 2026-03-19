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

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.utils.Utils;
import com.google.common.collect.Lists;

import wild.api.WildCommons;
import wild.api.bridges.PexBridge;
import wild.api.bridges.PexBridge.PrefixSuffix;
import wild.api.menu.Icon;
import wild.api.menu.PagedMenu;
import wild.api.menu.StaticIcon;

public class TeleporterMenu {
	
	private final Arena arena;
	private PagedMenu pagedMenu;
	
	public TeleporterMenu(Arena arena) {
		this.arena = arena;
		this.pagedMenu = new PagedMenu("Teletrasporto rapido", 5);
	}

	
	public void update() {
		Collection<PlayerStatus> gamerStatuses = Lists.newArrayList();
		
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (arena.isFightingPlayerStatus(playerStatus)) {
				gamerStatuses.add(playerStatus);
			}
		}
		
		pagedMenu.clearIcons();
		
		for (PlayerStatus gamerStatus : gamerStatuses) {
			Player gamer = gamerStatus.getPlayer();
			String gamerName = gamer.getName();
			
			ItemStack headItem = Utils.getHeadItem(gamerStatus.getPlayer());
			ItemMeta headItemMeta = headItem.getItemMeta();
			PrefixSuffix prefixSuffix = PexBridge.getCachedPrefixSuffix(gamer);
			headItemMeta.setDisplayName(ChatColor.WHITE + WildCommons.color(prefixSuffix.getPrefix() + gamer.getName() + prefixSuffix.getSuffix()));
			if (arena.isTeamsMode()) {
				headItemMeta.setLore(Arrays.asList(gamerStatus.getTeam().getChatColor() + "Team " + gamerStatus.getTeam().getNameSingular()));
			}
			headItem.setItemMeta(headItemMeta);
			
			Icon icon = new StaticIcon(headItem);
			icon.setClickHandler(clicker -> {
				Player target = Bukkit.getPlayerExact(gamerName);
				
				PlayerStatus clickerStatus = arena.getPlayerStatus(clicker);
				if (clickerStatus == null || !clickerStatus.isSpectator()) {
					clicker.sendMessage(ChatColor.RED + "Puoi teletrasportarti solo da spettatore.");
					return;
				}
				
				if (target != null) {
					PlayerStatus targetStatus = arena.getPlayerStatus(target);
					
					if (targetStatus != null && targetStatus.getTeam() != null && !targetStatus.isSpectator()) {
						clicker.teleport(target, TeleportCause.PLUGIN);
						clicker.sendMessage(ChatColor.GRAY + "Teletrasportato da " + arena.formatPlayer(target, targetStatus.getTeam()));
						return;
					}
				}
				
				clicker.sendMessage(ChatColor.RED + "Quel giocatore non è più online o è uno spettatore.");
			});
			
			pagedMenu.addIcon(icon);
		}
		
		pagedMenu.update();
	}

	public void open(Player player) {
		pagedMenu.open(player);
	}
}
