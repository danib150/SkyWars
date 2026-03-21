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

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.SpawnScoreboard;
import com.gmail.filoghost.skywars.Perms;
import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.gmail.filoghost.skywars.arena.ArenaModel;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.arena.player.PlayerStatus;
import com.gmail.filoghost.skywars.database.PlayerData;
import com.gmail.filoghost.skywars.database.SQLManager;
import com.gmail.filoghost.skywars.settings.MainSettings;
import com.gmail.filoghost.skywars.settings.objects.ArenaConfig;
import com.gmail.filoghost.skywars.settings.objects.LocationConfig;
import com.gmail.filoghost.skywars.utils.Format;
import com.gmail.filoghost.skywars.utils.Utils;

import net.cubespace.yamler.YamlerConfigurationException;
import net.md_5.bungee.api.ChatColor;
import wild.api.command.CommandFramework.Permission;
import wild.api.uuid.UUIDRegistry;
import wild.api.command.SubCommandFramework;

@Permission(Perms.COMMAND_SKYWARS)
public class SkywarsCommand extends SubCommandFramework {
	
	public SkywarsCommand(JavaPlugin plugin, String label, String... aliases) {
		super(plugin, label, aliases);
	}
	
	@Override
	public void noArgs(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_GREEN + "Lista comandi " + this.label + ":");
		for (SubCommandDetails sub : this.getAccessibleSubCommands(sender)) {
			sender.sendMessage(ChatColor.GREEN + "/" + this.label + " " + sub.getName() + (sub.getUsage() != null ?  " " + sub.getUsage() : ""));
		}
	}
	
	@SubCommand("debug")
	public void debug(CommandSender sender, String label, String[] args) {
		CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Comando solo per console.");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Giocatori (" + ArenasManager.getArenasByPlayers().size() + "):");
		for (Entry<Player, Arena> entry : ArenasManager.getArenasByPlayers().entrySet())  {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + entry.getKey().getName() + " -> " + (entry.getValue() != null ? entry.getValue().getName() : "/"));
		}
	}
	
	@SubCommand("spectator")	
	@SubCommandMinArgs(1)
	@SubCommandUsage("<giocatore>")
	public void spectator(CommandSender sender, String label, String[] args) {
		CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Comando solo per console.");
		Player target = Bukkit.getPlayerExact(args[0]);
		CommandValidate.notNull(target, "Giocatore non trovato.");
		Arena arena = ArenasManager.getArenaByPlayer(target);
		CommandValidate.notNull(arena, "Il giocatore non è dentro un'arena.");
		
		PlayerStatus targetStatus = arena.getPlayerStatus(target);
		targetStatus.setTeam(null);
		targetStatus.setSpectator(target, true);
		arena.giveEquip(target, targetStatus);
		sender.sendMessage(ChatColor.GREEN + "Impostato " + target + " come spettatore.");
	}

	@SubCommand("start")
	public void start(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		player.sendMessage(ChatColor.GREEN + "Partita avviata");

		Arena arena = ArenasManager.getArenaByPlayer(player);
		arena.getGameloop().forceStart();

		System.out.println("DEBUG");
	}

	@SubCommand("setSpawn")
	public void setSpawn(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Location spawn = Utils.roundedLocation(player.getLocation());
		
		SkyWars.setSpawn(spawn);
		MainSettings.spawn = new LocationConfig(spawn);
		spawn.getWorld().setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
		
		try {
			new MainSettings(SkyWars.get(), "config.yml").save();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile salvare la configurazione.");
		}

		player.sendMessage(ChatColor.GREEN + "Hai impostato lo spawn globale.");
	}
	
	@SubCommand("tpArena")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void tpArena(CommandSender sender, String label, String[] args) {
		String name = args[0];
		ArenaModel model = ArenasManager.getModel(name);
		CommandValidate.notNull(model, "Arena non trovata.");
		
		CommandValidate.getPlayerSender(sender).teleport(model.getLobby(), TeleportCause.COMMAND);
		sender.sendMessage(ChatColor.GREEN + "Teletrasportato nell'arena " + name + ".");
	}
	
	@SubCommand("loadArena")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void loadArena(CommandSender sender, String label, String[] args) {
		String name = args[0];
		try {
			CommandValidate.isTrue(ArenasManager.getModel(name) == null, "Arena già caricata.");
			ArenaConfig arenaConfig = SkyWars.loadArenaConfig(name);
			CommandValidate.notNull(arenaConfig, "Arena non trovata.");
			ArenaModel arenaModel = new ArenaModel(arenaConfig);
			ArenasManager.addModel(arenaModel);
			SkyWars.getArenaSelector().updateAll();
			ArenasManager.create(arenaModel);
			sender.sendMessage(ChatColor.GREEN + "Hai caricato l'arena " + name + ".");
			
			
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile leggere la configurazione.");
		} catch (ExecuteException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExecuteException("Eccezione non gestita durante il caricamento dell'arena.");
		}
	}
	
	@SubCommand("reset")
	public void reset(CommandSender sender, String label, String[] args) {
		CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Eseguibile solo da console.");
		
		sender.sendMessage(ChatColor.GRAY + "Attendi...");
		Bukkit.getScheduler().runTaskAsynchronously(SkyWars.get(), () -> {
			try {
				SQLManager.resetStats();
				Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
					SkyWars.resetOnlinePlayersData();
				});
				
				sender.sendMessage(ChatColor.GREEN + "Statistiche resettate!");
				
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Errore durante il reset.");
			}
		});
	}

	@SubCommand("addScore")
	@SubCommandMinArgs(2)
	@SubCommandUsage("<giocatore> <punti>")
	public void addScore(CommandSender sender, String label, String[] args) {
		CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Comando solo per console.");
		
		String targetName = args[0];
		int score = CommandValidate.getPositiveIntegerNotZero(args[1]);
		Player onlineTarget = Bukkit.getPlayerExact(targetName);

		if (onlineTarget != null) {
			PlayerData targetData = SkyWars.getOnlinePlayerData(onlineTarget);
			targetData.addScore(score);
			SpawnScoreboard spawnScoreboard = SkyWars.getSpawnScoreboard();
			if (spawnScoreboard.hasActiveScoreboard(onlineTarget)) {
				spawnScoreboard.setScore(onlineTarget, targetData.getScore());
			}
			sender.sendMessage(ChatColor.GREEN + "Hai aggiunto " + Format.formatPoints(score) + " al giocatore online " + onlineTarget.getName() + ".");
		} else {
			UUID targetUUID = UUIDRegistry.getUUID(targetName);
			CommandValidate.notNull(targetUUID, "Impossibile trovare l'UUID di " + targetName + ".");
			try {
				PlayerData targetData = SkyWars.loadStatsFromDatabase(targetUUID);
				targetData.addScore(score);
				SkyWars.unloadAndSaveStats(targetUUID);
				sender.sendMessage(ChatColor.GREEN + "Hai aggiunto " + Format.formatPoints(score) + " al giocatore offline " + targetName + ".");
			} catch (SQLException e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Errore interno: impossibile caricare le statistiche di " + targetName + " (" + e.toString() + ").");
			}
		}
	}

}
