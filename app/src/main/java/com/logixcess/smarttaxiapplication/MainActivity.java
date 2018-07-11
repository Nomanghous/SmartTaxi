package com.logixcess.smarttaxiapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.logixcess.smarttaxiapplication.Activities.OrderDetailsActivity;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.HttpConnection;
import com.logixcess.smarttaxiapplication.Utils.PathJsonParser;
import com.logixcess.smarttaxiapplication.Utils.UserLocationManager;
import com.schibstedspain.leku.LocationPickerActivity;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final int REQUEST_CODE_LOCATION = 1021;
    private static final int REQUEST_CODE_LOCATION_DROP_OFF = 1022;
    GoogleMap gMap;
    private EditText et_drop_off,et_pickup;
    private Order new_order;
    CheckBox cb_shared;
    public static HashMap<Integer,String> route_details;
    private ArrayList<Polyline> polyLineList;
    private UserLocationManager gps;
    private GregorianCalendar SELECTED_DATE_TIME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gps = new UserLocationManager(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new_order = new Order();
        et_pickup = findViewById(R.id.et_pickup);
        cb_shared = findViewById(R.id.cb_shared);
        new_order.setShared(false);
        new_order.setEstimated_cost("200.0");
        new_order.setTotal_kms("20");
        new_order.setPickup_time(Calendar.getInstance().getTime().toString());
        new_order.setPickup_date(Calendar.getInstance().getTime().toString());
        cb_shared.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new_order.setShared(isChecked);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        if(gps.canGetLocation()){
            LatLng usa = new LatLng(gps.getLatitude(), gps.getLongitude());
            gMap.moveCamera(CameraUpdateFactory.newLatLng(usa));
        }
        // Add a marker in Sydney and move the camera
        gMap.setOnPolylineClickListener(this);

    }

    public void selectVehicle(View view) {
        findViewById(R.id.ct_address).setVisibility(View.VISIBLE);
        findViewById(R.id.ct_vehicles).setVisibility(View.GONE);
        findViewById(R.id.btn_confirm).setVisibility(View.VISIBLE);

    }

    public void openVehicles(View view) {
        findViewById(R.id.ct_address).setVisibility(View.GONE);
        findViewById(R.id.ct_vehicles).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_confirm).setVisibility(View.GONE);

    }

    public void openPickupActivity(View view) {
        Double latitude = 37.62489;
        Double longitude = -122.3708;
//        if(gps.canGetLocation()){
//            latitude = gps.getLatitude();
//            longitude = gps.getLongitude();
//        }
        Intent intent = new LocationPickerActivity.Builder()
                .withLocation(latitude,longitude)
                .withGeolocApiKey(getResources().getString(R.string.google_maps_api))
                .withSearchZone("es_ES")
                .shouldReturnOkOnBackPressed()
                .withStreetHidden()
                .withCityHidden()
                .withZipCodeHidden()
                .withSatelliteViewHidden()
                .build(getApplicationContext());
        if(view.getId() == R.id.et_pickup)
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        else
            startActivityForResult(intent, REQUEST_CODE_LOCATION_DROP_OFF);
    }
    public void showDateTimePicker() {
        // Initialize
        SwitchDateTimeDialogFragment dateTimeFragment = SwitchDateTimeDialogFragment.newInstance(
                "SELECT DATE TIME",
                "OK",
                "Cancel"
        );


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());


        // Assign values
        dateTimeFragment.startAtCalendarView();
        dateTimeFragment.set24HoursMode(true);
        dateTimeFragment.setMinimumDateTime(new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime());
        dateTimeFragment.setMaximumDateTime(new GregorianCalendar(2025, Calendar.DECEMBER, 31).getTime());
        if(SELECTED_DATE_TIME == null)
            dateTimeFragment.setDefaultDateTime(new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)).getTime());
        else
            dateTimeFragment.setDefaultDateTime(SELECTED_DATE_TIME.getTime());

        try {
            dateTimeFragment.setSimpleDateMonthAndDayFormat(new SimpleDateFormat("dd MMMM", Locale.getDefault()));
        } catch (SwitchDateTimeDialogFragment.SimpleDateMonthAndDayFormatException e) {
            Log.e("err", e.getMessage());
        }

        dateTimeFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                String d = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
                String time = new SimpleDateFormat("hh:mm", Locale.getDefault()).format(date);
                new_order.setPickup_time(time);
                new_order.setPickup_date(d);
                SELECTED_DATE_TIME = new GregorianCalendar();
                SELECTED_DATE_TIME.setTime(date);
            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });
        dateTimeFragment.show(getSupportFragmentManager(), "dialog_time");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if(resultCode == RESULT_OK){
                double latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0);
                Log.d("LATITUDE****", String.valueOf(latitude));
                double longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0);
                Log.d("LONGITUDE****", String.valueOf(longitude));
                String address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);
                Log.d("ADDRESS****", String.valueOf(address));
                String postalcode = data.getStringExtra(LocationPickerActivity.ZIPCODE);
                Log.d("POSTALCODE****", String.valueOf(postalcode));
                Address fullAddress = data.getParcelableExtra(LocationPickerActivity.ADDRESS);
                if(fullAddress != null) {
                    Log.d("FULL ADDRESS****", fullAddress.toString());
                    et_pickup.setText(fullAddress.getAddressLine(0));
                    new_order.setPickup(fullAddress.getAddressLine(0));
                    new_order.setPickupLat(String.valueOf(latitude));
                    new_order.setPickupLong(String.valueOf(longitude));
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        if (requestCode == REQUEST_CODE_LOCATION_DROP_OFF) {
            if(resultCode == RESULT_OK){
                double latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0);
                Log.d("LATITUDE****", String.valueOf(latitude));
                double longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0);
                Log.d("LONGITUDE****", String.valueOf(longitude));
                String address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);
                Log.d("ADDRESS****", String.valueOf(address));
                String postalcode = data.getStringExtra(LocationPickerActivity.ZIPCODE);
                Log.d("POSTALCODE****", String.valueOf(postalcode));

                Address fullAddress = data.getParcelableExtra(LocationPickerActivity.ADDRESS);
                if(fullAddress != null) {
                    Log.d("FULL ADDRESS****", fullAddress.toString());
                    et_drop_off.setText(fullAddress.getAddressLine(0));
                    new_order.setDropoff(fullAddress.getAddressLine(0));
                    new_order.setDropoffLat(String.valueOf(latitude));
                    new_order.setDropoffLong(String.valueOf(longitude));


                    if(!TextUtils.isEmpty(et_pickup.getText())){
                        MarkerOptions options = new MarkerOptions();
                        Double pickupLat = Double.valueOf(new_order.getPickupLat());
                        Double pickupLng = Double.valueOf(new_order.getPickupLong());
                        options.position(new LatLng(pickupLat,pickupLng));
                        options.position(new LatLng(latitude,longitude));
                        gMap.addMarker(options);
                        String url = getMapsApiDirectionsUrl();
                        ReadTask downloadTask = new ReadTask();
                        downloadTask.execute(url);

                        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickupLat,pickupLng),
                                13));
                        addMarkers();
                    }

                }
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }


    public void openScheduleActivity(View view) {
        showDateTimePicker();
    }

    public void openBookNowActivity(View view) {
        et_drop_off = findViewById(R.id.et_dropoff);
        et_drop_off.setVisibility(View.VISIBLE);

//        if(dialogClass == null) {
//            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//                    new IntentFilter("book_now"));
//            dialogClass = new CustomDialogClass(this);
//            dialogClass.show();
//        }
//        else
//            dialogClass.populateDropOff(DROP_OFF_ADDRESS);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            if(message.equalsIgnoreCase("Drop_off_location")){
                openPickupActivity(null);
            }else{
//                dialogClass = null;
            }
        }
    };


    public void stopBroadcastReceiver(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }



    /*
    *
    * MAP JOB
    *
    * */

    private String getMapsApiDirectionsUrl() {
        String addresses = "optimize:true&origin="
                + new_order.getPickupLat().concat(",") + new_order.getPickupLong()
                + "&destination=" + new_order.getDropoffLat()+ ","
                + new_order.getDropoffLong();

        String sensor = "sensor=false";
        String params = addresses + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params +"&alternatives=true&key="+ getString(R.string.google_maps_api);
        return url;
    }

    private void addMarkers() {
        if (gMap != null) {
            Double pickupLat = Double.valueOf(new_order.getPickupLat());
            Double pickupLng = Double.valueOf(new_order.getPickupLong());
            Double dropOffLat = Double.valueOf(new_order.getDropoffLat());
            Double dropOffLng = Double.valueOf(new_order.getDropoffLong());
            gMap.addMarker(new MarkerOptions().position(new LatLng(pickupLat,pickupLng))
                    .title("First Point"));
            gMap.addMarker(new MarkerOptions().position(new LatLng(dropOffLat,dropOffLng))
                    .title("Second Point"));
        }
    }

    public void openOrderDetailsActivity(View view) {
        Helper.CURRENT_ORDER = new_order;
        startActivity(new Intent(this, OrderDetailsActivity.class));
    }


    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                route_details = new HashMap<>();
                PathJsonParser parser = new PathJsonParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            if(routes == null)
                return;
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);
                String distance = route_details.get(i+1).split("--")[0];
                String duration = route_details.get(i+1).split("--")[1];
                new_order.setTotal_kms(String.valueOf(Double.valueOf(distance.replace("mi","")) * 1.609344));
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                if(i == 0) {
                    polyLineOptions.width(20);
                    polyLineOptions.color(Color.BLUE);
                }else {
                    polyLineOptions.width(10);
                    polyLineOptions.color(Color.DKGRAY);
                }
                polyLineOptions.clickable(true);
                if(polyLineList == null)
                    polyLineList = new ArrayList<Polyline>();
                Polyline polyline = gMap.addPolyline(polyLineOptions);
                polyline.setTag(route_details.get(i+1));
                polyLineList.add(polyline);
            }


        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.i("POLYLINE",polyline.toString());
        for (Polyline pline : polyLineList){
            if(pline.getId().equals(polyline.getId())){
                pline.setWidth(20);
                pline.setColor(Color.BLUE);
            }else{
                pline.setWidth(10);
                pline.setColor(Color.DKGRAY);
            }
        }
        String[] value = ((String) polyline.getTag()).split("--");
        Toast.makeText(this, "Distance: ".concat(value[0]).concat(" and Duration: ").concat(value[1]), Toast.LENGTH_SHORT).show();
    }


}
