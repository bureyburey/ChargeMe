package com.burey.chargeme.DataBase;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by burey on 30/08/2015.
 */
public class ClientDbHelper extends SQLiteOpenHelper {

    private static final String FILE_DIR = "DATA_BASE";
    private static final String DATABASE_NAME = "CHARGE_ME.DB";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_QUERY_CLIENTS =
                    "CREATE TABLE IF NOT EXISTS "
                    + TablesDB.ClientInfoList.TABLE_NAME + "("
                    + TablesDB.ClientInfoList.CLIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TablesDB.ClientInfoList.CLIENT_NAME + " TEXT,"
                    + TablesDB.ClientInfoList.CLIENT_NUMBER + " TEXT,"
                    + TablesDB.ClientInfoList.IS_TOSHAV + " INTEGER);";

    private static String CREATE_QUERY_CHARGE_TABLE;

    private static final StringBuilder tableName = new StringBuilder();

    public ClientDbHelper(Context context, String tableName) {
        super(context, Environment.getExternalStorageDirectory()
                + File.separator
                + FILE_DIR
                + File.separator
                + DATABASE_NAME, null, DATABASE_VERSION);

//        if(this.tableName.length() == 0) // set table name with a date : clients_table_January_2015 for example
//        {
            setActiveTableName(tableName);
//        }
        Log.e("DATABASE OPERATIONS", "Database Created / Opened....");
        Log.e("Table Name", this.tableName.toString());
        Log.e("Table Name", CREATE_QUERY_CHARGE_TABLE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_QUERY_CHARGE_TABLE);
            db.execSQL(CREATE_QUERY_CLIENTS);
        } catch (SQLiteCantOpenDatabaseException ex) {}
        Log.e("DATABASE OPERATIONS", "Table Created....");
        Log.e("DATABASE OPERATIONS", CREATE_QUERY_CHARGE_TABLE);
        Log.e("DATABASE OPERATIONS", CREATE_QUERY_CLIENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + this.tableName.toString());
        db.execSQL("DROP TABLE IF EXISTS " + TablesDB.ClientInfoList.TABLE_NAME);
        onCreate(db);
    }

    public void setActiveTableName(String tableName) {
        String oldTable = "EMPTY";

        if(this.tableName.length() > 0) {
            oldTable = this.tableName.toString();
            this.tableName.delete(0, this.tableName.length());
        }
        this.tableName.insert(0, TablesDB.NewClientInfo.TABLE_NAME);
        this.tableName.append("_" + tableName);

        CREATE_QUERY_CHARGE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + this.tableName.toString() + "("
                + TablesDB.NewClientInfo.CLIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TablesDB.NewClientInfo.CLIENT_NAME + " TEXT,"
                + TablesDB.NewClientInfo.CLIENT_TOTAL + " TEXT,"
                + TablesDB.NewClientInfo.CLIENT_NUMBER + " TEXT,"
                + TablesDB.NewClientInfo.IS_TOSHAV + " INTEGER);";
        Log.e("Table Name", "Table Name Changed From " + oldTable + " To " + this.tableName.toString());
    }

    public ArrayList<String> getChargeTables(SQLiteDatabase db) {
        /*
            retrieves the names of the existing charge tables from the database
         */
        ArrayList<String> dirArray = new ArrayList<>(); // array list for the names
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null); // cursor to select the table names
        if(cursor == null)
            return dirArray;
        // attempt to move to the first record
        if (cursor.moveToFirst()) {
            // while still have records, add the table name to the list
            while (!cursor.isAfterLast()) {
                if(cursor.getString(cursor.getColumnIndex("name")).startsWith("clients_table")) {
                    // a table name is as follows: clients_table_Month_Year, for example: clients_table_April_2015
                    // since only required is the April_2015 portion, get the substring from where needed (14) = size(clients_table_)
                    dirArray.add(cursor.getString(cursor.getColumnIndex("name")).substring(14, cursor.getString(cursor.getColumnIndex("name")).length()));
                    Log.e("Get Charges Tables", cursor.getString(cursor.getColumnIndex("name")));
                }
                cursor.moveToNext(); // move to the next record
            }
            cursor.close(); // close the cursor
        }
        return dirArray; // return the array
    }

    public ArrayList<String> getArchivedChargeTables(SQLiteDatabase db){
        /*
            retrieves the names of the archived charge tables from the database
         */
        ArrayList<String> dirArray = new ArrayList<>(); // array list for the names
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null); // cursor to select the table names
        if(cursor == null)
            return dirArray;
        // attempt to move to the first record
        if (cursor.moveToFirst()) {
            // while still have records, add the table name to the list
            while (!cursor.isAfterLast()) {
                if(cursor.getString(cursor.getColumnIndex("name")).startsWith("archive")) {
                    // a table name is as follows: archive_clients_table_Month_Year, for example: archive_clients_table_April_2015
                    // since only required is the April_2015 portion, get the substring from where needed (22) = size(archive_clients_table_)
                    dirArray.add(cursor.getString(cursor.getColumnIndex("name")).substring(22, cursor.getString(cursor.getColumnIndex("name")).length()));
                    Log.e("Get Charges Tables", cursor.getString(cursor.getColumnIndex("name")));
                }
                cursor.moveToNext(); // move to the next record
            }
            cursor.close(); // close the cursor
        }
        return dirArray; // return the array
    }

    public void moveChargeTableToArchive(SQLiteDatabase db, String tableName){
        String oldName = TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName;
        String newName = "archive_" + oldName;
        String sqlCmd = "ALTER TABLE " + oldName + " RENAME TO " + newName + ";";

        db.beginTransaction();
        try{
            db.execSQL(sqlCmd);
            db.setTransactionSuccessful();
        } finally{
            db.endTransaction();
        }
        Log.e("DATABASE OPERATIONS", sqlCmd);
    }

    public void recoverChargeTableFromArchive(SQLiteDatabase db, String tableName){
        String oldName = "archive_" + TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName;
        String newName = TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName;
        String sqlCmd = "ALTER TABLE " + oldName + " RENAME TO " + newName + ";";

        db.beginTransaction();
        try{
            db.execSQL(sqlCmd);
            db.setTransactionSuccessful();
        } finally{
            db.endTransaction();
        }
        Log.e("DATABASE OPERATIONS", sqlCmd);
    }

    public Cursor getAllClients(SQLiteDatabase db) {
        /*
            return a cursor object containing all clients information
         */
        Cursor cursor;
        // projections are the requested columns as they appear in the database
        String[] projections = {
                TablesDB.ClientInfoList.CLIENT_ID,
                TablesDB.ClientInfoList.CLIENT_NAME,
                TablesDB.ClientInfoList.CLIENT_NUMBER,
                TablesDB.ClientInfoList.IS_TOSHAV};
        try {
            cursor = db.query(TablesDB.ClientInfoList.TABLE_NAME, projections, null, null, null, null, null);
            return cursor;
        }
        catch (SQLiteException e){}
        return null;

    }

    public void createClientTable(SQLiteDatabase db, String tableName) {
        setActiveTableName(tableName);
        try {
            db.execSQL(CREATE_QUERY_CHARGE_TABLE);
            db.execSQL(CREATE_QUERY_CLIENTS);
        } catch (SQLiteCantOpenDatabaseException ex) {}
        Log.e("DATABASE OPERATIONS", "Table Created....");
        Log.e("DATABASE OPERATIONS", CREATE_QUERY_CHARGE_TABLE);
    }

    public Cursor getClientInformation(SQLiteDatabase db) {
        Cursor cursor;
        String[] projections = {
                TablesDB.NewClientInfo.CLIENT_ID,
                TablesDB.NewClientInfo.CLIENT_NAME,
                TablesDB.NewClientInfo.CLIENT_TOTAL,
                TablesDB.NewClientInfo.CLIENT_NUMBER,
                TablesDB.NewClientInfo.IS_TOSHAV};
        try {
            cursor = db.query(this.tableName.toString(), projections, null, null, null, null, null);
            return cursor;
        }
        catch (SQLiteException e){}
        return null;
    }

    public void addClient(String clientName, String clientTotal, String clientNumber,boolean isToshav, SQLiteDatabase db, String tableName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewClientInfo.CLIENT_NAME, clientName);
        contentValues.put(TablesDB.NewClientInfo.CLIENT_TOTAL, clientTotal);
        contentValues.put(TablesDB.NewClientInfo.CLIENT_NUMBER, clientNumber);
        contentValues.put(TablesDB.NewClientInfo.IS_TOSHAV, (isToshav) ? 1 : 0);
        db.insert(TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName, null, contentValues);
        addClientToFullList(clientName,clientNumber,isToshav,db);
        //db.insertWithOnConflict(this.tableName.toString(), null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        Log.e("DATABASE OPERATIONS", "Row Inserted....");
        String sqlCmd = "INSERT INTO "
                + TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName + " ("
                + TablesDB.NewClientInfo.CLIENT_NAME + ","
                + TablesDB.NewClientInfo.CLIENT_TOTAL + ","
                + TablesDB.NewClientInfo.CLIENT_NUMBER + ","
                + TablesDB.NewClientInfo.IS_TOSHAV + ") VALUES ("
                + clientName + ","
                + clientTotal + ","
                + clientNumber + ","
                + ((isToshav) ? 1 : 0) + ");";
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void addClientToFullList(String clientName, String clientNumber, boolean isToshav, SQLiteDatabase db) {
        if(!ClientExistsFullList(clientName, db)) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TablesDB.ClientInfoList.CLIENT_NAME, clientName);
            contentValues.put(TablesDB.ClientInfoList.CLIENT_NUMBER, clientNumber);
            contentValues.put(TablesDB.ClientInfoList.IS_TOSHAV, (isToshav) ? 1 : 0);
            db.insert(TablesDB.ClientInfoList.TABLE_NAME, null, contentValues);
            String sqlCmd = "INSERT INTO "
                    + TablesDB.ClientInfoList.TABLE_NAME + " ("
                    + TablesDB.ClientInfoList.CLIENT_NAME + ","
                    + TablesDB.ClientInfoList.CLIENT_NUMBER + ","
                    + TablesDB.ClientInfoList.IS_TOSHAV + ") VALUES ("
                    + clientName + ","
                    + clientNumber + ","
                    + ((isToshav) ? 1 : 0) + ");";
            Log.e("SQL COMMAND", sqlCmd);
        }
        else
            Log.e("SQL MESSAGE", "Client Name: " + clientName + " Already Exists!!!");
    }

    public boolean ClientExistsFullList(String clientName, SQLiteDatabase db) {
        boolean exists = false;
        String sqlCmd = "SELECT 1 FROM " + TablesDB.ClientInfoList.TABLE_NAME + " WHERE " + TablesDB.ClientInfoList.CLIENT_NAME + "=?";
        Cursor cursor = db.rawQuery(sqlCmd, new String[] { clientName });
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exists = true;
            }
            cursor.close();
        }
        return exists;
    }

    public boolean tableNameExists(SQLiteDatabase db, String tableName){
        boolean exists = false;
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null); // cursor to select the table names
        if(cursor == null)
            return exists;
        // attempt to move to the first record
        if (cursor.moveToFirst()) {
            // while still have records, add the table name to the list
            while (!cursor.isAfterLast()) {
                String cursorTableName = cursor.getString(0);
                // check if the table already exists
                if(cursorTableName.startsWith("clients_table")){
                    cursorTableName = cursorTableName.substring(14);
                    if(cursorTableName.toLowerCase().equals(tableName.toLowerCase())) {
                        exists = true;
                        break;
                    }
                }
                // check if the table once existed (archived)
                else if(cursorTableName.startsWith("archive_clients_table")) {
                    cursorTableName = cursorTableName.substring(22);
                    if (cursorTableName.toLowerCase().equals(tableName.toLowerCase())) {
                        exists = true;
                        break;
                    }
                }
                cursor.moveToNext(); // move to the next record
            }
            cursor.close(); // close the cursor
        }
        return exists;
    }

    public void updateClientTotal(int rowId, String clientName, String clientTotal, SQLiteDatabase db, String tableName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewClientInfo.CLIENT_TOTAL, clientTotal);
        String sqlCmd =
                "UPDATE " + TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName
                + " SET " + contentValues
                + " WHERE " + TablesDB.NewClientInfo.CLIENT_ID + "=" + rowId;
        int rowsAffected = db.update(TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName, contentValues, TablesDB.NewClientInfo.CLIENT_ID + "=?", new String[]{Integer.toString(rowId)});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void updateClientName(int rowId, String newClientName, String oldClientName, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewClientInfo.CLIENT_NAME, newClientName);
        String sqlCmd =
                "UPDATE " + TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName
                + " SET " + contentValues
                + " WHERE " + TablesDB.NewClientInfo.CLIENT_NAME + "=" + rowId;
        int rowsAffected = db.update(this.tableName.toString(), contentValues, TablesDB.NewClientInfo.CLIENT_ID + "=?", new String[]{Integer.toString(rowId)});
        // update the name on the full clients list
        db.update(TablesDB.ClientInfoList.TABLE_NAME, contentValues, TablesDB.ClientInfoList.CLIENT_NAME + "=?", new String[]{oldClientName});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void updateClientNumber(int rowId, String clientName, String clientNumber, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewClientInfo.CLIENT_NUMBER, clientNumber);
        String sqlCmd =
                "UPDATE " + this.tableName.toString()
                + " SET " + contentValues
                + " WHERE " + TablesDB.NewClientInfo.CLIENT_ID + "=" + rowId;
        int rowsAffected = db.update(this.tableName.toString(), contentValues, TablesDB.NewClientInfo.CLIENT_ID + "=?", new String[]{Integer.toString(rowId)});
        db.update(TablesDB.ClientInfoList.TABLE_NAME, contentValues, TablesDB.ClientInfoList.CLIENT_NAME + "=?", new String[]{clientName});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void updateClientIsToshav(int rowId, String clientName, boolean isToshav, SQLiteDatabase db){
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewClientInfo.IS_TOSHAV, isToshav);
        String sqlCmd =
                "UPDATE " + this.tableName.toString()
                        + " SET " + contentValues
                        + " WHERE " + TablesDB.NewClientInfo.CLIENT_ID + "=" + rowId;
        int rowsAffected = db.update(this.tableName.toString(), contentValues, TablesDB.NewClientInfo.CLIENT_ID + "=?", new String[]{Integer.toString(rowId)});
        db.update(TablesDB.ClientInfoList.TABLE_NAME, contentValues, TablesDB.ClientInfoList.CLIENT_NAME + "=?", new String[]{clientName});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void deleteClientFromTable(int clientID, SQLiteDatabase db, String tableName){
        int rowsAffected = db.delete(TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName,
                TablesDB.NewClientInfo.CLIENT_ID + "=?",
                new String[]{Integer.toString(clientID)});
        Log.e("SQL COMMAND DELETE", rowsAffected + " rows were deleted from table " + TablesDB.NewClientInfo.TABLE_NAME + "_" + tableName);
    }
}
