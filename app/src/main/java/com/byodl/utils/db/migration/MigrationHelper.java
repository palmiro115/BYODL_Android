package com.byodl.utils.db.migration;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.StandardDatabase;
import org.greenrobot.greendao.internal.DaoConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper to migrate for greendao databases
 *
 */

class MigrationHelper {

    @SuppressWarnings("FieldCanBeLocal")
    private static boolean DEBUG = false;
    private static String TAG = "MigrationHelper";
    private static final String SQLITE_MASTER = "sqlite_master";
    private static final String SQLITE_TEMP_MASTER = "sqlite_temp_master";

    @SuppressWarnings("unchecked")
    static void migrate(SQLiteDatabase db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
        Database database = new StandardDatabase(db);

        printLog("【The Old Database Version】" + db.getVersion());
        printLog("【Generate temp table】start");
        generateTempTables(database, daoClasses);
        printLog("【Generate temp table】complete");

        dropTables(database, true, daoClasses);
        createTables(database, false, daoClasses);

        printLog("【Restore data】start");
        restoreData(database, daoClasses);
        printLog("【Restore data】complete");
    }

    @SuppressWarnings("unchecked")
    private static void generateTempTables(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
	    for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
		    String tempTableName = null;

		    DaoConfig daoConfig = new DaoConfig(db, daoClass);
		    String tableName = daoConfig.tablename;
		    if (!isTableExists(db, false, tableName)) {
			    printLog("【New Table】" + tableName);
			    continue;
		    }
		    try {
			    tempTableName = daoConfig.tablename.concat("_TEMP");
			    //noinspection StringBufferReplaceableByString
			    StringBuilder dropTableStringBuilder = new StringBuilder();
			    dropTableStringBuilder.append("DROP TABLE IF EXISTS ").append(tempTableName).append(";");
			    db.execSQL(dropTableStringBuilder.toString());

			    //noinspection StringBufferReplaceableByString
			    StringBuilder insertTableStringBuilder = new StringBuilder();
			    insertTableStringBuilder.append("CREATE TEMPORARY TABLE ").append(tempTableName);
			    insertTableStringBuilder.append(" AS SELECT * FROM ").append(tableName).append(";");
			    db.execSQL(insertTableStringBuilder.toString());
			    printLog("【Table】" + tableName + "\n ---Columns-->" + getColumnsStr(daoConfig));
			    printLog("【Generate temp table】" + tempTableName);
		    } catch (SQLException e) {
			    Log.e(TAG, "【Failed to generate temp table】" + tempTableName, e);
		    }
	    }
    }

    private static boolean isTableExists(Database db, boolean isTemp, String tableName) {
        if (db == null || TextUtils.isEmpty(tableName)) {
            return false;
        }
        String dbName = isTemp ? SQLITE_TEMP_MASTER : SQLITE_MASTER;
        String sql = "SELECT COUNT(*) FROM " + dbName + " WHERE type = ? AND name = ?";
        Cursor cursor=null;
        int count = 0;
        try {
            cursor = db.rawQuery(sql, new String[]{"table", tableName});
            if (cursor == null || !cursor.moveToFirst()) {
                return false;
            }
            count = cursor.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return count > 0;
    }


    private static String getColumnsStr(DaoConfig daoConfig) {
        if (daoConfig == null) {
            return "no columns";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < daoConfig.allColumns.length; i++) {
            builder.append(daoConfig.allColumns[i]);
            builder.append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }


    @SuppressWarnings({"WeakerAccess", "unchecked"})
    public static void dropTables(Database db, boolean ifExists, @NonNull Class<? extends AbstractDao<?, ?>>... daoClasses) {
        reflectMethod(db, "dropTable", ifExists, daoClasses);
        printLog("【Drop all table】");
    }

    @SuppressWarnings({"WeakerAccess", "unchecked"})
    public static void createTables(Database db, boolean ifNotExists, @NonNull Class<? extends AbstractDao<?, ?>>... daoClasses) {
        reflectMethod(db, "createTable", ifNotExists, daoClasses);
        printLog("【Create all table】");
    }

    /**
     * dao class already define the sql exec method, so just invoke it
     */
    @SuppressWarnings("unchecked")
    private static void reflectMethod(Database db, String methodName, boolean isExists, @NonNull Class<? extends AbstractDao<?, ?>>... daoClasses) {
        if (daoClasses.length < 1) {
            return;
        }
        try {
            for (Class cls : daoClasses) {
                Method method = cls.getDeclaredMethod(methodName, Database.class, boolean.class);
                method.invoke(null, db, isExists);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"WeakerAccess", "unchecked"})
    public static void restoreData(Database db, Class<? extends AbstractDao<?, ?>>... daoClasses) {
	    for (Class<? extends AbstractDao<?, ?>> daoClass : daoClasses) {
		    DaoConfig daoConfig = new DaoConfig(db, daoClass);
		    String tableName = daoConfig.tablename;
		    String tempTableName = daoConfig.tablename.concat("_TEMP");

		    if (!isTableExists(db, true, tempTableName)) {
			    continue;
		    }

		    try {
			    // get all columns from tempTable, take careful to use the columns list
			    List<String> columns = getColumns(db, tempTableName);
			    ArrayList<String> properties = new ArrayList<>(columns.size());
			    for (int j = 0; j < daoConfig.properties.length; j++) {
				    String columnName = daoConfig.properties[j].columnName;
				    if (columns.contains(columnName)) {
					    properties.add(columnName);
				    }
			    }
			    if (properties.size() > 0) {
				    final String columnSQL = TextUtils.join(",", properties);

				    //noinspection StringBufferReplaceableByString
				    StringBuilder insertTableStringBuilder = new StringBuilder();
				    insertTableStringBuilder.append("INSERT INTO ").append(tableName).append(" (");
				    insertTableStringBuilder.append(columnSQL);
				    insertTableStringBuilder.append(") SELECT ");
				    insertTableStringBuilder.append(columnSQL);
				    insertTableStringBuilder.append(" FROM ").append(tempTableName).append(";");
				    db.execSQL(insertTableStringBuilder.toString());
				    printLog("【Restore data】 to " + tableName);
			    }
			    //noinspection StringBufferReplaceableByString
			    StringBuilder dropTableStringBuilder = new StringBuilder();
			    dropTableStringBuilder.append("DROP TABLE ").append(tempTableName);
			    db.execSQL(dropTableStringBuilder.toString());
			    printLog("【Drop temp table】" + tempTableName);
		    } catch (SQLException e) {
			    Log.e(TAG, "【Failed to restore data from temp table 】" + tempTableName, e);
		    }
	    }
    }

    private static List<String> getColumns(Database db, String tableName) {
        List<String> columns = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName + " limit 0", null);
            if (null != cursor && cursor.getColumnCount() > 0) {
                columns = Arrays.asList(cursor.getColumnNames());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            if (null == columns)
                columns = new ArrayList<>();
        }
        return columns;
    }

    private static void printLog(String info){
        if(DEBUG){
            Log.d(TAG, info);
        }
    }
}
