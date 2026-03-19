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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.player.Team;
import com.gmail.filoghost.skywars.arena.player.TeamColor;
import com.gmail.filoghost.skywars.command.CommandValidateExtra;
import com.gmail.filoghost.skywars.settings.MainSettings;
import com.gmail.filoghost.skywars.settings.objects.ArenaConfig;
import com.gmail.filoghost.skywars.settings.objects.LocationConfig;
import com.gmail.filoghost.skywars.world.IntVector;
import com.gmail.filoghost.skywars.world.Region;
import com.gmail.filoghost.skywars.world.WEUtils;
import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public class ArenaModel {
	
	@Getter private String name;
	private Region region;
	@Getter private Location lobby;
	private Block sign;
	private int minPlayers;
	@Getter private int maxPlayers;
	@Getter private int maxPlayersPerTeam;
	private boolean teamsMode;
	private List<Team> teams;
	
	private ArenaModel() { }
	
	public ArenaModel(ArenaConfig config) throws Exception {
		CommandValidateExtra.checkArenaConfig(config);
		
		this.name = config.name;
		this.region = Region.fromConfig(config);
		this.region.addPadding(MainSettings.arenaPadding);
		this.lobby = config.lobby.getLocation();
		this.sign = config.sign.getBlock();
		this.maxPlayersPerTeam = config.maxPlayersPerTeam;
		this.maxPlayers = maxPlayersPerTeam * config.spawnPoints.size();
		this.minPlayers = maxPlayers;
		this.teamsMode = maxPlayersPerTeam > 1;
		
		this.teams = Lists.newArrayList();
		
		if (teamsMode) {
			Queue<TeamColor> usableTeamColors = new ArrayDeque<>(Arrays.asList(TeamColor.values()));
			Queue<Location> spawnPointsToAssign = new ArrayDeque<>();
			
			for (Entry<String, LocationConfig> entry : config.spawnPoints.entrySet()) {
				String possibleTeamName = entry.getKey();
				Location spawnPoint = entry.getValue().getLocation();
				try {
					TeamColor teamColor = TeamColor.valueOf(possibleTeamName.toUpperCase());
					usableTeamColors.remove(teamColor);
					this.teams.add(new Team("team-" + teamColor.toString().toLowerCase(), teamColor, spawnPoint));
				} catch (IllegalArgumentException e) {
					SkyWars.get().getLogger().warning("Arena " + name + " has spawnpoint with invalid team color: " + possibleTeamName);
					spawnPointsToAssign.add(spawnPoint);
					
				}
			}
			
			while (!spawnPointsToAssign.isEmpty()) {
				Location spawnPoint = spawnPointsToAssign.remove();
				TeamColor teamColor = usableTeamColors.poll();
				
				if (teamColor == null) {
					throw new IllegalStateException("Not enough team colors for " + config.spawnPoints.size() + " teams");
				}
				
				this.teams.add(new Team("team-" + teamColor.toString().toLowerCase(), teamColor, spawnPoint));
			}
			
		} else {
			int index = 0;
			for (LocationConfig spawnPoint : config.spawnPoints.values()) {
				this.teams.add(new Team("team-" + (index++), null, spawnPoint.getLocation()));
			}
		}
	}
	
	public int getLengthX() {
		return region.getMaxX() - region.getMinX();
	}
	
	public int getLengthZ() {
		return region.getMaxZ() - region.getMinZ();
	}
	
	public void make(World destinationWorld, int minCornerX, int minCornerZ, Consumer<Arena> onComplete) {
		// Fa in modo di spostare tutto con il minCorner in 0, 0 e poi aggiunge nuovamente il nuovo minCorner specificato
		int shiftX = 0 - this.region.getMinX() + minCornerX;
		int shiftZ = 0 - this.region.getMinZ() + minCornerZ;
		IntVector shiftVector = new IntVector(shiftX, 0, shiftZ);
		
		ArenaModel clone = new ArenaModel();
		clone.name = this.name;
		clone.region = Region.fromMinMaxCorners(
			destinationWorld,
			this.region.getMinCorner().add(shiftVector),
			this.region.getMaxCorner().add(shiftVector)
		);
		clone.lobby = shift(this.lobby, destinationWorld, shiftVector);
		clone.sign = this.sign;
		clone.minPlayers = this.minPlayers;
		clone.maxPlayers = this.maxPlayers;
		clone.maxPlayersPerTeam = this.maxPlayersPerTeam;
		clone.teamsMode = teamsMode;
		clone.teams = Lists.newArrayList();
		for (Team team : this.teams) {
			clone.teams.add(new Team(team.getId(), team.getTeamColor(), shift(team.getSpawnPoint(), destinationWorld, shiftVector)));
		}
		
		// Usa solo X e Z, la Y viene mantenuta uguale
		WEUtils.copy(this.name, this.region, destinationWorld, new IntVector(minCornerX, this.region.getMinY(), minCornerZ), () -> {
			
			IntVector cageMinCorner = new IntVector(clone.lobby.clone().add(0, -0.1, 0)).subtract(PregameCage.getSpawnDifferenceFromMinCorner());
			IntVector cageMaxCorner = cageMinCorner.add(PregameCage.getRegion().getSize());
			Region cageRegion = Region.fromMinMaxCorners(destinationWorld, cageMinCorner, cageMaxCorner);
			
			WEUtils.copy(this.name + "-lobby", PregameCage.getRegion(), destinationWorld, cageMinCorner, () -> {
				onComplete.accept(new Arena(clone, cageRegion));
			});
		}); 
	}

	private Location shift(Location original, World world, IntVector shiftVector) {
		return new Location(world, original.getX() + shiftVector.getX(), original.getY() + shiftVector.getY(), original.getZ() + shiftVector.getZ(), original.getYaw(), original.getPitch());
	}

}
