
package com.logixcess.smarttaxiapplication.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.logixcess.smarttaxiapplication.Activities.MiniMapActivity;
import com.logixcess.smarttaxiapplication.CustomerModule.CustomerMapsActivity;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.Passenger;
import com.logixcess.smarttaxiapplication.Models.Requests;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.FareCalculation;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.HttpConnection;
import com.logixcess.smarttaxiapplication.Utils.PathJsonParser;
import com.logixcess.smarttaxiapplication.Utils.PermissionHandler;
import com.logixcess.smarttaxiapplication.Utils.PushNotifictionHelper;
import com.logixcess.smarttaxiapplication.Utils.UserLocationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.logixcess.smarttaxiapplication.Utils.Constants.group_id;
import static com.logixcess.smarttaxiapplication.Utils.Constants.group_radius;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener, View.OnClickListener {
    
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int TO_SHOW_INFO_OF_PASSENGER = 113;
    private static final int TO_SHOW_INFO_OF_DRIVER = 114;
    private static final int TO_ACCEPT_INVITATION = 115;
    public static EditText et_drop_off, et_pickup;
    public static Order new_order;
    public static HashMap<Integer, String> route_details;
    public static HashMap<String, Marker> driver_in_map = new HashMap<>();
    public static HashMap<String, Marker> nearby_passengers_in_map;
    public static HashMap<String, Integer> driver_list_index = new HashMap<>();
    public static boolean CREATE_NEW_GROUP = false, IS_RIDE_SCHEDULED = false;
    static boolean isOrderAccepted = false, isDriverResponded = false;
    public GoogleMap gMap;
    CheckBox cb_shared, cb_scheduled;
    Switch cb_accepting;
    Firebase firebase_instance;
    ArrayList<Driver> driverList;
    Location MY_LOCATION;
    double total_cost = 0;
    Button btn_select_vehicle, btn_hide_details;
    android.app.AlertDialog builder;
    LinearLayout ct_address;
    RelativeLayout ct_vehicles,btn_invites_container,car_container;
    Button btn_confirm;
    LinearLayout layout_cost_detail;
    EditText radius_input;
    TextView txtLocation, txtDestination, txt_cost;
    View vehicle1, vehicle2, vehicle3, vehicle4, vehicle5;
    
    BroadcastReceiver driverResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getExtras().getString("data");
            String action = intent.getExtras().getString("action");
            NotificationPayload notificationPayload = new Gson().fromJson(data, NotificationPayload.class);
            isDriverResponded = true;
            if (notificationPayload != null) {
                if (!notificationPayload.getDriver_id().equals("-1")) {
                    // it's accepted
                    new_order.setDriver_id(notificationPayload.getDriver_id());
                    isOrderAccepted = true;
                } else {
                    isOrderAccepted = false;
                }
            }
        }
    };
    
    private DatabaseReference db_ref_user;
    private ArrayList<Polyline> polyLineList;
    private GregorianCalendar SELECTED_DATE_TIME;

    private OnFragmentInteractionListener mListener;
    private MapView mapFragment;
    private SharedRide currentSharedRide;
    private DatabaseReference db_ref_group, db_ref_requests;
    public static HashMap<String, Boolean> mPassengerList;
    public static HashMap<String, Boolean> mOrderList;
    private boolean thereIsActiveOrder = false;
    private boolean dialog_already_showing = false;
    private FirebaseDatabase firebase_db;
    private Button btn_add_members;
    private boolean isTimeout = false;
    private DatabaseReference db_ref_user_general;
    private List<Passenger> mNearbyPassengers;
    private TextView tv_distance, tv_estimated_cost;
    private CardView ct_details;
    private boolean isTimeoutForPassenger = false;
    private boolean isPassengerResponded = false;
    private boolean isPassengerAccepted = false;
    private boolean isJoiningOtherSharedRide = false;
    private Requests mRequest = null;
    
    
    public MapFragment() {
        // Required empty public constructor
        
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    FareCalculation fareCalculation;
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fareCalculation = new FareCalculation();
       
       
    }

    public void user_selection_dialog() {
        Context mContext = getActivity();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_shared_user_selection,
                null);
        EditText edt_user_numbers = layout.findViewById(R.id.edt_user_numbers);
        Button btn_done = layout.findViewById(R.id.btn_done);

        builder = new android.app.AlertDialog.Builder(mContext).create();
        builder.setView(layout);
        builder.show();
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edt_user_numbers.getText().toString())) {
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Please select number of users first !", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    builder.cancel();
                    builder.dismiss();
                    builder = null;
                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        firebase_db = FirebaseDatabase.getInstance();
        db_ref_user = firebase_db.getReference().child(Helper.REF_PASSENGERS);
        db_ref_user_general = firebase_db.getReference().child(Helper.REF_USERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        db_ref_requests = firebase_db.getReference().child(Helper.REF_REQUESTS);
        driverList = new ArrayList<>();
        tv_estimated_cost = view.findViewById(R.id.tv_estimated_cost);
        tv_distance = view.findViewById(R.id.tv_distance);
        LinearLayout layout_vehicle1, layout_vehicle2, layout_vehicle3, layout_vehicle4, layout_vehicle5;
        mapFragment = view.findViewById(R.id.map);
        btn_add_members = view.findViewById(R.id.btn_add_members);
        vehicle1 = view.findViewById(R.id.vehicle1);
        vehicle2 = view.findViewById(R.id.vehicle2);
        vehicle3 = view.findViewById(R.id.vehicle3);
        vehicle4 = view.findViewById(R.id.vehicle4);
        vehicle5 = view.findViewById(R.id.vehicle5);
        ct_details = view.findViewById(R.id.ct_details);
        cb_accepting = view.findViewById(R.id.cb_accepting);
        
        Helper.isAcceptingInvites = readPrefs("accepting_invites").equalsIgnoreCase("true");
        cb_accepting.setChecked(Helper.isAcceptingInvites);
        
        layout_vehicle1 = view.findViewById(R.id.layout_vehicle1);
        layout_vehicle1.setOnClickListener(this);
        layout_vehicle2 = view.findViewById(R.id.layout_vehicle2);
        layout_vehicle2.setOnClickListener(this);
        layout_vehicle3 = view.findViewById(R.id.layout_vehicle3);
        layout_vehicle3.setOnClickListener(this);
        layout_vehicle4 = view.findViewById(R.id.layout_vehicle4);
        layout_vehicle4.setOnClickListener(this);
        layout_vehicle5 = view.findViewById(R.id.layout_vehicle5);
        layout_vehicle5.setOnClickListener(this);

        layout_cost_detail = view.findViewById(R.id.layout_detail);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtDestination = view.findViewById(R.id.txtDestination);
        btn_hide_details = view.findViewById(R.id.btn_hide_details);
        txt_cost = view.findViewById(R.id.txt_cost);
        ct_address = view.findViewById(R.id.ct_address);
        ct_vehicles = view.findViewById(R.id.ct_vehicles);
        car_container = view.findViewById(R.id.car_container);
        btn_invites_container = view.findViewById(R.id.btn_invites_container);
        radius_input = view.findViewById(R.id.radius_input);
        btn_confirm = view.findViewById(R.id.btn_confirm);
        btn_select_vehicle = view.findViewById(R.id.btn_select_vehicle);

        btn_add_members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMemberDialog();
            }
        });

        btn_select_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new_order != null) {
                    new_order.setVehicle_id(Constants.selected_vehicle);
                    new_order.setShared(cb_shared.isChecked());
    
                    if (new_order.getShared())
                        saveRadiusInputForGroupRide();
                    else {
                        showCalculatedCost();
                        //btn_confirm.setVisibility(View.VISIBLE);
                    }
                }
                refreshDrivers();
                showNearbyPassengersForSharedRide();
            }
        });
        
        mapFragment.onCreate(savedInstanceState);
        mapFragment.getMapAsync(this);
        new_order = new Order();
        et_pickup = view.findViewById(R.id.et_pickup);
        et_drop_off = view.findViewById(R.id.et_dropoff);
        cb_shared = view.findViewById(R.id.cb_shared);
        cb_scheduled = view.findViewById(R.id.cb_scheduled);
        new_order.setShared(false);
        new_order.setUser_id(((MainActivity) getContext()).getmFirebaseUser().getUid());

        btn_hide_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_cost_detail.setVisibility(View.GONE);
                if (btn_confirm.getVisibility() == View.GONE)
                    btn_confirm.setVisibility(View.VISIBLE);
            }
        });
        new_order.setPickup_time(Calendar.getInstance().getTime().toString());
