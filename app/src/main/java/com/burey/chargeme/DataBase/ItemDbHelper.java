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

import com.burey.chargeme.Sale;

import org.apache.poi.hssf.record.formula.functions.T;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by burey on 01/09/2015.
 */
public class ItemDbHelper extends SQLiteOpenHelper {

    private static final String FILE_DIR = "DATA_BASE";
    private static final String DATABASE_NAME = "CHARGE_ME.DB";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_QUERY =
                    "CREATE TABLE IF NOT EXISTS "
                    + TablesDB.NewItemInfo.TABLE_NAME + "("
                    + TablesDB.NewItemInfo.ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + TablesDB.NewItemInfo.ITEM_NAME + " TEXT,"
                    + TablesDB.NewItemInfo.ITEM_QUANTITY + " TEXT,"
                    + TablesDB.NewItemInfo.ITEM_PRICE + " TEXT);";

    private static String CREATE_QUERY_SALES_INFO;

    private static final StringBuilder tableName = new StringBuilder();

    public ItemDbHelper(Context context, String tableName) {
        super(context, Environment.getExternalStorageDirectory()
                + File.separator
                + FILE_DIR
                + File.separator
                + DATABASE_NAME, null, DATABASE_VERSION);

//        if(this.tableName.length() == 0) // set table name with a date : item_sales_January_2015 for example
//        {
            setActiveTableName(tableName);
//        }
        Log.e("DATABASE OPERATIONS", "Database Created / Opened....");
    }

