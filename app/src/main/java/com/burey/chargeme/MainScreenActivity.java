package com.burey.chargeme;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainScreenActivity extends AppCompatActivity {
    /*
    main screen activity
    displays the main screen
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connects the code to the layout file (activity_main_screen.xml)
        setContentView(R.layout.activity_main_screen);

        // set the screen orientation to horizontal
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // set it so the virtual keyboard DOES NOT appears automatically on entering to this activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // action selector, uses the view id name to redirect to the appropriate intent
    public void SelectOption(View view)
    {
        //Toast.makeText(getApplicationContext(),getResources().getResourceEntryName(view.getId()) , Toast.LENGTH_LONG).show();

        // start the charge activity
        if (getResources().getResourceEntryName(view.getId()).equals("btn_charge_menu")) {
            //Toast.makeText(getApplicationContext(),"Starting Activity..." , Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ChargeActivity.class);
            startActivity(intent);
        }
        else if(getResources().getResourceEntryName(view.getId()).equals("btn_inventory_menu")) {
            //Toast.makeText(getApplicationContext(),"Starting Activity..." , Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, InventoryActivity.class);
            startActivity(intent);
        }
        else if(getResources().getResourceEntryName(view.getId()).equals("ibtn_excel_files_browse")) {
            Intent intent = new Intent(this, BrowseExcelFiles.class);
            startActivity(intent);
        }
        else if(getResources().getResourceEntryName(view.getId()).equals("btn_statistics")) {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
        }
        else if(getResources().getResourceEntryName(view.getId()).equals("btn_archive_browse")) {
            Intent intent = new Intent(this, BrowseArchive.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChargeActivity.currentTable.delete(0, ChargeActivity.currentTable.length());
        ChargeActivity.currentTable.setLength(0);
    }
}
