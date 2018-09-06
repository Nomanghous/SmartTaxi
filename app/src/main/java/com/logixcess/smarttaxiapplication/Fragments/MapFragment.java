package com.logixcess.smarttaxiapplication.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.NotificationPayload;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.Requests;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Services.LocationManagerService;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Constants;
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

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static EditText et_drop_off, et_pickup;
    public static Order new_order;
    public static HashMap<Integer, String> route_details;
    public static HashMap<String, Marker> driver_in_map = new HashMap<>(); // driver_id,
    public static HashMap<String, Integer> driver_list_index = new HashMap<>();
    public static boolean CREATE_NEW_GROUP = false, IS_RIDE_SCHEDULED = false;
    static boolean isOrderAccepted = false, isDriverResponded = false;
    public GoogleMap gMap;
    CheckBox cb_shared, cb_scheduled;
    Firebase firebase_instance;
    ValueEventListener valueEventListener;
    ArrayList<Driver> driverList;
    Location DRIVER_LOCATION;
    Location MY_LOCATION;
    double total_cost = 0;
    Button btn_select_vehicle, btn_hide_details;
    android.app.AlertDialog builder;
    LinearLayout ct_address;
    RelativeLayout ct_vehicles;
    Button btn_confirm;
    LinearLayout layout_cost_detail;
    TextView txtLocation, txtDestination, txt_cost;
    View vehicle1, vehicle2, vehicle3, vehicle4, vehicle5;
    int count_for_region = 0;
    private HashMap<String,Boolean> groupMembersForScheduledRide;


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
    private UserLocationManager gps;
    private GregorianCalendar SELECTED_DATE_TIME;
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private MapView mapFragment;
    private FirebaseUser USER_ME;
    private SharedRide currentSharedRide;
    private DatabaseReference db_ref_group, db_ref_requests;
    public static HashMap<String, Boolean> mPassengerList;
    public static HashMap<String, Boolean> mOrderList;
    private boolean thereIsActiveOrder = false;
    private boolean dialog_already_showing = false;
    private FirebaseDatabase firebase_db;
    private Button btn_add_members;
    private boolean isTimeout = false;

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
        gps = new UserLocationManager(getContext());
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void user_selection_dialog() {
        Context mContext = getActivity();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_shared_user_selection,
                null);
//        View layout = inflater.inflate(R.layout.dialog_shared_user_selection,
//                (ViewGroup) findViewById(R.id.rating_linear_layout));
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
                    builder.dismiss();
                    builder.cancel();
                    builder = null;
                }

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        firebase_db = FirebaseDatabase.getInstance();
        USER_ME = FirebaseAuth.getInstance().getCurrentUser();
        db_ref_user = firebase_db.getReference().child(Helper.REF_PASSENGERS);
        db_ref_group = firebase_db.getReference().child(Helper.REF_GROUPS);
        db_ref_requests = firebase_db.getReference().child(Helper.REF_REQUESTS);
        driverList = new ArrayList<>();

        LinearLayout layout_vehicle1, layout_vehicle2, layout_vehicle3, layout_vehicle4, layout_vehicle5;
        mapFragment = view.findViewById(R.id.map);
        btn_add_members = view.findViewById(R.id.btn_add_members);
        vehicle1 = view.findViewById(R.id.vehicle1);
        vehicle2 = view.findViewById(R.id.vehicle2);
        vehicle3 = view.findViewById(R.id.vehicle3);
        vehicle4 = view.findViewById(R.id.vehicle4);
        vehicle5 = view.findViewById(R.id.vehicle5);

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
        ct_address = view.findViewById(R.id.ct_address);// .setVisibility(View.VISIBLE);
        ct_vehicles = view.findViewById(R.id.ct_vehicles);//.setVisibility(View.GONE);
        btn_confirm = view.findViewById(R.id.btn_confirm);//.setVisibility(View.VISIBLE);
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
//                if(cb_shared2.isChecked())
//                {
//                    user_selection_dialog();
//                }

                if (new_order != null) {
                    new_order.setVehicle_id("Chingchi");
                    new_order.setShared(cb_shared.isChecked());
                }
