package com.burey.chargeme;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.burey.chargeme.DataBase.ItemDbHelper;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class StatisticsActivity extends AppCompatActivity {

    private ArrayList<String> tablesList;
    private ItemDbHelper itemDbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private ArrayAdapter<String> dataAdapter;
    private Spinner tableNamesSpinner;
    private ArrayList<Sale> salesList = new ArrayList<>();
    private PieChart pieChart;
    private BarChart barChart;
    private Switch switchPieBarChart;
    private TextView tvPiePointer;
    private Button spinButton;
    final Random mRandom = new Random(System.currentTimeMillis());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        itemDbHelper = new ItemDbHelper(getApplicationContext(), StaticHelper.getSalesTableName());
        sqLiteDatabase = itemDbHelper.getWritableDatabase();
        tablesList = itemDbHelper.getSalesTables(sqLiteDatabase);
        if(tablesList.size() == 0) {
            Toast.makeText(getApplicationContext(), "אין קבצים להצגה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvPiePointer = (TextView)findViewById(R.id.tv_pie_pointer);
        spinButton = (Button)findViewById(R.id.btn_spin_pie);
        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tablesList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pieChart = (PieChart)findViewById(R.id.pie_chart);
        barChart = (BarChart)findViewById(R.id.bar_chart);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showSales(e);
            }
            @Override
            public void onNothingSelected() {
            }
        });

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showSales(e);
            }
            @Override
            public void onNothingSelected() {
            }
        });

        switchPieBarChart = (Switch)findViewById(R.id.switch_pie_bar_chart);
        switchPieBarChart.setChecked(true);
        switchPieBarChart.setTextOn("Pie");
        switchPieBarChart.setTextOff("Bar");
        switchPieBarChart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    pieChart.setVisibility(View.VISIBLE);
                    barChart.setVisibility(View.INVISIBLE);
                    tvPiePointer.setVisibility(View.VISIBLE);
                    spinButton.setVisibility(View.VISIBLE);
                }
                else{
                    pieChart.setVisibility(View.INVISIBLE);
                    barChart.setVisibility(View.VISIBLE);
                    tvPiePointer.setVisibility(View.INVISIBLE);
                    spinButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        tableNamesSpinner = (Spinner)findViewById(R.id.spinner_table_names);
        tableNamesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                salesList.clear();
                salesList = itemDbHelper.getItemSales(sqLiteDatabase, tablesList.get(position));
                pieChart.clear();
                barChart.clear();
                makePieBarCharts();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        tableNamesSpinner.setAdapter(dataAdapter);
        salesList = itemDbHelper.getItemSales(sqLiteDatabase, tablesList.get(0));
        makePieBarCharts();
    }

    public void showSales(Entry e){
        ArrayList<String> itemList = new ArrayList<>();
        for(int i=0;i<salesList.size();i++){
            if(salesList.get(i).getItemName().compareTo(e.getData().toString()) == 0)
                itemList.add(salesList.get(i).getClientName() + " - " + salesList.get(i).getOrdered());
        }
        AlertDialog.Builder builderShowSales = new AlertDialog.Builder(StatisticsActivity.this);
        builderShowSales.setTitle("לקוחות שקנו " + e.getData() + ":");
        builderShowSales.setItems(itemList.toArray(new CharSequence[itemList.size()]), new DialogInterface.OnClickListener() {
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
    }

    public ArrayList<Item> getItemList(){
        boolean skip = false;
        ArrayList<Item> itemList = new ArrayList<>();
        for(int i=0;i<salesList.size();i++){
            int sumOrdered = salesList.get(i).getOrdered();
            for(int j=0;j<salesList.size();j++){
                // same item, different row (different client)
                if(salesList.get(i).get_item_id() == salesList.get(j).get_item_id() &&
                        salesList.get(i).get_client_id() != salesList.get(j).get_client_id())
                    sumOrdered += salesList.get(j).getOrdered();
            }
            skip = false;
            for(int k=0;k<itemList.size();k++){
                // find items which are already on the list and mark them to be skipped
                if(salesList.get(i).get_item_id() == itemList.get(k).get_id()) {
                    skip = true;
                    break;
                }
            }
            if(!skip) {
                Item newItem = new Item(salesList.get(i).get_item_id(), salesList.get(i).getItemName(), "0", "0");
                newItem.setOrdered(sumOrdered);
                itemList.add(newItem);
            }
        }
        return itemList;
    }

    public void makePieBarCharts(){
        // creating data entries containers
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> customLabels = new ArrayList<>();
        ArrayList<Integer> customColorsBars = new ArrayList<>();
        ArrayList<Item> itemList = getItemList();

        Collections.sort(itemList);

        // filling data containers
        for(int i=0;i<itemList.size();i++) {
            pieEntries.add(new PieEntry(itemList.get(i).getOrdered(), itemList.get(i).getName(), itemList.get(i).getName()));
            barEntries.add(new BarEntry(i, itemList.get(i).getOrdered(), (Object)(itemList.get(i).getName())));
            customLabels.add(itemList.get(i).getName());
            customColorsBars.add(generateRandomColor());
        }
        // setting the data sets
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "יחידות שנמכרו");
        BarDataSet barDataSet = new BarDataSet(barEntries,"יחידות שנמכרו" );

        // set outline border for each data entry
        barDataSet.setBarBorderWidth(0.5f);

//        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        int[] customColors = new int[]{
                Color.rgb(193, 37, 82),
                Color.rgb(255, 102, 0),
                Color.rgb(20, 20, 255),
                Color.rgb(245, 199, 0),
                Color.rgb(106, 150, 31),
                Color.rgb(179, 100, 53),

                Color.rgb(85, 30, 180),
                Color.rgb(255, 0, 20),
                Color.rgb(45, 120, 220),
                Color.rgb(120, 20, 220),
                Color.rgb(6, 250, 31),
                Color.rgb(245, 199, 0),

                Color.rgb(93, 237, 182),
                Color.rgb(155, 102, 0),
                Color.rgb(20, 120, 230),
                Color.rgb(45, 199, 100),
                Color.rgb(6, 250, 31),
                Color.rgb(79, 0, 53)
        };

        pieDataSet.setColors(customColors);
        barDataSet.setColors(customColorsBars);

        PieData pieData = new PieData(pieDataSet);
        BarData barData = new BarData(barDataSet);

        pieData.setValueFormatter(new MyValueFormatter());
        barData.setValueFormatter(new MyValueFormatter());

        pieChart.setData(pieData);
        barChart.setData(barData);

        pieChart.setHoleRadius(20f);
        pieChart.setTransparentCircleRadius(25f);

        // hide numerical values of x-axis (1,2,3,....)
        barChart.getXAxis().setEnabled(false);

        // draw the values above each bar instead of below
        barChart.setDrawValueAboveBar(true);

        barChart.setFitBars(true);
        barChart.setAutoScaleMinMaxEnabled(true);

        pieChart.setDescription("");
        barChart.setDescription("");

        pieChart.animateX(2000);
        barChart.animateX(2000);

        Legend pieLegend = pieChart.getLegend();
        Legend barLegend = barChart.getLegend();

        // sets custom colors and labels for the bar chart
        barLegend.setCustom(customColorsBars, customLabels);
        barLegend.setMaxSizePercent(100f);

        pieLegend.setDirection(Legend.LegendDirection.RIGHT_TO_LEFT);
        barLegend.setDirection(Legend.LegendDirection.RIGHT_TO_LEFT);

        pieLegend.setForm(Legend.LegendForm.CIRCLE);
        barLegend.setForm(Legend.LegendForm.CIRCLE);

        pieLegend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        barLegend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);

        barLegend.setOrientation(Legend.LegendOrientation.VERTICAL);

        barChart.setViewPortOffsets(25,15,150,25);
