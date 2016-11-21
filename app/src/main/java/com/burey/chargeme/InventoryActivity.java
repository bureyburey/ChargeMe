package com.burey.chargeme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.burey.chargeme.DataBase.ItemDbHelper;

public class InventoryActivity extends AppCompatActivity {
    /*
    activity to manage available inventory items
    from this activity the user is able to add new items, update existing items price/name/amount
     */
    private ListView listView;
    private SQLiteDatabase sqLiteDatabase;
    private ItemDbHelper itemDbHelper;
    private Cursor cursor;
    private ItemListDataAdapter itemListDataAdapter;
    private EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connects the code to the layout file (activity_inventory.xml)
        setContentView(R.layout.activity_inventory);

        // set screen orientation to horizontal
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // prevent virtual keyboard from showing automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // connect the listView object to the list view from the layout file
        listView = (ListView)findViewById(R.id.lv_inventory);
        // initialize the data adapter (for each row in the list view) and connect each row to the layout row_item_layout_view_update.xml
        itemListDataAdapter = new ItemListDataAdapter(this, R.layout.row_item_layout_view_update);
        // set the listView adapter to itemListDataAdapter (connects the rows of the list view with the item adapter)
        listView.setAdapter(itemListDataAdapter);
        listView.setTextFilterEnabled(true);
        // initialize itemDbHelper (database helper class for items)
        itemDbHelper = new ItemDbHelper(getApplicationContext(), StaticHelper.getSalesTableName());
        // initialize the sqLite database and get a read only database
        sqLiteDatabase = itemDbHelper.getReadableDatabase();
        // initialize a cursor for reading from the database
        cursor = itemDbHelper.getItemInformation(sqLiteDatabase);
        // connect the inputSearch text box to the layout file text box
        inputSearch = (EditText) findViewById(R.id.et_search_item);
        // add a watcher for changes in the text box
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // filter results when the text box input is changed
                itemListDataAdapter.filter(s);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // if the cursor is initialized and the database is NOT empty, call to a function which fills the listView
        if(cursor != null)
            FillList();
    }

    public void FillList() {
        /*
        function to fill the list view with items fetched from the database
        database Item table structure:
        Item(int _id, String name, String quantity, String price)
         */
        // check if cursor can fetch the first record
        if (cursor.moveToFirst()) {
            do {

                int id;
                String itemName;
                String itemQuantity;
                String itemPrice;
                // retrieve item id (key) from column 0
                id = cursor.getInt(0);
                // retrieve item name from column 1
                itemName = cursor.getString(1);
                // retrieve item quantity from column 2
                itemQuantity = cursor.getString(2);
                // retrieve item price from column 3
                itemPrice = cursor.getString(3);
                // create new item from the retrieved data
                Item item = new Item(id, itemName,itemQuantity,itemPrice);
                // add the item to the list view using the adapter itemListDataAdapter
                itemListDataAdapter.add(item);
            } while (cursor.moveToNext());
            // iterate while there is data to fetch and retrieve the records from the database
        }
    }

    public void AddItem (View view) {
        /*
        function to add new item to the inventory
         */
        // create a dialog builder for a pop up dialog window that will be filled with the new item information
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // create a new layout inflater
        LayoutInflater layoutInflater = this.getLayoutInflater();
        // initialize new dialogView with the layout add_new_item_layout.xml
        final View dialogView = layoutInflater.inflate(R.layout.add_new_item_layout, null);
        // set the builder's view to the dialogView
        builder.setView(dialogView);
        // set the title of the dialog pop up window
        builder.setTitle("הוספת פריט");

        // connect the text boxes objects to the text boxes in the layout (add_new_item_layout.xml)
        final EditText itemName = ((EditText) dialogView.findViewById(R.id.et_new_item_name));
        final EditText itemQuantity = ((EditText) dialogView.findViewById(R.id.et_new_item_quantity));
        final EditText itemPrice = ((EditText) dialogView.findViewById(R.id.et_new_item_price));

        // set confirmation button
        builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                on confirmation click, check that all fields were filled, if a field is missing, show a toast message and abort the action
                 */
                if (itemName.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "שם פריט חסר!!! פעולה בוטלה!!!", Toast.LENGTH_LONG).show();
                }
                else if (itemQuantity.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "כמות פריט חסרה!!! פעולה בוטלה!!!", Toast.LENGTH_LONG).show();
                }
                else if (itemPrice.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "מחיר פריט חסר!!! פעולה בוטלה!!!", Toast.LENGTH_LONG).show();
                }
                else {
                    //Toast.makeText(getApplicationContext(),itemName.getText().toString() , Toast.LENGTH_LONG).show();
                    // create new item that will be added to the inventory and to the database
                    Item newItem = new Item(itemListDataAdapter.getCountFullList() + 1, itemName.getText().toString(), itemQuantity.getText().toString(), itemPrice.getText().toString());
                    // add the item to the listView
                    itemListDataAdapter.add(newItem);
                    // open a writable database for adding the new item
                    sqLiteDatabase = itemDbHelper.getWritableDatabase();
                    // attempt to create a new table if not already exists
                    itemDbHelper.createItemTable(sqLiteDatabase, StaticHelper.getSalesTableName());
                    // add the item to the database
                    itemDbHelper.addItem(itemName.getText().toString(), itemQuantity.getText().toString(), itemPrice.getText().toString(), sqLiteDatabase);
                    // close the item database helper class
                    itemDbHelper.close();
                }
            }
        });
        // set abort button, show a toast notifying about abort action when clicked
        builder.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "מבטל הוספה...", Toast.LENGTH_LONG).show();
            }
        });
        // create the dialog using the builder that was initialized above
        AlertDialog dialog = builder.create();
        // force keyboard to start as hidden in this alert dialog
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // show the dialog
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        /*
        when existing the activity, close cursor and the sql database if they are still open
         */
        super.onDestroy();
        if(cursor != null)
            if(!cursor.isClosed())
                cursor.close();
        if(sqLiteDatabase != null)
            if(sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
