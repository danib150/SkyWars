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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {
	
	public static boolean isHelmet(ItemStack itemStack) {
		switch (itemStack.getType()) {
			case LEATHER_HELMET:
			case CHAINMAIL_HELMET:
			case IRON_HELMET:
			case GOLD_HELMET:
			case DIAMOND_HELMET:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isChestplate(ItemStack itemStack) {
		switch (itemStack.getType()) {
			case LEATHER_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
			case IRON_CHESTPLATE:
			case GOLD_CHESTPLATE:
			case DIAMOND_CHESTPLATE:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isLeggings(ItemStack itemStack) {
		switch (itemStack.getType()) {
			case LEATHER_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case IRON_LEGGINGS:
			case GOLD_LEGGINGS:
			case DIAMOND_LEGGINGS:
				return true;
			default:
				return false;
		}
	}
	
	public static boolean isBoots(ItemStack itemStack) {
		switch (itemStack.getType()) {
			case LEATHER_BOOTS:
			case CHAINMAIL_BOOTS:
			case IRON_BOOTS:
			case GOLD_BOOTS:
			case DIAMOND_BOOTS:
				return true;
			default:
				return false;
		}
	}
	

}
