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
package com.gmail.filoghost.skywars.settings;

import java.util.Map;

import org.bukkit.plugin.Plugin;

import com.gmail.filoghost.skywars.settings.objects.LocationConfig;
import com.google.common.collect.Maps;

import net.cubespace.yamler.PreserveStatic;
import net.cubespace.yamler.YamlerConfig;

@PreserveStatic
public class MainSettings extends YamlerConfig {
	
	public static LocationConfig spawn;
	
	public static int countdown_start = 60;
	public static int countdown_game = 1200;
	public static int countdown_end = 10;
	
	public static String mysql_host = "localhost";
	public static String mysql_database = "database";
	public static String mysql_user = "root";
	public static String mysql_pass = "toor";
	public static int mysql_port = 3306;
	
	public static int arenaPadding = 0;
	
	public static int score_kill_base = 10;
	public static Map<Integer, Integer> score_kill_byTeamsAmount = Maps.newHashMap();
	public static int score_win_base = 50;
	public static Map<Integer, Integer> score_win_byTeamsAmount = Maps.newHashMap();
	public static int score_death = -1;

	public static String chatFormat_spectators = "&7[Spettatori] &f{player} &8» &7";
	public static String chatFormat_team = "{teamcolor}[Team] &f{player} &8» &7";
	
	public MainSettings(Plugin plugin, String filename) {
		super(plugin, filename);
	}

}
