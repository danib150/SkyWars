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

import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.gmail.filoghost.skywars.settings.objects.ArenaConfig;

import wild.api.command.CommandFramework.CommandValidate;

public class CommandValidateExtra {
	
	public static Block getTargetBlock(Player player, Material type, String message) {
		Block targetBlock = player.getTargetBlock((Set<Material>) null, 64);
		CommandValidate.isTrue(targetBlock != null && targetBlock.getType() == type, message);
		return targetBlock;
	}
	
	public static void checkArenaConfig(ArenaConfig config) {
		CommandValidate.notNull(config.lobby, "Non hai impostato la lobby.");
		CommandValidate.notNull(config.sign, "Non hai impostato il cartello.");
		CommandValidate.notNull(config.spawnPoints.size() > 0, "Non hai impostato nessun punto di spawn.");
		CommandValidate.isTrue(config.maxPlayersPerTeam > 0, "Non hai impostato il massimo di giocatori per team.");
		CommandValidate.notNull(config.corner1, "Non hai impostato loc1.");
		CommandValidate.notNull(config.corner2, "Non hai impostato loc2.");
		CommandValidate.isTrue(config.corner1.worldName.equals(config.corner2.worldName), "loc1 e loc2 devono essere nello stesso mondo.");
	}


}
