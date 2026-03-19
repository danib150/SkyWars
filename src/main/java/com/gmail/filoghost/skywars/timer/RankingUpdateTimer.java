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
package com.gmail.filoghost.skywars.timer;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.database.SQLSingleStat;
import com.gmail.filoghost.skywars.settings.PodiumSettings;
import com.gmail.filoghost.skywars.settings.objects.BlockConfig;
import com.gmail.filoghost.skywars.utils.PodiumPosition;
import com.gmail.filoghost.skywars.utils.Ranking;

import wild.api.uuid.UUIDRegistry;

public class RankingUpdateTimer extends BukkitRunnable {
	
	
	public RankingUpdateTimer start() {
		this.runTaskTimerAsynchronously(SkyWars.get(), 0L, 60 * 20L);
		return this;
	}
	
	@Override
	public void run() {
		try {
			Ranking.loadRankings();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Thread principale
		Bukkit.getScheduler().runTask(SkyWars.get(), () -> {
			updatePodium(Ranking.getTopWins(), PodiumSettings.wins_heads, PodiumSettings.wins_signs);
			updatePodium(Ranking.getTopKills(), PodiumSettings.kills_heads, PodiumSettings.kills_signs);
		});
	}
	
	private void updatePodium(List<SQLSingleStat> topPlayers, Map<String, BlockConfig> topHeads, Map<String, BlockConfig> topSigns) {
		for (PodiumPosition podiumPosition : PodiumPosition.values()) {
			BlockConfig headConfig = topHeads != null ? topHeads.get(podiumPosition.name()) : null;
			BlockConfig signConfig = topSigns != null ? topSigns.get(podiumPosition.name()) : null;
			
			SQLSingleStat stat = podiumPosition.ordinal() < topPlayers.size() ? topPlayers.get(podiumPosition.ordinal()) : null;

			if (stat != null) {
				if (headConfig != null) {
					setHeadName(headConfig.getBlock(), UUIDRegistry.getNameFallback(stat.getPlayerUUID()));
				}
				if (signConfig != null) {
					setSign(signConfig.getBlock(), podiumPosition.getTitle(), "", UUIDRegistry.getNameFallback(stat.getPlayerUUID()), String.valueOf(stat.getValue()));
				}
			}
		}
	}
	
	private void setHeadName(Block block, String name) {
		BlockState state = block.getState();
		if (state instanceof Skull) {
			((Skull) state).setOwner(name);
			state.update();
		}
	}
	
	private void setSign(Block block, String first, String second, String third, String fourth) {
		BlockState state = block.getState();
		if (state instanceof Sign) {
			Sign sign = (Sign) state;
			sign.setLine(0, first);
			sign.setLine(1, second);
			sign.setLine(2, third);
			sign.setLine(3, fourth);
			state.update();
		}
	}

}
