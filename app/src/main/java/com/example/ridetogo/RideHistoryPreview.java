package com.example.ridetogo;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RideHistoryPreview extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {
    private static final int[] COLORS = new int[]{R.color.black};
    //ui vars
    private TextView txt_from_to;
    private TextView txt_price;
    private TextView txt_date;
    private TextView txt_name;
    private TextView txt_phone;
    private ImageView userImage;
    private RatingBar ratingBar;
    //google map vars
    private GoogleMap mymap;
    private SupportMapFragment mapFragment;
    //ride details vars
    private String rideId;
    private String riderId;
    private String driverId;
    private LatLng destinationLatlng, pickupLatlng;
    private String destination_name;
    private DatabaseReference history_ref;
    private String userId;
    //route drawing vars
    private List<Polyline> polylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history_preview);
        getSupportActionBar().hide();

        //link ui vars
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        txt_from_to = findViewById(R.id.txt_ride_from_to_history);
        txt_price = findViewById(R.id.txt_price_ride_history);
        txt_date = findViewById(R.id.txt_ride_date_history_prev);
        txt_name = findViewById(R.id.txt_ride_driver_name_history);
        txt_phone = findViewById(R.id.txt_driver_phone_history);
        userImage = findViewById(R.id.driver_image_ride_history);
        ratingBar = findViewById(R.id.rating_bar_ride);

        //assign google maps vars
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //initialize route vars
        polylines = new ArrayList<>();

        //firebase user initialize
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //get ride id to view from previous activity
        rideId = getIntent().getStringExtra("rideId");

        //get ride reference from database using ride id
        history_ref = FirebaseDatabase.getInstance().getReference().child("RidesHistory").child(rideId);

        //call ride history info function
        getRideHistoryInfo();
    }

    //ride info function
    private void getRideHistoryInfo() {
        history_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //if ride id data snapshot exists assign ride info to vars
                    for (DataSnapshot history_child : snapshot.getChildren()) {
                        if (history_child.getKey().equals("rider")) {
                            riderId = history_child.getValue().toString();
                        }
                        if (history_child.getKey().equals("driver")) {
                            driverId = history_child.getValue().toString();
                            //after getting driver id call driver info function using driver id
                            DriverInfo(driverId);
                            //call rating function
                            ratingFunction();
                        }
                        if (history_child.getKey().equals("timeStamp")) {
                            txt_date.setText(getDateOfTimeStamp(Long.valueOf(history_child.getValue().toString())));
                        }
                        if (history_child.getKey().equals("rating")) {
                            ratingBar.setRating(Integer.valueOf(history_child.getValue().toString()));
                        }
                        if (history_child.getKey().equals("destination_name")) {
                            destination_name = history_child.getValue().toString();
                            txt_from_to.setText(destination_name);
                        }
                        if (history_child.getKey().equals("price")) {
                            txt_price.setText(history_child.getValue().toString() + " EGP");
                        }
                        if (history_child.getKey().equals("location")) {
                            //get pickup location latlng
                            pickupLatlng = new LatLng(Double.parseDouble(history_child.child("from").child("lat").getValue().toString()), Double.parseDouble(history_child.child("from").child("lng").getValue().toString()));
                            //get destination latlng
                            destinationLatlng = new LatLng(Double.parseDouble(history_child.child("to").child("lat").getValue().toString()), Double.parseDouble(history_child.child("to").child("lng").getValue().toString()));
                            if (pickupLatlng != null && destinationLatlng != null) {
                                //add pickup and destination markers
                                mymap.addMarker(new MarkerOptions().position((pickupLatlng)).title("Pickup location").icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_flag_small)));
                                mymap.addMarker(new MarkerOptions().position((destinationLatlng)).title(destination_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_flag_small)));
                                //draw route between pickup and location
                                getRouteToMarker();
                                //animate camera to route
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                builder.include(pickupLatlng);
                                builder.include(destinationLatlng);
                                LatLngBounds bounds = builder.build();
                                int width = getResources().getDisplayMetrics().widthPixels;
                                int padding = (int) (width * 0.3);
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                mymap.animateCamera(cameraUpdate);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    //rating function
    private void ratingFunction() {
        //rating bar on rating changed listener
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //update ride rating using history reference
                history_ref.child("rating").setValue(rating);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
                reference.child("rating").child(rideId).setValue(rating);
            }
        });
    }

    //driver info function
    private void DriverInfo(String aDriverId) {
        //get driver reference using driver id
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(aDriverId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //get name, phone and profile photo
                    Map<String, Object> datamap = (Map<String, Object>) snapshot.getValue();
                    if (datamap.get("Name") != null) {
                        txt_name.setText(datamap.get("Name").toString());
                    }
                    if (datamap.get("Phone") != null) {
                        txt_phone.setText(datamap.get("Phone").toString());
                    }
                    if (datamap.get("profile_image") != null) {
                        String image_profileurl = datamap.get("profile_image").toString();
                        Glide.with(RideHistoryPreview.this).load(image_profileurl).into(userImage);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //get ride date using ride time stamp
    private String getDateOfTimeStamp(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timestamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString();
        return date;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mymap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(RideHistoryPreview.this, R.raw.day_mode));
    }

    //rout draw function already commented in many classes
    private void getRouteToMarker() {
        if (pickupLatlng != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(pickupLatlng, destinationLatlng)
                    .key(firebase_google_keys_ids.google_maps_api_key)
                    .build();
            routing.execute();
        }
    }

    //erase route poly lines if needed
    private void eraseRoutePolyLines() {
        for (Polyline pline : polylines) {
            pline.remove();
        }
        polylines.clear();
    }


    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mymap.addPolyline(polyOptions);
            polylines.add(polyline);

        }
    }

    @Override
    public void onRoutingCancelled() {

    }
}