    // IMPORTANT!!!!onCreate method is only called when creating a new .DB file!!!
    // in case we want to create a new table in an EXISTING DB file there is a need
    // to do so in another method, preferably using SQL command:
    // CREATE TABLE IF NOT EXISTS table_name (column1, column2.....)
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_QUERY);
            db.execSQL(CREATE_QUERY_SALES_INFO);
        } catch (SQLiteCantOpenDatabaseException ex) {}
        Log.e("DATABASE OPERATIONS", "Table Created....");
        Log.e("DATABASE OPERATIONS", CREATE_QUERY_SALES_INFO);
        Log.e("DATABASE OPERATIONS", CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + this.tableName.toString());
        db.execSQL("DROP TABLE IF EXISTS " + TablesDB.NewItemInfo.TABLE_NAME);
        onCreate(db);
    }

    public void setActiveTableName(String tableName){
        String oldTable = "EMPTY";

        if(this.tableName.length() > 0)
        {
            oldTable = this.tableName.toString();
            this.tableName.delete(0, this.tableName.length());
        }
        this.tableName.insert(0, TablesDB.ItemSalesInfo.TABLE_NAME);
        this.tableName.append("_" + tableName);

        CREATE_QUERY_SALES_INFO = "CREATE TABLE IF NOT EXISTS "
                + this.tableName.toString() + "("
                + TablesDB.ItemSalesInfo.ITEM_ID + " INTEGER,"
                + TablesDB.ItemSalesInfo.CLIENT_ID + " INTEGER,"
                + TablesDB.ItemSalesInfo.ITEM_NAME + " TEXT,"
                + TablesDB.ItemSalesInfo.CLIENT_NAME + " TEXT,"
                + TablesDB.ItemSalesInfo.ITEM_QUANTITY_SOLD + " INTEGER,"
                + "PRIMARY KEY (" + TablesDB.ItemSalesInfo.ITEM_ID + "," + TablesDB.ItemSalesInfo.CLIENT_ID + "));";

        Log.e("Table Name", "Table Name Changed From " + oldTable + " To " + this.tableName.toString());
    }

    public void createItemTable(SQLiteDatabase db, String tableName)
    {
        setActiveTableName(tableName);
        try {
            db.execSQL(CREATE_QUERY_SALES_INFO);
            db.execSQL(CREATE_QUERY);
        } catch (SQLiteCantOpenDatabaseException ex) {}
        Log.e("DATABASE OPERATIONS", "Table Created....");
        Log.e("DATABASE OPERATIONS", CREATE_QUERY_SALES_INFO);
        Log.e("DATABASE OPERATIONS",CREATE_QUERY);
    }

    public Cursor getItemInformation(SQLiteDatabase db) {
        Cursor cursor;
        String[] projections = {
                TablesDB.NewItemInfo.ITEM_ID,
                TablesDB.NewItemInfo.ITEM_NAME,
                TablesDB.NewItemInfo.ITEM_QUANTITY,
                TablesDB.NewItemInfo.ITEM_PRICE};
        try {
            cursor = db.query(TablesDB.NewItemInfo.TABLE_NAME, projections, null, null, null, null, null);
            return cursor;
        }
        catch (SQLiteException e){}
        return null;
    }

    public void addItem(String ItemName, String ItemQuantity, String ItemPrice, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewItemInfo.ITEM_NAME, ItemName);
        contentValues.put(TablesDB.NewItemInfo.ITEM_QUANTITY, ItemQuantity);
        contentValues.put(TablesDB.NewItemInfo.ITEM_PRICE, ItemPrice);
        db.insert(TablesDB.NewItemInfo.TABLE_NAME, null, contentValues);
        Log.e("DATABASE OPERATIONS", "Row Inserted....");
        String sqlCmd = "INSERT INTO "
                + TablesDB.NewItemInfo.TABLE_NAME + " ("
                + TablesDB.NewItemInfo.ITEM_NAME + ","
                + TablesDB.NewItemInfo.ITEM_QUANTITY + ","
                + TablesDB.NewItemInfo.ITEM_PRICE + ") VALUES ("
                + ItemName + ","
                + ItemQuantity + ","
                + ItemPrice + ");";
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void updateItemName(int rowId, String newItemName, String oldItemName, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewItemInfo.ITEM_NAME, newItemName);
        String sqlCmd =
                "UPDATE " + TablesDB.NewItemInfo.TABLE_NAME
                        + " SET " + contentValues
                        + " WHERE " + TablesDB.NewItemInfo.ITEM_ID + "=" + rowId;
        int rowsAffected = db.update(TablesDB.NewItemInfo.TABLE_NAME, contentValues, TablesDB.NewItemInfo.ITEM_ID + "=?", new String[]{Integer.toString(rowId)});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void updateItemQuantity(int rowId, String ItemName, String ItemQuantity, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewItemInfo.ITEM_QUANTITY, ItemQuantity);
        String sqlCmd =
                "UPDATE " + TablesDB.NewItemInfo.TABLE_NAME
                        + " SET " + contentValues
                        + " WHERE " + TablesDB.NewItemInfo.ITEM_ID + "=" + rowId;
        int rowsAffected = db.update(TablesDB.NewItemInfo.TABLE_NAME, contentValues, TablesDB.NewItemInfo.ITEM_ID + "=?", new String[]{Integer.toString(rowId)});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void updateItemPrice(int rowId, String ItemName, String ItemPrice, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.NewItemInfo.ITEM_PRICE, ItemPrice);
        String sqlCmd =
                "UPDATE " + TablesDB.NewItemInfo.TABLE_NAME
                        + " SET " + contentValues
                        + " WHERE " + TablesDB.NewItemInfo.ITEM_ID + "=" + rowId;
        int rowsAffected = db.update(TablesDB.NewItemInfo.TABLE_NAME, contentValues, TablesDB.NewItemInfo.ITEM_ID + "=?", new String[]{Integer.toString(rowId)});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public ArrayList<Sale> getItemSales(SQLiteDatabase db, String tableName){
        ArrayList<Sale> salesList = new ArrayList<>();
        Cursor cursor;
        String[] projections = {
                TablesDB.ItemSalesInfo.ITEM_ID,
                TablesDB.ItemSalesInfo.CLIENT_ID,
                TablesDB.ItemSalesInfo.ITEM_NAME,
                TablesDB.ItemSalesInfo.CLIENT_NAME,
                TablesDB.ItemSalesInfo.ITEM_QUANTITY_SOLD};
        try {
            cursor = db.query(TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName.toString(), projections, null, null, null, null, null);
            if(cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        // fetch item id
                        int _item_id = cursor.getInt(0);
                        // fetch client id
                        int _client_id = cursor.getInt(1);
                        // fetch item name
                        String item_name = cursor.getString(2);
                        // fetch client name
                        String client_name = cursor.getString(3);
                        // fetch ordered amount
                        int ordered = cursor.getInt(4);
                        // create new Sale from the retrieved data
                        Sale sale = new Sale(_item_id, _client_id, item_name, client_name, ordered);
                        // add the item to the list view using the adapter itemListDataAdapter
                        salesList.add(sale);
                    } while (cursor.moveToNext());
                    // iterate while there is data to fetch and retrieve the records from the database
                    cursor.close();
                    Collections.sort(salesList);
                }
            }
        }
        catch (SQLiteException e){
            e.printStackTrace();
        }
        return salesList;
    }