//                MainActivity mainActivity = ((MainActivity)getContext());
//                if(mainActivity == null)
//                    return;
//                if(driverList == null)
//                    driverList = new ArrayList<>();
//                if(new_order.getPickupLat() != null){
//                    Location pickup = new Location("pickup");
//                    pickup.setLatitude(new_order.getPickupLat());
//                    pickup.setLongitude(new_order.getPickupLong());
//                    mainActivity.getDrivers(pickup);
//                }
                ct_address.setVisibility(View.VISIBLE);
                ct_vehicles.setVisibility(View.GONE);
                btn_confirm.setVisibility(View.VISIBLE);
                refreshDrivers();
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
        everyTenSecondsTask();
        return view;
    }

    public void getDriverList(List<Driver> drivers) {
        if (new_order == null)
            return;
        for (Driver driver : drivers)
            goCheckSharedRideDriver(driver.getFk_user_id(), driver);

    }

    public void addDriverMarker(Driver driver1, int index) {
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

    public double check_cost(int shared_user_status, double base_fair_per_km) {
        if (shared_user_status == 1)//primary
        {
            total_cost = (base_fair_per_km / 100.0f) * 20; //give 20% discount
        } else if (shared_user_status == 2) // secondary
        {
            total_cost = (base_fair_per_km / 100.0f) * 10; //give 10% discount
        } else if (shared_user_status == 3) //tertiary
        {
            total_cost = (base_fair_per_km / 100.0f) * 5; //give
        }
        return total_cost;
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
    public void show_driverDetail(String driverid) {

        if (dialog_already_showing)
            return;
        String driverId = driverid;
        final CharSequence[] items = {"SELECT", "OPEN PROFILE",
                "CANCEL"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Driver Detail");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = PermissionHandler.checkPermission(getActivity());
                if (items[item].equals("SELECT")) {

//                    if (new_order.getShared()) {
                        new_order.setDriver_id(driverId);
                        sendNotificationToRequestGroupRide(driverId);
//                    } else {//non shared

//                    }
                    dialog_already_showing = false;
                    dialog.dismiss();
                    //check_cost(0,0.0);
                } else if (items[item].equals("OPEN PROFILE")) {

                } else if (items[item].equals("CANCEL")) {
                    dialog.dismiss();
                    dialog_already_showing = false;
                }
            }
        });
        builder.show();
        dialog_already_showing = true;
    }

    public void checkRidePassengers(String region_name, String driver_id) {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    SharedRide sharedRide = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().contains(driver_id)) {
                            sharedRide = snapshot.getValue(SharedRide.class);
                            int passengers_count = sharedRide.getPassengers().size();
                            double cost = Constants.BASE_FAIR_PER_KM * Double.parseDouble(new_order.getTotal_kms());
                            if (passengers_count == 0)
                                total_cost = (cost / 100.0f) * 20; //give 20% discount
                            else if (passengers_count == 1)
                                total_cost = (cost / 100.0f) * 10; //give 10% discount
                            else if (passengers_count > 2)
                                total_cost = (cost / 100.0f) * 5; //give

                            new_order.setEstimated_cost(String.valueOf(total_cost));
                            //Display Cost
                            if (layout_cost_detail.getVisibility() == View.GONE) {
                                layout_cost_detail.setVisibility(View.VISIBLE);
                                if (btn_confirm.getVisibility() == View.VISIBLE)
                                    btn_confirm.setVisibility(View.GONE);
                                txtLocation.setText(new_order.getPickup());
                                txtDestination.setText(new_order.getDropoff());
                                txt_cost.setText(String.valueOf(total_cost));
                            }
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "No Rides are going nearby, we will create your new Ride.", Toast.LENGTH_SHORT).show();
                    double total_cost = Constants.BASE_FAIR_PER_KM * Double.parseDouble(new_order.getTotal_kms());
                    if (layout_cost_detail.getVisibility() == View.GONE) {
                        layout_cost_detail.setVisibility(View.VISIBLE);
                        txtLocation.setText(new_order.getPickup());
                        txtDestination.setText(new_order.getDropoff());
                        txt_cost.setText(String.valueOf(total_cost));
                    }
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        firebase_instance.child("Group").orderByChild("region_name").equalTo(region_name).addListenerForSingleValueEvent(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.
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
        String driverId = (String) marker.getTag();
        if (driverId != null && !driverId.isEmpty()) {
            // do whatever with driver id.
            goCheckDriverStatus(driverId);

            return true;
        } else
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
                Constants.BASE_FAIR_PER_KM = 50;//car
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
                Constants.BASE_FAIR_PER_KM = 30;//option mini
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
                Constants.BASE_FAIR_PER_KM = 20;//option nano
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
                Constants.BASE_FAIR_PER_KM = 60;//option vip
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
                Constants.BASE_FAIR_PER_KM = 30;//option three wheeler
                break;

        }
    }

    public boolean getThereIsActiveOrder() {
        return this.thereIsActiveOrder;
    }

    public void setThereIsActiveOrder(boolean thereIsActiveOrder) {
        this.thereIsActiveOrder = thereIsActiveOrder;
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        Double latitude = 7.8731;
        Double longitude = 80.7718;
        LatLng usa = new LatLng(latitude, longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(usa));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(usa, 10));
        gMap.setOnPolylineClickListener(this);
        gMap.setOnMarkerClickListener(this);

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
        new_order.setSELECTED_ROUTE(pointsList);
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
        Toast.makeText(getContext(), "Distance: ".concat(value[0]).concat(" and Duration: ").concat(value[1]), Toast.LENGTH_SHORT).show();
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
                        group_id = dataSnapshot.child(Helper.REF_GROUP_ORDER).getValue().toString();
                        if (new_order.getShared()) {
                            new_order.setDriver_id(driverId);
                            goFetchGroupByID(group_id);
                            return;
                        } else {
                            Toast.makeText(getContext(), "Driver already has an active order.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }else{
                        if(new_order.getShared()) {
                            CREATE_NEW_GROUP = true;
                            showRadiusInputField();
                        }
                    }
                }else{
                    if(new_order.getShared()) {
                        CREATE_NEW_GROUP = true;
                        showRadiusInputField();
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
        if (isOrderAccepted && firebase_db == null)
            return;
        DatabaseReference db_driver_order_vault =
                firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        db_driver_order_vault.child(driverId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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
                    notificationPayload.setType(Helper.NOTI_TYPE_ORDER_CREATED_FOR_SHARED_RIDE);
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
                        generateNewRequest(driverId, new_order.getUser_id());

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
        }else if(isOrderAccepted){
            progressDialog.dismiss();

            double total_cost = Constants.BASE_FAIR_PER_KM * Double.parseDouble(new_order.getTotal_kms());
            if (layout_cost_detail.getVisibility() == View.GONE) {
                if (btn_confirm.getVisibility() == View.VISIBLE)
                    btn_confirm.setVisibility(View.GONE);
                layout_cost_detail.setVisibility(View.VISIBLE);
                txtLocation.setText("Location : " + new_order.getPickup());
                txtDestination.setText("Destination : " + new_order.getDropoff());
                txt_cost.setText(String.valueOf(total_cost));
                new_order.setEstimated_cost(String.valueOf(total_cost));
            }
            timer.cancel();

        }
        else if (isDriverResponded ||isTimeout) {
            progressDialog.dismiss();
            if(isTimeout)
                Toast.makeText(getContext(), "Driver didn't respond.", Toast.LENGTH_SHORT).show();
            else{
                Toast.makeText(getContext(), "Your request is declined", Toast.LENGTH_SHORT).show();
            }
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

                        Location starting = new Location("starting");
                        starting.setLatitude(currentSharedRide.getStartingLat());
                        starting.setLongitude(currentSharedRide.getStartingLng());
                        Location myPickup = new Location("myPickup");
                        myPickup.setLatitude(currentSharedRide.getStartingLat());
                        myPickup.setLongitude(currentSharedRide.getStartingLng());
                        if(starting.distanceTo(myPickup) > currentSharedRide.getRadius_constraint()){
                            Toast.makeText(getContext(), "Sorry, You cannot join this ride.", Toast.LENGTH_SHORT).show();
                            currentSharedRide = null;
                        }else{
                            show_driverDetail(new_order.getDriver_id());
                            new_order.setDriver_id(null);
                        }

                    }
                } else {
                    CREATE_NEW_GROUP = true;
                    showRadiusInputField();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRadiusInputField() {
        getActivity().findViewById(R.id.radius_input_container).setVisibility(View.VISIBLE);
    }

    private void calculateTheCosts() {
        int passengers_count = mOrderList.size();
        if(passengers_count>1)
        updateOtherPassengerCosts(passengers_count+1);// passenger count new passenger already included in it
        double cost = Constants.BASE_FAIR_PER_KM * Double.parseDouble(new_order.getTotal_kms());
        if (passengers_count == 0)
            total_cost = (cost / 100.0f) * 20; //give 20% discount
        else if (passengers_count == 1)
            total_cost = (cost / 100.0f) * 10; //give 10% discount
        else if (passengers_count > 2)
            total_cost = (cost / 100.0f) * 5; //give

        new_order.setEstimated_cost(String.valueOf(total_cost));
        //Display Cost
        if (layout_cost_detail.getVisibility() == View.GONE) {
            layout_cost_detail.setVisibility(View.VISIBLE);
            if (btn_confirm.getVisibility() == View.VISIBLE)
                btn_confirm.setVisibility(View.GONE);
            txtLocation.setText(new_order.getPickup());
            txtDestination.setText(new_order.getDropoff());
            txt_cost.setText(String.valueOf(total_cost));
        }
    }

    private void updateOtherPassengerCosts(int passenger_count)
    {

//        for (Map.Entry<String, Boolean> entry : mPassengerList.entrySet()) {
//            String key = entry.getKey();
//            Object value = entry.getValue();
//            // you code here
//        }
        DatabaseReference db_ref_order = firebase_db.getReference(Helper.REF_ORDERS);

        for (Map.Entry<String, Boolean> entry : mOrderList.entrySet())
        {
            String key = entry.getKey();
            Boolean value = (Boolean)entry.getValue();

            if(value)
            {
                db_ref_order.child(key).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            Order order = dataSnapshot.getValue(Order.class);
                            order.getEstimated_cost();
                            db_ref_order.child(key).child("estimated_cost").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            // you code here
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
        et_pickup.setText("");
        et_drop_off.setText("");
        gMap.clear();
        new_order = null;
        currentSharedRide = null;
        btn_confirm.setVisibility(View.GONE);
        layout_cost_detail.setVisibility(View.GONE);
        btn_select_vehicle.setVisibility(View.VISIBLE);

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
                            new_order.setDriver_id(request.getDriverId());
                            goRemoveRequest(request.getDriverId(),userId);
                            checkForResponse(dialog,timer);
                        } else if (request.getStatus() == Requests.STATUS_REJECTED) {
                            isDriverResponded = true;
                            isOrderAccepted = false;
                            goRemoveRequest(request.getDriverId(),userId);
                            checkForResponse(dialog,timer);
                        }
                    }
                }
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

    private void generateNewRequest(String driverId, String userId) {
        Requests requests = new Requests(driverId, userId, Requests.STATUS_PENDING);
        String res_id = Helper.getConcatenatedID(userId, driverId);
        db_ref_requests.child(res_id).setValue(requests).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                    isOrderAccepted = false;
                    waitForDriverResponse(driverId, userId);
                }
            }
        });
    }

    public void saveRadiusInputForGroupRide() {
        if(getActivity() == null)
            return;
        EditText editText = getActivity().findViewById(R.id.radius_input);
        RelativeLayout container = getActivity().findViewById(R.id.radius_input_container);
        if(TextUtils.isEmpty(editText.getText())){
            editText.setError("this cannot be empty");
            return;
        }

        group_radius = Integer.parseInt(editText.getText().toString());
        if(group_radius < 11){
            showToast("Radius must be at least 10");
            return;
        }
        currentSharedRide.setRadius_constraint(group_radius);
        container.setVisibility(View.GONE);
    }

    public boolean validateAll() {
        // validate data here.
        if(checkAddresses()){
            showToast("Please enter Address First");
            return false;
        }else if(TextUtils.isEmpty(new_order.getDriver_id())){
            showToast("Please select driver first");
            return false;
        }
        else if(new_order.getShared()) {
            if (CREATE_NEW_GROUP) {
                if(currentSharedRide.getRadius_constraint() < 10) {
                    showToast("Please enter Radius for Shared Ride");
                    showRadiusInputField();
                    return false;
                }else if(MY_LOCATION == null){
                    showToast("Location Service is not Running");
                    return false;
                }
                currentSharedRide.setStartingLat(MY_LOCATION.getLatitude());
                currentSharedRide.setStartingLng(MY_LOCATION.getLongitude());
            }else{
                if(TextUtils.isEmpty(Constants.group_id)){
                    showToast("Group must be selected first");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkAddresses() {
        return !TextUtils.isEmpty(et_pickup.getText()) && !TextUtils.isEmpty(et_drop_off.getText());
    }

    private void showToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
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
                    distance = String.valueOf(Double.valueOf(distance.replace("km", "")) * 1.609344);
                else if (distance.contains("m"))
                    distance = String.valueOf(Double.valueOf(distance.replace("m", "")) * 1.609344);
                new_order.setTotal_kms(distance);
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
                    if(!mPassengerList.containsKey(passenger.getUser_id()))
                        mPassengerList.put(passenger.getUser_id(),true);
                } else {
                    Toast.makeText(getContext(), "Sorry, No User Found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
}