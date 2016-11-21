package com.burey.chargeme;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.burey.chargeme.DataBase.ItemDbHelper;

public class AddChargeActivity extends AppCompatActivity {
    /*
    activity to add an amount of items to a client total charge
     */
    private static int REQUEST_CANCELLED = 0;
    private static int REQUEST_TOTAL = 1;
    private TextView total_charge;
    private ListView listView;
    private TextView clientNameDisplay;
    private SQLiteDatabase sqLiteDatabase;
    private ItemDbHelper itemDbHelper;
    private Cursor cursor;
    private ItemListDataAdapterForAddChargeActivity itemListDataAdapter;
    private EditText inputCharge;
    private String clientName;
    private int clientIndex;
    private Button finish;
    private String tableName;
    private int clientID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_charge);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        clientName = intent.getStringExtra("NAME");
        clientIndex = intent.getIntExtra("INDEX", 0);
        tableName = intent.getStringExtra("TABLE_NAME");
        clientID = intent.getIntExtra("CLIENT_ID", 0);

//        Toast.makeText(getApplicationContext(),tableName, Toast.LENGTH_LONG).show();

        clientNameDisplay = (TextView)findViewById(R.id.tv_client_name_add_charge);
        clientNameDisplay.setText("כרטיס חיוב עבור\n" + clientName);
        listView = (ListView)findViewById(R.id.lv_item_add_charge);
        itemListDataAdapter = new ItemListDataAdapterForAddChargeActivity(getApplicationContext(), R.layout.row_item_add_charge_layout);
        listView.setAdapter(itemListDataAdapter);
        listView.setTextFilterEnabled(true);
        itemDbHelper = new ItemDbHelper(getApplicationContext(), tableName);
        sqLiteDatabase = itemDbHelper.getReadableDatabase();
        cursor = itemDbHelper.getItemInformation(sqLiteDatabase);

        total_charge = (TextView)findViewById(R.id.tv_item_to_charge_add_charge);
        total_charge.setText("0");

        // finish charge button action
        finish = (Button)findViewById(R.id.btn_item_finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent returnTotal = new Intent();

                String total = total_charge.getText().toString();
                if(inputCharge.getText().toString().length() != 0)
                {
                    total = inputCharge.getText().toString();
                }
                else if (total.equals("לחיוב"))
                {
                    total = "0";
                }
                // put the message to return as result in Intent
                returnTotal.putExtra("TOTAL", total);
                returnTotal.putExtra("INDEX", clientIndex);
                // Set The Result in Intent
                setResult(REQUEST_TOTAL,returnTotal);

                // update items on sales table
                itemListDataAdapter.updateSales(tableName, clientID, clientName);

                // finish The activity
                finish();
            }
        });


        // observer for whenever a PLUS or MINUS button is clicked in a row, notifying there is a need
        // to update the total sum of items ordered
       itemListDataAdapter.registerDataSetObserver(new DataSetObserver() {
           @Override
           public void onChanged() {
               super.onChanged();
               if(itemListDataAdapter.getChargeTotal() != 0)
                   total_charge.setText(Integer.toString(itemListDataAdapter.getChargeTotal()));
               else
                   total_charge.setText("לחיוב");
           }
       });

        inputCharge = (EditText) findViewById(R.id.et_item_custom_add_charge);
        inputCharge.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //itemListDataAdapter.filter(s);
                //total_charge.setText(Integer.toString(itemListDataAdapter.getChargeTotal()));
                if(s.length() == 0)
                    total_charge.setText("0");
                else
                    total_charge.setText(inputCharge.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if(cursor != null)
            FillList();
    }

    public void FillList()
    {
        if (cursor.moveToFirst()) {
            do {
                int id;
                String itemName;
                String itemQuantity;
                String itemPrice;

                id = cursor.getInt(0);
                itemName = cursor.getString(1);
                itemQuantity = cursor.getString(2);
                itemPrice = cursor.getString(3);

                Item item = new Item(id, itemName,itemQuantity,itemPrice);
                itemListDataAdapter.add(item);
            } while (cursor.moveToNext());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cursor != null)
            if(!cursor.isClosed())
                cursor.close();
        if(itemDbHelper != null)
            itemDbHelper.close();
        if(sqLiteDatabase != null)
            if(sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_charge, menu);
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
