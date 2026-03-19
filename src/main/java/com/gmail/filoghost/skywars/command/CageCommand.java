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

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.Perms;
import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.settings.PregameCageSettings;
import com.gmail.filoghost.skywars.settings.objects.BlockConfig;
import com.gmail.filoghost.skywars.settings.objects.LocationConfig;
import com.gmail.filoghost.skywars.utils.Utils;
import net.cubespace.yamler.YamlerConfigurationException;
import net.md_5.bungee.api.ChatColor;
import wild.api.command.CommandFramework.Permission;
import wild.api.command.SubCommandFramework;

@Permission(Perms.COMMAND_CAGE)
public class CageCommand extends SubCommandFramework {
	
	
	public CageCommand(JavaPlugin plugin, String label) {
		super(plugin, label);
	}

	@Override
	public void noArgs(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_GREEN + "Lista comandi " + this.label + ":");
		for (SubCommandDetails sub : this.getAccessibleSubCommands(sender)) {
			sender.sendMessage(ChatColor.GREEN + "/" + this.label + " " + sub.getName() + (sub.getUsage() != null ?  " " + sub.getUsage() : ""));
		}
	}
	
	private void save(CommandSender sender, String successMessage) {
		try {
			new PregameCageSettings(SkyWars.get(), "pregame-cage.yml").save();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile salvare la configurazione.");
		}

		sender.sendMessage(ChatColor.GREEN + successMessage);
	}	
	
	@SubCommand("loc1")
	public void loc1(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Location loc1 = player.getLocation();
		PregameCageSettings.corner1 = new BlockConfig(loc1.getWorld().getName(), loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ());
		save(player, "Hai impostato la posizione 1.");
	}

	@SubCommand("loc2")
	public void loc2(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Location loc2 = player.getLocation();
		PregameCageSettings.corner2 = new BlockConfig(loc2.getWorld().getName(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
		save(player, "Hai impostato la posizione 2.");
	}
	
	
	@SubCommand("spawn")
	public void spawn(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		PregameCageSettings.spawn = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		save(player, "Hai impostato il punto di spawn nella lobby.");
	}

}
