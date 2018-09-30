package com.logixcess.smarttaxiapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.logixcess.smarttaxiapplication.Activities.BaseActivity;
import com.logixcess.smarttaxiapplication.Activities.LoginActivity;
import com.logixcess.smarttaxiapplication.Activities.MyNotificationManager;
import com.logixcess.smarttaxiapplication.Activities.OrderDetailsActivity;
import com.logixcess.smarttaxiapplication.CustomerModule.CustomerMapsActivity;
import com.logixcess.smarttaxiapplication.Fragments.FeedbackFragment;
import com.logixcess.smarttaxiapplication.Fragments.FindUserFragment;
import com.logixcess.smarttaxiapplication.Fragments.MapFragment;
import com.logixcess.smarttaxiapplication.Fragments.NotificationsFragment;
import com.logixcess.smarttaxiapplication.Fragments.RideHistoryFragment;
import com.logixcess.smarttaxiapplication.Fragments.UserProfileFragment;
import com.logixcess.smarttaxiapplication.Interfaces.IDrivers;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.Group;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.Services.FirebaseDataSync;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.Utils.Config;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.FetchDriversBasedOnRadius;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.NotificationUtils;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.logixcess.smarttaxiapplication.Fragments.MapFragment.new_order;
import static com.logixcess.smarttaxiapplication.Services.LocationManagerService.mLastLocation;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
         MapFragment.OnFragmentInteractionListener
        ,FeedbackFragment.OnFragmentInteractionListener,FindUserFragment.OnFragmentInteractionListener,NotificationsFragment.OnFragmentInteractionListener
        ,RideHistoryFragment.OnFragmentInteractionListener,UserProfileFragment.OnFragmentInteractionListener
        , IDrivers {

    private static final int REQUEST_CODE_LOCATION = 1021;
    private static final int REQUEST_CODE_LOCATION_DROP_OFF = 1022;
    private static final int REQUEST_CODE_ORDER_CREATION = 1023;
    private static final int REQUEST_CODE_MAPS = 1024;
    Order order_pending;
    Handler mHandler;
    NavigationView navigationView;
    private ProgressBar progressbar;
    private DrawerLayout drawer;
    BroadcastReceiver mRegistrationBroadcastReceiver;
    public static Order mRunningOrder = null;
    public static FirebaseUser mFirebaseUser;
    private int CURRENT_ORDER_STATUS = 0;
    private String CURRENT_ORDER_ID = "";
    private boolean IS_FOR_ORDER_VIEW = false;
    private NotificationPayload notificationPayload = null;
    private DatabaseReference db_ref_user_general;
    private DatabaseReference db_ref;

    public static String getRegionName(Context context, double lati, double longi) {
        String regioName = "";
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lati, longi, 1);
            if (addresses.size() > 0) {
                regioName = addresses.get(0).getLocality();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regioName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        progressbar = findViewById(R.id.progressbar);
        navigationView.setNavigationItemSelectedListener(this);
        db_ref = FirebaseDatabase.getInstance().getReference();
        db_ref_user_general = db_ref.child(Helper.REF_PASSENGERS);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        //if user login is saved then we can driectly login using this user object
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        View hView =  navigationView.getHeaderView(0);
        if(mFirebaseUser != null)
        {
            TextView nav_user_name = hView.findViewById(R.id.tv_person_name);
            nav_user_name.setText(mFirebaseUser.getDisplayName());
            TextView nav_user_status = hView.findViewById(R.id.tv_person_status);
            nav_user_status.setText(mFirebaseUser.getEmail());
            ImageView nav_user_image = hView.findViewById(R.id.iv_person_pic);
            if(mFirebaseUser.getPhotoUrl() != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(mFirebaseUser.getPhotoUrl().toString());
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.user_placeholder);
                requestOptions.circleCrop();
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageURL = uri.toString();
                        try {
                            //For loading image
                            Glide.with(MainActivity.this).setDefaultRequestOptions(requestOptions).load(imageURL)
                                    .into(nav_user_image);
                        }catch (IllegalArgumentException ignore){
                        
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Glide.with(MainActivity.this).setDefaultRequestOptions(requestOptions).load("")
                                .into(nav_user_image);
                    }
                });
            }
            new Timer().schedule(new Every10Seconds(),5000,10000);
        }


        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        mHandler = new Handler();


        navigationView.setNavigationItemSelectedListener(this);
        setUpNavigationView();
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

                  //  Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //txtMessage.setText(message);
                }
            }
        };
        displayFirebaseRegId();
        if(bundle != null){
            if(bundle.containsKey(MyNotificationManager.INTENT_FILTER_VIEW_ORDER)){
                IS_FOR_ORDER_VIEW = true;
                notificationPayload = new Gson()
                        .fromJson(bundle.getString(
                                MyNotificationManager.INTENT_FILTER_VIEW_ORDER),NotificationPayload.class);
                IS_FOR_ORDER_VIEW = (notificationPayload != null);
                if(IS_FOR_ORDER_VIEW)
                    goFetchOrder();
            }
        }
        getAllOrders();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            getAllNotifications(user.getUid());
        }
        new CountDownTimer(5000, 2500) {
            @Override
            public void onTick(long millisUntilFinished) {
                getCurrentOrderId(true);
            }
        
            @Override
            public void onFinish() {
                new FetchDriversBasedOnRadius(MainActivity.this, mLastLocation,MainActivity.this);
            }
        }.start();
        
        
        
        order_pending = new Order();
    }

    public Location getCurrentLocation(){
        return mLastLocation;
    }


    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e("push", "Firebase reg id: " + regId);

        //if (!TextUtils.isEmpty(regId))
          //  Toast.makeText(getApplicationContext(), "Push notification: " + regId, Toast.LENGTH_LONG).show();
            //txtRegId.setText("Firebase Reg Id: " + regId);
        //else
          //  Toast.makeText(getApplicationContext(), "Firebase Reg Id is not received yet!", Toast.LENGTH_LONG).show();
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
        getCurrentOrderId(true);
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
        

        return super.onOptionsItemSelected(item);
    }


    public FirebaseUser getmFirebaseUser(){
        return mFirebaseUser;
    }