//    public Cursor getItemSalesByTableName(SQLiteDatabase db, String tableName){
//        Cursor cursor;
//        String[] projections = {
//                TablesDB.ItemSalesInfo.ITEM_ID,
//                TablesDB.ItemSalesInfo.CLIENT_ID,
//                TablesDB.ItemSalesInfo.ITEM_NAME,
//                TablesDB.ItemSalesInfo.CLIENT_NAME,
//                TablesDB.ItemSalesInfo.ITEM_QUANTITY_SOLD};
//        try {
//            cursor = db.query(TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName.toString(), projections, null, null, null, null, null);
//            return cursor;
//        }
//        catch (SQLiteException e){
//            e.printStackTrace();
//        }
//        return null;
//    }

    public void addItemSales(int itemID,int clientID, String itemName, String clientName, int itemQuantitySold, SQLiteDatabase db, String tableName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.ItemSalesInfo.ITEM_ID, itemID);
        contentValues.put(TablesDB.ItemSalesInfo.CLIENT_ID, clientID);
        contentValues.put(TablesDB.ItemSalesInfo.ITEM_NAME, itemName);
        contentValues.put(TablesDB.ItemSalesInfo.CLIENT_NAME, clientName);
        contentValues.put(TablesDB.ItemSalesInfo.ITEM_QUANTITY_SOLD, itemQuantitySold);
        db.insert(TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName, null, contentValues);
        Log.e("DATABASE OPERATIONS", "Row Inserted....");
        String sqlCmd = "INSERT INTO "
                + TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName + " ("
                + TablesDB.ItemSalesInfo.ITEM_ID + ","
                + TablesDB.ItemSalesInfo.CLIENT_ID + ","
                + TablesDB.ItemSalesInfo.ITEM_NAME + ","
                + TablesDB.ItemSalesInfo.CLIENT_NAME + ","
                + TablesDB.ItemSalesInfo.ITEM_QUANTITY_SOLD + ") VALUES ("
                + itemID + ","
                + clientID + ","
                + itemName + ","
                + clientName + ","
                + itemQuantitySold + ");";
        Log.e("SQL COMMAND", sqlCmd);
    }

    public void updateItemSales(int itemID,int clientID, String itemName, String clientName, int itemQuantitySold, SQLiteDatabase db, String tableName) {
        /*
        update item sales in a table by the itemID and client ID
         */
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablesDB.ItemSalesInfo.ITEM_QUANTITY_SOLD, itemQuantitySold);
        String sqlCmd =
                        "UPDATE " + TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName
                        + " SET " + contentValues
                        + " WHERE (" + TablesDB.ItemSalesInfo.ITEM_ID + "=" + itemID
                        + " AND " + TablesDB.ItemSalesInfo.CLIENT_ID + "=" + clientID + ");";
        int rowsAffected = db.update(TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName, contentValues,
                TablesDB.ItemSalesInfo.ITEM_ID + "=? AND " + TablesDB.ItemSalesInfo.CLIENT_ID + "=?", new String[]{Integer.toString(itemID), Integer.toString(clientID)});
        Log.e("SQL COMMAND", sqlCmd);
    }

    public ArrayList<Sale> getSalesForClient(int clientID, SQLiteDatabase db, String tableName){
        /*
        get sales of a specific client in a specific table
         */
        ArrayList<Sale> salesList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT "
                + TablesDB.ItemSalesInfo.ITEM_ID + ","
                + TablesDB.ItemSalesInfo.CLIENT_ID + ","
                + TablesDB.ItemSalesInfo.ITEM_NAME + ","
                + TablesDB.ItemSalesInfo.CLIENT_NAME + ","
                + TablesDB.ItemSalesInfo.ITEM_QUANTITY_SOLD + " FROM "
                + TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName
                + " WHERE " + TablesDB.ItemSalesInfo.CLIENT_ID + "=?", new String[]{Integer.toString(clientID)});
        if(cursor == null)
            return salesList;
        if (cursor.moveToFirst()) {
            do {
                // fetch item id
                int _item_id = cursor.getInt(0);
                // fetch client id
                int _client_id = cursor.getInt(1);
                // fetch item name
                String item_name = cursor.getString(2);
                // fetch client name
                String client_name = cursor.getString(3);
                // fetch ordered amount
                int ordered = cursor.getInt(4);
                // create new Sale from the retrieved data
                Sale sale = new Sale(_item_id, _client_id, item_name, client_name, ordered);
                // add the item to the list view using the adapter itemListDataAdapter
                salesList.add(sale);
            } while (cursor.moveToNext());
            // iterate while there is data to fetch and retrieve the records from the database
            Collections.sort(salesList);
            cursor.close();
        }
        return salesList; // return the array
    }

    public void deleteClientSales(int clientID, SQLiteDatabase db, String tableName){
        int rowsAffected = db.delete(TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName,
                TablesDB.ItemSalesInfo.CLIENT_ID + "=?",
                new String[]{Integer.toString(clientID)});
        Log.e("SQL COMMAND DELETE", rowsAffected + " rows were deleted from table " + TablesDB.ItemSalesInfo.TABLE_NAME + "_" + tableName);
    }

    public ArrayList<String> getSalesTables(SQLiteDatabase db) {
        /*
            retrieves the names of the existing charge tables from the database
         */
        ArrayList<String> dirArray = new ArrayList<String>(); // array list for the names

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null); // cursor to select the table names
        if(c == null)
            return dirArray;
        // attempt to move to the first record
        if (c.moveToFirst()) {
            // while still have records, add the table name to the list
            while (!c.isAfterLast()) {
                if(c.getString(c.getColumnIndex("name")).contains("item_sales")) {
                    // a table name is as follows: clients_table_Month_Year, for example: item_sales_April_2015
                    // since only required is the April_2015 portion, get the substring from where needed (11) = size(item_sales_)
                    dirArray.add(c.getString(c.getColumnIndex("name")).substring(11, c.getString(c.getColumnIndex("name")).length()));
                    Log.e("Get Charges Tables", c.getString(c.getColumnIndex("name")));
                }
                c.moveToNext(); // move to the next record
            }
            c.close(); // close the cursor
        }
        return dirArray; // return the array
    }
}
