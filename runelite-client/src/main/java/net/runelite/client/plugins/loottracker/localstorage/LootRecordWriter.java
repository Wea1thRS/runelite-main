/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
package net.runelite.client.plugins.loottracker.localstorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.loottracker.LootTrackerItem;
import net.runelite.client.plugins.loottracker.LootTrackerRecord;
import net.runelite.client.plugins.loottracker.localstorage.events.LTNameChange;
import net.runelite.client.plugins.loottracker.localstorage.events.LTRecordStored;
import net.runelite.http.api.RuneLiteAPI;

@Slf4j
@Singleton
public class LootRecordWriter
{
	private static final String FILE_EXTENSION = ".log";
	private static final File LOOT_RECORD_DIR = new File(RUNELITE_DIR, "loots");
	final String url = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=" + System.getenv("DB_PASSWORD") + "sslmode=disable";

	private final EventBus bus;

	// Data is stored in a folder with the players in-game username
	private File playerFolder = LOOT_RECORD_DIR;
	private String name;

	@Inject
	public LootRecordWriter(EventBus bus)
	{
		this.bus = bus;
		LOOT_RECORD_DIR.mkdir();
	}

	private static String npcNameToFileName(final String npcName)
	{
		return npcName.toLowerCase().trim() + FILE_EXTENSION;
	}

	public void setPlayerUsername(final String username)
	{
		if (username.equalsIgnoreCase(name))
		{
			return;
		}

		playerFolder = new File(LOOT_RECORD_DIR, username);
		playerFolder.mkdir();
		name = username;
		bus.post(LTNameChange.class, new LTNameChange());
	}

	public Set<String> getKnownFileNames()
	{
		final Set<String> fileNames = new HashSet<>();

		final File[] files = playerFolder.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
		if (files != null)
		{
			for (final File f : files)
			{
				fileNames.add(f.getName().replace(FILE_EXTENSION, ""));
			}
		}

		return fileNames;
	}

	public synchronized Collection<LTRecord> loadLootTrackerRecords(String npcName)
	{
		final String fileName = npcNameToFileName(npcName);
		final File file = new File(playerFolder, fileName);
		final Collection<LTRecord> data = new ArrayList<>();

		StringBuilder sb = new StringBuilder();

		try (final BufferedReader br = new BufferedReader(new FileReader(file)))
		{
			// read line by line
			String line;
			int totalBrackets = 0;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("{"))
				{
					totalBrackets++;
				}
				else if (line.contains("}"))
				{
					totalBrackets--;
				}
				sb.append(line);

				if (totalBrackets == 0 && sb.length() > 0)
				{
					final LTRecord r = RuneLiteAPI.GSON.fromJson(sb.toString(), LTRecord.class);
					data.add(r);
					sb.setLength(0);
				}
			}
		}
		catch (FileNotFoundException e)
		{
			log.debug("File not found: {}", fileName);
		}
		catch (IOException e)
		{
			log.warn("IOException for file {}: {}", fileName, e.getMessage());
		}

		return data;
	}

	public synchronized boolean addLootTrackerRecord(LTRecord rec)
	{
		// Grab file
		final String fileName = npcNameToFileName(rec.getName());
		final File lootFile = new File(playerFolder, fileName);

		// Convert entry to JSON
		final String dataAsString = RuneLiteAPI.GSON.toJson(rec);

		// Open File in append mode and write new data
		try
		{
			final BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), true));
			file.append(dataAsString);
			file.newLine();
			file.close();
			bus.post(LTRecordStored.class, new LTRecordStored(rec));
			return true;
		}
		catch (IOException ioe)
		{
			log.warn("Error writing loot data to file {}: {}", fileName, ioe.getMessage());
			return false;
		}
	}

	// Mostly used to adjust previous loot entries such as adding pet drops/abyssal sire drops
	public synchronized boolean writeLootTrackerFile(String npcName, Collection<LTRecord> loots)
	{
		final String fileName = npcNameToFileName(npcName);
		final File lootFile = new File(playerFolder, fileName);

		try
		{
			final BufferedWriter file = new BufferedWriter(new FileWriter(String.valueOf(lootFile), false));
			for (final LTRecord rec : loots)
			{
				// Convert entry to JSON
				final String dataAsString = RuneLiteAPI.GSON.toJson(rec);
				file.append(dataAsString);
				file.newLine();
			}
			file.close();

			return true;
		}
		catch (IOException ioe)
		{
			log.warn("Error rewriting loot data to file {}: {}", fileName, ioe.getMessage());
			return false;
		}
	}

	public synchronized boolean deleteLootTrackerRecords(String npcName)
	{
		final String fileName = npcNameToFileName(npcName);
		final File lootFile = new File(playerFolder, fileName);

		if (lootFile.delete())
		{
			log.debug("Deleted loot file: {}", fileName);
			return true;
		}
		else
		{
			log.debug("Couldn't delete file: {}", fileName);
			return false;
		}
	}

	public Collection<LTRecord> loadAllLootTrackerRecords()
	{
		final List<LTRecord> recs = new ArrayList<>();

		for (final String n : getKnownFileNames())
		{
			recs.addAll(loadLootTrackerRecords(n));
		}

		return recs;
	}

	public void addLootTrackerRecordToDB(Collection<LootTrackerRecord> recs) throws SQLException
	{
		Connection conn = DriverManager.getConnection(url);
		List<LTItemEntry> drops = new ArrayList<>();
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

	public void addLootTrackerRecordToDB(String title, LootTrackerItem[] items) throws SQLException
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
}