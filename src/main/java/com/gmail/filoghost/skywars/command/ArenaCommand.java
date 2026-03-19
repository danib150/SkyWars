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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.Perms;
import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.settings.objects.ArenaConfig;
import com.gmail.filoghost.skywars.settings.objects.BlockConfig;
import com.gmail.filoghost.skywars.settings.objects.LocationConfig;
import com.gmail.filoghost.skywars.utils.Utils;
import com.google.common.collect.Lists;

import net.cubespace.yamler.YamlerConfigurationException;
import net.md_5.bungee.api.ChatColor;
import wild.api.chat.ChatBuilder;
import wild.api.command.CommandFramework.Permission;
import wild.api.sound.EasySound;
import wild.api.command.SubCommandFramework;
import wild.api.util.CaseInsensitiveMap;

@Permission(Perms.COMMAND_ARENA)
public class ArenaCommand extends SubCommandFramework {
	
	private static final DecimalFormat COORD_FORMAT = new DecimalFormat("0.00");
	
	private Map<String, ArenaConfig> playerSetups = new CaseInsensitiveMap<>();
	
	
	public ArenaCommand(JavaPlugin plugin, String label) {
		super(plugin, label);
	}

	private ArenaConfig getCurrentSetup(Player player) {
		return playerSetups.get(player.getName());
	}
	
	private void setCurrentSetup(Player player, ArenaConfig config) {
		playerSetups.put(player.getName(), config);
	}
	
	private ArenaConfig getCurrentSetupNotNull(Player player) {
		ArenaConfig setup = playerSetups.get(player.getName());
		CommandValidate.notNull(setup, "Non hai nessuna arena caricata.");
		return setup;
	}
	
