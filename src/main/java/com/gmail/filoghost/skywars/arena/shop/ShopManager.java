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
package com.gmail.filoghost.skywars.arena.shop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.menu.ItemShopMenu;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.database.PlayerData;
import com.gmail.filoghost.skywars.utils.Format;
import com.gmail.filoghost.skywars.utils.Utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShopManager {
	
	private static final int MAX_BUYABLE_ITEMS_PER_GAME = 5;
	private static final int MAX_POINTS_USED_PER_GAME = 100;
	
	private final Arena arena;
	private final ItemShopMenu shopMenu;
	
	public void openShop(Player player) {
		if (!arena.isFightingPlayer(player)) {
			player.sendMessage(ChatColor.RED + "Non puoi aprire il negozio ora.");
			return;
		}
		
		shopMenu.open(player);
	}
	
	
	private boolean collectPrice(Player player, int price) {
		PlayerData playerData = SkyWars.getOnlinePlayerData(player);
		if (playerData.getScore() < price) {
			player.sendMessage(ChatColor.RED + "Non hai abbastanza punti.");
			return false;
		}
		
		playerData.subtractScore(price);
		return true;
	}
	
	
	public boolean tryBuyFromShop(Player player, int price, ItemStack reward) {
		PlayerStatus playerStatus = arena.getFightingPlayerStatus(player);
		
		if (playerStatus == null) {
			player.sendMessage(ChatColor.RED + "Non puoi fare acquisti ora.");
			
			playerStatus = arena.getPlayerStatus(player);
			if (playerStatus.isSpectator()) {
				Utils.reportAnomaly("trying to buy from shop while spectator", this, player);
			} else if (playerStatus.getTeam() == null) {
				Utils.reportAnomaly("trying to buy from shop with no team", this, player);
			}
			return false;
		}
		
		if (playerStatus.getShoppingPointsUsed() + price > MAX_POINTS_USED_PER_GAME) {
			player.sendMessage(ChatColor.RED + "Non puoi spendere più di " + Format.formatPoints(MAX_POINTS_USED_PER_GAME) + " in una singola partita.");
			return false;
		}
		
		if (playerStatus.getShoppingItemsBought() >= MAX_BUYABLE_ITEMS_PER_GAME) {
			player.sendMessage(ChatColor.RED + "Non puoi comprare più di " + Format.formatSingularPlural(MAX_BUYABLE_ITEMS_PER_GAME, "oggetto", "oggetti") + " in una singola partita.");
			return false;
		}
		
		if (!Utils.canFullyFitItem(player.getInventory(), reward)) {
			player.sendMessage(ChatColor.RED + "Il tuo inventario è pieno.");
			return false;
		}
			
		if (collectPrice(player, price)) {
			player.getInventory().addItem(reward);
			playerStatus.addBoughtShoppingItem(price);
			return true;
		}

		return false;
	}

}
