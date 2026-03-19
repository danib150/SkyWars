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
package com.gmail.filoghost.skywars.arena;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import com.gmail.filoghost.skywars.utils.Utils;
import com.gmail.filoghost.skywars.world.Spiral2D;
import com.gmail.filoghost.skywars.world.VoidChunkGenerator;
import com.google.common.collect.Maps;

import lombok.Getter;
import wild.api.util.CaseInsensitiveMap;

public class ArenasManager {
	
	private static final String ARENAS_WORLD_NAME = "world_arenas";
	private static final int SPACE_BETWEEN_ARENAS = 128;
	
	private static World arenasWorld;
	private static int biggestModelLengthX, biggestModelLengthZ;
	private static Spiral2D spiralGenerator = new Spiral2D();

	private static final Map<String, ArenaModel> arenaModelsByName = Collections.synchronizedMap(new CaseInsensitiveMap<>());
	@Getter private static final Map<Player, Arena> arenasByPlayers = Maps.newConcurrentMap();
	private static final Map<String, Arena> arenasByName = Collections.synchronizedMap(new CaseInsensitiveMap<>()); // Le arene attive
	
	
	public static void createWorld() {
		// Succede dopo un crash
		File oldWorldDirectory = new File(Bukkit.getWorldContainer(), ARENAS_WORLD_NAME);
		if (oldWorldDirectory.isDirectory()) {
			Utils.deleteFolder(oldWorldDirectory);
		}
		
		arenasWorld = WorldCreator
			.name(ARENAS_WORLD_NAME)
			.environment(Environment.NORMAL)
			.type(WorldType.FLAT)
			.generateStructures(false)
			.generator(new VoidChunkGenerator())
			.createWorld();
		
		arenasWorld.setAutoSave(false);
		arenasWorld.setDifficulty(Difficulty.HARD);
		arenasWorld.setSpawnFlags(true, true);
		arenasWorld.setPVP(true);
		arenasWorld.setStorm(false);
		arenasWorld.setThundering(false);
		arenasWorld.setWeatherDuration(Integer.MAX_VALUE);
		arenasWorld.setKeepSpawnInMemory(false);
		arenasWorld.setTicksPerAnimalSpawns(0);
		arenasWorld.setTicksPerMonsterSpawns(0);
		arenasWorld.setGameRuleValue("doFireTick", "true");
		arenasWorld.setGameRuleValue("doDaylightCycle", "false");
		arenasWorld.setGameRuleValue("doMobSpawning", "false");
		arenasWorld.setGameRuleValue("spectatorsGenerateChunks", "false");
	}
	
	public static void deleteWorld() {
		if (arenasWorld != null) {
			File folder = arenasWorld.getWorldFolder();
			Bukkit.unloadWorld(arenasWorld, false);
			
			if (folder != null && folder.isDirectory()) {
				Utils.deleteFolder(folder);
			}
		}
	}
	
	public static boolean isArenaWorld(World world) {
		return world == arenasWorld;
	}
	
	public static void addModel(ArenaModel model) {
		if (arenaModelsByName.containsKey(model.getName())) {
			throw new IllegalArgumentException("arena model " + model.getName() + " already loaded");
		}
		
		if (model.getLengthX() > biggestModelLengthX) {
			biggestModelLengthX = model.getLengthX();
		}
		if (model.getLengthZ() > biggestModelLengthZ) {
			biggestModelLengthZ = model.getLengthZ();
		}
		
		arenaModelsByName.put(model.getName(), model);
	}

	public static ArenaModel getModel(String name) {
		return arenaModelsByName.get(name);
	}
	
	public static Collection<ArenaModel> getModels() {
		return arenaModelsByName.values();
	}
	
	public static Arena getArenaByPlayer(Player player) {
		return arenasByPlayers.get(player);
	}
	
	public static boolean isFightingPlayer(Player player) {
		Arena arena = getArenaByPlayer(player);
		return arena != null && arena.isFightingPlayer(player);
	}
	
	public static Arena getArenaByName(String name) {
		return arenasByName.get(name);
	}

	public static Collection<Arena> getArenas() {
		return arenasByName.values();
	}
	
	public static void onArenaDelete(Arena arena) {
		arenasByName.remove(arena.getName());
		create(arena.getName());
	}
	
	public static void createAll() {
		for (ArenaModel model : arenaModelsByName.values()) {
			create(model);
		}
	}
	
	private static void create(String arenaName) {
		if (arenasByName.containsKey(arenaName)) {
			Utils.reportAnomaly("arena already found, cannot create " + arenaName);
			return;
		}
		
		ArenaModel model = arenaModelsByName.get(arenaName);
		if (model == null) {
			throw new IllegalStateException("Model " + arenaName + " not found");
		}
		
		create(model);
	}
	
	public static void create(ArenaModel model) {
		if (arenasByName.containsKey(model.getName())) {
			Utils.reportAnomaly("arena already found, cannot create " + model.getName());
			return;
		}
		
		spiralGenerator.nextPoint();
		int gridX = spiralGenerator.getX();
		int gridZ = spiralGenerator.getZ();
		
		int minCornerX = gridX * (biggestModelLengthX + SPACE_BETWEEN_ARENAS);
		int minCornerZ = gridZ * (biggestModelLengthZ + SPACE_BETWEEN_ARENAS);

		model.make(arenasWorld, minCornerX, minCornerZ, (arena) -> {
			arenasByName.put(arena.getName(), arena);
		});
	}
	
}
