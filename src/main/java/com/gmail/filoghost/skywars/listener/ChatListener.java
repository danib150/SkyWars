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

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenaStatus;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.arena.player.Team;
import com.gmail.filoghost.skywars.database.PlayerData;
import com.gmail.filoghost.skywars.settings.MainSettings;
import com.google.common.collect.Sets;

import wild.api.WildCommons;

public class ChatListener implements Listener {
	
	public static Set<Player> forceGlobalChat = Sets.newConcurrentHashSet();
	public static Set<Player> receivedGlobalChatTip = Sets.newConcurrentHashSet();
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent event) {
		PlayerData playerData = SkyWars.getOnlinePlayerData(event.getPlayer());
		event.setFormat(event.getFormat().replace("[wins]", String.valueOf(playerData.getWins())));
		
		if (forceGlobalChat.contains(event.getPlayer())) {
			return; // Globale forzata
		}
		
		Arena arena = ArenasManager.getArenaByPlayer(event.getPlayer());
		
		if (arena == null || arena.getArenaStatus() == ArenaStatus.LOBBY) {
			return; // Nella lobby o fuori da un'arena, chat globale
		}
		
		PlayerStatus playerStatus = arena.getPlayerStatus(event.getPlayer());
		Team team = playerStatus.getTeam();
		
		if (team != null) {
			if (arena.isTeamsMode()) {
				setChatFormat(event, MainSettings.chatFormat_team.replace("{teamcolor}", team.getChatColor().toString()));
				
				event.getRecipients().clear();
				addPlayersByTeam(event.getRecipients(), arena, team);
				
				if (event.getRecipients().size() <= 0) {
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED + "Non c'è nessun altro nel team che possa leggere il tuo messaggio.");
					return;
				}
				
				if (!receivedGlobalChatTip.contains(event.getPlayer())) {
					receivedGlobalChatTip.add(event.getPlayer());
					event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Stai usando la chat del tuo team.");
					event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Per parlare in chat globale, usa " + ChatColor.GRAY + "/g <messaggio>");
				}
			} else {
				// Lascia chat globale
			}
		} else {
			setChatFormat(event, MainSettings.chatFormat_spectators);
			
			event.getRecipients().clear();
			addPlayersByTeam(event.getRecipients(), arena, null);
			
			if (event.getRecipients().size() <= 0) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "Non c'è nessun altro spettatore che possa leggere il tuo messaggio.");
				return;
			}
		}
	}
	
	
	private void setChatFormat(AsyncPlayerChatEvent chatEvent, String newFormat) {
		chatEvent.setFormat(WildCommons.color(newFormat).replace("{player}", "%1$2s") + "%2$2s");
	}
	
	
	private void addPlayersByTeam(Collection<Player> recipients, Arena arena, @Nullable Team team) {
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (playerStatus.getTeam() == team) {
				recipients.add(playerStatus.getPlayer());
			}
		}
	}
	
}
