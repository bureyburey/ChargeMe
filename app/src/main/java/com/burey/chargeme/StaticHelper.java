package com.burey.chargeme;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by burey on 06/07/2016.
 */
public class StaticHelper {
    public static String getSalesTableName()
    {
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // months start at 0

        StringBuilder currentTable = new StringBuilder();
        switch(month)
        {
            case 1:
                currentTable.insert(0, "January");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 2:
                currentTable.insert(0, "February");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 3:
                currentTable.insert(0, "March");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 4:
                currentTable.insert(0, "April");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 5:
                currentTable.insert(0, "May");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 6:
                currentTable.insert(0, "June");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 7:
                currentTable.insert(0, "July");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 8:
                currentTable.insert(0, "August");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 9:
                currentTable.insert(0, "September");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 10:
                currentTable.insert(0, "October");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 11:
                currentTable.insert(0, "November");
                currentTable.append("_");
                currentTable.append(year);
                break;
            case 12:
                currentTable.insert(0, "December");
                currentTable.append("_");
                currentTable.append(year);
                break;
            default:
                break;
        }
        return currentTable.toString();
    }
}
