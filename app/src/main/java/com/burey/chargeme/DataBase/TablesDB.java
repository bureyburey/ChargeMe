package com.burey.chargeme.DataBase;

/**
 * Created by burey on 30/08/2015.
 */

import java.util.Calendar;
import java.util.Date;

/**
 * this class is used as an abstract representation of the column names and the name of the table
 * for each table which we create there is a need to create similar classes which holds:
 * TABLE_NAME
 * COLUMN_NAME_1   COLUMN_NAME_2   .....   COLUMN_NAME_N
 */

public class TablesDB {

    public static abstract class ClientInfoList{
        /*
            scheme of all of the clients in the database
            (_ID, client_name, client_number, is_toshav)
            all clients from all charge tables will appear here once
         */
        public static final String TABLE_NAME = "all_clients";
        public static final String CLIENT_ID = "_ID";
        public static final String CLIENT_NAME = "client_name"; // String
        public static final String CLIENT_NUMBER = "client_number"; // String
        public static final String IS_TOSHAV = "is_toshav"; // boolean value stating the client is a toshav
    }

    public static abstract class NewClientInfo {
        /*
            scheme of a charge table of clients
            (_ID, client_name, client_total, client_number, is_toshav)
            a client will appear once on a charge table, but can appear on different ones
        */
        public static final String TABLE_NAME = "clients_table";
        public static final String CLIENT_ID = "_ID";
        public static final String CLIENT_NAME = "client_name"; // String
        public static final String CLIENT_TOTAL = "client_total"; // String
        public static final String CLIENT_NUMBER = "client_number"; // String
        public static final String IS_TOSHAV = "is_toshav"; // boolean value stating the client is a toshav
    }

    public static abstract class NewItemInfo {
        /*
            scheme of items in the database
            (_ID, item_name, item_quantity, item_price)
            each item will appear once on this table
         */
        public static final String TABLE_NAME = "item_table";
        public static final String ITEM_ID = "_ID";
        public static final String ITEM_NAME = "item_name"; // String
        public static final String ITEM_QUANTITY = "item_quantity"; // String
        public static final String ITEM_PRICE = "item_price"; // String
    }

    public static abstract class ItemSalesInfo{
        /*
            scheme of sales information
            (_ITEM_ID, _CLIENT_ID, item_name, client_name, item_quantity_sold)
         */
        public static final String TABLE_NAME = "item_sales";
        public static final String ITEM_ID = "_ITEM_ID";
        public static final String CLIENT_ID = "_CLIENT_ID";
        public static final String ITEM_NAME = "item_name";
        public static final String CLIENT_NAME = "client_name";
        public static final String ITEM_QUANTITY_SOLD = "item_quantity_sold";
    }
}