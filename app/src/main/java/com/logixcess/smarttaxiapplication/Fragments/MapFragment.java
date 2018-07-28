package com.logixcess.smarttaxiapplication.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.logixcess.smarttaxiapplication.Activities.OrderDetailsActivity;
import com.logixcess.smarttaxiapplication.DriverModule.MapsActivity;
import com.logixcess.smarttaxiapplication.MainActivity;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;
import com.logixcess.smarttaxiapplication.Utils.Helper;
import com.logixcess.smarttaxiapplication.Utils.HttpConnection;
import com.logixcess.smarttaxiapplication.Utils.PathJsonParser;
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

import static com.logixcess.smarttaxiapplication.Utils.Constants.SELECTED_RADIUS;
import static com.logixcess.smarttaxiapplication.Utils.Constants.USER_CURRENT_LOCATION;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static GoogleMap gMap;
    public static EditText et_drop_off,et_pickup;
    public static Order new_order;
    CheckBox cb_shared;
    public static HashMap<Integer,String> route_details;
    public static HashMap<String,Marker> driver_in_map; // driver_id,
    private ArrayList<Polyline> polyLineList;
    private UserLocationManager gps;
    private GregorianCalendar SELECTED_DATE_TIME;
    Firebase firebase_instance;
    ValueEventListener valueEventListener;
    ArrayList<Driver> driverList;
    Location DRIVER_LOCATION;
    Location MY_LOCATION;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private MapView mapFragment;


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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        driverList = new ArrayList<>();
        mapFragment = view.findViewById(R.id.map);
        mapFragment.onCreate(savedInstanceState);
        mapFragment.getMapAsync(this);
        new_order = new Order();
        et_pickup = view.findViewById(R.id.et_pickup);
        et_drop_off = view.findViewById(R.id.et_dropoff);
        cb_shared = view.findViewById(R.id.cb_shared);
        new_order.setShared(false);
        new_order.setEstimated_cost("200.0");
        new_order.setTotal_kms("20");


        new_order.setPickup_time(Calendar.getInstance().getTime().toString());
//        new_order.setPickup_date(Calendar.getInstance().getTime().toString());
        cb_shared.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new_order.setShared(isChecked);
            }
        });
        //everyTenSecondsTask();

        return  view;
    }
    private boolean checkWithinRadius(LatLng latLng) {
        DRIVER_LOCATION = new Location("driver");
        DRIVER_LOCATION.setLatitude(latLng.latitude);
        DRIVER_LOCATION.setLongitude(latLng.longitude);

        MY_LOCATION = new Location("me");
        MY_LOCATION.setLatitude(USER_CURRENT_LOCATION.latitude);
        MY_LOCATION.setLongitude(USER_CURRENT_LOCATION.longitude);

        return MY_LOCATION.distanceTo(DRIVER_LOCATION) < SELECTED_RADIUS;//distance in meters

    }
    public void getDriverList()
    {
        valueEventListener = new ValueEventListener() {
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
                        LatLng latLng = new LatLng(driver.getLatitude(),driver.getLongitude());
                        for (Polyline polyline: polyLineList)
                        {
                            //polyline.
                            if(PolyUtil.isLocationOnEdge(latLng,polyline.getPoints(),true))
                            {
                                //means that driver location is inside that path/route
                                LatLng driver_location = new LatLng(driver.getLatitude(),driver.getLongitude());
                                Boolean within_radius = checkWithinRadius(driver_location);
                                if(within_radius)
                                    driverList.add(driver);
                            }
                            else
                            {

                            }
                        }
                    }
                    //now update the routes and remove markers if already present in it.
                    for (Driver driver1:driverList)
                    {
                        if(driver_in_map.containsKey(driver1.getFk_user_id()))
                        {
                            Marker marker = driver_in_map.get(driver1.getFk_user_id());
                            marker.remove();
                        }
                        if (gMap != null)
                        gMap.addMarker(new MarkerOptions().position(new LatLng(driver1.getLatitude(),driver1.getLongitude()))
                                .title("Driver: ".concat(driver1.getFk_user_id())));
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
        firebase_instance.child(Helper.REF_DRIVERS).orderByChild("isOnline").equalTo(true).addValueEventListener(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.
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
//        if(gps.canGetLocation()){
//            LatLng usa = new LatLng(gps.getLatitude(), gps.getLongitude());
//            gMap.moveCamera(CameraUpdateFactory.newLatLng(usa));
//        }
        Double latitude = 7.873172;
        Double longitude = 80.665608;
        LatLng usa = new LatLng(latitude, longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(usa));
        gMap.setOnPolylineClickListener(this);
        getDriverList();
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

                distance = distance.replaceAll("\\D+","");
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
        Toast.makeText(getContext(), "Distance: ".concat(value[0]).concat(" and Duration: ").concat(value[1]), Toast.LENGTH_SHORT).show();
    }
    private void everyTenSecondsTask() {
        new Timer().schedule(new TenSecondsTask(),5000,10000);
    }

    private class TenSecondsTask extends TimerTask {
        @Override
        public void run() {
            // markers driver clear <driverId, marker>
            // place markers again
        }
    }













}
