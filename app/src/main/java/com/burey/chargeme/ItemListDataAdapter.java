package com.burey.chargeme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.burey.chargeme.DataBase.ItemDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by burey on 01/09/2015.
 */
public class ItemListDataAdapter extends ArrayAdapter {
    /*
    item list adapter class for managing inventory items
     */
    // full list of items, when using the filter, this list is not changed and is always invisible to the user (backstage list)
    private List<Item> fullList = new ArrayList<Item>();
    // when using the filter, this list is the one being filtered, and is the one that the user actually sees
    private List<Item> searchList = new ArrayList<Item>();
    private ItemDbHelper itemDbHelper;
    private SQLiteDatabase sqLiteDatabase;

    // class builder
    public ItemListDataAdapter(Context context, int resource) {
        super(context, resource);
    }

    // container class for dynamic labels (each text view will be changed according to the data retrieved from the database)
    public static class LayoutHandler {
        TextView ITEM_NAME;
        TextView ITEM_QUANTITY;
        TextView ITEM_PRICE;
    }

    @Override
    public void add(Object object) {
        /*
        add item to the lists
        since it is an overridden method which receives an Object, a casting to Item is required when adding to the list
         */
        super.add(object);
        fullList.add((Item) object);
        searchList.add((Item) object);
    }

    @Override
    public int getCount() {
        return searchList.size();
    }

    public int getCountFullList(){return fullList.size();}

    @Override
    public Object getItem(int position) {
        return searchList.get(position);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        /*
        this method generates the visualization of the listview, buttons, etc.....
        position - the row number in the list view
        convertView - the row data
        parent - the view parent
         */
        // create a new View which represent a row
        View row = convertView;
        final LayoutHandler layoutHandler;
        final LayoutInflater layoutInflater;
        // get item by position
        final Item item = (Item)this.getItem(position);
        layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // if the row is not initialized yet, initialize it
        if (row == null) {
            // connect the row to the item row layout
            row = layoutInflater.inflate(R.layout.row_item_layout_view_update, parent, false);
            layoutHandler = new LayoutHandler();
            // connect the container class objects to the item row layout
            layoutHandler.ITEM_NAME = (TextView) row.findViewById(R.id.tv_item_name);
            layoutHandler.ITEM_QUANTITY = (TextView) row.findViewById(R.id.tv_item_quantity);
            layoutHandler.ITEM_PRICE = (TextView) row.findViewById(R.id.tv_item_price);
            // set tag for the row
            row.setTag(layoutHandler);
        }

        else {
            // get the tag of the tow
            layoutHandler = (LayoutHandler) row.getTag();
        }

        // set on long click event listener (in case of long click a dialog pop up will open enabling editing of the item)
        row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(getContext(), layoutHandler.CLIENT_NAME.getText().toString(), Toast.LENGTH_LONG).show();

                final LayoutInflater inflater = layoutInflater;
                final AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());// get context of parent activity
                final View view = inflater.inflate(R.layout.add_new_item_layout, null);
                builder.setView(view);
                builder.setTitle("עדכון פרטים");

                final EditText itemName = ((EditText) view.findViewById(R.id.et_new_item_name));
                final EditText itemQuantity = ((EditText) view.findViewById(R.id.et_new_item_quantity));
                final EditText itemPrice = ((EditText) view.findViewById(R.id.et_new_item_price));

                itemName.setText(layoutHandler.ITEM_NAME.getText().toString());
                itemQuantity.setText(layoutHandler.ITEM_QUANTITY.getText().toString());
                itemPrice.setText(layoutHandler.ITEM_PRICE.getText().toString());

                builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        itemDbHelper = new ItemDbHelper(parent.getContext(), StaticHelper.getSalesTableName());
                        sqLiteDatabase = itemDbHelper.getWritableDatabase();
                        Item item = (Item)getItem(position);

                        if(!itemQuantity.getText().toString().equals(layoutHandler.ITEM_QUANTITY.getText().toString())) {
                            //Toast.makeText(getContext(), "Update To " + clientNumber.getText(), Toast.LENGTH_LONG).show();
                            itemDbHelper.updateItemQuantity(item.get_id(), layoutHandler.ITEM_NAME.getText().toString(), itemQuantity.getText().toString(), sqLiteDatabase);

                            // update list values
                            layoutHandler.ITEM_QUANTITY.setText(itemQuantity.getText().toString());
                            item.setQuantity(itemQuantity.getText().toString());
                        }

                        if(!itemPrice.getText().toString().equals(layoutHandler.ITEM_PRICE.getText().toString())) {
                            //Toast.makeText(getContext(), "Update To " + clientNumber.getText(), Toast.LENGTH_LONG).show();
                            itemDbHelper.updateItemPrice(item.get_id(), layoutHandler.ITEM_NAME.getText().toString(), itemPrice.getText().toString(), sqLiteDatabase);

                            // update list values
                            layoutHandler.ITEM_PRICE.setText(itemPrice.getText().toString());
                            item.setPrice(itemPrice.getText().toString());
                        }

                        if(!itemName.getText().toString().equals(layoutHandler.ITEM_NAME.getText().toString())) {
                            //Toast.makeText(getContext(), "Update To " + clientNumber.getText(), Toast.LENGTH_LONG).show();
                            itemDbHelper.updateItemName(item.get_id(), itemName.getText().toString(), layoutHandler.ITEM_NAME.getText().toString(), sqLiteDatabase);

                            // update list values
                            layoutHandler.ITEM_NAME.setText(itemName.getText().toString());
                            item.setName(itemName.getText().toString());
                        }
                        sqLiteDatabase.close();
                        itemDbHelper.close();
                    }
                });

                builder.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });

        // set the container class object values (get each value from the item object associated to the position (row number))
        layoutHandler.ITEM_NAME.setText(item.getName().toString());
        layoutHandler.ITEM_QUANTITY.setText(item.getQuantity().toString());
        layoutHandler.ITEM_PRICE.setText(item.getPrice().toString());

        return row;
    }

    public void filter(CharSequence charText) {
        /*
        filtering method
        receives a character sequence and filters out results from the viewable list(searchList)
         */
        charText = charText.toString().toLowerCase(Locale.getDefault());
        // clear the search list
        searchList.clear();
        if (charText.length() == 0) {
            searchList.addAll(fullList);
        } else {
            // iterate through the fullList of items and add only items which meets the search criteria
            for (Item item : fullList) {
                if (item.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    searchList.add(item);
                }
            }
        }
        // send notification that data set was updated and there is a need to refresh the view
        notifyDataSetChanged();
    }
}
