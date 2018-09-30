package com.logixcess.smarttaxiapplication.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.Order;
import com.logixcess.smarttaxiapplication.R;
import com.logixcess.smarttaxiapplication.Utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("MissingPermission")
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    public static final String KEY_DRIVER_ID = "driver_id";
    public static final String KEY_CURRENT_ORDER = "current_order";
    private GoogleMap mMap;
    private Marker mDriverMarker;
    private ArrayList<Polyline> polylines;
    private Order CURRENT_ORDER = null;
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.colorPrimary,R.color.colorPrimaryDark,R.color.colorAccent,R.color.primary_dark_material_light};

    private double totalDistance = 100, totalTime = 120; // total time in minutes
    private double distanceRemaining = 90;
    private LatLng start, end;
    private List<LatLng> waypoints;
    private DatabaseReference db_ref, db_ref_driver;
    private String selectedDriverId;
    private Driver SELECTED_DRIVER;
    private LatLng pickup = null;
    private LatLng driver = null;
    private Marker driverMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.containsKey(KEY_DRIVER_ID) && bundle.containsKey(KEY_CURRENT_ORDER)){
            selectedDriverId = bundle.getString(KEY_DRIVER_ID);
            CURRENT_ORDER = bundle.getParcelable(KEY_CURRENT_ORDER);

            askLocationPermission();
            setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            db_ref = FirebaseDatabase.getInstance().getReference();
            db_ref_driver = db_ref.child(Helper.REF_DRIVERS).child(selectedDriverId);
            //fetching selected driver for the route
            db_ref_driver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        SELECTED_DRIVER = dataSnapshot.getValue(Driver.class);
                        if(SELECTED_DRIVER != null){
                            requestNewRoute();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            sendNotification("Get Ready","New Order is assigned to You");
        }else{
            // driver id not provided
            finish();
        }

    }
    private void requestNewRoute() {
        driver = new LatLng(SELECTED_DRIVER.getLatitude(), SELECTED_DRIVER.getLongitude());
        if(pickup == null)
            pickup = new LatLng(CURRENT_ORDER.getPickupLat(),CURRENT_ORDER.getPickupLong());
        List<LatLng> points = new ArrayList<>();
        points.add(driver);
        points.add(pickup);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(points)
                .build();
        routing.execute();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        populateMap();
    }

    private void populateMap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                askLocationPermission();
                return;
            }
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        addRoute();
    }

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
        }
    }
    //vehicle color change by distance
    private void changeVehicleColorByDistance(){
        int percentageLeft = (int) ((int) distanceRemaining  / totalDistance* 100);
        LatLng markerPosition = driverMarker.getPosition();
        String title = driverMarker.getTitle();
        driverMarker.remove();
        if(percentageLeft < 25){
            mDriverMarker = mMap.addMarker(getDesiredMarker(BitmapDescriptorFactory.HUE_RED,driver,title));
            sendNotification("Congratulations","You have reached your destination.");
        }else if(percentageLeft < 50){
            mDriverMarker = mMap.addMarker(getDesiredMarker(BitmapDescriptorFactory.HUE_BLUE,markerPosition,title));
        }else if(percentageLeft < 75){
            mDriverMarker = mMap.addMarker(getDesiredMarker(BitmapDescriptorFactory.HUE_ROSE,markerPosition,title));
        }else if(percentageLeft < 100){
            mDriverMarker = mMap.addMarker(getDesiredMarker(BitmapDescriptorFactory.HUE_YELLOW,markerPosition,title));
        }
    }

    private MarkerOptions getDesiredMarker(float kind, LatLng posToSet, String title) {
        return new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(kind))
                .position(posToSet).title(title);
    }

    public void setAnimation(GoogleMap myMap, final List<LatLng> directionPoint, final Bitmap bitmap) {
        Marker marker = myMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .position(directionPoint.get(0))
                .flat(true));

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 10));

        animateMarker(myMap, marker, directionPoint, false);
    }
    private void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint,
                                      final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                if (i < directionPoint.size())
                    marker.setPosition(directionPoint.get(i));
                i++;


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public void addRoute() {
        start = new LatLng(33.968584, -118.420771);
        end = new LatLng(33.968443, -118.421046);

        waypoints = decodePoly("}syl@qqwiNQk@EQOg@a@oAGSo@gBs@uBOc@i@aB{@gCuA}DK_@]kAI]Mq@WyAOs@Mk@Oq@Ou@Q{@[yAUcAMe@Mg@Y}@Uo@M_@[w@OYEKGIa@m@Ya@GKi@s@GGU]IOMSKSQ_@O_@Yq@Si@Uo@Ww@CIKWk@kBg@sAy@eBqBcEs@uA[m@Yi@KQOUKSSYMQ]e@]]o@m@{AsA{@u@a@_@w@s@e@c@[YSS_@_@]]MOiAsAiAsAgAsAKMIKKOKSMWOa@EKc@iAM]IWQg@Wq@O[EMEKGMIMQUOQYYc@][Wa@Wg@[IEa@Q[KQEWI_AQe@Ia@IoAQ}@MeAK{@Mo@Ia@G_@GUIs@Uq@Ua@O]Ka@IMAKAc@AWAWCYEc@GUESKc@[cAy@CAaAw@k@e@iBeBwBsBYYWYKMKOOWQ[IUGKISIQKQIOMMMKUMUIWIk@SYIOGMGUM[W}@k@c@]g@[s@g@uByAe@[_As@s@g@uAkAiA_Ai@c@i@c@u@g@YS[Q{A{@qAu@qBaAsB}@mB{@QIqAi@e@U]Qu@c@WQKIAAc@[WUUUYYU[Q[O[M[Ma@Uu@e@aBe@cBU}@YkAOi@Qk@KYISWm@Yg@m@_Am@aAq@gAe@m@[_@SWOQ]_@[Wa@[WQm@_@k@_@m@]eAg@{@Yg@Q]MYKWMQIWOMKMMKOIMGIIIIIOOGCACICYKgA[QGSIGCIEQIWO_@Uk@_@wAaAqA{@aBeAyA}@iBiAaBeA_Ak@oAw@mAw@qBqAeBkAqCeBuCkB}@k@s@g@k@]o@c@e@YqA}@mC}A_By@uAq@eAg@uAm@eBw@w@_@uAs@mBcAi@[g@]qA}@y@m@e@[i@WyAw@iAk@gAg@uAo@i@Ui@Wi@Wy@_@g@WgAi@u@_@g@Ym@[WO[SYSMKIKSSMQMS]m@m@cAa@o@GIMSKMIKKKOOOMOK]U_@QYOQI}Aq@qAi@}B}@iBs@aA_@e@Qc@Me@Ma@I_@EeAQyAU}@Mu@Mm@Oq@ScA[y@Yu@UaA[k@SyAc@_@O]M[K}@_@q@[}BgAWKm@[y@c@g@[g@]QMQM_@]m@k@o@k@y@u@mBcBgBaBaBwAaBwAy@s@c@]y@o@cAw@QMmA{@g@_@i@a@k@c@WQ[U_@Ye@[YUe@]_Aq@u@i@q@e@aAo@oAw@y@i@a@W}@s@a@]YUc@]c@]g@_@e@]_As@c@[g@_@{@o@u@i@u@c@s@g@u@e@iBgAgAo@eAk@g@W[OYO_@Om@S[K[Is@OoAYiBa@eBc@}Cu@i@Mc@Mu@QgAUoAUcAOo@Mo@I}@Os@Kk@KeAOs@Kw@Ki@GmAIyAMsAIaGe@_Fa@_E[kCUy@K}@K]Eo@GyAOs@Gg@GsAQ{ASaAOkAQmAS}@OOC}@Mg@Ge@Ie@Iu@OqAUkASi@Ke@Kc@KaB[gCc@e@Ic@I_@I_@Ie@M]I_@Ie@Ok@OWG_@KA?g@S_A[q@SsAc@e@QOEKGYMSKSMw@e@{@i@OI}A_AsAu@k@[s@a@iAq@kBcAa@Wm@[YOyAu@q@[]O]Qe@So@Ye@UeAi@oAo@iAo@c@W[UUOYSg@a@USkAgAy@w@u@s@c@c@sAoAeAcAcB_BiAgAoBqBgBgBgBgBc@a@_@a@UW[_@U]o@aAaAyAsAoB_AsAsAkBy@kAy@gA[_@k@s@e@e@]a@]]g@k@]a@c@i@a@e@_@a@s@{@e@i@m@k@{@y@g@a@o@i@iA{@kByAw@k@_@[G?GCECiA_AuAkA_Au@qBeBiAcAgByAaAw@a@]CAGGeAw@cAu@sA_A_Am@MKOIqGsEiBmA}B{AaBeAkDwBOIcAq@UOMGgBeAq@m@eAo@oBiAqAu@kAm@c@UWKa@Si@WYMYOw@g@cAo@sA{@QKMKSMKKIIIIMQMWQ_@M[M]KYGQIOU[GGKKu@}@w@y@_AcAUUe@e@[YiAcA{@u@y@s@_A{@m@m@m@m@i@k@i@m@cAkAaAkAq@{@c@g@k@o@g@m@m@o@k@o@uA}AaAeAmAsAk@m@UUWUc@_@o@k@e@]s@g@mAw@aC_BmBoAyAaAkAs@eAi@WMgAk@i@[[QUQSMMKKMII_@c@[a@SSUM]OSIoASs@KwLgBkJsBoOgDqHmBmCq@_CeAwCiBmBkA[OYM[QKGOI]Q[Og@Ya@Ug@Y]Q_@UYSOKMKSQUSMMoBuBu@{@oAqAkAoAk@o@UWSWS[QYIMEKEGIKIIGGKIc@WSIWEa@Og@Oi@Wc@Ue@Yk@]WSIE]UOIOGOEOG_@I_@E]EMA[C[CKA]E_@ImAS{A]m@Oq@QUIm@OQGMCMEQIQKm@[QIICGEMM]_@k@m@c@e@[_@?AIIGIGIMKKIKGIEIEIAGAI?I?C?C?E@GBUHKDMDMBEBE?E@G@G?I?I?MASCIAKAEAKEQEOEMEOISMQMQMSOUS[WMMOQOSMOIKIGKKSMOKKGQIQIOEIEIEOKOKc@_@o@g@m@c@MIQIQIQIa@M_@KYEWE]Ca@GUESG[Me@OSESE[GQCOCa@E]Ac@Am@AcAIm@G_@EUEYCSCSCG?G?K?K?O?I?Q@Q@K@O@O?i@AQ?]?Q?O@O@OBQBQDODOBM@O@M?OAOCMCQGOIQISISGQESCMCMAU?Y?O@K@KBK@e@J]Fa@D]D_@Dc@B[@]@S?Q?M@IAK?MAOEOGMGOKIIKKGKGGIGIEICCAYEyAG{@Kk@Ou@YYKQKWQ]WSQQUOSOYGUCQCICIEIKKIIKEKC_@M]OUMQIe@Ws@_@e@[WQOMMMSUQWSYMSO[MWIUGWCOEUIc@W{@O[IOUWQQSOUM_@Qc@Qk@[WMWMUIYISGUEQEUEOCWGYGWGi@Mg@Kq@OeAWqAYw@Qm@Oc@M}Bo@w@WoA_@AA{GgC[MUIe@OgAYWI_@G]Ic@IaCa@uAWqBYmBUw@KC?m@GiAEy@CiAAC?aCEoACkA?mB?_A@c@?_@B]@g@D{ANK@cAJ_AFo@Dw@BQ@aA@aCJsBJ_@@mADoADcBDiDB}CL_BD_ABqA?{@?_ACk@Cc@Cm@Go@GcAMi@GeAKu@Gg@CqAK{@Iq@GuAKiEc@oD_@_BOoBUYEKAOEm@Mq@Qm@Q_@Mi@UQKSKWSOOOSSWW_@mA_BU]s@_Am@o@a@c@UWWUQQk@e@UUm@e@s@k@q@e@aAq@u@i@e@[}B}AqCiBe@W_@Uy@g@IEu@c@_@OWMMGsAm@u@_@eAg@cAi@eAi@y@c@oAs@cAk@u@e@aAo@s@e@WUUSo@o@UUUY_@c@SY]i@e@q@_@i@e@o@i@s@_@e@a@e@[[QSSQOM[Sc@Yk@[s@_@cAi@OCKCUKyGaDMIQGMISKOIOKQOMMy@aA[a@Y[a@c@s@q@o@o@a@_@KKKI][_@]a@a@e@a@a@_@a@_@e@_@a@c@[UEG_BwA_BuA[WWWQSWWOOOOEGEK?AEIEICIAGCGAMAIEIEKGMIIGG?AMGIEG?C?E?CAYK[M[MWMAAICIECAIGKGMKQO][YUc@_@MOc@_@GEQOUUOMe@c@SO?AQOCCECUQQOKIMKKIYY{@u@]YKIg@c@k@g@mC{BQOCGCG_@]cAy@]Wa@[SQYQSMYO]Om@YcAc@y@a@mB}@QGOIKGOIMMKMMQMOAASWKOSQY[m@m@c@c@a@_@qAoAqAoA_A_AQOIKUUKIIGQMu@_@]QCAaAi@c@Wu@e@YS_@Uw@i@gAq@sAu@wBoAi@YGCGCEAQG_@S]QKIMIWWOSOSOQ[]a@g@k@k@o@k@{@u@}@u@}@u@wB}A_BkAaAq@yAaAyA}@u@c@c@WoBqAoCgBkBkAWOSOQMSQWUm@u@mBuBaBiB{AaBaB_BmBkBoAmAc@a@][a@Y[S_@SuBkAmBeAoC}AgCyAu@a@_CwA_B_AmBmAkBiAaBcA_Ak@m@_@e@Ya@WUMSOg@a@YWUSi@m@]a@U]e@s@aBgCk@_AgAkBw@oAu@mAo@cAc@s@Yi@a@s@o@qAMWMQQW[]US]YgA{@_Aq@kE{C}@q@gBoAmA{@uA}@UQgBiAiC{A_BaAgC}A_CyAsBmA}BwAkGwDuBuAkBgA_BaAq@c@m@a@kA{@o@g@a@]QKYSy@g@q@c@}@q@aAw@iA{@mA}@iA_Au@i@uCiBmBmAu@a@uAs@cAe@uB_AcAa@c@QMGAA}B_AyBcAoCqA]Qa@QcAc@]O_@Qo@Wo@Wk@YYKuAe@kBm@iAa@gAc@cCgAQIy@_@[Mw@a@}@c@y@a@u@]_Ac@kEoB{@c@}@c@w@e@q@_@g@]}AcAuA}@e@Y_B_Ag@Y_@WUOc@Y{@k@w@i@w@i@}AgA[QYQq@e@}B}AqBsAuByAuBwAsByAi@c@WSk@i@OQMMQUMOMUSYm@{@e@u@c@k@QQKKKMGGg@]YQc@Yo@[e@Uq@[m@WSIOIQIOIUOWQm@a@}@o@y@q@iAcAgBgBk@i@WU][m@g@k@c@USWQe@Y[QOIQKQIYKYKu@Su@SsAWw@M_BSgBQ{@GeCUWEUEICOEWKSIeCaAaC_AiB{@_CiAy@a@kAk@sBaAk@UYMUGiBc@mCq@sBg@IAm@Oc@Ks@MWCe@G_@CcAGc@AaBIq@EYE]G_@MUIYQaAi@aAg@uA}@iAy@ECgAq@mA{@_As@g@c@i@[SMQI_@MqAWk@IuAIiAGeAEiAG}@Gq@Is@Mk@Mm@Qo@Su@[I?KCKG_BcA{BeB_@[[[u@cAYa@U[W][]w@}@[[]]_BoAu@e@{@k@w@a@a@W_@QWMy@YoCaAq@WiAe@w@]_@S_@Uk@[]W[U}@q@MIqB}AmCmB]YSO{AiAwG{Em@c@qA}@mC}AsEgCkDoBy@c@uEeCa@YEE");
        start = waypoints.get(0);
        end = waypoints.get(waypoints.size() - 1);

        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(start,12);
        mMap.animateCamera(center);


        PolylineOptions line = new PolylineOptions().addAll(waypoints);
        mMap.addPolyline(line);
        mDriverMarker = mMap.addMarker(getDesiredMarker(BitmapDescriptorFactory.HUE_YELLOW,start,"driver"));
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(options);
//        Routing routing = new Routing.Builder()
//                .travelMode(AbstractRouting.TravelMode.DRIVING)
//                .withListener(this)
//                .alternativeRoutes(true)
//                .waypoints(waypoints)
//                .build();
//
//        routing.execute();


    }

    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
        
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

//        polylines = new ArrayList<>();
        //add route(s) to the map.
        Route shortestRoute = route.get(shortestRouteIndex);
//        int colorIndex = shortestRouteIndex % COLORS.length;
//        PolylineOptions polyOptions = new PolylineOptions();
//        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
//        polyOptions.width(10 + shortestRouteIndex * 3);
//        polyOptions.addAll(shortestRoute.getPoints());
//        Polyline polyline = mMap.addPolyline(polyOptions);
//        polylines.add(polyline);
        distanceRemaining = shortestRoute.getDistanceValue();
        MarkerOptions options = new MarkerOptions();
        options.position(driver);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car_black));
        if(mDriverMarker != null)
            mDriverMarker.remove();
        mDriverMarker = mMap.addMarker(options);
        changeVehicleColorByDistance();


    }

    @Override
    public void onRoutingCancelled() {

    }


    public void sendNotification(String title, String message) {

        //Get an instance of NotificationManager//

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message);


        // Gets an instance of the NotificationManager service//

        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//
        mNotificationManager.notify(001, mBuilder.build());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1001){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                populateMap();
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    
}

