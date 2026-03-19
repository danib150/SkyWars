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
package com.gmail.filoghost.skywars.utils;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.skywars.arena.kit.KitItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import wild.api.translation.Translation;
import wild.api.util.UnitFormatter;

public class Format {

	private static final String LORE_EFFECT = 	ChatColor.DARK_BLUE + "\u25CF" + ChatColor.BLUE;
	private static final String LORE_ITEM = 	ChatColor.DARK_GRAY + "\u25CF" + ChatColor.GRAY;
	private static final String LORE_AMOUNT = 	ChatColor.WHITE + "x";
	private static final String LORE_ENCHANT = 	ChatColor.DARK_PURPLE + "";
	
	private static Set<PotionEffectType> uselessAmplifier = Sets.newHashSet(
			PotionEffectType.BLINDNESS,
			PotionEffectType.CONFUSION,
			PotionEffectType.FIRE_RESISTANCE,
			PotionEffectType.INVISIBILITY,
			PotionEffectType.NIGHT_VISION,
			PotionEffectType.WITHER
	);


	public static String formatKitItem(KitItem kitItem) {
		StringBuilder output = new StringBuilder();

		output.append(Format.LORE_ITEM);
		output.append(" ");
		output.append(kitItem.getNameTranslation());

		ItemStack itemStack = kitItem.getStackedItem();

		if (itemStack != null && itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
            List<String> pieces = Lists.newArrayList();

			try {
				Potion potion = Potion.fromItemStack(itemStack);
				PotionEffect baseEffect = Utils.convertToPotionEffect(potion);

                pieces.add(formatEffectNoColor(baseEffect));
            } catch (Exception ignored) {
				// item non interpretabile come pozione vanilla
			}

			// Effetti custom
			for (PotionEffect effect : potionMeta.getCustomEffects()) {
				pieces.add(formatEffectNoColor(effect));
			}

			if (!pieces.isEmpty()) {
				output.append(" di ");
				output.append(StringUtils.join(pieces, ", "));
			}
		}

		if (itemStack.getAmount() > 1) {
			output.append(" ");
			output.append(Format.LORE_AMOUNT);
			output.append(itemStack.getAmount());
		}

		if (!itemStack.getEnchantments().isEmpty()) {
			List<String> pieces = Lists.newArrayList();

			for (Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
				pieces.add(Translation.of(entry.getKey()) + " " + UnitFormatter.getRoman(entry.getValue()));
			}

			output.append(" ");
			output.append(LORE_ENCHANT);
			output.append(StringUtils.join(pieces, ", "));
		}

		return output.toString();
	}
	
	public static String formatEffect(PotionEffect effect) {
		return Format.LORE_EFFECT + " " + formatEffectNoColor(effect);
	}
	
	private static String formatEffectNoColor(PotionEffect effect) {
		StringBuilder output = new StringBuilder();
		output.append(Translation.of(effect.getType()));
		
		if (!uselessAmplifier.contains(effect.getType())) {
			output.append(" ");
			output.append(UnitFormatter.getRoman(effect.getAmplifier() + 1));
		}
		
		if (!effect.getType().isInstant() && effect.getDuration() < Integer.MAX_VALUE) {
			int seconds = effect.getDuration() / 20;
			int minutes = 0;
			
			if (seconds >= 60) {
				minutes = seconds / 60;
				seconds = seconds % 60;
			}
			
			output.append(" (");
			output.append(minutes);
			output.append(":");
			if (seconds < 10) output.append("0");
			output.append(seconds);
			output.append(")");
		}

		return output.toString();
	}

	public static String formatPoints(int price) {
		if (price == 1) {
			return "1 punto";
		} else {
			return price + " punti";
		}
	}
	
	public static String formatSingularPlural(int quantity, String singular, String plural) {
		if (quantity == 1) {
			return "1 " + singular;
		} else {
			return quantity + " " + plural;
		}
	}
	
}
