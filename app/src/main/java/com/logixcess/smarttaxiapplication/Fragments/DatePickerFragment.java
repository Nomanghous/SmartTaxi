package com.logixcess.smarttaxiapplication.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener
{

    String selected_fragment="";
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        //new SimpleDateFormat(dateFormat,Locale.US(or your locale));
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.JAPAN);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        selected_fragment = getArguments().getString("fragment");
        //Toast.makeText(getActivity().getApplicationContext(),selected_fragment, Toast.LENGTH_LONG).show();
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        int cal_month=month+1;

        if(selected_fragment.equals("issue"))
        {
            //EditText edt_flowering_date = (EditText)getActivity().findViewById(R.id.edt_flowering_date);
            //edt_flowering_date.setText(""+view.getYear()+"/"+cal_month+"/"+view.getDayOfMonth());
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(Calendar.DATE,day);
            c.set(Calendar.MONTH,month);
            c.set(Calendar.YEAR,year);
            Constants.date_selected_issue = c.getTimeInMillis();
        }
        else if(selected_fragment.equals("expiry"))
        {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(Calendar.DATE,day);
            c.set(Calendar.MONTH,month);
            c.set(Calendar.YEAR,year);
            Constants.date_selected_expiry = c.getTimeInMillis();
        }

    }

}