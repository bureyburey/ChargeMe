package com.burey.chargeme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.burey.chargeme.DataBase.ClientDbHelper;
import com.burey.chargeme.DataBase.ItemDbHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;


/**
 * Created by burey on 30/08/2015.
 */
public class ClientListDataAdapter extends ArrayAdapter {

    private static int REQUEST_TOTAL = 1;
    private List<Client> fullList = new ArrayList<>();
    private List<Client> searchList = new ArrayList<>();
    private ClientDbHelper clientDbHelper;
    private ItemDbHelper itemDbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private String currentTable;

    public ClientListDataAdapter(Context context, int resource, String currentTable) {
        super(context, resource);
        this.currentTable = currentTable;
    }

    public static class LayoutHandler {
        CheckBox IS_TOSHAV;
        TextView CLIENT_NUMBER;
        TextView CLIENT_NAME;
        TextView CLIENT_TOTAL;
        Button PLUS_MINUS_BTN;
    }

    @Override
    public void add(Object object) {
        super.add(object);
        fullList.add((Client) object);
        searchList.add((Client) object);
    }

    @Override
    public int getCount() {
        return searchList.size();
    }

    public int getCountFullList(){ return this.fullList.size(); }

    public boolean clientExists(String clientName) {
        for(Client client : fullList) {
            if(client.getName().equals(clientName))
                return true;
        }
        return false;
    }

    public void clear() {
        fullList.clear();
        searchList.clear();
    }

    @Override
    public Object getItem(int position) {
        return searchList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View row = convertView;
        final LayoutHandler layoutHandler;
        final LayoutInflater layoutInflater;
        final Client client = (Client)this.getItem(position);
        layoutInflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // IMPORTANT!!!!!
        // keep Listeners outside of the below if else scopes!!!!
        // otherwise, filtering issues are awaiting!!!
        if (row == null) {
            row = layoutInflater.inflate(R.layout.row_client_layout_view_update, parent, false);
            layoutHandler = new LayoutHandler();
            layoutHandler.IS_TOSHAV = (CheckBox) row.findViewById(R.id.cb_is_toshav);
            layoutHandler.CLIENT_NUMBER = (TextView) row.findViewById(R.id.tv_client_number);
            layoutHandler.CLIENT_NAME = (TextView) row.findViewById(R.id.tv_client_name);
            layoutHandler.CLIENT_TOTAL = (TextView) row.findViewById(R.id.tv_client_total);
            layoutHandler.PLUS_MINUS_BTN = (Button) row.findViewById(R.id.btn_add_total);

            row.setTag(layoutHandler);
        }
        else {
            layoutHandler = (LayoutHandler) row.getTag();
        }

        layoutHandler.PLUS_MINUS_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // force a data change notifying which client's button was clicked
                client.setIsSelected(true);
                //Toast.makeText(getContext().getApplicationContext(), "onClick " + client.getName() , Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });

        // listener for long click on a client
        row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Toast.makeText(getContext(), layoutHandler.CLIENT_NAME.getText().toString(), Toast.LENGTH_LONG).show();

                final LayoutInflater inflater = layoutInflater;
                final AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());// get context of parent activity
                View view = inflater.inflate(R.layout.add_new_client_layout, null);
                builder.setView(view);
                builder.setTitle("עדכון פרטים");
                final EditText clientName = ((EditText) view.findViewById(R.id.et_new_client_name));
                final EditText clientNumber = ((EditText) view.findViewById(R.id.et_new_client_number));
                final CheckBox isToshav = ((CheckBox) view.findViewById(R.id.cb_new_is_toshav));
                final ImageButton delClient = ((ImageButton) view.findViewById(R.id.ibtn_delete_client));

                delClient.setVisibility(View.VISIBLE);

                clientName.setText(layoutHandler.CLIENT_NAME.getText().toString());
                clientNumber.setText(layoutHandler.CLIENT_NUMBER.getText().toString());
                isToshav.setChecked(layoutHandler.IS_TOSHAV.isChecked());

                builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        clientDbHelper = new ClientDbHelper(parent.getContext(), currentTable);
                        sqLiteDatabase = clientDbHelper.getWritableDatabase();

                        Client client = (Client) getItem(position);

                        if (!clientNumber.getText().toString().equals(layoutHandler.CLIENT_NUMBER.getText().toString())) {
                            //Toast.makeText(getContext(), "Update To " + clientNumber.getText(), Toast.LENGTH_LONG).show();
                            clientDbHelper.updateClientNumber(client.get_id(), layoutHandler.CLIENT_NAME.getText().toString(), clientNumber.getText().toString(), sqLiteDatabase);

                            // update list values
                            layoutHandler.CLIENT_NUMBER.setText(clientNumber.getText().toString());
                            client.setClient_number(clientNumber.getText().toString());
                        }

