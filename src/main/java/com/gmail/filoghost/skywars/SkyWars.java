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
package com.gmail.filoghost.skywars;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.skywars.arena.ArenaModel;
import com.gmail.filoghost.skywars.arena.ArenasManager;
import com.gmail.filoghost.skywars.arena.PregameCage;
import com.gmail.filoghost.skywars.arena.kit.Kit;
import com.gmail.filoghost.skywars.arena.kit.KitsManager;
import com.gmail.filoghost.skywars.arena.menu.ArenaSelectorMenu;
import com.gmail.filoghost.skywars.arena.reward.KillReward;
import com.gmail.filoghost.skywars.command.ArenaCommand;
import com.gmail.filoghost.skywars.command.ClassificaCommand;
import com.gmail.filoghost.skywars.command.GlobalCommand;
import com.gmail.filoghost.skywars.command.CageCommand;
import com.gmail.filoghost.skywars.command.PodiumCommand;
import com.gmail.filoghost.skywars.command.ShopCommand;
import com.gmail.filoghost.skywars.command.SkywarsCommand;
import com.gmail.filoghost.skywars.command.SpawnCommand;
import com.gmail.filoghost.skywars.command.StatsCommand;
import com.gmail.filoghost.skywars.database.PlayerData;
import com.gmail.filoghost.skywars.database.SQLManager;
import com.gmail.filoghost.skywars.listener.ChatListener;
import com.gmail.filoghost.skywars.listener.DamageListener;
import com.gmail.filoghost.skywars.listener.DeathListener;
import com.gmail.filoghost.skywars.listener.InteractListener;
import com.gmail.filoghost.skywars.listener.NatureListener;
import com.gmail.filoghost.skywars.listener.PlayerJoinQuitListener;
import com.gmail.filoghost.skywars.listener.PlayerListener;
import com.gmail.filoghost.skywars.settings.ChestSettings;
import com.gmail.filoghost.skywars.settings.KitSettings;
import com.gmail.filoghost.skywars.settings.PregameCageSettings;
import com.gmail.filoghost.skywars.settings.ShopSettings;
import com.gmail.filoghost.skywars.settings.MainSettings;
import com.gmail.filoghost.skywars.settings.PodiumSettings;
import com.gmail.filoghost.skywars.settings.RewardsSettings;
import com.gmail.filoghost.skywars.settings.objects.ArenaConfig;
import com.gmail.filoghost.skywars.timer.MySQLKeepAliveTimer;
import com.gmail.filoghost.skywars.timer.RankingUpdateTimer;
import com.gmail.filoghost.skywars.timer.SpectatorLocationCheckTimer;
import com.gmail.filoghost.skywars.utils.Utils;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;
import net.cubespace.yamler.YamlerConfigurationException;
import wild.api.WildCommons;
import wild.api.command.CommandFramework.ExecuteException;
import wild.api.item.BookTutorial;
import wild.api.world.SpectatorAPI;

public class SkyWars extends JavaPlugin {

	public static final String SIDEBAR_TITLE = "       " + ChatColor.RED + ChatColor.BOLD + ChatColor.UNDERLINE + "Sky Wars" + ChatColor.RESET + "       ";
	public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.RED + "Sky Wars" + ChatColor.GRAY + "] ";
	
	private static SkyWars plugin;
	@Getter private static PodiumSettings podiumSettings;
	private static BookTutorial bookTutorial;
	@Getter @Setter private static Location spawn;
	private static File arenasFolder;
	@Getter private static SpawnScoreboard spawnScoreboard;
	@Getter private static ArenaSelectorMenu arenaSelector;
	
