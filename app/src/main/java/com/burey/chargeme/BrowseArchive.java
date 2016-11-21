package com.burey.chargeme;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.burey.chargeme.DataBase.ClientDbHelper;

import java.io.File;
import java.util.ArrayList;

public class BrowseArchive extends AppCompatActivity {

    private ArrayList<String> archivedTables;
    private ListView lv;
    private ClientDbHelper clientDbHelper;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_archive);

        // set the screen orientation to horizontal
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        clientDbHelper = new ClientDbHelper(getApplicationContext(), StaticHelper.getSalesTableName());
        sqLiteDatabase = clientDbHelper.getReadableDatabase();

        archivedTables = clientDbHelper.getArchivedChargeTables(sqLiteDatabase);
        lv = (ListView) findViewById(R.id.lv_browse_archive);
        if(archivedTables == null) {
            Toast.makeText(getApplicationContext(), "אין קבצים להצגה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, archivedTables));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(BrowseArchive.this);

                builder.setTitle("בחר פעולה");

                builder.setPositiveButton("שחזר", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sqLiteDatabase = clientDbHelper.getWritableDatabase();
                        clientDbHelper.recoverChargeTableFromArchive(sqLiteDatabase, archivedTables.get(position));
                        archivedTables.remove(position);
                        lv.invalidateViews();
                    }
                });
                builder.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                dialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(clientDbHelper != null)
            clientDbHelper.close();
        if(sqLiteDatabase != null)
            if(sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
    }
}