//        barChart.calculateOffsets();

        pieChart.invalidate();
        barChart.invalidate();

//        pieLegend.setXEntrySpace(7);
//        pieLegend.setYEntrySpace(5);

//        barLegend.setXEntrySpace(7);
//        barLegend.setYEntrySpace(5);
    }

    public void SpinPie(View view){
        pieChart.spin(5000, pieChart.getRotationAngle(), pieChart.getRotationAngle() + (mRandom.nextInt()%1080) + 360, Easing.EasingOption.EaseInOutCubic);
    }

    public int generateRandomColor() {
        // This is the base color which will be mixed with the generated one
        final int baseColor = Color.WHITE;

        final int baseRed = Color.red(baseColor);
        final int baseGreen = Color.green(baseColor);
        final int baseBlue = Color.blue(baseColor);

        final int red = (baseRed + mRandom.nextInt(256)) / 2;
        final int green = (baseGreen + mRandom.nextInt(256)) / 2;
        final int blue = (baseBlue + mRandom.nextInt(256)) / 2;

        return Color.rgb(red, green, blue);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(itemDbHelper != null)
            itemDbHelper.close();
        if(sqLiteDatabase != null)
            if(sqLiteDatabase.isOpen())
                sqLiteDatabase.close();
    }

    public class MyValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return Math.round(value)+"";
        }
    }
}
