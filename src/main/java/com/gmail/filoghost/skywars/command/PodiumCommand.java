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

import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.Perms;
import com.gmail.filoghost.skywars.settings.PodiumSettings;
import com.gmail.filoghost.skywars.settings.objects.BlockConfig;
import com.gmail.filoghost.skywars.utils.PodiumPosition;

import lombok.AllArgsConstructor;
import net.cubespace.yamler.YamlerConfigurationException;
import wild.api.command.CommandFramework;
import wild.api.command.CommandFramework.Permission;

@Permission(Perms.COMMAND_PODIUM)
public class PodiumCommand extends CommandFramework {
	
	@AllArgsConstructor
	private enum PodiumType {
		KILLS (() -> PodiumSettings.kills_heads, () -> PodiumSettings.kills_signs),
		WINS (() -> PodiumSettings.wins_heads, () -> PodiumSettings.wins_signs);


		private ConfigGetter headsConfigGetter, signsConfigGetter;
	}
	
	private interface ConfigGetter {
		Map<String, BlockConfig> getConfig();
	}
	

	public PodiumCommand(JavaPlugin plugin, String label) {
		super(plugin, label);
	}
	
	
	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		Player player = CommandValidate.getPlayerSender(sender);
		
		if (args.length < 3) {
			player.sendMessage(ChatColor.GOLD + "========== Comandi /" + super.label + " ==========");
			for (PodiumType rankingType : PodiumType.values()) {
				player.sendMessage(ChatColor.YELLOW + "/" + super.label + " " + rankingType.name().toLowerCase() + " <head|sign> <1-3>");
			}
			return;
		}
		
		PodiumType rankingType;
		try {
			rankingType = PodiumType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ExecuteException("Tipo di classifica non valida.");
		}
		
		String rankingPart = args[1].toLowerCase();
		
		int number = CommandValidate.getInteger(args[2]);
		CommandValidate.isTrue(1 <= number && number <= PodiumPosition.size(), "Il numero deve essere compreso tra 1 e " + PodiumPosition.size() + ".");
		PodiumPosition podiumPosition = PodiumPosition.values()[number - 1];
		
		Map<String, BlockConfig> targetBlocks;
		if (rankingPart.equals("head")) {
			targetBlocks = rankingType.headsConfigGetter.getConfig();
		} else if (rankingPart.equals("sign")) {
			targetBlocks = rankingType.signsConfigGetter.getConfig();
		} else {
			throw new ExecuteException("Specifica \"head\" oppure \"sign\".");
		}
		
		Block block = player.getTargetBlock((Set<Material>) null, 64);
		if (rankingPart.equals("head")) {
			CommandValidate.isTrue(block.getType() == Material.SKULL, "Non stai guardando una testa.");
		} else if (rankingPart.equals("sign")) {
			CommandValidate.isTrue(block.getType() == Material.WALL_SIGN, "Non stai guardando un cartello a muro.");
		}
		
		targetBlocks.put(podiumPosition.name(), new BlockConfig(block));
		try {
			SkyWars.getPodiumSettings().save();
			sender.sendMessage(ChatColor.GREEN + "Hai impostato " + (rankingPart.equals("head") ? "la testa" : "il cartello") + " per il " + number + "° classificato.");
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile salvare la configurazione del podio.");
		}
	}
}
