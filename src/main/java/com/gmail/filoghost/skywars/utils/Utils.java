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

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;

import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenaModel;
import com.gmail.filoghost.skywars.arena.entities.EntityOwnership;

public class Utils {

	private static Map<Player, ItemStack> headsByPlayer = new WeakHashMap<>();

	public static ItemStack getHeadItem(Player player) {
		ItemStack headItem = headsByPlayer.get(player);

		if (headItem == null) {
			headItem = new ItemStack(Material.SKULL_ITEM);
			headItem.setDurability((short) 3);
			SkullMeta meta = (SkullMeta) headItem.getItemMeta();
			meta.setOwner(player.getName());
			headItem.setItemMeta(meta);
			headsByPlayer.put(player, headItem);
		}

		return headItem;
	}

	public static void consumeOneItemInHand(Player player) {
		PlayerInventory inventory = player.getInventory();
		ItemStack item = inventory.getItemInHand();

		if (item == null) return;

		if (item.getAmount() > 1) {
			item.setAmount(item.getAmount() - 1);
		} else {
			inventory.setItemInHand(null);
		}
	}

	public static Location getRandomAround(Player player) {
		Location loc = player.getLocation();
		double angle = Math.random() * Math.PI * 2;
		return new Location(player.getWorld(), loc.getX() + Math.cos(angle) * 2.0, loc.getY(), loc.getZ() + Math.sin(angle) * 2.0);
	}

	public static PlayerDamageSource getPlayerDamageSource(final Entity originalAttackerEntity) {
		if (originalAttackerEntity == null) {
			return null;
		}
		
		Player attackerPlayer = null;
		Entity attackerEntity = originalAttackerEntity;

		if (originalAttackerEntity.getType() == EntityType.PLAYER) {
			attackerPlayer = (Player) originalAttackerEntity;

		} else if (originalAttackerEntity instanceof Projectile) {
			Projectile originalAttackerProjectile = (Projectile) originalAttackerEntity;
			ProjectileSource shooter = originalAttackerProjectile.getShooter();
			
			if (shooter instanceof Player) {
				attackerPlayer = (Player) shooter;
			} else if (shooter instanceof Entity) {
				// Lo shooter potrebbe essere un'entità generata da un giocatore
				attackerEntity = (Entity) shooter;
			}
		}

		EntityOwnership ownership = EntityOwnership.get(attackerEntity);
		if (ownership != null && ownership.getPlayerUUID() != null) {
			attackerPlayer = Bukkit.getPlayer(ownership.getPlayerUUID());
		}
		
		if (attackerPlayer != null && attackerPlayer.isOnline()) {
			return new PlayerDamageSource(attackerPlayer, attackerEntity);
		} else {
			return null;
		}
	}

	public static boolean canFullyFitItem(Inventory inventory, ItemStack reward) {
		int remaining = reward.getAmount();

		for (ItemStack inventoryItem : inventory.getContents()) {
			if (inventoryItem == null || inventoryItem.getType() == Material.AIR) {
				return true; // C'è almeno uno spazio vuoto
			}

			if (inventoryItem.isSimilar(reward)) {
				remaining -= inventoryItem.getMaxStackSize() - inventoryItem.getAmount();
				if (remaining <= 0) {
					return true;
				}
			}
		}

		return false;
	}

	public static Location roundedLocation(Location location) {
		Location roundedLocation = location.clone();
		roundedLocation.setPitch(0);
		if (Math.abs(roundedLocation.getYaw()) % 45.0f != 0.0f) {
			float yawNormalized = Math.round(roundedLocation.getYaw() / 45.0f) * 45.0f;
			roundedLocation.setYaw(yawNormalized);
		}

		roundedLocation.setX(Math.round(roundedLocation.getX() * 2.0) / 2.0);
		roundedLocation.setZ(Math.round(roundedLocation.getZ() * 2.0) / 2.0);
		return roundedLocation;
	}

	public static PotionEffect convertToPotionEffect(Potion potion) {
		PotionType type = potion.getType();
		PotionEffectType effectType = type.getEffectType();

		if (effectType == null) {
			return null;
		}

		boolean upgraded = potion.getLevel() > 1;
		boolean extended = potion.hasExtendedDuration();

		if (effectType == PotionEffectType.HEAL || effectType == PotionEffectType.HARM) {
			return new PotionEffect(effectType, 1, upgraded ? 1 : 0);
		} else if (effectType == PotionEffectType.REGENERATION || effectType == PotionEffectType.POISON) {
			if (extended) {
				return new PotionEffect(effectType, 1800, 0);
			} else if (upgraded) {
				return new PotionEffect(effectType, 440, 1);
			} else {
				return new PotionEffect(effectType, 900, 0);
			}
		} else if (effectType == PotionEffectType.NIGHT_VISION
				|| effectType == PotionEffectType.INVISIBILITY
				|| effectType == PotionEffectType.FIRE_RESISTANCE
				|| effectType == PotionEffectType.WATER_BREATHING) {
			return new PotionEffect(effectType, extended ? 9600 : 3600, 0);
		} else if (effectType == PotionEffectType.WEAKNESS
				|| effectType == PotionEffectType.SLOW) {
			return new PotionEffect(effectType, extended ? 4800 : 1800, 0);
		} else if (extended) {
			return new PotionEffect(effectType, 9600, 0);
		} else if (upgraded) {
			return new PotionEffect(effectType, 1800, 1);
		} else {
			return new PotionEffect(effectType, 3600, 0);
		}
	}

	public static boolean deleteFolder(@Nonnull File file) {
		if (file.exists()) {
			boolean result = true;

			if (file.isDirectory()) {
				File[] contents = file.listFiles();

				if (contents != null) {
					for (File f : contents) {
						result = result && deleteFolder(f);
					}
				}
			}

			return result && file.delete();
		}

		return false;
	}

	public static void reportAnomaly(String message, Object... params) {
		StringBuilder paramsString = new StringBuilder();
		for (Object param : params) {
			if (paramsString.length() > 0) {
				paramsString.append(", ");
			}
			paramsString.append(formatParam(param));
		}

		Thread.dumpStack();
		Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + message + (paramsString.length() > 0 ? " (" + paramsString + ")" : ""));
	}

	private static String formatParam(Object param) {
		if (param instanceof Player) {
			return "player: " + ((Player) param).getName();
		} else if (param instanceof ArenaModel) {
			return "arena model: " + ((ArenaModel) param).getName();
		} else if (param instanceof Arena) {
			return "arena: " + ((Arena) param).getName();
		} else if (param instanceof Enum) {
			return param.getClass().getSimpleName() + ": " + ((Enum<?>) param).name();
		} else {
			return "unknown object: " + param.toString();
		}
	}

}