/*
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
        }show
        findViewById(R.id.ct_address).setVisibility(View.VISIBLE);
        findViewById(R.id.ct_vehicles).setVisibility(View.GONE);
        findViewById(R.id.btn_confirm).setVisibility(View.VISIBLE);
        getDriverList();


    }*/
//When vehicle dialog is clicked then openVehicles is called
    public void openVehicles(View view) {
        if(new_order == null)
            new_order = new Order();
        findViewById(R.id.ct_address).setVisibility(View.GONE);
        findViewById(R.id.ct_vehicles).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_confirm).setVisibility(View.GONE);

    }
    public void openPickupActivity(View view) {
        if(new_order == null)
            new_order = new Order();
        if(mapFragment.getThereIsActiveOrder()){
            Toast.makeText(this, "There is already an order in Progress.", Toast.LENGTH_SHORT).show();
            return;
        }
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();

        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(typeFilter)
                            .build(this);
            if(view.getId() == R.id.et_pickup)
                startActivityForResult(intent, REQUEST_CODE_LOCATION);
            else
                startActivityForResult(intent, REQUEST_CODE_LOCATION_DROP_OFF);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(new_order == null)
            new_order = new Order();
        if (requestCode == REQUEST_CODE_LOCATION)
        {
            if(resultCode == RESULT_OK){
                mapFragment.getgMap().clear();
                Place place = PlaceAutocomplete.getPlace(this, data);
                if(place != null && place.getAddress() != null) {
                    Log.d("FULL ADDRESS****", place.toString());
                    MapFragment.et_pickup.setText(place.getAddress());
                    new_order.setPickup(place.getAddress().toString());
                    new_order.setPickupLat(place.getLatLng().latitude);
                    new_order.setPickupLong(place.getLatLng().longitude);
                    mapFragment.showCalculatedCost();

                }
            }
            else if (resultCode == RESULT_CANCELED)
            {
                //Write your code if there's no result
            }
        }
        if (requestCode == REQUEST_CODE_LOCATION_DROP_OFF)
        {
            if(resultCode == RESULT_OK){
                mapFragment.getgMap().clear();
                Place place = PlaceAutocomplete.getPlace(this, data);
                if(place != null && place.getAddress() != null) {
                    Log.d("FULL ADDRESS****", place.toString());
                    MapFragment.et_drop_off.setText(place.getAddress());
                    new_order.setDropoff(place.getAddress().toString());
                    new_order.setDropoffLat(place.getLatLng().latitude);
                    new_order.setDropoffLong(place.getLatLng().longitude);
                    mapFragment.showCalculatedCost();
                    if (!TextUtils.isEmpty(MapFragment.et_pickup.getText())) {
                        MarkerOptions options = new MarkerOptions();
                        Double pickupLat = new_order.getPickupLat();
                        Double pickupLng = new_order.getPickupLong();
                        options.position(new LatLng(pickupLat, pickupLng));
                        options.position(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude));
                        mapFragment.getgMap().addMarker(options);
                        String url = mapFragment.getMapsApiDirectionsUrl();
                        mapFragment.getgMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickupLat, pickupLng),
                                13));
                        mapFragment.addMarkers();
                    }

                }
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        if (requestCode == UserProfileFragment.GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Constants.FilePathUri = data.getData();
                onSelectFromGalleryResult(data);
            }
        } else if (requestCode == UserProfileFragment.CAMERA_REQUEST) {
            if (resultCode == RESULT_OK) {
                Constants.FilePathUri = data.getData();
                onCaptureImageResult(data);
            }
        }else if(requestCode == REQUEST_CODE_ORDER_CREATION){
            if(resultCode == RESULT_OK){
                getCurrentOrderId(true);
                mapFragment.resetUI();
            }
        }else if(requestCode == REQUEST_CODE_MAPS){
            if(resultCode == RESULT_OK){
                new_order = null;
                mapFragment.setIsJoiningOtherRide(false);
                MapFragment.CREATE_NEW_GROUP = false;
                Constants.group_id = "";
                Constants.group_radius = 0;
                mapFragment.resetUI();
                getCurrentOrderId(true);
                mapFragment.setThereIsActiveOrder(false);
                
            }
        }
    }

    private void updateUserLocation(){
        
        if(mLastLocation == null)
            return;
        Double lat = mLastLocation.getLatitude();
        Double lng = mLastLocation.getLongitude();
        db_ref_user_general.child(mFirebaseUser.getUid()).child("latitude").setValue(lat);
        db_ref_user_general.child(mFirebaseUser.getUid()).child("longitude").setValue(lng);

    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream is = new ByteArrayInputStream(bitmapdata);
                //InputStream is =
                Bitmap bmImg = BitmapFactory.decodeStream(is);
                Drawable background = new BitmapDrawable(bmImg);
                CircularImageView user_image_layout = profileFragment.getView().findViewById(R.id.profile_image);
                user_image_layout.setBackground(background);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Imagebitmap=thumbnail;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream is = new ByteArrayInputStream(bitmapdata);
        //InputStream is =
        Bitmap bmImg = BitmapFactory.decodeStream(is);
        Drawable background = new BitmapDrawable(bmImg);
        CircularImageView user_image_layout = profileFragment.getView().findViewById(R.id.profile_image);
        user_image_layout.setBackground(background);
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    MapFragment mapFragment;
    public void openScheduleActivity(View view) {
        mapFragment.showDateTimePicker();
    }

    public void openBookNowActivity(View view) {
        openOrderDetailsActivity(null);
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
        mHandler.post(mPendingRunnable);
        // show or hide the fab button
        // toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //Check to see which item was being clicked and perform appropriate action
        switch (id) {
            //Replacing the main content with ContentFragment Which is our Inbox View
            case R.id.nav_user_profile:
                navItemIndex = 2;
                CURRENT_TAG = TAG_USER_PROFILE;
                break;
            case R.id.nav_ride_history:
                navItemIndex = 1;
                CURRENT_TAG = TAG_RIDE_HISTORY;
                break;
            case R.id.nav_add_ride:
                navItemIndex = 0;
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
            case R.id.nav_current_ride:
                navItemIndex = 6;
                getCurrentOrderId(false);
                break;
            case R.id.nav_logout:
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                if(task.isSuccessful())
                                {
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this,"Error "+task.getException(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            default:
                navItemIndex = 0;

                //Checking if the item is in checked state or not, if not make it in checked state
                loadHomeFragment();
                item.setChecked(!item.isChecked());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    ArrayList<Order> my_orders;
    String order_pending_id,order_pending_driver_name,order_pending_driver_id,pending_dest,pending_pickup;
    private void getAllOrders() {
        my_orders = new ArrayList<>();
        FirebaseUser USER_ME = FirebaseAuth.getInstance().getCurrentUser();
        Query db_ref_order = FirebaseDatabase.getInstance().getReference().child(Helper.REF_ORDERS).orderByChild("user_id").equalTo(USER_ME.getUid());
        db_ref_order.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot:dataSnapshot.getChildren()) {
                        Order order = snapshot.getValue(Order.class);
                        if( (order.getStatus() == Order.OrderStatusCompleted ) || (order.getStatus() == Order.OrderStatusCompletedReview ))
                        {
                            my_orders.add(order);
                            getDriverId(my_orders.indexOf(order),order.getDriver_id());
                        }
                        if (order.getStatus() == Order.OrderStatusCompleted)
                        {
                            order_pending_id = order.getOrder_id();
                            //order_pending_driver_name = order.getDriver_name();
                            order_pending_driver_id = order.getDriver_id();
                            pending_dest = order.getDropoff();
                            pending_pickup = order.getPickup();
                            getDriver(order.getDriver_id());
                        }
                        else if (order.getStatus() == Order.OrderStatusInProgress)
                        {
                            order_pending = order;
                            //order_pending_id = order.getOrder_id();
                            //order_pending_driver_name = order.getDriver_name();
                            //order_pending_driver_id = order.getDriver_id();
                            //pending_dest = order.getDropoff();
                            //pending_pickup = order.getPickup();
                        }
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
    RideHistoryFragment rideHistoryFragment;
    UserProfileFragment profileFragment;
    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
            // add ride fragment
            mapFragment = new MapFragment();
            return mapFragment;
            case 1:
                // ride history
                rideHistoryFragment = new RideHistoryFragment();
                if(my_orders != null){
                Bundle args = new Bundle();
                args.putParcelableArrayList("history_orders", my_orders );
                rideHistoryFragment.setArguments(args);}
                return rideHistoryFragment;
            case 2:
                // home user profile
                profileFragment = new UserProfileFragment();
                return profileFragment;
            case 3:
                // notifications fragment
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                if(notificationPayloads != null){
                    Bundle args = new Bundle();
                    args.putParcelableArrayList("history_notifications", notificationPayloads );
                    notificationsFragment.setArguments(args);}
                return notificationsFragment;
            case 4:
                // feedback fragment
                FeedbackFragment feedbackFragment = new FeedbackFragment();
                if(order_pending_id!=null || (!TextUtils.isEmpty(order_pending_id)))
                {
                    Bundle args = new Bundle();
                    args.putString("order_id",order_pending_id);
                    args.putString("driver_id",order_pending_driver_id);
                    args.putString("driver_name",order_pending_driver_name);
                    args.putString("pending_dest",pending_dest);
                    args.putString("pending_pickup",pending_pickup);
                    feedbackFragment.setArguments(args);
                    order_pending_id = null;
                    order_pending_driver_id =  null;
                    order_pending_driver_name =  null;
                    pending_dest = null;
                    pending_pickup = null;
                }
                return feedbackFragment;
//            case 5:
//                // find user fragment
//                FindUserFragment findUserFragment = new FindUserFragment();
//                return findUserFragment;



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
        if(mapFragment.validateAll())
            startActivityForResult(new Intent(this, OrderDetailsActivity.class),REQUEST_CODE_ORDER_CREATION);
    }
    private void setUpNavigationView()
    {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View
                    case R.id.nav_add_ride:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_ADD_RIDE;
                        break;
                    case R.id.nav_ride_history:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_RIDE_HISTORY;
                        break;
                    case R.id.nav_user_profile:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_USER_PROFILE;
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
                    case R.id.nav_current_ride:
                        navItemIndex = 6;
                        CURRENT_TAG = "current_ride";
                        getCurrentOrderId(false);
                        break;
                    case R.id.nav_logout:
                        AuthUI.getInstance()
                                .signOut(MainActivity.this)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // user is now signed out
                                        if(task.isSuccessful())
                                        {
                                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                        else
                                        {
                                            Toast.makeText(MainActivity.this,"Error"+task.getException(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    default:
                        navItemIndex = 0;

                        //Checking if the item is in checked state or not, if not make it in checked state
//                        menuItem.setChecked(!menuItem.isChecked());
//                        loadHomeFragment();
                }
                menuItem.setChecked(!menuItem.isChecked());
                loadHomeFragment();


                return true;
            }
        });
        loadHomeFragment();//by default
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void getDrivers(Location location){
        new FetchDriversBasedOnRadius(this, location,this);
//        return this.DriversInRadius;
    }

    public void showCurrentOrder() {
        DatabaseReference db_ref, db_ref_order;
        db_ref = FirebaseDatabase.getInstance().getReference();
        db_ref_order = db_ref.child(Helper.REF_ORDERS).child("fk_user_id");
        db_ref_order.equalTo(mFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Order order = dataSnapshot.getValue(Order.class);
                    if(order != null){
                        if(order.getStatus() == Order.OrderStatusInProgress) {
                            // order is in progress
                            CURRENT_ORDER_STATUS = Order.OrderStatusInProgress;
                        }else if(order.getStatus() == Order.OrderStatusPending){
                            // order is not assigned yet
                            CURRENT_ORDER_STATUS = Order.OrderStatusPending;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public int getCurrentOrderStatus(){
        return CURRENT_ORDER_STATUS;
    }

    public void openCurrentOrder(View view){
        getCurrentOrderId(false);
    }

    public void getCurrentOrderId(boolean isForConditionCheck){
        progressbar.setVisibility(View.VISIBLE);
        DatabaseReference db_orders = FirebaseDatabase.getInstance().getReference().child(Helper.REF_ORDERS);
        Query ref = db_orders.orderByChild("user_id").equalTo(mFirebaseUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Order order = snapshot.getValue(Order.class);
                        if(order != null){
                            if(order.getStatus() == Order.OrderStatusWaiting ||
                                    order.getStatus() == Order.OrderStatusInProgress){
                                mRunningOrder = order;
                                
                                mapFragment.hideDetails();
                                startService(new Intent(MainActivity.this, FirebaseDataSync.class));
                                if(!isForConditionCheck) {
                                    openOrderActivity(order);
                                }
                                findViewById(R.id.current_order_view).setVisibility(View.VISIBLE);
                                if(mapFragment != null) {
                                    mapFragment.setThereIsActiveOrder(true);
                                    if(order.getStatus() == Order.OrderStatusWaiting){
                                        if(order.getShared())
                                            goFetchGroup(order);
                                        break;
                                    }
                                }
                                findViewById(R.id.current_order_view).setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                    if(!mapFragment.getThereIsActiveOrder() ){
                        stopService(new Intent(MainActivity.this, FirebaseDataSync.class));
                        mRunningOrder = null;
                        if(!isForConditionCheck)
                            Toast.makeText(MainActivity.this, "No Order is Currently in Progress", Toast.LENGTH_SHORT).show();
                        mapFragment.setThereIsActiveOrder(false);
                        findViewById(R.id.current_order_view).setVisibility(View.GONE);
                    }
                }else{
                    stopService(new Intent(MainActivity.this, FirebaseDataSync.class));
                    mRunningOrder = null;
                    if(!isForConditionCheck)
                        Toast.makeText(MainActivity.this, "No Order is Currently in Progress", Toast.LENGTH_SHORT).show();
                    mapFragment.setThereIsActiveOrder(false);
                    findViewById(R.id.current_order_view).setVisibility(View.GONE);
                }
                progressbar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressbar.setVisibility(View.GONE);
                mRunningOrder = null;
                stopService(new Intent(MainActivity.this, FirebaseDataSync.class));
                Toast.makeText(MainActivity.this, "Something went Wrong", Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void openOrderActivity(Order order) {
        Intent intent = new Intent(MainActivity.this, CustomerMapsActivity.class);
        intent.putExtra(CustomerMapsActivity.KEY_CURRENT_ORDER, order);
        startActivityForResult(intent,REQUEST_CODE_MAPS);
    }

    @Override
    public void DriversListAdded(List<Driver> drivers) {
        DriversInRadius = drivers;
        // drivers refreshed
        if(mRunningOrder == null)
            if (mapFragment != null)
                mapFragment.getDriverList(DriversInRadius);
    }
    
    private void goFetchOrder() {
        DatabaseReference db_ref_order = FirebaseDatabase.getInstance().getReference().child(Helper.REF_ORDERS).child(notificationPayload.getOrder_id());
        db_ref_order.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Order order = dataSnapshot.getValue(Order.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MainActivity.this, CustomerMapsActivity.class);
                            if(!notificationPayload.getGroup_id().equalsIgnoreCase("--NA--")) {
                                intent.putExtra(CustomerMapsActivity.KEY_CURRENT_SHARED_RIDE, notificationPayload.getGroup_id());
                            }
                            intent.putExtra(CustomerMapsActivity.KEY_CURRENT_ORDER, order);
                            startActivityForResult(intent,REQUEST_CODE_MAPS);
                            IS_FOR_ORDER_VIEW = false;
                            notificationPayload = null;
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
    }
    
    private void goFetchGroup(Order order) {
        DatabaseReference db_ref_order = FirebaseDatabase.getInstance().getReference().child(Helper.REF_GROUPS).child(order.getGroup_id());
        db_ref_order.child("radius_constraint").setValue(Constants.group_radius);
        db_ref_order.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    SharedRide sharedRide = dataSnapshot.getValue(SharedRide.class);
                    if(sharedRide != null){
                        if(sharedRide.getOrder_id().equals(order.getOrder_id())) {
                            mRunningOrder = order;
                            new_order = order;
                            mapFragment.setIsJoiningOtherRide(false);
                            MapFragment.CREATE_NEW_GROUP = true;
                            Constants.group_id = sharedRide.getGroup_id();
                            Constants.group_radius = sharedRide.getRadius_constraint();
                            mapFragment.resetUI();
                            if(order.getShared())
                                mapFragment.showPostRadiusInput(order);
                        }else{
                            mRunningOrder = order;
                            new_order = order;
                            mapFragment.setIsJoiningOtherRide(true);
                            MapFragment.CREATE_NEW_GROUP = false;
                            Constants.group_id = sharedRide.getGroup_id();
                            Constants.group_radius = sharedRide.getRadius_constraint();
                            mapFragment.resetUI();
    
                        }
    
                    }
                    
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getDriver(String driver_id)
    {
        DatabaseReference db_ref_user = FirebaseDatabase.getInstance().getReference(Helper.REF_USERS);
        db_ref_user.child(driver_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    User driver = dataSnapshot.getValue(User.class);
                    order_pending_driver_name = driver.getName();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getDriverId(int order_index,String driver_id)
    {
        HashMap<String,Integer> hm_driver = new HashMap<>();
        DatabaseReference db_ref_user = FirebaseDatabase.getInstance().getReference(Helper.REF_USERS);
        hm_driver.put(driver_id,order_index);
        db_ref_user.child(driver_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    User driver = dataSnapshot.getValue(User.class);
                    if (my_orders != null) {
                        int index  = hm_driver.get(driver.getUser_id());
                        Order order = my_orders.get(index);
                        order.setDriver_name(driver.getName());
                        my_orders.set(order_index,order);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    ArrayList<NotificationPayload> notificationPayloads;
    private void getAllNotifications(String user_id) {
        notificationPayloads = new ArrayList<>();
        DatabaseReference db_ref_notifications = FirebaseDatabase.getInstance().getReference().child(Helper.REF_NOTIFICATIONS).child(user_id);
        db_ref_notifications.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snapshot:dataSnapshot.getChildren())
                    {
                        NotificationPayload notificationPayload = snapshot.getValue(NotificationPayload.class);
                        notificationPayloads.add(notificationPayload);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
    }
    
    public void refreshPassengers(View view) {
        EditText text = findViewById(R.id.post_radius_input);
        String val = text.getText().toString();
        if(TextUtils.isEmpty(val)){
            Toast.makeText(this, "Please enter radius", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Constants.group_radius = Integer.valueOf(val);
        if(mRunningOrder != null)
            goFetchGroup(mRunningOrder);
        else
            getCurrentOrderId(true);
    }
    
    private class Every10Seconds extends TimerTask {
        @Override
        public void run() {
            updateUserLocation();
        }
    }
    
    
    
    
}
