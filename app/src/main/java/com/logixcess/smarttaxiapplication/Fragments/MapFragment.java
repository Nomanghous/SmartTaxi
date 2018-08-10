package com.logixcess.smarttaxiapplication.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.logixcess.smarttaxiapplication.Activities.OrderDetailsActivity;
import com.logixcess.smarttaxiapplication.Activities.Register_Next_Step;
import com.logixcess.smarttaxiapplication.DriverModule.MapsActivity;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.Models.RoutePoints;
import com.logixcess.smarttaxiapplication.Models.SharedRide;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Constants;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.HttpConnection;
import com.logixcess.smarttaxiapplication.Utils.PathJsonParser;
import com.logixcess.smarttaxiapplication.Utils.PermissionHandler;
import com.logixcess.smarttaxiapplication.Utils.PolyUtil;
import com.logixcess.smarttaxiapplication.Utils.UserLocationManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.logixcess.smarttaxiapplication.Utils.Constants.SELECTED_RADIUS;
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener,View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private DatabaseReference db_ref_user;
    public static GoogleMap gMap;
    public static EditText et_drop_off,et_pickup;
    public static Order new_order;
    CheckBox cb_shared;
    public static HashMap<Integer,String> route_details;
    public static HashMap<String,Marker> driver_in_map = new HashMap<>(); // driver_id,
    private ArrayList<Polyline> polyLineList;
    private UserLocationManager gps;
    private GregorianCalendar SELECTED_DATE_TIME;
    Firebase firebase_instance;
    ValueEventListener valueEventListener;
    ArrayList<Driver> driverList;
    Location DRIVER_LOCATION;
    Location MY_LOCATION;
    double total_cost = 0 ;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private MapView mapFragment;
    Button btn_select_vehicle,btn_hide_details;
    private FirebaseUser USER_ME;

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
    android.app.AlertDialog builder;
    public void user_selection_dialog()
    {
        Context mContext = getActivity();
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
//        View layout = inflater.inflate(R.layout.dialog_shared_user_selection,
//                (ViewGroup) findViewById(R.id.rating_linear_layout));
        EditText edt_user_numbers = layout.findViewById(R.id.edt_user_numbers);
        Button btn_done = layout.findViewById(R.id.btn_done);
        builder = new android.app.AlertDialog.Builder(mContext).create();
        builder.setView(layout);
        builder.show();
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(TextUtils.isEmpty(edt_user_numbers.getText().toString()))
                {
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Please select number of users first !", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                else {
                    builder.dismiss();
                }

            }
        });
    }
    LinearLayout ct_address;
    RelativeLayout ct_vehicles;
    Button btn_confirm;
    CheckBox cb_shared2;
    View layout;
    private FirebaseDatabase firebase_db;
    LinearLayout layout_cost_detail;
    TextView txtLocation,txtDestination,txt_cost;
    View vehicle1,vehicle2,vehicle3,vehicle4,vehicle5;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        firebase_db = FirebaseDatabase.getInstance();
        USER_ME = FirebaseAuth.getInstance().getCurrentUser();
        db_ref_user = firebase_db.getReference().child(Helper.REF_PASSENGERS);
        driverList = new ArrayList<>();
         layout = inflater.inflate(R.layout.dialog_shared_user_selection,
                (ViewGroup) view.findViewById(R.id.rating_linear_layout));
    LinearLayout layout_vehicle1,layout_vehicle2,layout_vehicle3,layout_vehicle4,layout_vehicle5;
        mapFragment = view.findViewById(R.id.map);

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
        cb_shared2 = view.findViewById(R.id.cb_shared);
        ct_address = view.findViewById(R.id.ct_address);// .setVisibility(View.VISIBLE);
        ct_vehicles = view.findViewById(R.id.ct_vehicles);//.setVisibility(View.GONE);
        btn_confirm = view.findViewById(R.id.btn_confirm);//.setVisibility(View.VISIBLE);
        btn_select_vehicle = view.findViewById(R.id.btn_select_vehicle);
        btn_select_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cb_shared2.isChecked())
                {
                    user_selection_dialog();
                }
                if(new_order != null)
                    new_order.setVehicle_id("Chingchi");
                getDriverList();
                ct_address.setVisibility(View.VISIBLE);
                ct_vehicles.setVisibility(View.GONE);
                btn_confirm.setVisibility(View.VISIBLE);
            }
        });
        mapFragment.onCreate(savedInstanceState);
        mapFragment.getMapAsync(this);
        new_order = new Order();
        et_pickup = view.findViewById(R.id.et_pickup);
        et_drop_off = view.findViewById(R.id.et_dropoff);
        cb_shared = view.findViewById(R.id.cb_shared);
        new_order.setShared(false);


        btn_hide_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_cost_detail.setVisibility(View.GONE);
                if(btn_confirm.getVisibility()==View.GONE)
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
        everyTenSecondsTask();
        return  view;
    }
    private boolean checkWithinRadius(LatLng latLng,Location mine) {
        DRIVER_LOCATION = new Location("driver");
        DRIVER_LOCATION.setLatitude(latLng.latitude);
        DRIVER_LOCATION.setLongitude(latLng.longitude);


        return mine.distanceTo(DRIVER_LOCATION) < SELECTED_RADIUS;//distance in meters

    }
    public void getDriverList()
    {
        MainActivity mainActivity = ((MainActivity)getContext());
        if(mainActivity == null)
            return;
        for(Driver driver : mainActivity.getDrivers())
        {
            driverList.add(driver);
            addDriverMarker(driver);
        }
        /*valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())//check if user exist
                {
                    Driver driver = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        //PolyUtil.isLocationOnEdge();
                        driver = snapshot.getValue(Driver.class);

                        LatLng driver_location = new LatLng(driver.getLatitude(), driver.getLongitude());
                        if(new_order != null && new_order.getShared())
                        {
                            for (Polyline polyline : polyLineList) {
                                //polyline.
                                //if (PolyUtil.isLocationOnEdge(driver_location, polyline.getPoints(), false)) {
                                //if(PolyUtil.isLocationOnPath(driver_location, polyline.getPoints(), true))
                                if(PolyUtil.containsLocation(driver_location, polyline.getPoints(), true)
                                        || checkWithinRadius(driver_location, ((MainActivity)getContext()).getCurrentLocation()))
                                {
                                    //means that driver location is inside that path/route
                                    driverList.add(driver);
                                    addDriverMarker(driver);
//                                    Boolean within_radius = checkWithinRadius(driver_location);
//                                    if (within_radius) {
//                                        driverList.add(driver);
//                                        addDriverMarker(driver);
//                                    }
                                } else {

                                }
                            }
                        }else{
                            Boolean within_radius = checkWithinRadius(driver_location,((MainActivity)getContext()).getCurrentLocation());
                            if(within_radius){
                                driverList.add(driver);
                                addDriverMarker(driver);
                            }

                        }
                    }

                }
                else
                {
                    Toast.makeText(getActivity(),"No Data Found !",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        firebase_instance.child(Helper.REF_DRIVERS).orderByChild("inOnline").equalTo(true).addValueEventListener(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.
        */
    }
    public void addDriverMarker(Driver driver1)
    {
        //now update the routes and remove markers if already present in it.
        LatLng driverLatLng = new LatLng(driver1.getLatitude(), driver1.getLongitude());
            if(driver_in_map.containsKey(driver1.getFk_user_id()))
            {
                Marker marker = driver_in_map.get(driver1.getFk_user_id());
                marker.setPosition(driverLatLng);
            }
            else
            {
                if (gMap != null) {
                    Marker marker = gMap.addMarker(new MarkerOptions().position(driverLatLng)
                            .title("Driver: ".concat(driver1.getFk_user_id())));

                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    marker.setTag(driver1.getFk_user_id());
                    driver_in_map.put(driver1.getFk_user_id(),marker);
                }

            }

    }
    public double check_cost(int shared_user_status,double base_fair_per_km)
    {
        if(shared_user_status == 1)//primary
        {
            total_cost = (base_fair_per_km / 100.0f) * 20; //give 20% discount
        }
        else if(shared_user_status == 2) // secondary
        {
            total_cost = (base_fair_per_km / 100.0f) * 10; //give 10% discount
        }
        else if(shared_user_status == 3) //tertiary
        {
            total_cost = (base_fair_per_km / 100.0f) * 5; //give
        }
        return total_cost;
    }
    public void  show_driverDetail(String driverId)
    {
        final CharSequence[] items = { "SELECT", "OPEN PROFILE",
                "CANCEL" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Driver Detail");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= PermissionHandler.checkPermission(getActivity());
                if (items[item].equals("SELECT"))
                {
                    new_order.setDriver_id(driverId);
                    if(new_order.getShared())
                    checkRidePassengers(Constants.region_name,driverId);
                    else {//non shared
                    double total_cost = Constants.BASE_FAIR_PER_KM * Double.parseDouble(new_order.getTotal_kms());
                        //Display Cost
                        if(layout_cost_detail.getVisibility() == View.GONE)
                        {
                            if(btn_confirm.getVisibility()==View.VISIBLE)
                                btn_confirm.setVisibility(View.GONE);
                            layout_cost_detail.setVisibility(View.VISIBLE);
                            txtLocation.setText("Location : "+new_order.getPickup());
                            txtDestination.setText("Destination : "+new_order.getDropoff());
                            txt_cost.setText(String.valueOf(total_cost));
                            new_order.setEstimated_cost(String.valueOf(total_cost));
                        }
                    }
                    //check_cost(0,0.0);
                }
                else if (items[item].equals("OPEN PROFILE"))
                {
                }
                else if (items[item].equals("CANCEL")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    public void checkRidePassengers(String region_name,String driver_id) {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    SharedRide sharedRide = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        if(snapshot.getKey().contains(driver_id))
                        {
                            sharedRide = snapshot.getValue(SharedRide.class);
                            int passengers_count = sharedRide.getPassengers().size();
                            double cost = Constants.BASE_FAIR_PER_KM * Double.parseDouble(new_order.getTotal_kms());
                            if(passengers_count == 0)
                                total_cost = (cost / 100.0f) * 20; //give 20% discount
                            else if(passengers_count == 1)
                                total_cost = (cost / 100.0f) * 10; //give 10% discount
                            else if(passengers_count > 2)
                                total_cost = (cost / 100.0f) * 5; //give
                            //Display Cost
                            if(layout_cost_detail.getVisibility() == View.GONE)
                            {
                                layout_cost_detail.setVisibility(View.VISIBLE);
                                txtLocation.setText(new_order.getPickup());
                                txtDestination.setText(new_order.getDropoff());
                                txt_cost.setText(String.valueOf(total_cost));
                            }
                        }
                    }
                }
                else
                {
                    Toast.makeText(getActivity(),"No Rides are going nearby, we will create your new Ride.",Toast.LENGTH_SHORT).show();
                    double total_cost = Constants.BASE_FAIR_PER_KM * Double.parseDouble(new_order.getTotal_kms());
                    if(layout_cost_detail.getVisibility() == View.GONE)
                    {
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
        if(mapFragment != null)
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
        if(driverId != null &&  !driverId.isEmpty()){
            // do whatever with driver id.
            goCheckDriverStatus(driverId);

            return  true;
        }else
            return false;
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.layout_vehicle1:
                if(vehicle1.getVisibility()==View.GONE)
                    vehicle1.setVisibility(View.VISIBLE);
                if(vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if(vehicle3.getVisibility() == View.VISIBLE)
                vehicle3.setVisibility(View.GONE);
                if(vehicle4.getVisibility() == View.VISIBLE)
                vehicle4.setVisibility(View.GONE);
                if(vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = 50;//car
            break;
            case R.id.layout_vehicle2:
                if(vehicle2.getVisibility()==View.GONE)
                    vehicle2.setVisibility(View.VISIBLE);
                if(vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                if(vehicle3.getVisibility() == View.VISIBLE)
                    vehicle3.setVisibility(View.GONE);
                if(vehicle4.getVisibility() == View.VISIBLE)
                    vehicle4.setVisibility(View.GONE);
                if(vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = 30;//option mini
                break;
            case R.id.layout_vehicle3:
                if(vehicle3.getVisibility()==View.GONE)
                    vehicle3.setVisibility(View.VISIBLE);
                if(vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if(vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                if(vehicle4.getVisibility() == View.VISIBLE)
                    vehicle4.setVisibility(View.GONE);
                if(vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = 20;//option nano
                break;
            case R.id.layout_vehicle4:
                if(vehicle4.getVisibility()==View.GONE)
                    vehicle4.setVisibility(View.VISIBLE);
                if(vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if(vehicle3.getVisibility() == View.VISIBLE)
                    vehicle3.setVisibility(View.GONE);
                if(vehicle5.getVisibility() == View.VISIBLE)
                    vehicle5.setVisibility(View.GONE);
                if(vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = 60;//option vip
                break;
            case R.id.layout_vehicle5:
                if(vehicle5.getVisibility()==View.GONE)
                    vehicle5.setVisibility(View.VISIBLE);
                if(vehicle2.getVisibility() == View.VISIBLE)
                    vehicle2.setVisibility(View.GONE);
                if(vehicle3.getVisibility() == View.VISIBLE)
                    vehicle3.setVisibility(View.GONE);
                if(vehicle4.getVisibility() == View.VISIBLE)
                    vehicle4.setVisibility(View.GONE);
                if(vehicle1.getVisibility() == View.VISIBLE)
                    vehicle1.setVisibility(View.GONE);
                Constants.BASE_FAIR_PER_KM = 30;//option three wheeler
                break;

        }
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
                + "&destination=" + new_order.getDropoffLat()+ ","
                + new_order.getDropoffLong();
        String sensor = "sensor=false";
        String params = addresses + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params +"&alternatives=true&key="+ getString(R.string.google_maps_api);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
        return url;
    }

    public void addMarkers() {
        if (gMap != null) {;
            Double pickupLat = new_order.getPickupLat();
            Double pickupLng = new_order.getPickupLong();
            Double dropOffLat = new_order.getDropoffLat();
            Double dropOffLng =new_order.getDropoffLong();
            gMap.addMarker(new MarkerOptions().position(new LatLng(pickupLat,pickupLng))
                    .title("First Point"));
            gMap.addMarker(new MarkerOptions().position(new LatLng(dropOffLat,dropOffLng))
                    .title("Second Point"));
        }
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
            }
            catch (Exception e) {
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

                distance = distance.replaceAll("\\D+\\.\\D+","");
                if(distance.contains("mi"))
                    distance = String.valueOf(Double.valueOf(distance.replace("mi","")) * 1.609344);
                else if( distance.contains(("km")))
                    distance = String.valueOf(Double.valueOf(distance.replace("km","")) * 1.609344);
                else if(distance.contains("m"))
                    distance = String.valueOf(Double.valueOf(distance.replace("m","")) * 1.609344);
                new_order.setTotal_kms(distance);
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
                if(i == 0){
                    addSelectedRoute(polyline);
                }
                polyLineList.add(polyline);
                getDriverList();
            }
        }
    }

    private void addSelectedRoute(Polyline polyline) {
        ArrayList<RoutePoints> pointsList = new ArrayList<>();
        for(LatLng latLng : polyline.getPoints()){
            pointsList.add(new RoutePoints(latLng.latitude,latLng.longitude));
        }
        new_order.setSELECTED_ROUTE(pointsList);
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
        addSelectedRoute(polyline);
        String[] value = ((String) polyline.getTag()).split("--");
        Toast.makeText(getContext(), "Distance: ".concat(value[0]).concat(" and Duration: ").concat(value[1]), Toast.LENGTH_SHORT).show();
    }
    private void everyTenSecondsTask()
    {
        new Timer().schedule(new TenSecondsTask(),5000,10000);
    }
    int count_for_region = 0;
    private class TenSecondsTask extends TimerTask
    {
        @Override
        public void run() {
            // markers driver clear <driverId, marker>
            // place markers again
            count_for_region ++;
            if(count_for_region == 60)
            {
                count_for_region = 0;
                if(MY_LOCATION != null)
                    getRegionName(getActivity(),MY_LOCATION.getLatitude(),MY_LOCATION.getLongitude());
            }
        }
    }
    public void getRegionName(Context context, double lati, double longi) {
        String regioName = "";
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(lati, longi, 1);
            if (addresses.size() > 0) {
                regioName = addresses.get(0).getLocality();
                if(!TextUtils.isEmpty(regioName))
                {
                    String region_name = "region_name";
                    Constants.region_name = regioName;
                    db_ref_user.child(USER_ME.getUid()).child(region_name).setValue(regioName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goCheckDriverStatus(String driverId){
        DatabaseReference db_driver_order_vault =
                firebase_db.getReference().child(Helper.REF_ORDER_TO_DRIVER);
        db_driver_order_vault.child(driverId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                   if(dataSnapshot.hasChild(Helper.REF_SINGLE_ORDER) || dataSnapshot.hasChild(Helper.REF_GROUP_ORDER)){
                       Toast.makeText(getContext(), "Driver already has an active order.", Toast.LENGTH_SHORT).show();
                       return;
                   }else if( dataSnapshot.hasChild(Helper.REF_GROUP_ORDER)){
                       if(new_order.getShared()){
                           show_driverDetail(driverId);
                       }else {
                           Toast.makeText(getContext(), "Driver already has an active order.", Toast.LENGTH_SHORT).show();
                           return;
                       }
                   }
                }
                show_driverDetail(driverId);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}