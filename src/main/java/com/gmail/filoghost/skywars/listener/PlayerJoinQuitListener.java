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

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.VanishManager;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.utils.Utils;

public class PlayerJoinQuitListener implements Listener {
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onAsyncLogin(AsyncPlayerPreLoginEvent event) {
		if (event.getLoginResult() == Result.ALLOWED) {
			try {
				SkyWars.loadStatsFromDatabase(event.getUniqueId());
			} catch (SQLException e) {
				e.printStackTrace();
				event.setKickMessage("Non è stato possibile caricare i tuoi dati.");
				event.setLoginResult(Result.KICK_OTHER);
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player joiner = event.getPlayer();
		if (SkyWars.getOnlinePlayerData(joiner) == null) {
			Utils.reportAnomaly("player data not loaded on join", joiner);
			joiner.kickPlayer(ChatColor.RED + "I tuoi dati non erano caricati al tuo ingresso, errore interno.");
			return;
		}
		
		SkyWars.setupToLobby(joiner);
		
		event.setJoinMessage(null);
		VanishManager.onJoin(joiner);
		joiner.resetMaxHealth();
		joiner.setHealth(joiner.getMaxHealth());
		joiner.setFoodLevel(20);
		joiner.setSaturation(0);
		joiner.setExhaustion(0);
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player quitter = event.getPlayer();
		
		ChatListener.forceGlobalChat.remove(quitter);
		ChatListener.receivedGlobalChatTip.remove(quitter);
		
		event.setQuitMessage(null);
		VanishManager.onQuit(quitter);

		Arena quitterArena = ArenasManager.getArenaByPlayer(quitter);
		if (quitterArena != null) {
			quitterArena.removePlayerAndEventuallyProcessDeath(event.getPlayer());
		}
		
		UUID quitterUUID = quitter.getUniqueId();
		SkyWars.unloadAndSaveStats(quitterUUID);
		
		SkyWars.getSpawnScoreboard().onQuit(quitter);
	}

}
