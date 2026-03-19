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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.gmail.filoghost.skywars.arena.menu.KitsMenu;
import com.gmail.filoghost.skywars.settings.KitSettings;
import com.gmail.filoghost.skywars.utils.InventoryUtils;
import com.gmail.filoghost.skywars.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.cubespace.yamler.YamlerConfigurationException;
import wild.api.item.parsing.ItemParser;
import wild.api.item.parsing.ItemParser.JsonReturner;
import wild.api.item.parsing.ParserException;
import wild.api.item.parsing.PotionEffectParser;

public class KitsManager {
	
	private static List<Kit> kits = Lists.newArrayList();
	private static Map<String, Kit> kitsByName = Maps.newHashMap();
	private static KitsMenu kitsMenu;
	
	public static Kit loadKit(KitSettings kitSettings) throws YamlerConfigurationException {
		if (kitSettings.price < 0) {
			throw new YamlerConfigurationException("Negative price: " + kitSettings.price);
		}

		KitItem helmet = null;
		KitItem chestplate = null;
		KitItem leggings = null;
		KitItem boots = null;
		KitItem shield = null;
		List<KitItem> items = Lists.newArrayList();
		List<PotionEffect> effects = Lists.newArrayList();
		
		List<ItemStackWrapper> itemWrappersLocal = Lists.newArrayList();
		
		if (kitSettings.items != null) {
			for (String serializedItem : kitSettings.items) {
				int position = -1;
				
				// Estrae posizione se presente
				if (serializedItem.matches("^[0-9]+\\|.+")) {
					String[] pieces = serializedItem.split("\\|", 2);
					
					try {
						position = Integer.parseInt(pieces[0]);
					} catch (NumberFormatException e) {
						throw new YamlerConfigurationException("Invalid position: " + pieces[0]);
					}
					serializedItem = pieces[1];
				}
			
				JsonReturner jsonReturner = new JsonReturner();
				ItemStack item;
				try {
					item = ItemParser.parse(serializedItem, jsonReturner);
				} catch (ParserException ex) {
					throw new YamlerConfigurationException("Invalid item '" + serializedItem + "': " + ex.getMessage());
				}
				ItemStackWrapper itemWrapper = new ItemStackWrapper(item, position);
				if (jsonReturner.getJson() != null) {
					if (jsonReturner.getJson().has("helmet")) {
						itemWrapper.setForceHelmet(jsonReturner.getJson().get("helmet").getAsBoolean());
					}
					
					if (jsonReturner.getJson().has("stack")) {
						itemWrapper.setForceStacking(jsonReturner.getJson().get("stack").getAsBoolean());
					}
					
					if (jsonReturner.getJson().has("translation")) {
						itemWrapper.setTranslation(jsonReturner.getJson().get("translation").getAsString());
					}
				}
				
				itemWrappersLocal.add(itemWrapper);
			}
		}
			
		// Ora iteriamo e distribuiamo le cose negli slot
		for (ItemStackWrapper itemWrapper : itemWrappersLocal) {
			
			ItemStack item = itemWrapper.getItemStack();
			String translation = itemWrapper.getTranslation();
			int position = itemWrapper.getPosition();

			if ((InventoryUtils.isHelmet(item) || itemWrapper.isForceHelmet()) && helmet == null) {
				helmet = new KitItem(item, translation, position, false);
			} else if (InventoryUtils.isChestplate(item) && chestplate == null) {
				chestplate = new KitItem(item, translation, position, false);
			} else if (InventoryUtils.isLeggings(item) && leggings == null) {
				leggings = new KitItem(item, translation, position, false);
			} else if (InventoryUtils.isBoots(item) && boots == null) {
				boots = new KitItem(item, translation, position, false);
			} else if (InventoryUtils.isShield(item) && shield == null) {
				shield = new KitItem(item, translation, position, false);
			} else {
				items.add(new KitItem(item, translation, position, itemWrapper.isForceStacking()));
			}
		}
		
		// Pozioni
		if (kitSettings.effects != null) {
			for (String serializedEffect : kitSettings.effects) {
				boolean show = true;
				if (serializedEffect.contains("hidden")) {
					serializedEffect = serializedEffect.replace("hidden", "");
					show = false;
				}
				try {
					PotionEffect potionEffect = PotionEffectParser.parse(serializedEffect, true, show);
					effects.add(potionEffect);
	
				} catch (ParserException ex) {
					throw new YamlerConfigurationException("Invalid effect '" + serializedEffect + "': " + ex.getMessage());
				}
			}
		}
		
		ItemStack icon;
		try {
			icon = ItemParser.parse(kitSettings.icon);
		} catch (ParserException ex) {
			throw new YamlerConfigurationException("Invalid menu icon '" + kitSettings.icon + "': " + ex.getMessage());
		}
		
		Kit kit = new Kit(kitSettings.name, icon, kitSettings.menuOrder, kitSettings.price, items, helmet, chestplate, leggings, boots, shield, effects);
		kits.add(kit);
		kitsByName.put(kit.getName(), kit);
		return kit;
	}

	public static void loadSelectorMenu() {
		Collections.sort(kits);
		kitsMenu = new KitsMenu(kits);
	}
	
	public static void openSelectorMenu(Player player) {
		if (kitsMenu != null) {
			kitsMenu.open(player);
		} else {
			Utils.reportAnomaly("kit selector menu not set");
		}
	}

}
