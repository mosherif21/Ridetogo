package com.example.ridetogo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.ridetogo.databinding.ActivityDriverMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class driver_MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener , RoutingListener {

    private ActivityDriverMapsBinding binding;
    GoogleApiClient googleApiClient;
    Location mlocation;
    LocationRequest location_Request;
    GoogleMap mymap;
    GeoFire geofire_ref_available;
    GeoFire geofire_ref_working;
    String userid;
    Button logout;
    Switch switch_Driver_on_off;
    ConstraintLayout layout_driver_settings;
    ConstraintLayout layout_onGoing_ride;
     Button btn_end_ride;


    //get assigned customer vars
    String assigned_customer_id="";
    Marker pickup_location_marker;
    Marker destination_location_marker;
    DatabaseReference customer_request_ref;
    ValueEventListener customer_request_refListsner;
    TextView customer_phone;
    TextView customer_name;
    TextView customer_destination;
    ConstraintLayout layout_assigned_customer_info;
    Button call_customer;
    Button cancel_ride_request;
    Button btn_pick_cusomer;
    String assigned_customer_dest_name;
    LatLng customer_lat_lng;
    LatLng destination_lat_lng;
    Boolean customer_picked_bol=false;
     Long start_ride_timestamp;
    Long end_ride_timestamp;
    CardView my_location_button;
    //logout vars for handling location on logout
    private boolean logout_bol=false;
    private int location_changed_onstop=0;


    //drawing route for pickup location
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        polylines = new ArrayList<>();
        switch_Driver_on_off=findViewById(R.id.switch_driver_onOFF);
        customer_phone=findViewById(R.id.customer_phone_indriver);
        customer_name=findViewById(R.id.customer_name_indriver);
        customer_destination=findViewById(R.id.customer_destination_txt);
        layout_driver_settings=findViewById(R.id.layout_driver_Settings);
        layout_assigned_customer_info=findViewById(R.id.customer_assigned_info);
        layout_assigned_customer_info.setVisibility(View.INVISIBLE);
        call_customer=findViewById(R.id.btn_call_customer);
        btn_end_ride=findViewById(R.id.btn_end_ride_inDriver);
        cancel_ride_request=findViewById(R.id.btn_cancel_customer_request_indriver);
        btn_pick_cusomer=findViewById(R.id.btn_pick_customer);
        layout_onGoing_ride=findViewById(R.id.layout_onGoing_ride_driver);
        layout_onGoing_ride.setVisibility(View.INVISIBLE);
        logout=findViewById(R.id.btn_logout_driver);
        my_location_button=findViewById(R.id.myloc_Sd);
        userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        switch_Driver_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    connectDriverForAsAvailable();
                    switch_Driver_on_off.setBackground(getDrawable(R.drawable.switch_driver_back_green));
                    switch_Driver_on_off.setText("Working");
                }
                else{
                    switch_Driver_on_off.setBackground(getDrawable(R.drawable.switch_driver_back_red));
                    switch_Driver_on_off.setText("Offline");
                    diconnect_driver_asAvailable();
                }
            }
        });
        btn_pick_cusomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             if(!assigned_customer_id.equals("")){

                 DatabaseReference reference=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                 if(reference!=null)
                     reference.child("pickRequest").child(userid).child("pickCustomer").setValue("ask");
                 getIfUserAcceptPickupRequest();
             }
            }
        });
        cancel_ride_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(driver_MapsActivity.this);
                builder.setTitle("Cancel Request");
                builder.setMessage("Are you sure you want to cancel the request?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                fn_cancel_request();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
           btn_end_ride.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(driver_MapsActivity.this);
                    builder.setTitle("End Ride");
                    builder.setMessage("Are you sure you want to end the ride?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    fn_EndRide();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                   }
           });

     logout.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             AlertDialog.Builder builder = new AlertDialog.Builder(driver_MapsActivity.this);
             builder.setTitle("Logout");
             builder.setMessage("Are you sure you want to logout?")
                     .setCancelable(false)
                     .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             dialog.dismiss();
                             logout_bol=true;
                             diconnect_driver_asAvailable();
                             FirebaseAuth.getInstance().signOut();
                             Intent intent=new Intent(driver_MapsActivity.this,login.class);
                             startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                             finish();
                             return;
                         }
                     })
                     .setNegativeButton("No", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             dialog.dismiss();
                         }
                     });
             AlertDialog alert = builder.create();
             alert.show();
         }
     });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
        private void fn_EndRide(){

            saveRideInfo();
            layout_onGoing_ride.setVisibility(View.INVISIBLE);
    if(destination_location_marker!=null)
        destination_location_marker.remove();
    eraseRoutePolyLines();
    fn_cancel_request();
     }
    private void saveRideInfo(){
        String userid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(userid).child("ride_history");
        DatabaseReference riderRef=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(assigned_customer_id).child("ride_history");
        DatabaseReference historyRef=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("RidesHistory");
        String rideHistoryId=historyRef.push().getKey();
        driverRef.child(rideHistoryId).setValue(true);
        riderRef .child(rideHistoryId).setValue(true);
        HashMap datamap=new HashMap();
        datamap.put("rider",assigned_customer_id);
        datamap.put("driver",userid);
        datamap.put("rating",0);
        datamap.put("timeStamp",getCurrentTimestamp());
        datamap.put("location/from/lat",customer_lat_lng.latitude);
        datamap.put("location/from/lng",customer_lat_lng.longitude);
        if(assigned_customer_dest_name.equals("no_dest"))
        {
            datamap.put("location/to/lat",mlocation.getLatitude());
            datamap.put("location/to/lng",mlocation.getLongitude());
            datamap.put("destination_name","Destination Location");
        }
        else{
            datamap.put("location/to/lat",destination_lat_lng.latitude);
            datamap.put("location/to/lng",destination_lat_lng.longitude);
            datamap.put("destination_name",assigned_customer_dest_name);
        }
        DatabaseReference ref=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Ride_pick_time").child(userid).child("pick_time");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    start_ride_timestamp=Long.valueOf(snapshot.getValue().toString());
                    ref.setValue(null);
                    end_ride_timestamp=getCurrentTimestamp();
                    double ride_time = (end_ride_timestamp - start_ride_timestamp) / 60d;
                    Location loc_ride_start=new Location("");
                    loc_ride_start.setLatitude(customer_lat_lng.latitude);
                    loc_ride_start.setLongitude(customer_lat_lng.longitude);
                    Location loc_ride_end=new Location("");
                    loc_ride_end.setLatitude(mlocation.getLatitude());
                    loc_ride_end.setLongitude(mlocation.getLongitude());
                    float distance_between=loc_ride_start.distanceTo(loc_ride_end)/1000;
                    double time_fare=ride_time*0.36;
                    double distance_fare=distance_between*2.61;
                    double base_fare=7.20;
                    double total_fare=time_fare+distance_fare+base_fare;
                    datamap.put("price",Math.floor(total_fare));
                    historyRef.child(rideHistoryId).updateChildren(datamap);
                    DatabaseReference reference=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Ride_end_notify").child(userid).child("price");
                    reference.setValue(Math.floor(total_fare));
                    AlertDialog.Builder builder = new AlertDialog.Builder(driver_MapsActivity.this);
                    builder.setTitle("Ride end");
                    builder.setMessage("Ride total is "+Math.floor(total_fare)+" EGP")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private Long getCurrentTimestamp(){
        Long timestamp=System.currentTimeMillis()/1000;
        return timestamp;
    }



    ValueEventListener listener_pickup;
    private void getIfUserAcceptPickupRequest() {
        String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("pickRequest").child(driverID).child("pickCustomer");
        listener_pickup =  reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String pcikup_string=snapshot.getValue().toString();
                    if(pcikup_string.equals("OK")){
                        layout_assigned_customer_info.setVisibility(View.INVISIBLE);
                        layout_onGoing_ride.setVisibility(View.VISIBLE);
                        customer_picked_bol=true;
                        eraseRoutePolyLines();
                        if(destination_lat_lng.longitude!=0&&destination_lat_lng.latitude!=0)
                        getRouteToMarker(destination_lat_lng);
                             if(pickup_location_marker!=null)
                            pickup_location_marker.remove();

                        if(destination_lat_lng.latitude!=0&&destination_lat_lng.longitude!=0)
                        destination_location_marker = mymap.addMarker(new MarkerOptions().position((destination_lat_lng)).title("Ride Destination"));
                        reference.removeEventListener(listener_pickup);
                        reference.setValue(null);
                        DatabaseReference ref=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Ride_pick_time").child(driverID).child("pick_time");
                        ref.setValue(getCurrentTimestamp());
                    }
                }
                else  reference.removeEventListener(listener_pickup);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void fn_cancel_request(){

        layout_assigned_customer_info.setVisibility(View.INVISIBLE);
        layout_driver_settings.setVisibility(View.VISIBLE);
        eraseRoutePolyLines();
        customer_name.setText("");
        customer_destination.setText("");
        customer_phone.setText("");
        String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference driver_ref=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(driverID).child("customerRequest");
                if(driver_ref!=null)
                    driver_ref.setValue(null);
                if(!assigned_customer_id.equals("")||assigned_customer_id!=null)
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("CustomerRequest").child(assigned_customer_id).setValue(null);
            DatabaseReference reference=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("CustomerRequest");
            GeoFire geofire=new GeoFire(reference);
            geofire.removeLocation(driverID, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
            if(!assigned_customer_id.equals(""))
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(assigned_customer_id).child("ongoingRequest").setValue(null);
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("pickRequest").child(userid).setValue(null);

        assigned_customer_id="";
            if(pickup_location_marker!=null)
                pickup_location_marker.remove();



    }
    private void getAssignedCustomer() {
       String driverID=FirebaseAuth.getInstance().getCurrentUser().getUid();
         customer_request_ref=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(driverID).child("customerRequest");
        customer_request_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()&&snapshot.getChildrenCount()>0){
                        Map<String,Object> datamap=(Map<String, Object>) snapshot.getValue();
                        if(datamap.get("CustomerRideId")!=null){
                            assigned_customer_id=datamap.get("CustomerRideId").toString();
                        }
                        if(datamap.get("destination_name")!=null){
                            assigned_customer_dest_name=datamap.get("destination_name").toString();
                        }
                        if(assigned_customer_dest_name.equals("no_dest"))
                            customer_destination.setText("Not specified");
                        else
                            customer_destination.setText(assigned_customer_dest_name);
                        Double Lat=0.0;
                        Double Long=0.0;

                        if(datamap.get("destination_lat")!=null)
                            Lat=Double.parseDouble(datamap.get("destination_lat").toString());

                        if(datamap.get("destination_lng")!=null)
                            Long=Double.parseDouble(datamap.get("destination_lng").toString());

                        destination_lat_lng=new LatLng(Lat,Long);
                        getcustomerLocation();
                        getcustomerinfo();
                        connectDriverForAsAvailable();

                }
                else
                {
                    if(customer_request_refListsner!=null)
                    customer_request_ref.removeEventListener(customer_request_refListsner);
                    fn_cancel_request();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void  getcustomerinfo(){
        layout_driver_settings.setVisibility(View.INVISIBLE);
        layout_assigned_customer_info.setVisibility(View.VISIBLE);
      DatabaseReference  customer_Ref =FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child("Riders").child(assigned_customer_id);
        customer_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&&snapshot.getChildrenCount()>1){
                    Map<String,Object> datamap=(Map<String, Object>) snapshot.getValue();
                    String full_name="";
                    if(datamap.get("Name")!=null){
                        full_name=datamap.get("Name").toString();
                        customer_name.setText(full_name);
                    }
                    if(datamap.get("Phone")!=null){
                        customer_phone.setText(datamap.get("Phone").toString());
                    }
                call_customer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent call_customer=new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+customer_phone.getText().toString()));
                        startActivity(call_customer);
                    }
                });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getcustomerLocation() {
        DatabaseReference customer_req_ref_loc=FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("CustomerRequest").child(assigned_customer_id).child("l");
        customer_request_refListsner = customer_req_ref_loc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && !assigned_customer_id.equals("")){
                    List<Object> map_loc=(List<Object>) snapshot.getValue();
                    double customer_long=0;
                    double customer_lat=0;
                    if(map_loc.get(0)!=null)
                        customer_lat=Double.parseDouble(map_loc.get(0).toString());
                    if(map_loc.get(1)!=null)
                        customer_long=Double.parseDouble(map_loc.get(1).toString());

                     customer_lat_lng=new LatLng(customer_lat,customer_long);
                  pickup_location_marker= mymap.addMarker(new MarkerOptions().position((customer_lat_lng)).title("Pickup location"));
                  if(customer_lat_lng.longitude!=0&&customer_lat_lng.latitude!=0)
                 getRouteToMarker(customer_lat_lng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getRouteToMarker(LatLng customer_lat_lng){
        if(mlocation!=null){
        Routing routing=new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mlocation.getLatitude(),mlocation.getLongitude()),customer_lat_lng)
                .key("AIzaSyDhwZjpwoi5aX3XwxkbVyLzdHbyT-6KcOw")
                .build();
        routing.execute();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mymap = googleMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.day_mode));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        setGoogleApiClient();
        googleMap.setMyLocationEnabled(true);


    }
    protected synchronized void setGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(driver_MapsActivity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        location_Request=new LocationRequest();
        location_Request.setInterval(1000);
        location_Request.setFastestInterval(1000);
        location_Request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        getAssignedCustomer();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void connectDriverForAsAvailable(){
        if(ActivityCompat.checkSelfPermission(driver_MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(driver_MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            return;
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,location_Request,this);
        location_changed_onstop=0;
    }

    private void diconnect_driver_asAvailable(){
        location_changed_onstop=1;
      //  LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("AvailableDrivers");

       GeoFire geofire=new GeoFire(ref);
        geofire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    Boolean zoom_first_time=false;
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(!logout_bol&&location_changed_onstop==0){
            mlocation=location;
            if(customer_lat_lng!=null&&!zoom_first_time){
                getRouteToMarker(customer_lat_lng);
            }
            if(!zoom_first_time){
                zoom_first_time=true;
                LatLng L=new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
                mymap.moveCamera(CameraUpdateFactory.newLatLng(L));
                mymap.animateCamera(CameraUpdateFactory.zoomTo(17),1000, null);
            }
             my_location_button.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     LatLng L=new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
                     mymap.moveCamera(CameraUpdateFactory.newLatLng(L));
                     mymap.animateCamera(CameraUpdateFactory.zoomTo(17),1000, null);
                 }
             });

            userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref_drivers_available= FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("AvailableDrivers");
            DatabaseReference ref_drivers_working= FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("DriversWorking");
            geofire_ref_available=new GeoFire(ref_drivers_available);
            geofire_ref_working=new GeoFire(ref_drivers_working);
           if(!assigned_customer_id.equals("")){
                geofire_ref_available.removeLocation(userid, new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });
                geofire_ref_working.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });
            }
            else{
                geofire_ref_working.removeLocation(userid, new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                    }});

                geofire_ref_available.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: " + error);
                        } else {
                            // System.out.println("Location saved on server successfully!");
                        }
                    }
                });
            }
       }
    }




    @Override
    public void onRoutingFailure(RouteException e) {
       if(e!=null){
           Toast.makeText(driver_MapsActivity.this, "error:  "+e.getMessage(), Toast.LENGTH_LONG).show();
       }
       else Toast.makeText(driver_MapsActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mymap.addPolyline(polyOptions);
            polylines.add(polyline);

           // Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    private void eraseRoutePolyLines(){
        for(Polyline pline:polylines){
            pline.remove();
        }
        polylines.clear();
    }
}