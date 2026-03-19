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
package com.gmail.filoghost.skywars.database;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.gmail.filoghost.skywars.SkyWars;
import com.gmail.filoghost.skywars.arena.Arena;
import com.google.common.collect.Lists;

import lombok.Cleanup;
import wild.api.mysql.MySQL;
import wild.api.mysql.SQLResult;

public class SQLManager {
		
	private static final String TABLE_MAIN = "skywars_players";
	private static final String TABLE_ANALYTICS = "skywars_analytics";
	private static final String TABLE_EVENTS = "skywars_events";
	private static MySQL mysql;
	
	public static void connect(String host, int port, String database, String user, String pass) throws SQLException {
		mysql = new MySQL(host, port, database, user, pass);
		mysql.connect();
	}

	public static void checkConnection() throws SQLException {
		mysql.isConnectionValid();
	}
	
	public static void close() {
		if (mysql != null) {
			mysql.close();
		}
	}
	
	public static void createTables() throws SQLException {
		mysql.update("CREATE TABLE IF NOT EXISTS " + TABLE_MAIN + " (" +
			SQLColumns.PLAYERS_NAME + " varchar(36) NOT NULL, " +
			SQLColumns.PLAYERS_WINS + " INT NOT NULL, " +
			SQLColumns.PLAYERS_KILLS + " INT NOT NULL, " +
			SQLColumns.PLAYERS_DEATHS + " INT NOT NULL, " +
			SQLColumns.PLAYERS_SCORE + " INT NOT NULL, " +
			"PRIMARY KEY(" + SQLColumns.PLAYERS_NAME + ")" +
		") ENGINE = InnoDB DEFAULT CHARSET = UTF8");

		mysql.update("CREATE TABLE IF NOT EXISTS " + TABLE_ANALYTICS + " (" +
			SQLColumns.ANALYTICS_KEY + " varchar(20) NOT NULL, " +
			SQLColumns.ANALYTICS_VALUE + " varchar(20) NOT NULL, " +
			SQLColumns.ANALYTICS_PLAYERS_PER_TEAM + " TINYINT unsigned NOT NULL, " +
			SQLColumns.ANALYTICS_TEAMS_AMOUNT + " TINYINT unsigned NOT NULL, " +
			SQLColumns.ANALYTICS_ARENA + " varchar(20) NOT NULL, " +
			SQLColumns.ANALYTICS_TIME + " BIGINT unsigned NOT NULL" +
		") ENGINE = InnoDB DEFAULT CHARSET = UTF8");
	}
	
	
	public static void insertAnalyticsAsync(String key, String value, Arena arena) {
		long time = System.currentTimeMillis();
		Bukkit.getScheduler().runTaskAsynchronously(SkyWars.get(), () -> {
			try {
				SQLManager.mysql.preparedUpdate(
					"INSERT INTO " + TABLE_ANALYTICS + " (" +
						SQLColumns.ANALYTICS_KEY + ", " +
						SQLColumns.ANALYTICS_VALUE + ", " +
						SQLColumns.ANALYTICS_PLAYERS_PER_TEAM + ", " +
						SQLColumns.ANALYTICS_TEAMS_AMOUNT + ", " +
						SQLColumns.ANALYTICS_ARENA + ", " +
						SQLColumns.ANALYTICS_TIME +
					") VALUES(?, ?, ?, ?, ?, ?);", key, value, arena.getMaxPlayersPerTeam(), arena.getTeams().size(), arena.getName(), time);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	
	public static PlayerData getStats(UUID playerUUID) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT * FROM " + TABLE_MAIN + " WHERE " + SQLColumns.PLAYERS_NAME + " = ?", playerUUID.toString());
		if (result.next()) {
			return new PlayerData(
					result.getInt(SQLColumns.PLAYERS_WINS),
					result.getInt(SQLColumns.PLAYERS_KILLS),
					result.getInt(SQLColumns.PLAYERS_DEATHS),
					result.getInt(SQLColumns.PLAYERS_SCORE));
		} else {
			PlayerData playerData = new PlayerData(0, 0, 0, 0);
			playerData.setNeedSave(true); // Perché è un giocatore nuovo, va salvato la prima volta
			return playerData;
		}
	}
	
	public static void savePlayerData(UUID playerUUID, PlayerData data) throws SQLException {
		mysql.preparedUpdate(
				"INSERT INTO " + TABLE_MAIN + " (" +
					SQLColumns.PLAYERS_NAME + ", " +
					SQLColumns.PLAYERS_WINS + ", " +
					SQLColumns.PLAYERS_KILLS + ", " +
					SQLColumns.PLAYERS_DEATHS + ", " +
					SQLColumns.PLAYERS_SCORE +
				") VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
					SQLColumns.PLAYERS_NAME + " = ?, " +
					SQLColumns.PLAYERS_WINS + " = ?, " +
					SQLColumns.PLAYERS_KILLS + " = ?, " +
					SQLColumns.PLAYERS_DEATHS + " = ?, " +
					SQLColumns.PLAYERS_SCORE + " = ?;",
					
				playerUUID.toString(), data.getWins(), data.getKills(), data.getDeaths(), data.getScore(),
				playerUUID.toString(), data.getWins(), data.getKills(), data.getDeaths(), data.getScore());

	}

	public static List<SQLSingleStat> getTop(String statSQLColumn, int limit) throws SQLException {
		@Cleanup SQLResult result = mysql.preparedQuery("SELECT " + SQLColumns.PLAYERS_NAME + ", " + statSQLColumn + " FROM " + TABLE_MAIN + " ORDER BY " + statSQLColumn + " DESC LIMIT " + limit);
			
		List<SQLSingleStat> stats = Lists.newArrayList();
		while (result.next()) {
			stats.add(new SQLSingleStat(UUID.fromString(result.getString(SQLColumns.PLAYERS_NAME)), result.getInt(statSQLColumn)));
		}
			
		return stats;
	}

	public static void resetStats() throws SQLException {
		mysql.update("UPDATE " + TABLE_MAIN + " SET " +
			SQLColumns.PLAYERS_WINS + " = 0, " +
			SQLColumns.PLAYERS_KILLS + " = 0, " +
			SQLColumns.PLAYERS_DEATHS + " = 0, " +
			SQLColumns.PLAYERS_SCORE + " = 0;"
		);
		mysql.update("DELETE FROM " + TABLE_EVENTS + ";");
	}

}
