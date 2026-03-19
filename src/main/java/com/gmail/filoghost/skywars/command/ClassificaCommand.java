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

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.database.SQLSingleStat;
import com.gmail.filoghost.skywars.utils.Ranking;

import wild.api.command.CommandFramework;
import wild.api.uuid.UUIDRegistry;

public class ClassificaCommand extends CommandFramework {

	public ClassificaCommand(JavaPlugin plugin, String label) {
		super(plugin, label);
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(header("Comandi classifica"));
			sender.sendMessage(ChatColor.YELLOW + "/" + super.label + " uccisioni" + ChatColor.GRAY + " - numero di uccisioni totali");
			sender.sendMessage(ChatColor.YELLOW + "/" + super.label + " vittorie" + ChatColor.GRAY + " - numero di vittorie totali");
			sender.sendMessage("");
			return;
		}
		
		if (args[0].equalsIgnoreCase("uccisioni")) {
			sendRankingList(sender, "Classifica uccisioni", Ranking.getTopKills());
		} else if (args[0].equalsIgnoreCase("vittorie")) {
			sendRankingList(sender, "Classifica vittorie", Ranking.getTopWins());
		} else {
			sender.sendMessage(ChatColor.RED + "Tipo di classifica non valido. Scrivi /" + label + " per i comandi.");
		}
	}
	
	private void sendRankingList(CommandSender sender, String title, List<SQLSingleStat> list) {
		sender.sendMessage(header(title));
		for (int i = 0; i < list.size(); i++) {
			SQLSingleStat entry = list.get(i);
			sender.sendMessage("" + ChatColor.DARK_GRAY + (i+1) + ". " + ChatColor.GRAY + entry.getValue() + ChatColor.DARK_GRAY + " - " + ChatColor.WHITE + UUIDRegistry.getNameFallback(entry.getPlayerUUID()));
		}
		sender.sendMessage("");
		return;
	}
	
	private String header(String title) {
		return "" + ChatColor.GOLD + ChatColor.STRIKETHROUGH + "-----" + ChatColor.RESET + " " + ChatColor.GOLD + ChatColor.BOLD + title + " " + ChatColor.GOLD + ChatColor.STRIKETHROUGH + "-----";
	}
}
