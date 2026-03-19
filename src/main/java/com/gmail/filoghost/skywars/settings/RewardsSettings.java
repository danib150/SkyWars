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
package com.gmail.filoghost.skywars.settings;

import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.gmail.filoghost.skywars.arena.chest.ChestItem;
import com.google.common.collect.Lists;

import net.cubespace.yamler.Path;
import net.cubespace.yamler.PreserveStatic;
import net.cubespace.yamler.YamlerConfig;
import net.cubespace.yamler.YamlerConfigurationException;
import wild.api.item.parsing.ItemParser;
import wild.api.item.parsing.ParserException;

@PreserveStatic
public class RewardsSettings extends YamlerConfig {
	
	@Path("items")
	private static List<String> serializedItems = Lists.newArrayList("50% stone, 10", "50% grass, 10");
	
	public static transient List<ChestItem> items;
	

	public RewardsSettings(Plugin plugin, String filename) {
		super(plugin, filename);
	}
	
	@Override
	public void init() throws YamlerConfigurationException {
		super.init();
		items = Lists.newArrayList();
		
		for (String item : serializedItems) {
			try {
	            String[] itemData = item.split(" ", 2);
	            if (itemData.length != 2) {
	            	throw new ParserException("Item format must be: [weight] [item]");
	            }
	
	            double weight = Double.parseDouble(itemData[0].replace("%", ""));
	            ItemStack itemStack = ItemParser.parse(itemData[1]);
	            
	            items.add(new ChestItem(itemStack, weight));
	            
			} catch (ParserException ex) {
				throw new YamlerConfigurationException("Invalid item '" + item + "': " + ex.getMessage());
			} catch (NumberFormatException ex) {
				throw new YamlerConfigurationException("Invalid item weight '" + item + "'.");
			}
        }
	}

}
