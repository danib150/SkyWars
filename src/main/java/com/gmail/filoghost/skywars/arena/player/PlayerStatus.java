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
package com.gmail.filoghost.skywars.arena.player;

import org.bukkit.entity.Player;

import com.gmail.filoghost.skywars.VanishManager;
import com.gmail.filoghost.skywars.arena.kit.Kit;

import lombok.Getter;
import lombok.Setter;
import wild.api.bridges.CosmeticsBridge;
import wild.api.world.SpectatorAPI;

public class PlayerStatus {

	@Getter private final Player player;
	@Getter private Team team;
	// Nota: un player senza team è per forza uno spettatore oppure la partita deve ancora cominciare.
	// I giocatori sono sempre in un team, anche se da soli.
	@Getter private boolean spectator;
	// Nota: uno spettatore non ha mai un team.
	@Getter @Setter private Kit selectedKit;
	
	@Getter private int shoppingPointsUsed;
	@Getter private int shoppingItemsBought;
	
	public PlayerStatus(Player player) {
		this.player = player;
	}
	
	public void setTeam(Team team) {
		this.team = team;
	}
	
	public void setSpectator(Player player, boolean spectator) {
		this.spectator = spectator;
		VanishManager.setHidden(player, spectator);
		if (spectator) {
			SpectatorAPI.setSpectator(player);
			CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.SPECTATOR);
		} else {
			SpectatorAPI.removeSpectator(player);
			CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.GAME);
		}
	}
	
	public void addBoughtShoppingItem(int price) {
		this.shoppingPointsUsed += price;
		this.shoppingItemsBought++;
	}

}
