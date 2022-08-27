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
    GoogleMap mymap;
    SupportMapFragment mapFragment;
    String rideId;
    String riderId;
    String driverId;
    String timeStamp;
    LatLng destinationLatlng, pickupLatlng;
    String destination_name;
    DatabaseReference history_ref;
    String userId;
    //history vars
    TextView txt_from_to;
    TextView txt_price;
    TextView txt_date;
    TextView txt_name;
    TextView txt_phone;
    ImageView userImage;
    RatingBar ratingBar;
    //route drawing vars
    private List<Polyline> polylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history_preview);
        getSupportActionBar().hide();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        polylines = new ArrayList<>();
        txt_from_to = findViewById(R.id.txt_ride_from_to_history);
        txt_price = findViewById(R.id.txt_price_ride_history);
        txt_date = findViewById(R.id.txt_ride_date_history_prev);
        txt_name = findViewById(R.id.txt_ride_driver_name_history);
        txt_phone = findViewById(R.id.txt_driver_phone_history);
        userImage = findViewById(R.id.driver_image_ride_history);
        ratingBar = findViewById(R.id.rating_bar_ride);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rideId = getIntent().getStringExtra("rideId");
        history_ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("RidesHistory").child(rideId);
        getRideHistoryInfo();
    }

    private void getRideHistoryInfo() {
        history_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot history_child : snapshot.getChildren()) {
                        if (history_child.getKey().equals("rider")) {
                            riderId = history_child.getValue().toString();
                        }
                        if (history_child.getKey().equals("driver")) {
                            driverId = history_child.getValue().toString();
                            DriverInfo(driverId);
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
                            pickupLatlng = new LatLng(Double.parseDouble(history_child.child("from").child("lat").getValue().toString()), Double.parseDouble(history_child.child("from").child("lng").getValue().toString()));
                            destinationLatlng = new LatLng(Double.parseDouble(history_child.child("to").child("lat").getValue().toString()), Double.parseDouble(history_child.child("to").child("lng").getValue().toString()));
                            if (pickupLatlng != null && destinationLatlng != null) {
                                mymap.addMarker(new MarkerOptions().position((pickupLatlng)).title("Pickup location"));
                                mymap.addMarker(new MarkerOptions().position((destinationLatlng)).title(destination_name));
                                getRouteToMarker();
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

    private void ratingFunction() {
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                history_ref.child("rating").setValue(rating);
                DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(driverId);
                reference.child("rating").child(rideId).setValue(rating);
            }
        });
    }

    private void DriverInfo(String aDriverId) {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(aDriverId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
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

    private void getRouteToMarker() {
        if (pickupLatlng != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(pickupLatlng, destinationLatlng)
                    .key("AIzaSyDhwZjpwoi5aX3XwxkbVyLzdHbyT-6KcOw")
                    .build();
            routing.execute();
        }
    }


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