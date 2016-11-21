package com.burey.chargeme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.burey.chargeme.DataBase.ClientDbHelper;

import org.apache.poi.common.usermodel.Fill;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChargeActivity extends AppCompatActivity {
    /*
    main charge activity
    from this activity the user is able to add new clients, create new charging tables, add new charges for a client(directs to AddChargeActivity)
    */
    private static int REQUEST_CANCELLED = 0;
    private static int REQUEST_TOTAL = 1;
    private ListView listView;
    private SQLiteDatabase sqLiteDatabase;
    private ClientDbHelper clientDbHelper;
    private Cursor cursor;
    private ClientListDataAdapter clientListDataAdapter;
    private EditText inputSearch;
    public static StringBuilder currentTable = new StringBuilder();
    private TextView tableNameLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_charge);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setActiveTableName();

        tableNameLabel = (TextView)findViewById(R.id.tv_charge_table_label_name);
        tableNameLabel.setText(currentTable);
        listView = (ListView)findViewById(R.id.lv_charges);
        clientListDataAdapter = new ClientListDataAdapter(this, R.layout.row_client_layout_view_update, this.currentTable.toString());
        listView.setAdapter(clientListDataAdapter);
        listView.setTextFilterEnabled(true);
        clientDbHelper = new ClientDbHelper(getApplicationContext(), currentTable.toString());
        sqLiteDatabase = clientDbHelper.getReadableDatabase();

        clientListDataAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                int selected = clientListDataAdapter.getSelectedClient();
                if(selected != -1)
                    AddCharge(selected);
            }
        });

        inputSearch = (EditText) findViewById(R.id.et_search_client);
        inputSearch.addTextChangedListener(new TextWatcher() { // text watcher: will be invoked when an item is searched
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clientListDataAdapter.filter(s);
                clientListDataAdapter.notifyDataSetChanged();
                //clientListDataAdapter.ToastItems();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        FillList();
    }

    public void FillList() {
        cursor = clientDbHelper.getClientInformation(sqLiteDatabase);
        if(cursor == null)
            return;
        clientListDataAdapter.clear();
        if (cursor.moveToFirst()) {
            do {
                int id;
                String clientName;
                String clientTotal;
                String clientNumber;
                boolean is_toshav;

                id = cursor.getInt(0);
                clientName = cursor.getString(1);
                clientTotal = cursor.getString(2);
                clientNumber = cursor.getString(3);
                is_toshav = (cursor.getInt(4) != 0);

                if(clientNumber.equals("-1"))
                    clientNumber = "";

                Client client = new Client(id, clientName,clientTotal,clientNumber,is_toshav);
                clientListDataAdapter.add(client);
            } while (cursor.moveToNext());
        }
        clientListDataAdapter.notifyDataSetChanged();
    }

    public void SetChargeTable(View view) {
        /*
            allows selection of the current working charges table
         */
        ArrayList<String> tableChargeAL; // array with the names of the tables extracted from the database
        tableChargeAL = clientDbHelper.getChargeTables(sqLiteDatabase);
        final CharSequence[] tableNames = tableChargeAL.toArray(new CharSequence[tableChargeAL.size()]);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("דפי חיוב");

        builder.setSingleChoiceItems(tableNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(), tableNames[which].toString(), Toast.LENGTH_SHORT).show();

                // should load the selected table if different from the current

                final int tableIndex = which;

                AlertDialog.Builder setChargeTableClickMenuBuilder = new AlertDialog.Builder(builder.getContext());
                setChargeTableClickMenuBuilder.setTitle("בחר פעולה:");
                setChargeTableClickMenuBuilder.setSingleChoiceItems(new CharSequence[]{"פתח", "העבר לארכיון", "בטל"}, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            // open the table
                            if (currentTable.toString().equals(tableNames[tableIndex]))
                                Toast.makeText(getApplicationContext(), "הינך משתמש ברשימה זו כרגע", Toast.LENGTH_SHORT).show();
                            else
                                LoadTable(tableNames[tableIndex].toString(), false);
                        }
                        else if(which == 1){
                            // move to archive
                            if(tableNames.length == 1)
                                Toast.makeText(getApplicationContext(), "לא ניתן להעביר לארכיון כיוון שחייבת להיות לפחות רשימה פעילה אחת!!!", Toast.LENGTH_SHORT).show();

                            else{
                                Toast.makeText(getApplicationContext(), "מעביר את רשימה " + tableNames[tableIndex] + " לארכיון", Toast.LENGTH_SHORT).show();
                                MoveTableToArchive(tableIndex, tableNames);
                            }
                        }
                        else{
                            // cancel
                        }
                        dialog.dismiss();
                    }
                });

                AlertDialog ChargeTableClickMenuDialog = setChargeTableClickMenuBuilder.create();
                ChargeTableClickMenuDialog.show();

                dialog.dismiss();
            }
        });

        // button for creating new charges table
        builder.setNeutralButton("חדש", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                final EditText newTableName;
                AlertDialog.Builder builderNewTable = new AlertDialog.Builder(ChargeActivity.this);
                LayoutInflater layoutInflaterNewTable = getLayoutInflater();

                final View dialogViewNewTable = layoutInflaterNewTable.inflate(R.layout.add_new_table_charge_layout, null);

                newTableName = (EditText) dialogViewNewTable.findViewById(R.id.et_add_new_table_charge);
                builderNewTable.setTitle("הוספת רשימת חיוב חדשה");
                builderNewTable.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!checkValidTableName(newTableName.getText().toString()))
                            Toast.makeText(getApplicationContext(), "שם טבלה אינו חוקי!!!", Toast.LENGTH_SHORT).show();
                        else if(clientDbHelper.tableNameExists(sqLiteDatabase, newTableName.getText().toString()))
                            Toast.makeText(getApplicationContext(), "רשימת חיוב " + newTableName.getText().toString() + " כבר קיימת\n(ברשימה פעילה או בארכיון)", Toast.LENGTH_LONG).show();
                        else if (newTableName.getText().toString().length() > 0) {
                            LoadTable(newTableName.getText().toString(), true);
                            Toast.makeText(getApplicationContext(), "רשימת חיוב " + newTableName.getText().toString() + " נוספה", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "לא הוקלד דבר, מבטל...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builderNewTable.setView(dialogViewNewTable);
                AlertDialog dialogNewTable = builderNewTable.create();
                dialogNewTable.show();
            }
        });

        AlertDialog dialog = builder.create();
        // force keyboard to start as hidden in this alert dialog
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.show();
    }

    private void MoveTableToArchive(int tableIndex, CharSequence[] tableNames) {
        String tableName = tableNames[tableIndex].toString();
        clientDbHelper.moveChargeTableToArchive(sqLiteDatabase, tableName);
        // if the table that was moved to the archive was the active table, change the active table
        if(tableName.equals(currentTable.toString())){
            // force loading of a different table
            if(tableIndex == 0) // there should always be at least one table which is not archived (besides first run of the application)
                LoadTable(tableNames[tableIndex+1].toString(), false);
            else
                LoadTable(tableNames[tableIndex-1].toString(), false);
        }
    }

    public void SetChargeTable2(View view) {
        ListView tableChargeLV;
        ArrayList<String> tableChargeAL;
        Button AddTableCharge;
        TextView TableName;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = this.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.set_table_charge_layout, null);

        builder.setTitle("דפי חיוב");

        tableChargeLV = (ListView)dialogView.findViewById(R.id.lv_table_charge_names);
        tableChargeAL = clientDbHelper.getChargeTables(sqLiteDatabase);
        AddTableCharge = (Button)dialogView.findViewById(R.id.btn_new_table_charge);
        AddTableCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Log.e("checkpoint", tableChargeLV.toString());


        tableChargeLV.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.table_charge_row_style, tableChargeAL));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // force keyboard to start as hidden in this alert dialog
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.show();
    }

    public void AddClient (View view) {
        /*
        add new client dialog box
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = this.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.add_new_client_layout, null);
        builder.setView(dialogView);

        builder.setTitle("הוספת לקוח");

        builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText clientName = ((EditText) dialogView.findViewById(R.id.et_new_client_name));
                EditText clientNumber = ((EditText) dialogView.findViewById(R.id.et_new_client_number));
                CheckBox isToshav = ((CheckBox) dialogView.findViewById(R.id.cb_new_is_toshav));

                if (clientName.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "שם חסר!!! הוספת לקוח בוטלה!!!", Toast.LENGTH_SHORT).show();
                } else if (clientListDataAdapter.clientExists(clientName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "שם לקוח כבר קיים!!!", Toast.LENGTH_SHORT).show();
                } else {
                    String clientNum = clientNumber.getText().toString();

                    //Toast.makeText(getApplicationContext(),clientName.getText().toString() + " " + clientNum, Toast.LENGTH_LONG).show();

                    Client newClient = new Client(clientListDataAdapter.getCountFullList() + 1, clientName.getText().toString(), "0", clientNum, isToshav.isChecked());
                    clientListDataAdapter.add(newClient);
                    sqLiteDatabase = clientDbHelper.getWritableDatabase();

                    if (clientNum.length() == 0)
                        clientNum = "-1";

                    clientDbHelper.createClientTable(sqLiteDatabase, currentTable.toString());
                    clientDbHelper.addClient(clientName.getText().toString(), "0", clientNum, isToshav.isChecked(), sqLiteDatabase, currentTable.toString());

                    FillList();

                    //clientListDataAdapter.AddCharge(clientListDataAdapter.getCountFullList() - 1);
                }
            }
        });
        builder.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "מבטל הוספה...", Toast.LENGTH_LONG).show();
            }
        });

        // add client from a list dialog
        builder.setNeutralButton("מרשימה", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                AlertDialog.Builder builderAddClients = new AlertDialog.Builder(ChargeActivity.this);
                builderAddClients.setTitle("הוספת לקוחות");

                Cursor AllClientsCursor;
                // fetch all existing clients from database
                AllClientsCursor = clientDbHelper.getAllClients(sqLiteDatabase);
                final List<Client> AllClientsAL = new ArrayList<>();
                final List<String> AllClientsNamesAL = new ArrayList<>();

                if (AllClientsCursor == null) {
                    Toast.makeText(getApplicationContext(), "רשימה ריקה!!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (AllClientsCursor.moveToFirst()) {
                        do {
                            int id;
                            String clientName;
                            String clientNumber;
                            boolean is_toshav;

                            id = AllClientsCursor.getInt(0);
                            clientName = AllClientsCursor.getString(1);
                            clientNumber = AllClientsCursor.getString(2);
                            is_toshav = (AllClientsCursor.getInt(3) != 0);

                            if (clientNumber.equals("-1"))
                                clientNumber = "";

                            Client client = new Client(id, clientName, "0", clientNumber, is_toshav);
                            AllClientsAL.add(client);
                            AllClientsNamesAL.add(clientName);
                        } while (AllClientsCursor.moveToNext());
                        AllClientsCursor.close();

                        CharSequence[] AllClientsCS = AllClientsNamesAL.toArray(new CharSequence[AllClientsNamesAL.size()]);
                        final boolean[] selectedItems = new boolean[AllClientsNamesAL.size()];

                        builderAddClients.setMultiChoiceItems(AllClientsCS, selectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            }
                        });
                        builderAddClients.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sqLiteDatabase = clientDbHelper.getWritableDatabase();
                                clientDbHelper.createClientTable(sqLiteDatabase, currentTable.toString());
                                for (int i = 0; i < AllClientsNamesAL.size(); i++) {
                                    if (selectedItems[i]) {
                                        // implement add to database method using the selected items array and the clients array
                                        // should be implemented with Transaction for efficiency
                                        if (!clientListDataAdapter.clientExists(AllClientsAL.get(i).getName())) {
                                            clientListDataAdapter.add(AllClientsAL.get(i));
                                            clientDbHelper.addClient(AllClientsAL.get(i).getName(), "0", AllClientsAL.get(i).getClient_number(), AllClientsAL.get(i).is_toshav(), sqLiteDatabase, currentTable.toString());
                                        }
                                        else {
                                            Toast.makeText(getApplicationContext(), "הלקוח " + AllClientsAL.get(i).getName() + " כבר קיים ברשימה זו!!!", Toast.LENGTH_SHORT).show();
                                        }
                                        //Toast.makeText(getApplicationContext(), AllClientsAL.get(i).getName() , Toast.LENGTH_SHORT).show();
                                    }
                                }
                                FillList();
                            }
                        });
                    }
                    AlertDialog dialogAddClients = builderAddClients.create();
                    dialogAddClients.show();
                }
            }
        });

            AlertDialog dialog = builder.create();
            // force keyboard to start as hidden in this alert dialog
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.show();
        }

    public void AddCharge(final int selected) {
        //Toast.makeText(getApplicationContext(),((Client)clientListDataAdapter.getItem(selected)).getName() + " index " + Integer.toString(selected), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), AddChargeActivity.class);
        intent.putExtra("NAME", ((Client) (clientListDataAdapter.getItem(selected))).getName());
        intent.putExtra("INDEX", selected);
        intent.putExtra("TABLE_NAME", currentTable.toString());
        intent.putExtra("CLIENT_ID", ((Client) (clientListDataAdapter.getItem(selected))).get_id());
        startActivityForResult(intent, REQUEST_TOTAL);
    }

    public boolean checkValidTableName(String tableName) {
        final String[] invalidChars = {"+", "-", "?", "!", "*", "@", "%", "^", "&", "#", "=", "/", "\\", ":", " ", "'", "."};
        if(tableName.length() == 0)
            return false;
        for(int i=0;i<invalidChars.length;i++){
            if(tableName.contains(invalidChars[i]))
                return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode == REQUEST_TOTAL) {
            if(data == null) {
                Toast.makeText(getApplicationContext(),"בקשה בוטלה!!!", Toast.LENGTH_SHORT).show();
            }
            else {
                // fetch the message String
                String total = data.getStringExtra("TOTAL");
                int index = data.getIntExtra("INDEX", 0);
                clientListDataAdapter.updateClientTotal(index, total, currentTable.toString());

                // Set the message string in textView
                //Toast.makeText(getApplicationContext(), total + " at index " + index, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        setActiveTableName();
        clientDbHelper.setActiveTableName(currentTable.toString());

        if(cursor != null)
            if(!cursor.isClosed())
                cursor.close();
        if(clientDbHelper != null)
            clientDbHelper.close();
        if(sqLiteDatabase != null)
            if(sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
    }

    public void LoadTable(String tableName, boolean newTable) {
        currentTable.delete(0, currentTable.length());
        currentTable.insert(0, tableName);
        clientDbHelper.setActiveTableName(tableName);
        clientListDataAdapter.setCurrentTable(currentTable.toString());
        if(newTable) {
            clientListDataAdapter.clear();
            clientDbHelper.createClientTable(sqLiteDatabase, currentTable.toString());
        }
        else {
            FillList();
        }
        tableNameLabel.setText(currentTable);
        clientListDataAdapter.notifyDataSetChanged();
    }

    public void ExportToExcel(View view) {
        clientListDataAdapter.export(getApplicationContext(), currentTable.toString());
        SendToMail(currentTable.toString() + ".xls");
    }

    public void SendToMail(String fileName) {
        File file = new File(getApplicationContext().getExternalFilesDir(null), fileName);
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // setType defines the type of Intent, in this case, it will determine which applications are to be selected from
        emailIntent.setType("message/rfc822");
        //emailIntent.setType("text/message");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "רשימת חיובים - מיד בר");
        emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(emailIntent, "Send Mail...."));
    }

    public void setActiveTableName() {
        if(currentTable.length() > 0)
            return;
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // months start at 0
        //int day = cal.get(Calendar.DAY_OF_MONTH);

        //currentTable.setLength(0);
        switch(month) {
            case 1:
                this.currentTable.insert(0, "January");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 2:
                this.currentTable.insert(0, "February");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 3:
                this.currentTable.insert(0, "March");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 4:
                this.currentTable.insert(0, "April");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 5:
                this.currentTable.insert(0, "May");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 6:
                this.currentTable.insert(0, "June");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 7:
                this.currentTable.insert(0, "July");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 8:
                this.currentTable.insert(0, "August");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 9:
                this.currentTable.insert(0, "September");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 10:
                this.currentTable.insert(0, "October");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 11:
                this.currentTable.insert(0, "November");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            case 12:
                this.currentTable.insert(0, "December");
                this.currentTable.append("_");
                this.currentTable.append(year);
                break;
            default:
                break;
        }
//        Toast.makeText(getApplicationContext(),this.currentTable.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_charge, menu);
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
