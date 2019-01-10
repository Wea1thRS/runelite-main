/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * Copyright (c) 2018, Wea1th <https://github.com/>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.loottracker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.loottracker.LootRecord;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
class LootRecordWriter
{
	@Inject
	private ClientThread clientThread;

	@Inject
	private LootTrackerPlugin loottracker;

	private static final String FILE_EXTENSION = ".log";
	private static final File LOOT_RECORD_DIR = new File(RUNELITE_DIR, "loots");
	final String url = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=" + System.getenv("DB_PASSWORD") + "sslmode=disable";

	private long coins1 = 0;
	private long coins2 = 0;
	private long coins3 = 0;

	private File playerFolder = LOOT_RECORD_DIR;
	private final ItemManager itemManager;

	@Inject
	LootRecordWriter(final ItemManager itemManager)
	{
		this.itemManager = itemManager;
		playerFolder.mkdir();
	}

	private static String asFileName(String name)
	{
		return name.toLowerCase().trim() + FILE_EXTENSION;
	}

	void setPlayerUsername(String username)
	{
		playerFolder = new File(LOOT_RECORD_DIR, username);
		playerFolder.mkdir();
	}

	synchronized void addLootTrackerRecord(LootRecord rec)
	{
		File lootFile = new File(playerFolder, asFileName(rec.getEventId()));
		String dataAsString = RuneLiteAPI.GSON.toJson(rec);

		try
		{
			BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), true));
			file.append(dataAsString);
			file.newLine();
			file.close();
		}
		catch (IOException ioe)
		{
			log.warn("Error writing loot data to file {}: {}", asFileName(rec.getEventId()), ioe.getMessage());
		}
	}

	void addLootTrackerRecordToDB(Collection<LootTrackerRecord> recs) throws SQLException
	{
		Connection conn = DriverManager.getConnection(url);
		List<LootTrackerItem> drops = new ArrayList<>();
		for (LootTrackerRecord rec : recs)
		{
			String mob = "\"" + rec.getTitle() + "\"";
			String sql1 = "CREATE TABLE IF NOT EXISTS $tablename (index SERIAL, item_id INTEGER NOT NULL PRIMARY KEY,item_name TEXT NOT NULL, item_quantity INTEGER NOT NULL, item_price INTEGER NOT NULL, total_price BIGINT NOT NULL)".replace("$tablename", mob);
			String sql2 = "INSERT INTO $tablename as tn (item_id, item_name, item_quantity, item_price, total_price) VALUES (?, ?, ?, ?, ?) ON CONFLICT (item_id) DO UPDATE SET item_quantity = tn.item_quantity + (?), item_price = (?), total_price = (tn.item_quantity +(?)) * (?)".replace("$tablename", mob);
			PreparedStatement st = conn.prepareStatement(sql1);
			PreparedStatement st2 = conn.prepareStatement(sql2);
			st.executeUpdate();
			for (LootTrackerItem drop : rec.getItems())
			{
				setStatement(st2, drop);
			}
			st.close();
			st2.close();
		}
		conn.close();
	}

	private void setStatement(PreparedStatement st2, LootTrackerItem drop) throws SQLException
	{
		int item_id = drop.getId();
		long total_price = drop.getPrice();
		int item_quantity = drop.getQuantity();
		int item_price = (int) (total_price / item_quantity);
		String name = drop.getName();
		System.out.println("Id: " + drop.getId() + "\tName: " + name + "\tQuantity: " + drop.getQuantity() + "\tIPrice: " + drop.getPrice() / drop.getQuantity() + "\tTPrice: " + drop.getPrice());

		st2.setInt(1, item_id);
		st2.setString(2, "\"" + name + "\"");
		st2.setInt(3, item_quantity);
		st2.setInt(4, item_price);
		st2.setLong(5, total_price);
		st2.setInt(6, item_quantity);
		st2.setInt(7, item_price);
		st2.setInt(8, item_quantity);
		st2.setInt(9, item_price);
		st2.executeUpdate();
	}

	void addLootTrackerRecordToDB(String title, LootTrackerItem[] items) throws SQLException
	{
		Connection conn = DriverManager.getConnection(url);
		String mob = "\"" + title + "\"";
		String sql1 = "CREATE TABLE IF NOT EXISTS $tablename (index SERIAL, item_id INTEGER NOT NULL PRIMARY KEY,item_name TEXT NOT NULL, item_quantity INTEGER NOT NULL, item_price INTEGER NOT NULL, total_price BIGINT NOT NULL)".replace("$tablename", mob);
		String sql2 = "INSERT INTO $tablename as tn (item_id, item_name, item_quantity, item_price, total_price) VALUES (?, ?, ?, ?, ?) ON CONFLICT (item_id) DO UPDATE SET item_quantity = tn.item_quantity + (?), item_price = (?), total_price = (tn.item_quantity +(?)) * (?)".replace("$tablename", mob);
		PreparedStatement st = conn.prepareStatement(sql1);
		PreparedStatement st2 = conn.prepareStatement(sql2);
		st.executeUpdate();
		for (LootTrackerItem drop : items)
		{
			setStatement(st2, drop);
		}
		st.close();
		st2.close();
		conn.close();
	}
}