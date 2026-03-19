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
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenasManager;

import wild.api.command.CommandFramework;

public class SpawnCommand extends CommandFramework {
	
	public SpawnCommand(JavaPlugin plugin, String label) {
		super(plugin, label);
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Arena arena = ArenasManager.getArenaByPlayer(player);

		if (arena != null && arena.isFightingPlayer(player)) {
			if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
				arena.removePlayerAndEventuallyProcessDeath(player);
				SkyWars.setupToLobby(player);
				player.sendMessage(ChatColor.YELLOW + "Hai interrotto la partita e sei andato allo spawn.");
			} else {
				player.sendMessage(ChatColor.RED + "La partita è ancora in corso. Se sei sicuro di voler uscire, scrivi " + ChatColor.GRAY + "/" + this.label + " confirm");
			}
			
		} else {
			if (arena != null) {
				arena.removePlayerAndEventuallyProcessDeath(player);
				SkyWars.setupToLobby(player);
			} else {
				// E' già allo spawn, non c'è bisogno di fare altro
				player.teleport(SkyWars.getSpawn(), TeleportCause.COMMAND);
			}
			
			player.sendMessage(ChatColor.GREEN + "Sei andato allo spawn.");
		}
	}
	

}
