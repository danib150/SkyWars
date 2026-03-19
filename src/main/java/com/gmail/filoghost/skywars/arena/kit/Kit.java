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
package com.gmail.filoghost.skywars.arena.kit;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import com.gmail.filoghost.skywars.utils.Format;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import wild.api.WildCommons;
import wild.api.menu.Icon;
import wild.api.menu.StaticIcon;

@AllArgsConstructor
public class Kit implements Comparable<Kit> {
	
	public static final ChatColor NAME_COLOR = ChatColor.GREEN;
	
	@Getter @NonNull private final String name;
	private final ItemStack menuIcon;
	private final int menuOrder;
	@Getter private final int price;
	@NonNull private final List<KitItem> items;
	private final KitItem helmet, chestplate, leggings, boots, shield;
	@NonNull private final List<PotionEffect> effects;
	
	
	public void apply(Player player) {
		WildCommons.removePotionEffects(player);
		WildCommons.clearInventoryFully(player);
		PlayerInventory inv = player.getInventory();
		
		if (helmet != null) {
			inv.setHelmet(helmet.getStackedItem());
		}
		if (chestplate != null) {
			inv.setChestplate(chestplate.getStackedItem());
		}
		if (leggings != null) {
			inv.setLeggings(leggings.getStackedItem());
		}
		if (boots != null) {
			inv.setBoots(boots.getStackedItem());
		}
		if (shield != null) {
			inv.setItemInHand(shield.getStackedItem());
		}
		
		for (KitItem kitItem : items) {
			int position = kitItem.getPosition();
			
			for (ItemStack kitItemStack : kitItem.getItemsToGive()) {
				if (position < 0) {
					inv.addItem(kitItemStack.clone());
				} else {
					while (position < inv.getSize() && isPositionUsed(inv, position)) {
						position++;
					}
					
					if (isPositionUsed(inv, position)) {
						// Non c'è proprio spazio, aggiunge nel primo posto libero
						inv.addItem(kitItemStack.clone());
					} else {
						inv.setItem(position, kitItemStack.clone());
					}
				}
			}
		}
		
		if (effects.size() > 0) {
			for (PotionEffect effect : effects) {
				player.addPotionEffect(effect, true);
			}
		}
	}
	
	
	private boolean isPositionUsed(Inventory inventory, int position) {
		return inventory.getItem(position) != null && inventory.getItem(position).getType() != Material.AIR;
	}
	
	
	public Icon generateIcon() {
		ItemStack iconItem = menuIcon.clone();
		
		ItemMeta meta = iconItem.getItemMeta();
		meta.setDisplayName(NAME_COLOR + name);
		
		List<String> lore = Lists.newArrayList();

		if (helmet != null) {
			lore.add(Format.formatKitItem(helmet));
		}
		if (chestplate != null) {
			lore.add(Format.formatKitItem(chestplate));
		}
		if (leggings != null) {
			lore.add(Format.formatKitItem(leggings));
		}
		if (boots != null) {
			lore.add(Format.formatKitItem(boots));
		}
		if (shield != null) {
			lore.add(Format.formatKitItem(shield));
		}
		
		for (KitItem item : items) {
			lore.add(Format.formatKitItem(item));
		}
		
		if (!effects.isEmpty()) {
			lore.add("");
			
			for (PotionEffect effect : effects) {
				lore.add(Format.formatEffect(effect));
			}
		}
		
		lore.add("");	
		lore.add(ChatColor.GOLD + "Prezzo: " + Format.formatPoints(price));
		
		meta.setLore(lore);
		iconItem.setItemMeta(meta);
		
		return new StaticIcon(iconItem);
	}


	@Override
	public int compareTo(Kit other) {
		int diff = this.menuOrder - other.menuOrder;
		
		if (diff == 0) {
			diff = this.name.hashCode() - other.name.hashCode();
		}
		
		return diff;
	}

}
