package com.byodl.utils.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.byodl.model.DaoMaster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Helps to migrate greendao scheme versions
 */
public class DatabaseUpgradeHelper extends DaoMaster.OpenHelper {

	public DatabaseUpgradeHelper(Context context, String name) {
		super(context, name);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		List<Migration> migrations = getMigrations();

		// Only run migrations past the old version
		for (Migration migration : migrations) {
			if (oldVersion < migration.getVersion()) {
				migration.runMigration(db);
			}
		}
	}

	private List<Migration> getMigrations() {
		List<Migration> migrations = new ArrayList<>();

		// Sorting just to be safe, in case other people add migrations in the wrong order.
		Comparator<Migration> migrationComparator = new Comparator<Migration>() {
			@Override
			public int compare(Migration m1, Migration m2) {
				return m1.getVersion().compareTo(m2.getVersion());
			}
		};
		Collections.sort(migrations, migrationComparator);

		return migrations;
	}


	public interface Migration {
		Integer getVersion();

		void runMigration(SQLiteDatabase db);
	}
}