//        new_order.setPickup_date(Calendar.getInstance().getTime().toString());
        cb_shared.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new_order.setShared(isChecked);
                if(isChecked) {
                    radius_input.setVisibility(View.VISIBLE);
                }
                else {
                    radius_input.setVisibility(View.GONE);
                }
            }
        });
        cb_scheduled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IS_RIDE_SCHEDULED = isChecked;
                if(isChecked)
                    btn_add_members.setVisibility(View.VISIBLE);
                else
                    btn_add_members.setVisibility(View.GONE);
            }
        });
      
        cb_accepting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    savePrefs("accepting_invites", "true");
                }else{
                    savePrefs("accepting_invites", "false");
                }
                Helper.isAcceptingInvites = isChecked;
            }
        });
        
        everyTenSecondsTask();
        MY_LOCATION = LocationManagerService.mLastLocation;
        if(getActivity() != null) {
            String userId = ((MainActivity) getActivity()).getmFirebaseUser().getUid();
            listenerForRequests(userId);
        }
        return view;
    }
    
    private void savePrefs(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(key, value).apply();
    }
    
    private String readPrefs(String key){
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString(key,"");
    }
    
    public void showCalculatedCost() {
        if(new_order.getTotal_kms() == null)
            return;
        
        total_cost = fareCalculation.getCostSingleRide();
        if (layout_cost_detail.getVisibility() == View.GONE) {
            if (btn_confirm.getVisibility() == View.VISIBLE)
                btn_confirm.setVisibility(View.GONE);
            layout_cost_detail.setVisibility(View.VISIBLE);
            txtLocation.setText("Location : " + new_order.getPickup());
            txtDestination.setText("Destination : " + new_order.getDropoff());
            txt_cost.setText(String.valueOf(total_cost));
            new_order.setEstimated_cost(String.valueOf(total_cost));
            tv_estimated_cost.setText("Cost: ".concat(String.valueOf(total_cost).concat(" Rs")));
        }
        ct_address.setVisibility(View.VISIBLE);
        btn_invites_container.setVisibility(View.VISIBLE);
        ct_vehicles.setVisibility(View.GONE);
        if (btn_confirm.getVisibility() == View.VISIBLE)
            btn_confirm.setVisibility(View.GONE);
    }
    
    public void getDriverList(List<Driver> drivers) {
        if (new_order == null || thereIsActiveOrder || MainActivity.mRunningOrder != null)
            return;
        for (Driver driver : drivers)
            goCheckSharedRideDriver(driver.getFk_user_id(), driver);

    }

    public void addDriverMarker(Driver driver1, int index) {
        if(isJoiningOtherSharedRide)
            return;
        //now update the routes and remove markers if already present in it.
        LatLng driverLatLng = new LatLng(driver1.getLatitude(), driver1.getLongitude());
        if (driver_in_map.containsKey(driver1.getFk_user_id())) {
            Marker marker = driver_in_map.get(driver1.getFk_user_id());
            marker.setPosition(driverLatLng);
            marker.remove();
            driver_in_map.remove(driver1.getFk_user_id());
            if (gMap != null) {
                marker = gMap.addMarker(new MarkerOptions().position(driverLatLng)
                        .title("Driver: ".concat(driver1.getFk_user_id())));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                marker.setTag(driver1.getFk_user_id());
                driver_in_map.put(driver1.getFk_user_id(), marker);
            }
        } else {
            if (gMap != null) {
                Marker marker = gMap.addMarker(new MarkerOptions().position(driverLatLng)
                        .title("Driver: ".concat(driver1.getFk_user_id())));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                marker.setTag(driver1.getFk_user_id());
                driver_list_index.put(driver1.getFk_user_id(), index);
                driver_in_map.put(driver1.getFk_user_id(), marker);
            }
        }
    }


    /*public void getRegionName(Context context, double lati, double longi) {
        String regioName = "";
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lati, longi, 1);
            if (addresses.size() > 0) {
                regioName = addresses.get(0).getLocality();
                if(!TextUtils.isEmpty(regioName))
                {
                    String region_name = "region_name";
                    Constants.region_name =regioName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    ProgressDialog progressDialog;
    public void  show_driverDetail(String driverid)
    {
        if (dialog_already_showing)
            return;
        String driverId = driverid;
        final CharSequence[] items = { "SELECT", "OPEN PROFILE",
                "CANCEL" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Driver Detail");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = PermissionHandler.checkPermission(getActivity());
                if (items[item].equals("SELECT")) {
                    new_order.setDriver_id(driverId);
                    sendNotificationToRequestGroupRide(driverId);
                    dialog_already_showing = false;
                    dialog.dismiss();
                }
                else if (items[item].equals("OPEN PROFILE"))
                {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.show();
                    db_ref_user_general.child(driverId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists())
                            {
                                User driver = dataSnapshot.getValue(User.class);
                                if(driver == null)
                                    return;
                                open_profile(driver.getUser_id(),driver.getName(),driver.getUser_image_url(),driver.getPhone(), TO_SHOW_INFO_OF_DRIVER);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else if (items[item].equals("CANCEL")) {
                    dialog_already_showing = false;
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    
    public void onMapReadyCustom(GoogleMap gMap){
    
    }
    
    
    private void open_profile(String user_id, String name, String url, String phone, int purpose)
    {
        if(getActivity() == null)
            return;
        ImageView image = new ImageView(getActivity());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(300,300);
        image.setLayoutParams(layoutParams);
        
        
        if(url != null && (!TextUtils.isEmpty(url)) )
        {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.placeholder(R.drawable.user_placeholder);
            requestOptions = requestOptions.circleCrop();
            requestOptions = requestOptions.centerInside();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(url);
            RequestOptions finalRequestOptions = requestOptions;
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri)
                {
                    String imageURL = uri.toString();
                    Glide.with(getActivity()).setDefaultRequestOptions(finalRequestOptions).load(imageURL)
                            .into(image);
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(progressDialog!=null && progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                    String status = "OFFLINE";
                    status = "ONLINE";
                    String canceltext = "Cancel";
                    final CharSequence[] items = { "Name : "+name, "Phone No : "+phone,"Status : "+status };
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom));
                    builder.setTitle("Information :");
                    builder.setView(image);
                    String text = "Request Now";
                    if(purpose == TO_SHOW_INFO_OF_PASSENGER)
                        text = "Send Invitation";
                    if(purpose == TO_ACCEPT_INVITATION) {
                        text = "Accept Invitation";
                        canceltext = "Show Map";
                    }
                    
                    builder.setPositiveButton(text, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //driver_selected(user_id);
                            if(purpose == TO_SHOW_INFO_OF_DRIVER) {
                                sendNotificationToRequestGroupRide(user_id);
                                new_order.setDriver_id(user_id);
                            }
                            else if(purpose == TO_SHOW_INFO_OF_PASSENGER){
                                if(group_id != null) {
                                    if (!group_id.isEmpty()) {
                                        sendInvitationForGroupRide(user_id);
                                    } else
                                        showToast("Please create order first after that you can send request to other passengers.");
                                }else
                                    showToast("Please create order first after that you can send request to other passengers.");
                            } else if(purpose == TO_ACCEPT_INVITATION) {
                                goAcceptInvitation(user_id);
                            }
                            dialog.dismiss();
                        }
                    });
    
    
                    String finalCanceltext = canceltext;
                    builder.setNegativeButton(canceltext, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            
                            if(finalCanceltext.equals("Cancel")) {
                                dialog.dismiss();
                                dialog_already_showing = false;
                            }else{
                                showRoute(mRequest.getOrder_id());
                            }
                        }
                    });

                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item)
                        {
//                if (items[item].equals("SendRequest"))
//                {
//                    driver_selected(user_id);
//                }
                        }
                    });

                    builder.show();
                    dialog_already_showing = true;
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            //  Glide.with(getActivity()).setDefaultRequestOptions(requestOptions).load(url)
                            //        .into(image);

                        }
                    });
        }
        else
        {
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
            String status;
            status = "ONLINE";
            String canceltext = "Cancel";
            final CharSequence[] items = { "Name : "+name, "Phone No : "+phone,"Status : "+status };
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom));
            builder.setTitle("Information :");
            builder.setView(image);
            String text = "Request Now";
            if(purpose == TO_SHOW_INFO_OF_PASSENGER)
                text = "Send Invitation";
            if(purpose == TO_ACCEPT_INVITATION) {
                text = "Accept Invitation";
                canceltext = "Show Map";
            }
    
            builder.setPositiveButton(text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //driver_selected(user_id);
                    if(purpose == TO_SHOW_INFO_OF_DRIVER) {
                        sendNotificationToRequestGroupRide(user_id);
                        new_order.setDriver_id(user_id);
                    }
                    else if(purpose == TO_SHOW_INFO_OF_PASSENGER){
                        if(group_id != null) {
                            if (!group_id.isEmpty()) {
                                sendInvitationForGroupRide(user_id);
                            } else
                                showToast("Please create order first after that you can send request to other passengers.");
                        }else
                            showToast("Please create order first after that you can send request to other passengers.");
                    } else if(purpose == TO_ACCEPT_INVITATION) {
                        goAcceptInvitation(user_id);
                    }
                    dialog.dismiss();
                }
            });
    
    
            String finalCanceltext = canceltext;
            builder.setNegativeButton(canceltext, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
            
                    if(finalCanceltext.equals("Cancel")) {
                        dialog.dismiss();
                        dialog_already_showing = false;
                    }else{
                        showRoute(mRequest.getOrder_id());
                    }
                }
            });

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item)
                {
                    if (items[item].equals("CANCEL")) {
                        dialog.dismiss();
                        dialog_already_showing = false;
                    }
                }
            });

            builder.show();
            dialog_already_showing = true;
        }
    }
    
    private void showRoute(String order_id) {
        firebase_db.getReference().child(Helper.REF_ORDERS).child(order_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Order order = dataSnapshot.getValue(Order.class);
                    if(order != null){
                        Helper.invitationLatlngs = new ArrayList<>();
                        for(RoutePoints routePoints : order.getSelectedRoute()){
                            Helper.invitationLatlngs.add(new LatLng(routePoints.getLatitude(),routePoints.getLongitude()));
                            startActivity(new Intent(getContext(),MiniMapActivity.class));
                        }
                    }
                }
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapFragment != null)
            mapFragment.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
       
        
        if(marker.getSnippet() != null){
            if(marker.getSnippet().equals("Nearby")){
                showUserDetails(marker.getTitle());
                return true;
            }else
                return false;
            
        } else if(marker.getTag() != null) {
            String driverId = (String) marker.getTag();
            if (driverId != null && !driverId.isEmpty()) {
                // do whatever with driver id.
                if(isJoiningOtherSharedRide){
                    showToast("You are joining with Other user Please Create order Directly.");
                    return true;
                }
                goCheckDriverStatus(driverId);
                return true;
            } else
                return false;
        }else
            return false;
    }
    
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_vehicle1:
                if (vehicle1.getVisibility() == View.GONE)
                    vehicle1.setVisibility(View.VISIBLE);
                if (vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if (vehicle3.getVisibility() == View.VISIBLE)
                    vehicle3.setVisibility(View.GONE);
                if (vehicle4.getVisibility() == View.VISIBLE)
                    vehicle4.setVisibility(View.GONE);
                if (vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = fareCalculation.getBaseFare2(Helper.VEHICLE_CAR);
                Constants.selected_vehicle = Helper.VEHICLE_CAR;
                //Constants.BASE_FAIR_PER_KM = 50;//car
                break;
            case R.id.layout_vehicle2:
                if (vehicle2.getVisibility() == View.GONE)
                    vehicle2.setVisibility(View.VISIBLE);
                if (vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                if (vehicle3.getVisibility() == View.VISIBLE)
                    vehicle3.setVisibility(View.GONE);
                if (vehicle4.getVisibility() == View.VISIBLE)
                    vehicle4.setVisibility(View.GONE);
                if (vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = fareCalculation.getBaseFare2(Helper.VEHICLE_MINI);
                Constants.selected_vehicle = Helper.VEHICLE_MINI;
                //Constants.BASE_FAIR_PER_KM = 30;//option mini
                break;
            case R.id.layout_vehicle3:
                if (vehicle3.getVisibility() == View.GONE)
                    vehicle3.setVisibility(View.VISIBLE);
                if (vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if (vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                if (vehicle4.getVisibility() == View.VISIBLE)
                    vehicle4.setVisibility(View.GONE);
                if (vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = fareCalculation.getBaseFare2(Helper.VEHICLE_NANO);
                Constants.selected_vehicle = Helper.VEHICLE_NANO;
                //Constants.BASE_FAIR_PER_KM = 20;//option nano
                break;
            case R.id.layout_vehicle4:
                if (vehicle4.getVisibility() == View.GONE)
                    vehicle4.setVisibility(View.VISIBLE);
                if (vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if (vehicle3.getVisibility() == View.VISIBLE)
                    vehicle3.setVisibility(View.GONE);
                if (vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                if (vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = fareCalculation.getBaseFare2(Helper.VEHICLE_VIP);
                Constants.selected_vehicle = Helper.VEHICLE_VIP;
                //Constants.BASE_FAIR_PER_KM = 60;//option vip
                break;
            case R.id.layout_vehicle5:
                if (vehicle5.getVisibility() == View.GONE)
                    vehicle5.setVisibility(View.VISIBLE);
                if (vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if (vehicle3.getVisibility() == View.VISIBLE)
                    vehicle3.setVisibility(View.GONE);
                if (vehicle4.getVisibility() == View.VISIBLE)
                    vehicle4.setVisibility(View.GONE);
                if (vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = fareCalculation.getBaseFare2(Helper.VEHICLE_THREE_WHEELER);
                Constants.selected_vehicle = Helper.VEHICLE_THREE_WHEELER;
                //Constants.BASE_FAIR_PER_KM = 30;//option three wheeler
                break;

        }
    }

    public boolean getThereIsActiveOrder() {
        return this.thereIsActiveOrder;
    }

    public void setThereIsActiveOrder(boolean thereIsActiveOrder) {
        this.thereIsActiveOrder = thereIsActiveOrder;
    }
    

    public boolean getisJoiningOtherRide() {
        return this.isJoiningOtherSharedRide;
    }

    public void setIsJoiningOtherRide(boolean thereIsActiveOrder) {
        this.isJoiningOtherSharedRide = thereIsActiveOrder;
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
        if (SELECTED_DATE_TIME == null)
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
        dateTimeFragment.show(getActivity().getSupportFragmentManager(), "dialog_time");
    }

    /*
     *
     * MAP JOB
     *
     * */
    Marker myMarker = null;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        Double latitude = 7.8731;
        Double longitude = 80.7718;
        LatLng usa = new LatLng(latitude, longitude);
        if(MY_LOCATION != null){
            usa = new LatLng(MY_LOCATION.getLatitude(), MY_LOCATION.getLongitude());
            myMarker = gMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmapDescriptorFromVector(getActivity(),R.drawable.personn))).position(new LatLng(usa.latitude, usa.longitude))
                    .title("You"));

        }
        gMap.moveCamera(CameraUpdateFactory.newLatLng(usa));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usa, 12));
        gMap.setOnPolylineClickListener(this);
        gMap.setOnMarkerClickListener(this);

    }
    private Bitmap bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.personn);
        //background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        background.setBounds(0, 0, 0, 0);
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        //vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() , vectorDrawable.getIntrinsicHeight() );
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return bitmap;//BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    public String getMapsApiDirectionsUrl() {
        String addresses = "optimize:true&origin="
                + new_order.getPickupLat().toString().concat(",") + new_order.getPickupLong()
                + "&destination=" + new_order.getDropoffLat() + ","
                + new_order.getDropoffLong();
        String sensor = "sensor=false";
        String params = addresses + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params + "&alternatives=true&key=" + getString(R.string.google_maps_api);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
        return url;
    }

    public void addMarkers() {
        if (gMap != null) {
            ;
            Double pickupLat = new_order.getPickupLat();
            Double pickupLng = new_order.getPickupLong();
            Double dropOffLat = new_order.getDropoffLat();
            Double dropOffLng = new_order.getDropoffLong();
            gMap.addMarker(new MarkerOptions().position(new LatLng(pickupLat, pickupLng))
                    .title("First Point"));
            gMap.addMarker(new MarkerOptions().position(new LatLng(dropOffLat, dropOffLng))
                    .title("Second Point"));
            if(myMarker!=null)
            {
                myMarker.remove();
            }
        }
    }

    private void refreshDrivers() {
        MainActivity mainActivity = ((MainActivity) getContext());
        if (mainActivity == null)
            return;
        if (driverList == null)
            driverList = new ArrayList<>();
        if (new_order.getPickupLat() != null) {
            Location pickup = new Location("pickup");
            pickup.setLatitude(new_order.getPickupLat());
            pickup.setLongitude(new_order.getPickupLong());
            mainActivity.getDrivers(pickup);
        }
    }

    private void addSelectedRoute(Polyline polyline) {
        ArrayList<RoutePoints> pointsList = new ArrayList<>();
        for (LatLng latLng : polyline.getPoints()) {
            pointsList.add(new RoutePoints(latLng.latitude, latLng.longitude));
        }
        new_order.setSelectedRoute(pointsList);
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.i("POLYLINE", polyline.toString());
        for (Polyline pline : polyLineList) {
            if (pline.getId().equals(polyline.getId())) {
                pline.setWidth(20);
                pline.setColor(Color.BLUE);
            } else {
                pline.setWidth(10);
                pline.setColor(Color.DKGRAY);
            }
        }
        addSelectedRoute(polyline);
        String[] value = ((String) polyline.getTag()).split("--");
        String distance = value[0].replaceAll("\\D+\\.\\D+", "");
        if (distance.contains("mi"))
            distance = String.valueOf(Double.valueOf(distance.replace("mi", "")) * 1.609344);
        else if (distance.contains(("km")))
            distance = String.valueOf(Double.valueOf(distance.replace("km", "")));
        else if (distance.contains("m"))
            distance = String.valueOf(Double.valueOf(distance.replace("m", "")) / 1000);
        tv_distance.setText("Distance: ".concat(distance).concat(" km"));
        new_order.setPickupLat(polyline.getPoints().get(0).latitude);
        new_order.setPickupLong(polyline.getPoints().get(0).longitude);
        new_order.setDropoffLat(polyline.getPoints().get(polyline.getPoints().size() - 1).latitude);
        new_order.setDropoffLong(polyline.getPoints().get(polyline.getPoints().size() - 1).longitude);
        new_order.setTotal_kms(distance);
        calculateTheCosts();
        ////Toast.makeText(getContext(), "Distance: ".concat(distance).concat(" and Duration: ").concat(value[1]), Toast.LENGTH_SHORT).show();
    }

    private void everyTenSecondsTask() {
        new Timer().schedule(new TenSecondsTask(), 5000, 10000);
    }
    
    public String getRegionName(Context context, double lati, double longi) {
        String regioName = "";
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lati, longi, 1);
            if (addresses.size() > 0) {
                regioName = addresses.get(0).getLocality();
                if (!TextUtils.isEmpty(regioName)) {
                    return regioName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "--NA--";
    }

    private void goCheckDriverStatus(String driverId) {
        DatabaseReference db_driver_order_vault =
                firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        db_driver_order_vault.child(driverId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild(Helper.REF_SINGLE_ORDER)) {
                        Toast.makeText(getContext(), "Driver already has an active order.", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (dataSnapshot.hasChild(Helper.REF_GROUP_ORDER)) {
//                        group_id = dataSnapshot.child(Helper.REF_GROUP_ORDER).getValue().toString();
//                        if (new_order.getShared()) {
//                            new_order.setDriver_id(driverId);
//                            goFetchGroupByID(group_id);
//                            return;
//                        } else {
                            Toast.makeText(getContext(), "Driver already has an active order.", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        return;
                    }else{
                        if(new_order.getShared()) {
                            CREATE_NEW_GROUP = true;
                        }
                    }
                }else{
                    if(new_order.getShared()) {
                        CREATE_NEW_GROUP = true;
                    }
                }
                show_driverDetail(driverId);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void goCheckSharedRideDriver(String driverId, Driver driver) {
        if (isOrderAccepted || firebase_db == null || driverId == null)
            return;
        if(getActivity() == null)
            return;
        if(thereIsActiveOrder)
            return;
        DatabaseReference db_driver_order_vault =
                firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        db_driver_order_vault.child(driverId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                
                if (dataSnapshot.exists()) {
                    if(new_order == null)
                        return;
                    if (new_order.getShared() && dataSnapshot.hasChild(Helper.REF_GROUP_ORDER)) {
                        driverList.add(driver);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addDriverMarker(driver, driverList.indexOf(driver));
                            }
                        });
                    }
                } else {
                    driverList.add(driver);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addDriverMarker(driver, driverList.indexOf(driver));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void sendNotificationToRequestGroupRide(String driverId) {
        if (isOrderAccepted) {
            Toast.makeText(getContext(), "Your order is already accepted by driver", Toast.LENGTH_SHORT).show();
            return;
        } else if (isDriverResponded) {

        }
        DatabaseReference db_ref_user = FirebaseDatabase.getInstance().getReference().child(Helper.REF_USERS);
        db_ref_user.child(driverId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User driver = dataSnapshot.getValue(User.class);
                    if (driver == null)
                        return;
                    String token = driver.getUser_token();
                    NotificationPayload notificationPayload = new NotificationPayload();
                    if(new_order.getShared()) {
                        notificationPayload.setType(Helper.NOTI_TYPE_ORDER_CREATED_FOR_SHARED_RIDE);
                        notificationPayload.setTitle("\"New Passenger Request\"");
                        notificationPayload.setDescription("\"Do you want to accept it\"");
                    }
                    else {
                        notificationPayload.setType(Helper.NOTI_TYPE_ORDER_CREATED);
                        notificationPayload.setTitle("\"Order Created\"");
                        notificationPayload.setDescription("\"Do you want to accept it\"");
                    }
                    notificationPayload.setUser_id("\"" + new_order.getUser_id() + "\"");
                    notificationPayload.setDriver_id("\"" + driver.getUser_id() + "\"");
                    notificationPayload.setOrder_id("\"" + new_order.getOrder_id() + "\"");
                    notificationPayload.setPercentage_left("\"" + -1 + "\"");
                    String str = new Gson().toJson(notificationPayload);
                    try {
                        JSONObject json = new JSONObject(str);
                        new PushNotifictionHelper(getContext()).execute(token, json);
                        generateNewRequest(driverId, new_order.getUser_id(),true);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "Driver not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void waitForDriverResponse(String driverId, String userId) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Waiting for Driver Response");
        progressDialog.setCancelable(false);
        progressDialog.show();
        isTimeout = false;
        new CountDownTimer(30000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                listenForDriverResponse(driverId, userId, progressDialog,this);
            }

            @Override
            public void onFinish() {
                isTimeout = true;
                listenForDriverResponse(driverId, userId, progressDialog,this);
            }
        }.start();
    }
    private void waitForPassengerResponse(String myId, String otherId) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Waiting for Passenger Response");
        progressDialog.setCancelable(false);
        progressDialog.show();
        isTimeoutForPassenger = false;
        new CountDownTimer(30000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                checkForResponseOfPassenger(progressDialog,this,otherId);
            }
            
            @Override
            public void onFinish() {
                isTimeoutForPassenger = true;
                checkForResponseOfPassenger(progressDialog,this,otherId);
            }
        }.start();
    }

    public void showNearbyPassengersForSharedRide(){
        DatabaseReference db_passengers = firebase_db.getReference().child(Helper.REF_PASSENGERS);
        db_passengers.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Passenger passenger = snapshot.getValue(Passenger.class);
                        if(passenger != null && passenger.getFk_user_id() != null && new_order != null){
                            if(!passenger.getFk_user_id().equals(new_order.getUser_id())
                                    && passenger.getInOnline())
                                addMarkersForPassenger(passenger);
                        }
                    }
                }
                goSetLiveListenerForPassengers(db_passengers);
                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
    }
    
    private void goSetLiveListenerForPassengers(DatabaseReference db_passengers) {
        db_passengers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Passenger passenger = dataSnapshot.getValue(Passenger.class);
                if(passenger != null && passenger.getFk_user_id() != null){
                    if(!passenger.getFk_user_id().equals(new_order.getUser_id()))
                        addMarkersForPassenger(passenger);
                }
            }
    
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Passenger passenger = dataSnapshot.getValue(Passenger.class);
                if(passenger != null && passenger.getFk_user_id() != null){
                    if(!passenger.getFk_user_id().equals(new_order.getUser_id()))
                        addMarkersForPassenger(passenger);
                }
            }
    
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        
            }
    
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    public void addMarkersAfterRefresh(){
        if(mNearbyPassengers == null)
            return;
        for(Passenger passenger : mNearbyPassengers) {
            Location ps = new Location("passsenger");
            ps.setLatitude(passenger.getLatitude());
            ps.setLongitude(passenger.getLongitude());
            Location pickup = new Location("pickup");
            pickup.setLatitude(passenger.getLatitude());
            pickup.setLongitude(passenger.getLongitude());
            if (pickup.distanceTo(ps) < group_radius) {
                MarkerOptions markerOptions = new MarkerOptions().title(passenger.getFk_user_id()).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .position(new LatLng(passenger.getLatitude(), passenger.getLongitude())).snippet("Nearby");
                gMap.addMarker(markerOptions);
            }
        }
    }

    private void addMarkersForPassenger(Passenger passenger) {
        if(mNearbyPassengers == null)
            mNearbyPassengers = new ArrayList<>();
        if(nearby_passengers_in_map == null)
            nearby_passengers_in_map = new HashMap<>();
        if(nearby_passengers_in_map.containsKey(passenger.getFk_user_id()))
            return;
        for(Passenger p : mNearbyPassengers){
            if(p.getFk_user_id().equals(passenger.getFk_user_id()))
                return;
        }
        if(passenger.getLatitude() == 0)
            return;
        Location ps = new Location("passsenger");
        ps.setLatitude(passenger.getLatitude());
        ps.setLongitude(passenger.getLongitude());
        Location pickup = new Location("pickup");
        pickup.setLatitude(new_order.getPickupLat());
        pickup.setLongitude(new_order.getPickupLong());
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(pickup.distanceTo(ps) < group_radius) {
                    MarkerOptions markerOptions = new MarkerOptions().title(passenger.getFk_user_id()).icon(

                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            .position(new LatLng(passenger.getLatitude(), passenger.getLongitude())).snippet("Nearby");
                    Marker marker = gMap.addMarker(markerOptions);
                    nearby_passengers_in_map.put(passenger.getFk_user_id(),marker);
                    mNearbyPassengers.add(passenger);
                }
            }
        });

    }
    
    
    private void checkForResponse(ProgressDialog progressDialog, CountDownTimer timer) {
            if (isOrderAccepted && new_order.getShared()) {
            progressDialog.dismiss();
            if(!CREATE_NEW_GROUP) {
                mPassengerList = currentSharedRide.getPassengers();
                mOrderList = currentSharedRide.getOrderIDs();
            }
            calculateTheCosts();
            Toast.makeText(getContext(), "Your request is Accepted", Toast.LENGTH_SHORT).show();
            timer.cancel();
            showRadiusInputField();
        }else if(isOrderAccepted){
            progressDialog.dismiss();
            timer.cancel();
        }
        else if (isDriverResponded ||isTimeout) {
            progressDialog.dismiss();
            new_order.setDriver_id("");
            if(isTimeout)
                Toast.makeText(getContext(), "Other User Doesn't respond.", Toast.LENGTH_SHORT).show();
        else{
            Toast.makeText(getContext(), "Your request is declined", Toast.LENGTH_SHORT).show();
        }
            timer.cancel();
        }
    }
    
    private void checkForResponseOfPassenger(ProgressDialog progressDialog, CountDownTimer timer, String otherId) {
        if(getActivity() == null) {
            timer.cancel();
            return;
        }
        if (isPassengerAccepted) {
            progressDialog.dismiss();
            if(mPassengerList == null)
                mPassengerList =  new HashMap<>();
            addPassenger(otherId);
            if(currentSharedRide == null)
                currentSharedRide = new SharedRide();
            currentSharedRide.setPassengers(mPassengerList);
            if(new_order.getEstimated_cost() == null)
                calculateTheCosts();
            goRemoveRequest(otherId,((MainActivity)getActivity()).getmFirebaseUser().getUid());
            Toast.makeText(getContext(), "Your request is Accepted", Toast.LENGTH_SHORT).show();
            timer.cancel();
        }
        else if (isPassengerResponded || isTimeoutForPassenger) {
            progressDialog.dismiss();
            if(new_order.getEstimated_cost() == null)
                calculateTheCosts();
            if(isTimeout)
                Toast.makeText(getContext(), "Other User Doesn't respond.", Toast.LENGTH_SHORT).show();
            else{
                Toast.makeText(getContext(), "Your request is declined", Toast.LENGTH_SHORT).show();
            }
            goRemoveRequest(otherId,((MainActivity)getActivity()).getmFirebaseUser().getUid());
            timer.cancel();
        }
    }

    private void goFetchGroupByID(String groupId) {
        db_ref_group.child(groupId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentSharedRide = dataSnapshot.getValue(SharedRide.class);
                    if (currentSharedRide != null) {
                        if(isJoiningOtherSharedRide)
                            return;
                        Location starting = new Location("starting");
                        starting.setLatitude(currentSharedRide.getStartingLat());
                        starting.setLongitude(currentSharedRide.getStartingLng());
                        Location myPickup = new Location("myPickup");
                        myPickup.setLatitude(new_order.getPickupLat());
                        myPickup.setLongitude(new_order.getPickupLong());
                        if(starting.distanceTo(myPickup) > currentSharedRide.getRadius_constraint()){
                            Toast.makeText(getContext(), "Sorry, You cannot join this ride.", Toast.LENGTH_SHORT).show();
                            currentSharedRide = null;
                        }else{
//                            show_driverDetail(new_order.getDriver_id());
//                            new_order.setDriver_id(null);
                        }

                    }
                } else {
                    CREATE_NEW_GROUP = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRadiusInputField() {
        ct_vehicles.setVisibility(View.VISIBLE);
        radius_input.setVisibility(View.VISIBLE);
        btn_invites_container.setVisibility(View.GONE);
    }

    private void calculateTheCosts() {
        int passengers_count ;
        if(mOrderList == null)
            passengers_count = 0;
        else
            passengers_count = mOrderList.size();
            updateSharedDiscountedPrice(passengers_count);
    }
    HashMap<String,Integer> userStatus = new HashMap<>();
    private void updateSharedDiscountedPrice(int passenger_count)
    {
        //give new user cost and discount
        total_cost = fareCalculation.getUserDiscountedPrice(passenger_count);
        new_order.setEstimated_cost(String.valueOf(total_cost));
        tv_estimated_cost.setText("Cost: ".concat(String.valueOf(total_cost).concat(" Rs")));
        //Display Cost
        if (layout_cost_detail.getVisibility() == View.GONE) {
            layout_cost_detail.setVisibility(View.VISIBLE);
            if (btn_confirm.getVisibility() == View.VISIBLE)
                btn_confirm.setVisibility(View.GONE);
            txtLocation.setText(new_order.getPickup());
            txtDestination.setText(new_order.getDropoff());
            txt_cost.setText(String.valueOf(total_cost));
        }
        if (passenger_count == 0) {
      // this is because for now only 1 user is riding that is you so we don't need to update other user's cost
            return;
        }
        //update ride passengers discount
        DatabaseReference db_ref_order = firebase_db.getReference(Helper.REF_ORDERS);
        int user_count = 0;
        for (Map.Entry<String, Boolean> entry : mOrderList.entrySet())
        {
            String key = entry.getKey();
            Boolean value = entry.getValue();
            userStatus.put(key,user_count);
                db_ref_order.child(key).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            Order order = dataSnapshot.getValue(Order.class);
                            double cost = 0;
                            if(order.getEstimated_cost() != null)
                             cost = Double.parseDouble(order.getEstimated_cost().toString());
                            total_cost = fareCalculation.getPassengerDiscountedPrice(cost,userStatus.get(order.getOrder_id()));
                            String estimated_cost_final = String.valueOf(total_cost);
                            db_ref_order.child(key).child("estimated_cost").setValue(estimated_cost_final).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    //send push notifications
                                    //PushNotifictionHelper
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            user_count++;
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(driverResponseReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(driverResponseReceiver, new IntentFilter(Helper.BROADCAST_DRIVER_RESPONSE));
    }

    public GoogleMap getgMap() {
        return gMap;
    }

    public void resetUI() {
        
        if(CREATE_NEW_GROUP && !isJoiningOtherSharedRide){
            et_pickup.setText("");
            et_drop_off.setText("");
            if(gMap != null)
                gMap.clear();
            ct_address.setVisibility(View.GONE);
            ct_vehicles.setVisibility(View.GONE);
            car_container.setVisibility(View.GONE);
            btn_invites_container.setVisibility(View.GONE);
            radius_input.setVisibility(View.GONE);
            btn_confirm.setVisibility(View.GONE);
            layout_cost_detail.setVisibility(View.GONE);
            if(nearby_passengers_in_map != null)
                nearby_passengers_in_map.clear();
            nearby_passengers_in_map = null;
            if(mNearbyPassengers != null)
                mNearbyPassengers.clear();
            showNearbyPassengersForSharedRide();
        } else{
            et_pickup.setText("");
            et_drop_off.setText("");
            gMap.clear();
            ct_address.setVisibility(View.GONE);
            radius_input.setVisibility(View.GONE);
            tv_distance.setText("");
            tv_estimated_cost.setText("");
            ct_details.setVisibility(View.GONE);
            new_order = new Order();
            currentSharedRide = null;
            if(mNearbyPassengers != null)
                mNearbyPassengers.clear();
            btn_confirm.setVisibility(View.GONE);
            layout_cost_detail.setVisibility(View.GONE);
            btn_select_vehicle.setVisibility(View.VISIBLE);
    
        }
    }

    public void hideDetails(){
        ct_address.setVisibility(View.GONE);
        ct_details.setVisibility(View.GONE);
        ct_vehicles.setVisibility(View.GONE);
        car_container.setVisibility(View.GONE);
    }
    
    
    private void listenForDriverResponse(String driverId, String userId, ProgressDialog dialog, CountDownTimer timer) {
        db_ref_requests.child(Helper.getConcatenatedID(userId, driverId)).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Requests request = dataSnapshot.getValue(Requests.class);
                    if (request != null) {
                        if (request.getStatus() == Requests.STATUS_ACCEPTED) {
                            isDriverResponded = true;
                            isOrderAccepted = true;
                            new_order.setDriver_id(driverId);
                            goRemoveRequest(request.getReceiverId(),userId);
                            checkForResponse(dialog,timer);
                        } else if (request.getStatus() == Requests.STATUS_REJECTED) {
                            isDriverResponded = true;
                            isOrderAccepted = false;
                            new_order.setDriver_id("");
                            goRemoveRequest(request.getReceiverId(),userId);
                            checkForResponse(dialog,timer);
                        }
                    }
                }
                if(isTimeout)
                    checkForResponse(dialog,timer);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goRemoveRequest(String driverId, String userId) {
        String res_id = Helper.getConcatenatedID(userId, driverId);
        db_ref_requests.child(res_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }

    private void generateNewRequest(String otherId, String userId, boolean forDriver) {
        Requests requests = new Requests(otherId, userId, Requests.STATUS_PENDING,new_order.getShared());
        if(group_id != null)
            requests.setGroup_id(Constants.group_id);
        else {
            showToast("Group id not found");
            return;
        }
        if(TextUtils.isEmpty(new_order.getDriver_id())){
            showToast("Driver is not selected yet");
            return;
        }
        requests.setVehicle_type(new_order.getVehicle_id());
        requests.setDriverId(new_order.getDriver_id());
        requests.setOrder_id(new_order.getOrder_id());
        String res_id = Helper.getConcatenatedID(userId, otherId);
        db_ref_requests.child(res_id).setValue(requests).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                    isOrderAccepted = false;
                    if(forDriver)
                        waitForDriverResponse(otherId, userId);
                    else
                        waitForPassengerResponse(userId,otherId);
                }
            }
        });
    }

    public void saveRadiusInputForGroupRide() {
        if(getActivity() == null)
            return;
        if(TextUtils.isEmpty(radius_input.getText())){
            radius_input.setError("this cannot be empty");
            return;
        }

        group_radius = Integer.parseInt(radius_input.getText().toString());
        if(group_radius < 11){
            showToast("Radius must be at least 10");
            return;
        }
        ct_address.setVisibility(View.VISIBLE);
        ct_vehicles.setVisibility(View.GONE);
        btn_confirm.setVisibility(View.VISIBLE);
        showNearbyPassengersForSharedRide();
    }

    public boolean validateAll() {
        // validate data here.
        if(checkAddresses()){
            showToast("Please enter Address First");
            return false;
        }else if(TextUtils.isEmpty(new_order.getDriver_id())){
            showToast("Please select driver first");
            return false;
        }else if(TextUtils.isEmpty(new_order.getEstimated_cost())){
            showToast("Cost is not set");
            return false;
        }
        else if(new_order.getShared()) {
            if (CREATE_NEW_GROUP) {
                if(group_radius < 10) {
                    showToast("Please enter Radius for Shared Ride");
                    showRadiusInputField();
                    return false;
                }
            }else{
                if(isJoiningOtherSharedRide)
                if(TextUtils.isEmpty(Constants.group_id)){
                    showToast("Group must be selected first");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkAddresses() {
        return TextUtils.isEmpty(et_pickup.getText()) && TextUtils.isEmpty(et_drop_off.getText());
    }

    private void showToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }
    
    public void showPostRadiusInput(Order order) {
        if(getActivity() == null)
            return;
        
        new CountDownTimer(4000, 4000) {
            @Override
            public void onTick(long millisUntilFinished) {
        
            }
    
            @Override
            public void onFinish() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(order.getStatus() == Order.OrderStatusWaiting) {
                            RelativeLayout layout = getActivity().findViewById(R.id.post_radius_container);
                            if(layout != null)
                                layout.setVisibility(View.VISIBLE);
                            EditText editText = getActivity().findViewById(R.id.post_radius_input);
                            if(editText != null)
                                editText.setText(String.valueOf(Constants.group_radius));
                        }
                        else {
                            RelativeLayout layout = getActivity().findViewById(R.id.post_radius_container);
                            if (layout != null)
                                layout.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }.start();
        
        
    }
    
    
    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
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
            if (routes == null)
                return;
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);
                String distance = route_details.get(i + 1).split("--")[0];
                String duration = route_details.get(i + 1).split("--")[1];

                distance = distance.replaceAll("\\D+\\.\\D+", "");
                if (distance.contains("mi"))
                    distance = String.valueOf(Double.valueOf(distance.replace("mi", "")) * 1.609344);
                else if (distance.contains(("km")))
                    distance = String.valueOf(Double.valueOf(distance.replace("km", "")));
                else if (distance.contains("m"))
                    distance = String.valueOf(Double.valueOf(distance.replace("m", "")) / 1000);
    
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                if (i == 0) {
                    polyLineOptions.width(20);
                    polyLineOptions.color(Color.BLUE);
                } else {
                    polyLineOptions.width(10);
                    polyLineOptions.color(Color.DKGRAY);
                }
                polyLineOptions.clickable(true);
                if (polyLineList == null)
                    polyLineList = new ArrayList<Polyline>();
                Polyline polyline = gMap.addPolyline(polyLineOptions);
                polyline.setTag(route_details.get(i + 1));
                if (i == 0) {
                    addSelectedRoute(polyline);
                    ct_details.setVisibility(View.VISIBLE);
                    tv_distance.setText("Distance: ".concat(distance).concat(" km"));
                    new_order.setPickupLat(points.get(0).latitude);
                    new_order.setPickupLong(points.get(0).longitude);
                    new_order.setDropoffLat(points.get(points.size() - 1).latitude);
                    new_order.setDropoffLong(points.get(points.size() - 1).longitude);
                    new_order.setTotal_kms(distance);
                    calculateTheCosts();
                }
                
                polyLineList.add(polyline);
                refreshDrivers();
            }
        }
    }

    private class TenSecondsTask extends TimerTask {
        @Override
        public void run() {
            MY_LOCATION = LocationManagerService.mLastLocation;
//            count_for_region += 10;
//            if(MY_LOCATION != null) {
//                count_for_region = 0;
//                getRegionName(getActivity(), MY_LOCATION.getLatitude(), MY_LOCATION.getLongitude());
//            }

        }
    }

    private void showAddMemberDialog() {
        if(!(IS_RIDE_SCHEDULED && new_order.getShared())){
            Toast.makeText(getContext(), "Ride is not Shared", Toast.LENGTH_SHORT).show();
            return;
        }
        AddMemberDialog memberDialog = new AddMemberDialog(getActivity());
        memberDialog.setCancelable(false);
        memberDialog.show();
    }

    private void addMember(final String username) {
        db_ref_user.orderByChild("email").equalTo(username).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User passenger = dataSnapshot.getValue(User.class);
                    if(passenger == null)
                        return;
                    addPassenger(passenger.getUser_id());
                } else {
                    Toast.makeText(getContext(), "Sorry, No User Found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    
    private void addPassenger(String user_id) {
        if(new_order.getVehicle_id().equals(Helper.VEHICLE_THREE_WHEELER)
                && mPassengerList.size() >= 3)
            return;
        if(!mPassengerList.containsKey(user_id))
            mPassengerList.put(user_id,true);
    }
    
    public class AddMemberDialog extends Dialog {

        Button yes;
        EditText tv_username;
        private Button no;

        public AddMemberDialog(Activity a) {
            super(a);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_add_user);
            yes = findViewById(R.id.btn_dialog_add);
            no = findViewById(R.id.btn_cancel);
            tv_username = findViewById(R.id.et_name);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(tv_username.getText())) {
                        tv_username.setError("Please Enter Email");
                        return;
                    }
                    addMember(tv_username.getText().toString());
//                    dismiss();
                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }
    }
    
    
    /*
    *
    *  Change Requirements; Shared Ride
    *
    * */
    
    public void sendInvitationForGroupRide(String otherId) {
        generateNewRequest(otherId, new_order.getUser_id(),false);
        isPassengerAccepted = false;
        isPassengerResponded = false;
        waitForPassengerResponse(new_order.getUser_id(),otherId);
    }
    
    private void showUserDetails(String title) {
        if(isJoiningOtherSharedRide)
            return;
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        db_ref_user_general.child(title).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    User sender = dataSnapshot.getValue(User.class);
                    if(sender != null)
                        open_profile(sender.getUser_id(), sender.getName(), sender.getUser_image_url(),sender.getPhone(), TO_SHOW_INFO_OF_PASSENGER);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
    }
    
    public void listenerForRequests(String mUserId){
        DatabaseReference db_ref_requests = firebase_db.getReference().child(Helper.REF_REQUESTS);
        db_ref_requests.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    mRequest = dataSnapshot.getValue(Requests.class);
                    if (mRequest != null) {
                        if (mRequest.getReceiverId().equals(mUserId) && mRequest.getStatus() == Requests.STATUS_PENDING) {
                            if(cb_accepting.isChecked())
                                showRequestedInvitation(mRequest);
                        }
                    }
                }
            }
    
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Requests request = dataSnapshot.getValue(Requests.class);
                    if (request != null) {
                        if (request.getReceiverId().equals(mUserId) && request.getStatus() == Requests.STATUS_ACCEPTED) {
                            Toast.makeText(getContext(),"Reqeust Accepted",Toast.LENGTH_LONG).show();
                        } else if (request.getReceiverId().equals(mUserId) && request.getStatus() == Requests.STATUS_REJECTED) {
                            Toast.makeText(getContext(),"Reqeust Rejected",Toast.LENGTH_LONG).show();
                        }if (request.getSenderId().equals(mUserId) && request.getStatus() == Requests.STATUS_ACCEPTED) {
                            isPassengerAccepted = true;
                            isPassengerResponded = true;
                        } else if (request.getSenderId().equals(mUserId) && request.getStatus() == Requests.STATUS_REJECTED) {
                            isPassengerAccepted = false;
                            isPassengerResponded = true;
                        }
                    }
                }
            }
    
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
        
            }
    
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        
            }
    
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
        
            }
        });
    }
    
    private void showRequestedInvitation(Requests request) {
        if(getActivity() == null)
            return;
        if(progressDialog == null)
            progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Incoming Invitation...");
        progressDialog.show();
        db_ref_user_general.child(request.getSenderId()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    User sender = dataSnapshot.getValue(User.class);
                    if(sender != null)
                        open_profile(sender.getUser_id(),sender.getName(),sender.getUser_image_url(),sender.getPhone(), TO_ACCEPT_INVITATION);
                }
            }
        
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            
            }
        });
    }
    
    private void goAcceptInvitation(String user_id) {
        String myid = getActivity() != null ? ((MainActivity)getActivity()).getmFirebaseUser().getUid() : "";
        String req_id = Helper.getConcatenatedID(user_id, myid);
        db_ref_requests.child(req_id).child("status").setValue(Requests.STATUS_ACCEPTED);
        Toast.makeText(getContext(), "Request Accepted", Toast.LENGTH_SHORT).show();
        isJoiningOtherSharedRide = true;
        car_container.setVisibility(View.GONE);
        btn_invites_container.setVisibility(View.GONE);
        btn_add_members.setVisibility(View.GONE);
        group_id = mRequest.getGroup_id();
        if(new_order == null)
            new_order = new Order();
        new_order.setDriver_id(mRequest.getDriverId());
        new_order.setVehicle_id(mRequest.getVehicle_type());
        new_order.setShared(true);
        if(driver_in_map != null)
            for(HashMap.Entry<String,Marker> dm : driver_in_map.entrySet())
                dm.getValue().remove();
        if(nearby_passengers_in_map != null)
            for(HashMap.Entry<String,Marker> dm : nearby_passengers_in_map.entrySet())
                dm.getValue().remove();
        goFetchGroupByID(mRequest.getGroup_id());
        
    }
    
    
    
    public class CustomDialogClass extends Dialog implements
            android.view.View.OnClickListener, OnMapReadyCallback {
        
        public Activity c;
        public Dialog d;
        Button yes;
        MapView miniMap;
        List<LatLng> latLngs = new ArrayList<>();
        public CustomDialogClass(Activity a,Order order) {
            super(a);
            this.c = a;
            for(RoutePoints routePoints : order.getSelectedRoute()){
                this.latLngs.add(new LatLng(routePoints.getLatitude(),routePoints.getLongitude()));
            }
        }
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            setContentView(R.layout.mini_mapview);
            miniMap = findViewById(R.id.map_mini);
            miniMap.getMapAsync(CustomDialogClass.this);
            yes =  findViewById(R.id.btn_close);
            yes.setOnClickListener(this);
        }
        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button:
                    
                    c.finish();
                    break;
                default:
                    break;
            }
            dismiss();
        }
    
    
        @Override
        public void onMapReady(GoogleMap googleMap) {
            PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngs).width(5f).color(Color.GREEN);
            googleMap.addPolyline(polylineOptions);
        }
    }
    
    
}