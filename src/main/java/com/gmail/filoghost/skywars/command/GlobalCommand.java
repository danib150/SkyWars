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
package com.gmail.filoghost.skywars.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.listener.ChatListener;

import wild.api.command.CommandFramework;

public class GlobalCommand extends CommandFramework {

	public GlobalCommand(JavaPlugin plugin, String label) {
		super(plugin, label);
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		
		if (args.length == 0) {
			if (ChatListener.forceGlobalChat.contains(player)) {
				ChatListener.forceGlobalChat.remove(player);
				player.sendMessage(ChatColor.YELLOW + "Hai " + ChatColor.RED + "disattivato" + ChatColor.YELLOW + " la scrittura chat globale.");
				player.sendMessage(ChatColor.YELLOW + "Scrivi " + ChatColor.GREEN + "/g" + ChatColor.YELLOW + " per attivare.");
			} else {
				ChatListener.forceGlobalChat.add(player);
				player.sendMessage(ChatColor.YELLOW + "Hai " + ChatColor.GREEN + "attivato" + ChatColor.YELLOW + " la scrittura chat globale.");
				player.sendMessage(ChatColor.YELLOW + "Scrivi " + ChatColor.RED + "/g" + ChatColor.YELLOW + " per disattivare.");
			}
		} else {
			String message = String.join(" ", args);
			ChatListener.forceGlobalChat.add(player);
			player.chat(message);
			ChatListener.forceGlobalChat.remove(player);
		}
	}

}