	private static Map<UUID, PlayerData> statsByPlayerUUID = Maps.newConcurrentMap();

	
	public static SkyWars get() {
		return plugin;
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
		if (!checkDependancies()) {
			return;
		}

		bookTutorial = new BookTutorial(this, "Sky Wars");
		
		try {
			new MainSettings(this, "config.yml").init();
			if (MainSettings.spawn != null) {
				spawn = MainSettings.spawn.getLocation();
			} else {
				spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
			}
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare config.yml");
			return;
		}
		
		try {
			new ChestSettings(this, "chest.yml").init();
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare chest.yml");
			return;
		}
		
		try {
			new ShopSettings(this, "shop.yml").init();
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare shop.yml");
			return;
		}
		
		try {
			(podiumSettings = new PodiumSettings(this, "podium.yml")).init();
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare podium.yml");
			return;
		}
		
		try {
			new RewardsSettings(this, "rewards.yml").init();
			KillReward.init();
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare rewards.yml");
			return;
		}
		
		try {
			new PregameCageSettings(this, "pregame-cage.yml").init();
			PregameCage.init();
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare pregame-cage.yml");
			return;
		}
		
		File kitsFolder = new File(getDataFolder(), "kits");
		if (!kitsFolder.isDirectory()) {
			kitsFolder.mkdir();
		}
		
		for (File kitFile : kitsFolder.listFiles()) {
			if (kitFile.getName().toLowerCase().endsWith(".yml")) {
				try {
					KitSettings kitSettings = new KitSettings(kitFile);
					kitSettings.init();
					Kit kit = KitsManager.loadKit(kitSettings);
					SkyWars.get().getLogger().info("[Kit] Caricato kit " + kit.getName() + ", prezzo " + kit.getPrice());
					
				} catch (YamlerConfigurationException e) {
					e.printStackTrace();
					criticalShutdown("Impossibile caricare il kit " + kitFile.getName());
					return;
				}
			}
		}
		KitsManager.loadSelectorMenu();	

		// Database
		try {
			SQLManager.connect(MainSettings.mysql_host, MainSettings.mysql_port, MainSettings.mysql_database, MainSettings.mysql_user, MainSettings.mysql_pass);
			SQLManager.createTables();
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile connettersi al database");
			return;
		}
		
		// Inizializzazione
		arenasFolder = new File(getDataFolder(), "arenas");
		arenasFolder.mkdirs();
		spawnScoreboard = new SpawnScoreboard();
		arenaSelector = new ArenaSelectorMenu();
		
		// Listener
		Bukkit.getPluginManager().registerEvents(new NatureListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), this);
		Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
		Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
		Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		
		// Comandi
		new SkywarsCommand(this, "skywars", "sw");
		new ArenaCommand(this, "arena");
		new SpawnCommand(this, "spawn");
		new ClassificaCommand(this, "classifica");
		new StatsCommand(this, "stats");
		new PodiumCommand(this, "podium");
		new GlobalCommand(this, "g");
		new ShopCommand(this, "shop");
		new CageCommand(this, "cage");
		
		// Carica le arene salvate
		for (File arenaFile : arenasFolder.listFiles()) {
			try {
				if (arenaFile.getName().toLowerCase().endsWith(".yml")) {
					ArenaConfig arenaConfig = loadArenaConfig(arenaFile);
					ArenasManager.addModel(new ArenaModel(arenaConfig));
				}
			} catch (ExecuteException e) {
				criticalShutdown("Non è stato possibile caricare l'arena " + arenaFile.getName() + ": " + e.getMessage());
				return;
			} catch (Exception e) {
				e.printStackTrace();
				criticalShutdown("Eccezione non gestita durante il caricamento dell'arena " + arenaFile.getName() + ".");
				return;
			}
		}
		
		arenaSelector.updateAll();
		
		// Timer
		new MySQLKeepAliveTimer().start();
		new RankingUpdateTimer().start();
		new SpectatorLocationCheckTimer().start();
		