                        if ((!isToshav.isChecked() && layoutHandler.IS_TOSHAV.isChecked()) || (isToshav.isChecked() && !layoutHandler.IS_TOSHAV.isChecked())) {
                            //Toast.makeText(getContext(), "Update To is toshav" , Toast.LENGTH_LONG).show();
                            clientDbHelper.updateClientIsToshav(client.get_id(), layoutHandler.CLIENT_NAME.getText().toString(), isToshav.isChecked(), sqLiteDatabase);
                            layoutHandler.IS_TOSHAV.setChecked(isToshav.isChecked());
                            client.setIs_toshav(isToshav.isChecked());
                        }

                        if (!clientName.getText().toString().equals(layoutHandler.CLIENT_NAME.getText().toString())) {
                            //Toast.makeText(getContext(), "Update To " + clientName.getText(), Toast.LENGTH_LONG).show();
                            clientDbHelper.updateClientName(client.get_id(), clientName.getText().toString(), layoutHandler.CLIENT_NAME.getText().toString(), sqLiteDatabase);
                            layoutHandler.CLIENT_NAME.setText(clientName.getText().toString());
                            client.setName(clientName.getText().toString());
                        }
                        clientDbHelper.close();
                        sqLiteDatabase.close();
                    }
                });

                builder.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.setNeutralButton("רשימת חיובים", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        itemDbHelper = new ItemDbHelper(parent.getContext(), currentTable);
                        sqLiteDatabase = itemDbHelper.getReadableDatabase();
                        ArrayList<Sale> salesList = itemDbHelper.getSalesForClient(client.get_id(), sqLiteDatabase, currentTable);
                        CharSequence[] salesForCustomer = new CharSequence[salesList.size()];
                        for(int i=0; i<salesList.size();i++){
                            salesForCustomer[i] = salesList.get(i).getItemName() + " - " + salesList.get(i).getOrdered();
                        }
                        AlertDialog.Builder builderShowSales = new AlertDialog.Builder(parent.getContext());
                        builderShowSales.setTitle("חיובים עבור: " + client.getName());
                        builderShowSales.setItems(salesForCustomer, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builderShowSales.setPositiveButton("חזור", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        AlertDialog dialogShowSales = builderShowSales.create();
                        dialogShowSales.show();

                        itemDbHelper.close();
                        sqLiteDatabase.close();
                    }
                });
                final AlertDialog dialogClient = builder.create();
                dialogClient.show();
                delClient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // set confirmation password input dialog
                        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(parent.getContext());
                        confirmBuilder.setTitle("הכנס סיסמא לאישור מחיקת הלקוח:\n" + client.getName());
                        final EditText inputPassword = new EditText(parent.getContext());
                        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        inputPassword.setHint("סיסמא:");
                        inputPassword.setHintTextColor(Color.BLACK);
                        // attach the password input to the dialog builder
                        confirmBuilder.setView(inputPassword);
                        // Set up the buttons
                        confirmBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(inputPassword.getText().toString().equals("midbar")){
                                    clientDbHelper = new ClientDbHelper(parent.getContext(), currentTable);
                                    itemDbHelper = new ItemDbHelper(parent.getContext(), currentTable);

                                    sqLiteDatabase = clientDbHelper.getWritableDatabase();

                                    clientDbHelper.deleteClientFromTable(((Client)getItem(position)).get_id(), sqLiteDatabase, currentTable);
                                    itemDbHelper.deleteClientSales(((Client)getItem(position)).get_id(), sqLiteDatabase, currentTable);

                                    clientDbHelper.close();
                                    itemDbHelper.close();
                                    sqLiteDatabase.close();

                                    Toast.makeText(getContext(), "הלקוח " + client.getName() + " נמחק מרשימה " + currentTable, Toast.LENGTH_SHORT).show();
                                    fullList.remove(client);
                                    searchList.clear();
                                    searchList.addAll(fullList);
                                    notifyDataSetChanged();
                                }
                                else
                                    Toast.makeText(getContext(), "סיסמא שגויה!!!", Toast.LENGTH_SHORT).show();
                                dialogClient.dismiss();
                            }
                        });
                        confirmBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialogClient.cancel();
                            }
                        });
                        confirmBuilder.show();
                    }
                });
                return false;
            }
        });

        layoutHandler.CLIENT_NAME.setText(client.getName().toString());
        layoutHandler.CLIENT_TOTAL.setText(client.getTotal().toString());
        layoutHandler.CLIENT_NUMBER.setText(client.getClient_number().toString());
        layoutHandler.IS_TOSHAV.setChecked(client.is_toshav());

        return row;
    }

    public void updateClientTotal(int position, String AddToTotal, String tableName) {
        Client client = (Client)getItem(position);
        int newTotalInt = Integer.parseInt(client.getTotal()) + Integer.parseInt(AddToTotal);
        String newTotalString = Integer.toString(newTotalInt);
        client.setTotal(newTotalString);
        clientDbHelper = new ClientDbHelper(getContext(), currentTable);
        sqLiteDatabase = clientDbHelper.getWritableDatabase();
        clientDbHelper.updateClientTotal(client.get_id(), client.getName(), newTotalString, sqLiteDatabase, tableName);
        notifyDataSetChanged();
        clientDbHelper.close();
        sqLiteDatabase.close();
    }

    public int getSelectedClient() {
        int selected = -1;
        for(int i = 0; i < searchList.size(); i++) {
            if(((Client)getItem(i)).isSelected()) {
                selected = i;
                ((Client)getItem(i)).setIsSelected(false);
                break;
            }
        }
        return selected;
    }

    public void setCurrentTable(String currentTable){
        this.currentTable = currentTable;
    }

    public void filter(CharSequence charText) {
        charText = charText.toString().toLowerCase(Locale.getDefault());
        searchList.clear();
        if (charText.length() == 0) {
            searchList.addAll(fullList);
        } else {
            for (Client client : fullList) {
                if (client.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    searchList.add(client);
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean export(Context context, String fileName) {
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            return false;
        }

        boolean success = false;

        //New Workbook
        HSSFWorkbook wb = new HSSFWorkbook();

        Cell c = null;

        //Cell style for header row
        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cs.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

        //New Sheet
        HSSFSheet sheet = null;
        sheet = wb.createSheet("חברים");
        sheet.setRightToLeft(true);

        HSSFSheet sheetToshavim = null;
        sheetToshavim = wb.createSheet("תושבים");
        sheetToshavim.setRightToLeft(true);


        // Generate column headings
        Row row = sheet.createRow(0);

        c = row.createCell(0);
        c.setCellValue("שם");
        c.setCellStyle(cs);

        c = row.createCell(1);
        c.setCellValue("מספר קומונה");
        c.setCellStyle(cs);

        c = row.createCell(2);
        c.setCellValue("לחיוב");
        c.setCellStyle(cs);

        Row rowToshav = sheetToshavim.createRow(0);

        c = rowToshav.createCell(0);
        c.setCellValue("שם");
        c.setCellStyle(cs);

        c = rowToshav.createCell(1);
        c.setCellValue("מספר קומונה");
        c.setCellStyle(cs);

        c = rowToshav.createCell(2);
        c.setCellValue("לחיוב");
        c.setCellStyle(cs);

        int count = 2, count_toshav = 2;
        // iterate through the items and fill the excel file
        for (int i = 0; i < this.fullList.size(); i++) {

            Row newRow = null;
            // create new row (starting with 1 [0+1])
            if(fullList.get(i).is_toshav())
                newRow = sheetToshavim.createRow(count++);

            else
                newRow = sheet.createRow(count_toshav++);


            // create new cell at column 0 of the newly made row
            c = newRow.createCell(0);
            c.setCellValue(fullList.get(i).getName());
            c.setCellStyle(cs);

            // create new cell at the column 1 of the newly made row
            c = newRow.createCell(1);
            c.setCellValue(fullList.get(i).getClient_number());
            c.setCellStyle(cs);

            // create new cell at column 2 of the newly made row
            c = newRow.createCell(2);
            c.setCellValue(fullList.get(i).getTotal());
            c.setCellStyle(cs);

        }

        sheet.setColumnWidth(0, (15 * 550)); // name
        sheet.setColumnWidth(1, (15 * 500)); // number
        sheet.setColumnWidth(2, (15 * 500)); // total
        sheetToshavim.setColumnWidth(0, (15 * 550));
        sheetToshavim.setColumnWidth(1, (15 * 500));
        sheetToshavim.setColumnWidth(2, (15 * 500));

        // Create a path where we will place our List of objects on external storage
        File file = new File(context.getExternalFilesDir(null), fileName + ".xls");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}