	@Override
	public void noArgs(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_GREEN + "Lista comandi " + this.label + ":");
		for (SubCommandDetails sub : this.getAccessibleSubCommands(sender)) {
			sender.sendMessage(ChatColor.GREEN + "/" + this.label + " " + sub.getName() + (sub.getUsage() != null ?  " " + sub.getUsage() : ""));
		}
	}
	

	@SubCommand("info")
	@SubCommandUsage("[-v]")
	public void info(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		player.sendMessage(ChatColor.WHITE + "Arena in fase di modifica: " + ChatColor.GRAY + currentSetup.name);
		player.sendMessage(ChatColor.WHITE + "Giocatori massimi per team: " + ChatColor.GRAY + currentSetup.maxPlayersPerTeam);

		sendLocationInfo(player, "Lobby: ", currentSetup.lobby);
		sendBlockInfo(player, "Cartello: ", currentSetup.sign);
		
		player.sendMessage(ChatColor.WHITE + "Punti di spawn: ");
		for (Entry<String, LocationConfig> entry : currentSetup.spawnPoints.entrySet()) {
			sendLocationInfo(player, " - " + entry.getKey() + ": ", entry.getValue());
		}
		
		if (Arrays.asList(args).contains("-v")) {
			displayArenaVolume(player, currentSetup);
		} else {
			player.sendMessage(ChatColor.GRAY + "Usa \"-v\" per visualizzare il volume dell'arena");
		}
	}
	
	private static void sendLocationInfo(Player player, String prefix, LocationConfig loc) {
		ChatBuilder messageBuilder = new ChatBuilder(prefix).color(ChatColor.WHITE);
		if (loc != null) {
			messageBuilder.append(COORD_FORMAT.format(loc.x) + ", " + COORD_FORMAT.format(loc.y) + ", " + COORD_FORMAT.format(loc.z)).color(ChatColor.GRAY);
			messageBuilder.tooltip(ChatColor.LIGHT_PURPLE, "Clicca per teletrasportarti!");
			messageBuilder.runCommand(
				"/arena tp " + 
				loc.worldName + " " + 
				COORD_FORMAT.format(loc.x) + " " + 
				COORD_FORMAT.format(loc.y) + " " + 
				COORD_FORMAT.format(loc.z) + " " + 
				COORD_FORMAT.format(loc.yaw) + " " + 
				COORD_FORMAT.format(loc.pitch)
			);
		} else {
			messageBuilder.append("-").color(ChatColor.GRAY);
		}
		messageBuilder.send(player);
	}
	
	private static void sendBlockInfo(Player player, String prefix, BlockConfig block) {
		ChatBuilder messageBuilder = new ChatBuilder(prefix).color(ChatColor.WHITE);
		if (block != null) {
			messageBuilder.append(COORD_FORMAT.format(block.x) + ", " + COORD_FORMAT.format(block.y) + ", " + COORD_FORMAT.format(block.z)).color(ChatColor.GRAY);
			messageBuilder.tooltip(ChatColor.LIGHT_PURPLE, "Clicca per teletrasportarti!");
			messageBuilder.runCommand(
				"/arena tp " + 
				block.worldName + " " + 
				(block.x + 0.5) + " " + 
				(block.y + 0.5) + " " + 
				(block.z + 0.5)
			);
		} else {
			messageBuilder.append("-").color(ChatColor.GRAY);
		}
		messageBuilder.send(player);
	}
	
	@SubCommand("new")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void _new(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		String name = args[0];
		
		CommandValidate.isTrue(getCurrentSetup(player) == null, "Stai già modificando un'arena. Fai prima /" + this.label + " unload o /" + this.label + " save");
		CommandValidate.isTrue(ArenasManager.getModel(name) == null, "Esiste già un'arena con quel nome.");
		
		ArenaConfig currentSetup = new ArenaConfig();
		setCurrentSetup(player, currentSetup);
		currentSetup.name = name;
		
		player.sendMessage(ChatColor.GREEN + "Stai creando l'arena " + name + ".");
	}
	
	
	@SubCommand("load")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void load(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		String name = args[0];
		CommandValidate.isTrue(getCurrentSetup(player) == null, "Stai già modificando un'arena. Fai prima /" + this.label + " unload.");
		
		ArenaConfig config;
		
		try {
			config = SkyWars.loadArenaConfig(name);
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile caricare il file dell'arena " + name + ".");
		}
		
		CommandValidate.notNull(config, "Arena non trovata.");
		setCurrentSetup(player, config);
		
		player.sendMessage(ChatColor.GREEN + "Hai caricato l'arena " + name + ".");
	}
	
	
	@SubCommand("unload")
	public void unload(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		getCurrentSetupNotNull(player);
		
		setCurrentSetup(player, null);
		player.sendMessage(ChatColor.GREEN + "Hai scaricato l'arena, le eventuali modifiche non salvate sono state ignorate.");
	}
	
	
	@SubCommand("save")
	public void save(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		CommandValidateExtra.checkArenaConfig(currentSetup);
		
		try {
			SkyWars.saveArenaConfig(currentSetup);
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile salvare il file dell'arena " + currentSetup.name + ".");
		}
		
		player.sendMessage(ChatColor.GREEN + "Hai salvato le modifiche all'arena " + currentSetup.name + ".");
	}
	
	@SubCommand("playersPerTeam")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<numero>")
	public void playersPerTeam(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		currentSetup.maxPlayersPerTeam = CommandValidate.getPositiveIntegerNotZero(args[0]);
		player.sendMessage(ChatColor.GREEN + "Hai impostato il numero di giocatori per team.");
	}
	
	
	@SubCommand("loc1")
	public void loc1(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		Location loc1 = player.getLocation();
		currentSetup.corner1 = new BlockConfig(loc1.getWorld().getName(), loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ());
		player.sendMessage(ChatColor.GREEN + "Hai impostato la posizione 1.");
	}

	@SubCommand("loc2")
	public void loc2(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		Location loc2 = player.getLocation();
		currentSetup.corner2 = new BlockConfig(loc2.getWorld().getName(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
		player.sendMessage(ChatColor.GREEN + "Hai impostato la posizione 2.");
	}
	
	
	@SubCommand("lobby")
	public void lobby(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		currentSetup.lobby = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		player.sendMessage(ChatColor.GREEN + "Hai impostato la lobby.");
	}
	
	@SubCommand("sign")
	public void sign(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		Block signBlock = CommandValidateExtra.getTargetBlock(player, Material.WALL_SIGN, "Non stai guardando un cartello sul muro.");
		
		currentSetup.sign = new BlockConfig(signBlock);
		player.sendMessage(ChatColor.GREEN + "Hai impostato il cartello.");
	}
	
	@SubCommand("setSpawn")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void addspawn(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		String spawnName = args[0].toLowerCase();
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		LocationConfig locationConfig = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		currentSetup.spawnPoints.put(spawnName, locationConfig);
		player.sendMessage(ChatColor.GREEN + "Hai impostato il punto di spawn " + spawnName);
	}
	
	
	@SubCommand("removeSpawn")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void removespawn(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		String spawnName = args[0].toLowerCase();
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		CommandValidate.isTrue(!currentSetup.spawnPoints.isEmpty(), "Non ci sono ancora punti di spawn impostati.");
		CommandValidate.isTrue(currentSetup.spawnPoints.containsKey(spawnName), "Punto di spawn non trovato.");
		
		currentSetup.spawnPoints.remove(spawnName);
		player.sendMessage(ChatColor.GREEN + "Hai rimosso il punto di spawn " + spawnName);
	}
	
	
	@SubCommand("tp")
	@SubCommandMinArgs(4)
	@SubCommandUsage("<mondo> <x> <y> <z> [yaw] [pitch]")
	public void tp(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		World world = Bukkit.getWorld(args[0]);
		CommandValidate.notNull(world, "Mondo non trovato.");
		double x = CommandValidate.getDouble(args[1]);
		double y = CommandValidate.getDouble(args[2]);
		double z = CommandValidate.getDouble(args[3]);
		float yaw = 0;
		if (args.length > 4) {
			yaw = (float) CommandValidate.getDouble(args[4]);
		}
		float pitch = 0;
		if (args.length > 5) {
			pitch = (float) CommandValidate.getDouble(args[5]);
		}
		
		player.teleport(new Location(world, x, y, z, yaw, pitch), TeleportCause.COMMAND);
		EasySound.quickPlay(player, Sound.ENDERMAN_TELEPORT);
	}

	
	@SuppressWarnings("deprecation")
	private void displayArenaVolume(Player player, ArenaConfig currentSetup) {
		if (currentSetup.corner1 != null && currentSetup.corner2 != null) {
			Block corner1 = currentSetup.corner1.getBlock();
			Block corner2 = currentSetup.corner2.getBlock();
			World world = corner1.getWorld();
			
			List<Block> changedBlocks = Lists.newArrayList();
			
			for (int x : range(corner1.getX(), corner2.getX())) {
				for (int y : range(corner1.getY(), corner2.getY())) {
					for (int z : range(corner1.getZ(), corner2.getZ())) {
						Block block = world.getBlockAt(x, y, z);
						
						if (countTrues(x == corner1.getX(), x == corner2.getX(), y == corner1.getY(), y == corner2.getY(), z == corner1.getZ(), z == corner2.getZ()) >= 2) {
							DyeColor woolColor = (x + y + z) % 2 == 0 ? DyeColor.BLACK : DyeColor.YELLOW;
							player.sendBlockChange(block.getLocation(), Material.WOOL, woolColor.getWoolData());
							changedBlocks.add(block);
							continue;
						}
					}
				}
			}
			
			Bukkit.getScheduler().runTaskLater(SkyWars.get(), () -> {
				if (player.isOnline()) {
					for (Block changedBlock : changedBlocks) {
						player.sendBlockChange(changedBlock.getLocation(), changedBlock.getType(), changedBlock.getData());
					}
				}
			}, 200);
		}
	}
	
	private int countTrues(boolean... values) {
		int count = 0;
		for (boolean value : values) {
			if (value) {
				count++;
			}
		}
		return count;
	}
	
	private int[] range(int from, int to) {
		int index = 0;
		if (from < to) {
			int[] range = new int[to - from + 1];
			for (int current = from; current <= to; current++) {
				range[index++] = current;
			}
			return range;
		} else {
			int[] range = new int[from - to + 1];
			for (int current = to; current <= from; current++) {
				range[index++] = current;
			}
			return range;
		}
	}


}