		Bukkit.getScheduler().runTaskLater(this, () -> {
			ArenasManager.createWorld();
			ArenasManager.createAll();
		}, 100L);
	}
	
	@Override
	public void onDisable() {		
		for (Entry<UUID, PlayerData> entry : statsByPlayerUUID.entrySet()) {
			if (entry.getValue().isNeedSave()) {
				try {
					SQLManager.savePlayerData(entry.getKey(), entry.getValue());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		ArenasManager.deleteWorld();
		
		SQLManager.close();
	}
	
	public static void setupToLobby(Player player) {
		if (ArenasManager.getArenaByPlayer(player) != null) {
			throw new IllegalStateException("Player is still inside arena");
		}
		
		player.resetMaxHealth();
		player.setHealth(player.getMaxHealth());
		player.setExp(0);
		player.setLevel(0);
		player.teleport(spawn, TeleportCause.PLUGIN);
		giveLobbyEquip(player);
		
		VanishManager.setHidden(player, false);
		SpectatorAPI.removeSpectator(player);


		PlayerData stats = SkyWars.getOnlinePlayerData(player);
		SpawnScoreboard spawnScoreboard = SkyWars.getSpawnScoreboard();
		spawnScoreboard.setActiveScoreboard(player);
		spawnScoreboard.setScore(player, stats.getScore());
		spawnScoreboard.setWins(player, stats.getWins());
		spawnScoreboard.setKills(player, stats.getKills());
		spawnScoreboard.setDeaths(player, stats.getDeaths());
	}
	
	public static void giveLobbyEquip(Player player) {
		player.setGameMode(GameMode.SURVIVAL);
		WildCommons.clearInventoryFully(player);
		WildCommons.removePotionEffects(player);
		
		PlayerInventory inventory = player.getInventory();
		inventory.setItem(0, Constants.ITEM_ARENA_PICKER);
		inventory.setItem(4, bookTutorial.getItemStack());


	}
	
	public static PlayerData getOnlinePlayerData(Player player) {
		PlayerData stats = statsByPlayerUUID.get(player.getUniqueId());
		if (stats == null) {
			Utils.reportAnomaly("stats were not loaded", player);
			stats = new PlayerData(0, 0, 0, 0); // Dummy stats per evitare errori, alla peggio non vengono segnate
		}
		
		return stats;
	}
	
	public static void resetOnlinePlayersData() {
		for (PlayerData stats : statsByPlayerUUID.values()) {
			stats.resetToZero();
		}
	}
	
	public static PlayerData loadStatsFromDatabase(UUID playerUUID) throws SQLException {
		PlayerData playerData = SQLManager.getStats(playerUUID);
		statsByPlayerUUID.put(playerUUID, playerData);
		return playerData;
	}
	
	public static void unloadAndSaveStats(UUID playerUUID) {
		PlayerData playerData = statsByPlayerUUID.remove(playerUUID);
		if (playerData == null) {
			throw new IllegalStateException(playerUUID + "'s stats were not loaded");
		}
		
		if (playerData.isNeedSave()) {
			Bukkit.getScheduler().runTaskAsynchronously(SkyWars.get(), () -> {
				try {
					SQLManager.savePlayerData(playerUUID, playerData);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}
	
	public static ArenaConfig loadArenaConfig(String name) throws YamlerConfigurationException {
		return loadArenaConfig(new File(arenasFolder, name.toLowerCase() + ".yml"));
	}

	private static ArenaConfig loadArenaConfig(File arenaFile) throws YamlerConfigurationException {
		if (!arenaFile.exists()) {
			return null;
		}
		
		String fileName = arenaFile.getName();
		String arenaName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
		
		ArenaConfig arenaConfig = new ArenaConfig();
		arenaConfig.load(arenaFile);
		if (!arenaConfig.name.equalsIgnoreCase(arenaName)) {
			throw new YamlerConfigurationException("name mismatch: " + arenaConfig.name + " vs " + arenaName);
		}
		return arenaConfig;
	}
	
	public static void saveArenaConfig(ArenaConfig arenaConfig) throws YamlerConfigurationException {
		File arenaFile = new File(arenasFolder, arenaConfig.name.toLowerCase() + ".yml");
		arenaConfig.save(arenaFile);
	}
	
	private boolean checkDependancies() {
		return checkDependancy("WildCommons") && checkDependancy("HolographicDisplays");
	}
	
	private boolean checkDependancy(String pluginName) {
		if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
			return true;
		} else {
			criticalShutdown("Richiesto " + pluginName);
			return false;
		}
	}
	
	private void criticalShutdown(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] " + message);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) { }
		setEnabled(false);
		Bukkit.shutdown();
	}
	
}
