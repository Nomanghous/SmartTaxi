package com.logixcess.smarttaxiapplication.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.R;

public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public TextView yes, no;
    Button btn_confirm_booking;
    EditText et_drop_off;
    TextView tv_open_location_picker;
    public CustomDialogClass(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_book_now);
        yes = findViewById(R.id.btn_back);
        no = findViewById(R.id.btn_skip);
        tv_open_location_picker = findViewById(R.id.tv_open_location_picker);
        btn_confirm_booking = findViewById(R.id.btn_confirm);
        et_drop_off = findViewById(R.id.et_dropoff);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        tv_open_location_picker.setOnClickListener(this);
        btn_confirm_booking.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                ((MainActivity)c).stopBroadcastReceiver();
                c.finish();
                break;
            case R.id.btn_skip:
                dismiss();
                break;
            case R.id.tv_open_location_picker:
                openLocationPicker();
                break;
            case R.id.btn_confirm:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void openLocationPicker() {
        Intent intent = new Intent("book_now");
        // You can also include some extra data.
        intent.putExtra("message", "Drop_off_location");
        LocalBroadcastManager.getInstance(this.c).sendBroadcast(intent);
    }


    public void populateDropOff(String s) {
        et_drop_off.setText(s);
    }
}