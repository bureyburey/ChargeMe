package com.burey.chargeme;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.burey.chargeme.DataBase.ItemDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by burey on 02/09/2015.
 */
public class ItemListDataAdapterForAddChargeActivity extends ArrayAdapter {

    //private TextView total_chargeTextView;
    private int total_charge;
    private List<Item> fullList = new ArrayList<Item>();
    private List<Item> searchList = new ArrayList<Item>();
    private ItemDbHelper itemDbHelper;
    private SQLiteDatabase sqLiteDatabase;

    public ItemListDataAdapterForAddChargeActivity(Context context, int resource) {
        super(context, resource);
        total_charge = 0;
    }

    public static class LayoutHandler {
        int ITEM_ID;
        TextView ITEM_NAME;
        TextView ITEM_QUANTITY;
        TextView ITEM_PRICE;
        Button PLUS;
        Button MINUS;
        TextView CHARGE_AMOUNT;
        int ordered;
    }

    @Override
    public void add(Object object) {
        super.add(object);
        fullList.add((Item) object);
        searchList.add((Item) object);
    }

    @Override
    public int getCount() {
        return searchList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchList.get(position);
    }

    public int getChargeTotal()
    {
        return this.total_charge;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;
        final LayoutHandler layoutHandler;
        final LayoutInflater layoutInflater;
        final Item item = (Item)this.getItem(position);
        layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (row == null) {

            row = layoutInflater.inflate(R.layout.row_item_add_charge_layout, parent, false);
            layoutHandler = new LayoutHandler();
            layoutHandler.ITEM_NAME = (TextView) row.findViewById(R.id.tv_item_row_name_add_charge);
            layoutHandler.ITEM_QUANTITY = (TextView) row.findViewById(R.id.tv_item_row_quantity_add_charge);
            layoutHandler.ITEM_PRICE = (TextView) row.findViewById(R.id.tv_item_row_price_add_charge);
            layoutHandler.PLUS = (Button)row.findViewById(R.id.btn_item_row_plus_add_charge);
            layoutHandler.MINUS = (Button)row.findViewById(R.id.btn_item_row_minus_add_charge);
            layoutHandler.CHARGE_AMOUNT = (TextView) row.findViewById(R.id.tv_items_amount_add_charge);
            layoutHandler.CHARGE_AMOUNT.setText("0");

            layoutHandler.ordered = 0;


            row.setTag(layoutHandler);
        }

        else {
            layoutHandler = (LayoutHandler) row.getTag();
        }

        layoutHandler.PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutHandler.ordered = item.getOrdered();
                layoutHandler.ordered++;
                item.setOrdered(layoutHandler.ordered);
                layoutHandler.CHARGE_AMOUNT.setText(Integer.toString(layoutHandler.ordered));
                //Toast.makeText(getContext(), "PLUS " + item.getName() + " position: " + position + " Ordered: " + layoutHandler.ordered, Toast.LENGTH_SHORT).show();

                total_charge += Integer.parseInt(item.getPrice());
                notifyDataSetChanged();
            }
        });

        layoutHandler.MINUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutHandler.ordered = item.getOrdered();
                layoutHandler.ordered--;
                if (layoutHandler.ordered >= 0) {

                    item.setOrdered(layoutHandler.ordered);
                    layoutHandler.CHARGE_AMOUNT.setText(Integer.toString(layoutHandler.ordered));

                    total_charge -= Integer.parseInt(item.getPrice());
                    notifyDataSetChanged();
                } else
                    layoutHandler.ordered = 0;
            }
        });

        layoutHandler.ITEM_NAME.setText(item.getName().toString());
        layoutHandler.ITEM_QUANTITY.setText(item.getQuantity().toString());
        layoutHandler.ITEM_PRICE.setText(item.getPrice().toString());
        layoutHandler.CHARGE_AMOUNT.setText(Integer.toString(item.getOrdered()));

        return row;
    }


    public void updateSales(String tableName, int clientID, String clientName){
        itemDbHelper = new ItemDbHelper(getContext(), tableName);
        sqLiteDatabase = itemDbHelper.getWritableDatabase();
        itemDbHelper.createItemTable(sqLiteDatabase, tableName);
//        Cursor cursor = itemDbHelper.getItemSales(sqLiteDatabase);
//        List<Sale> salesList = new ArrayList<Sale>();
        List<Sale> salesList = itemDbHelper.getItemSales(sqLiteDatabase, tableName);

//        if(cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    // fetch item id
//                    int _item_id = cursor.getInt(0);
//                    // fetch client id
//                    int _client_id = cursor.getInt(1);
//                    // fetch item name
//                    String item_name = cursor.getString(2);
//                    // fetch client name
//                    String client_name = cursor.getString(3);
//                    // fetch ordered amount
//                    int ordered = cursor.getInt(4);
//
//                    // create new Sale from the retrieved data
//                    Sale sale = new Sale(_item_id, _client_id, item_name, client_name, ordered);
//                    // add the item to the list view using the adapter itemListDataAdapter
//                    salesList.add(sale);
//                } while (cursor.moveToNext());
//                // iterate while there is data to fetch and retrieve the records from the database
//            }
//        }

        for(Item item: fullList){
            if(item.getOrdered() > 0) {
                int index = getSaleIndex(item, salesList, clientID);
                if(index == -1)
                    itemDbHelper.addItemSales(item.get_id(), clientID, item.getName(), clientName, item.getOrdered(), sqLiteDatabase, tableName);
                else
                    itemDbHelper.updateItemSales(item.get_id(), clientID, item.getName(),clientName, (item.getOrdered() + salesList.get(index).getOrdered()), sqLiteDatabase, tableName);
            }
//            Toast.makeText(getContext(), item.getName() + " Ordered: " + item.getOrdered(), Toast.LENGTH_SHORT).show();
        }
        itemDbHelper.close();
        sqLiteDatabase.close();
    }

    public int getSaleIndex(Item item, List<Sale> saleList, int clientID){
        for(int i = 0; i < saleList.size(); i++){
            // if item and client already exists in the list, just need to update ordered amount
            if(saleList.get(i).get_item_id() == item.get_id() && clientID == saleList.get(i).get_client_id())
                return i;
//            if(itemList.get(i).getName().toString().equals(item.getName().toString()))
//                return i;
        }
        return -1;
    }

    public void filter(CharSequence charText) {
        charText = charText.toString().toLowerCase(Locale.getDefault());
        searchList.clear();
        if (charText.length() == 0) {
            searchList.addAll(fullList);
        } else {
            for (Item item : fullList) {
                if (item.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    searchList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }
}
