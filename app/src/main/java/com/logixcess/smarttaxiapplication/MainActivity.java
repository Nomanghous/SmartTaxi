package com.logixcess.smarttaxiapplication;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.logixcess.smarttaxiapplication.Activities.OrderDetailsActivity;
import com.logixcess.smarttaxiapplication.Fragments.FeedbackFragment;
import com.logixcess.smarttaxiapplication.Fragments.FindUserFragment;
import com.logixcess.smarttaxiapplication.Fragments.MapFragment;
import com.logixcess.smarttaxiapplication.Fragments.NotificationsFragment;
import com.logixcess.smarttaxiapplication.Fragments.RideHistoryFragment;
import com.logixcess.smarttaxiapplication.Fragments.UserProfileFragment;
import com.logixcess.smarttaxiapplication.Utils.Config;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.NotificationUtils;
import com.logixcess.smarttaxiapplication.Utils.UserLocationManager;
import com.schibstedspain.leku.LocationPickerActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
         MapFragment.OnFragmentInteractionListener
        ,FeedbackFragment.OnFragmentInteractionListener,FindUserFragment.OnFragmentInteractionListener,NotificationsFragment.OnFragmentInteractionListener
        ,RideHistoryFragment.OnFragmentInteractionListener,UserProfileFragment.OnFragmentInteractionListener{

    private static final int REQUEST_CODE_LOCATION = 1021;
    private static final int REQUEST_CODE_LOCATION_DROP_OFF = 1022;

    Handler mHandler;
    NavigationView navigationView;
    private UserLocationManager gps;
    private DrawerLayout drawer;
    private PlaceAutocompleteFragment place_autocomplete_fragment;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gps = new UserLocationManager(this);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);




        //Ahmads
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        mHandler = new Handler();


        navigationView.setNavigationItemSelectedListener(this);
        setUpNavigationView();
        //change
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId();
    }

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e("push", "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId))
            Toast.makeText(getApplicationContext(), "Push notification: " + regId, Toast.LENGTH_LONG).show();
            //txtRegId.setText("Firebase Reg Id: " + regId);
        else
            Toast.makeText(getApplicationContext(), "Firebase Reg Id is not received yet!", Toast.LENGTH_LONG).show();
            //txtRegId.setText("Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

AlertDialog builder;
    public void user_selection_dialog()
    {
        Context mContext = MainActivity.this;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_shared_user_selection,
                (ViewGroup) findViewById(R.id.rating_linear_layout));
        EditText edt_user_numbers = layout.findViewById(R.id.edt_user_numbers);
        Button btn_done = layout.findViewById(R.id.btn_done);
        builder = new AlertDialog.Builder(mContext).create();
        builder.setView(layout);
        builder.show();
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                    if(TextUtils.isEmpty(edt_user_numbers.getText().toString()))
                    {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Please select number of users first !", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                    else {
                        builder.dismiss();
                    }

            }
        });
    }
    public void selectVehicle(View view) {
        CheckBox cb_shared = findViewById(R.id.cb_shared);
        if(cb_shared.isChecked())
        {
            user_selection_dialog();
        }
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
        Double latitude = 7.873172;
        Double longitude = 80.665608;
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
                    MapFragment.et_pickup.setText(fullAddress.getAddressLine(0));
                    MapFragment.new_order.setPickup(fullAddress.getAddressLine(0));
                    MapFragment.new_order.setPickupLat(latitude);
                    MapFragment.new_order.setPickupLong(longitude);
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
                    MapFragment.et_drop_off.setText(fullAddress.getAddressLine(0));
                    MapFragment.new_order.setDropoff(fullAddress.getAddressLine(0));
                    MapFragment.new_order.setDropoffLat(latitude);
                    MapFragment.new_order.setDropoffLong(longitude);


                    if(!TextUtils.isEmpty(MapFragment.et_pickup.getText())){
                        MarkerOptions options = new MarkerOptions();
                        Double pickupLat = Double.valueOf(MapFragment.new_order.getPickupLat());
                        Double pickupLng = Double.valueOf(MapFragment.new_order.getPickupLong());
                        options.position(new LatLng(pickupLat,pickupLng));
                        options.position(new LatLng(latitude,longitude));
                        MapFragment.gMap.addMarker(options);
                        String url = mapFragment.getMapsApiDirectionsUrl();


                        MapFragment.gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickupLat,pickupLng),
                                13));
                        mapFragment.addMarkers();
                    }

                }
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    MapFragment mapFragment;
    public void openScheduleActivity(View view) {
        mapFragment.showDateTimePicker();
    }

    public void openBookNowActivity(View view) {


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
    *
    *
    * AHMAD's PART
    *
    *
    *
    *
    * */



    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            // show or hide the fab button
            //toggleFab();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.fragment_container, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        // toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }
    // index to identify current nav menu item
    public int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_ADD_RIDE = "add_ride";
    private static final String TAG_USER_PROFILE = "user_profile";
    private static final String TAG_FEEDBACK = "feedback";
    private static final String TAG_NOTIFICATIONS = "notifications";
    private static final String TAG_FIND_USER = "find_user";
    private static final String TAG_RIDE_HISTORY = "ride_history";
    public static String CURRENT_TAG = TAG_ADD_RIDE;
    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 2:
                // home user profile
                UserProfileFragment homeFragment = new UserProfileFragment();
                return homeFragment;
            case 1:
                // ride history
                RideHistoryFragment rideHistoryFragment = new RideHistoryFragment();
                return rideHistoryFragment;
            case 0:
                // add ride fragment
                mapFragment = new MapFragment();
                return mapFragment;
            case 3:
                // notifications fragment
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                return notificationsFragment;

            case 4:
                // feedback fragment
                FeedbackFragment feedbackFragment = new FeedbackFragment();
                return feedbackFragment;
            case 5:
                // find user fragment
                FindUserFragment findUserFragment = new FindUserFragment();
                return findUserFragment;
            default:
                return mapFragment = new  MapFragment();
            //return new UserProfileFragment();
        }
    }
    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;
    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }
    public void openOrderDetailsActivity(View view) {
        Helper.CURRENT_ORDER = MapFragment.new_order;
        startActivity(new Intent(this, OrderDetailsActivity.class));
    }
    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_user_profile:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_USER_PROFILE;
                        break;
                    case R.id.nav_ride_history:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_RIDE_HISTORY;
                        break;
                    case R.id.nav_add_ride:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_ADD_RIDE;
                        break;
                    case R.id.nav_notifications:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_NOTIFICATIONS;
                        break;
                    case R.id.nav_feedback:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_FEEDBACK;
                        break;
                    case R.id.nav_find_user:
                        navItemIndex = 5;
                        CURRENT_TAG = TAG_FIND_USER;
                        break;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });
        loadHomeFragment();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
