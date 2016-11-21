package com.burey.chargeme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class BrowseExcelFiles extends AppCompatActivity {
    /*
    activity to browse existing excel sheets of previous charges
    */
    private ArrayList<String> FilesInFolder;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_excel_files);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FilesInFolder = GetFiles(getApplicationContext().getExternalFilesDir(null).toString());
        lv = (ListView) findViewById(R.id.lv_browse_excel);
        if(FilesInFolder == null) {
            Toast.makeText(getApplicationContext(), "אין קבצים להצגה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, FilesInFolder));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(BrowseExcelFiles.this);

                builder.setTitle("בחר פעולה");

                builder.setPositiveButton("שלח", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(getApplicationContext().getExternalFilesDir(null), FilesInFolder.get(position));
                        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        // setType defines the type of Intent, in this case, it will determine which applications are to be selected from
                        emailIntent.setType("message/rfc822");
                        //emailIntent.setType("text/message");
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "רשימת חיובים - מיד בר");
                        emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivity(Intent.createChooser(emailIntent, "Send Mail...."));
                    }
                });

                builder.setNeutralButton("פתח", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // create file using the FilesInFolder array list and the item position
                        File file = new File(getApplicationContext().getExternalFilesDir(null), FilesInFolder.get(position));
                        //Toast.makeText(MailInventory.this, FilesInFolder.get(position), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(BrowseExcelFiles.this, "No Application Available to View Excel", Toast.LENGTH_SHORT).show();
                        }
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


    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return null;
        else {
            for (int i = 0; i < files.length; i++)
                MyFiles.add(files[i].getName());
        }
        return MyFiles;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse_excel_files, menu);
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
