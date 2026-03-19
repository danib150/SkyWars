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
package com.gmail.filoghost.skywars.arena.reward;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.chest.ChestItem;
import com.gmail.filoghost.skywars.settings.RewardsSettings;

import wild.api.item.ItemBuilder;
import wild.api.translation.Translation;

public class KillReward {

	private static final Random RNG = new Random();
	
	private static ItemStack REWARD_ITEM_ACTIVATOR = ItemBuilder
			.of(Material.MAGMA_CREAM)
			.enchant(Enchantment.DURABILITY)
			.name(ChatColor.GOLD + "Ricompensa uccisione")
			.lore(
				ChatColor.GRAY +  "Un premio che si riceve dopo un uccisione,",
				ChatColor.GRAY + "contiene una ricompensa " + ChatColor.WHITE + ChatColor.BOLD + "casuale" + ChatColor.GRAY + ".",
				"",
				ChatColor.GRAY + "" + ChatColor.ITALIC + "Clicca col destro tenendolo in mano!"
			).build();
	
	private static List<ChestItem> possibleRewards;
	private static int totalWeight;
	
	
	public static void init() {
		possibleRewards = RewardsSettings.items;
		
		totalWeight = 0;
		for (ChestItem possibleReward : possibleRewards) {
			totalWeight += possibleReward.getChance();
		}
		
		printProbabilities();
	}
	
	private static void printProbabilities() {
		DecimalFormat format = new DecimalFormat("0.00");
		for (ChestItem chestItem : possibleRewards) {
			SkyWars.get().getLogger().info("[Kill Reward] " + chestItem.getItem().getType() + " " + format.format(chestItem.getChance() * 100.0D / totalWeight) + "%");
		}
	}
	
	public static void giveActivatorItem(Player player) {
		addSafe(player, REWARD_ITEM_ACTIVATOR);
	}
	
	public static boolean isActivatorItem(ItemStack item) {
		return REWARD_ITEM_ACTIVATOR.isSimilar(item);
	}
	
	public static void giveRandomReward(Player player) {
		double pseudoIndex = RNG.nextDouble() * totalWeight;
		for (ChestItem reward : possibleRewards) {
			pseudoIndex -= reward.getChance();
			
			if (pseudoIndex <= 0) {
				Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
					player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_BLAST, 1F, 1F);
					player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_TWINKLE, 1F, 1F);
					addSafe(player, reward.getItem());
					player.updateInventory();
					player.sendMessage(ChatColor.YELLOW + ">> " + ChatColor.GREEN + "Hai ricevuto " + Translation.of(reward.getItem().getType()) + " x" + reward.getItem().getAmount() + "!");
				});
				return;
			}
		}
	}
	
	private static void addSafe(Player player, ItemStack item) {
		HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item.clone());
		if (!remaining.isEmpty()) {
			for (ItemStack i : remaining.values()) {
				Item drop = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.5)), i);
				drop.setVelocity(new Vector(0, 0, 0));
			}
		}
	}
	
}
