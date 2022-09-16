package com.example.ridetogo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Home_fragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener {
    private static final int[] COLORS = new int[]{R.color.black};
    protected boolean first_back = false;
    //ui or general vars
    private LottieAnimationView map_marker_pickup_point;
    private TextView dest_text;
    private TextView ride_request_progress_txt;
    private ConstraintLayout locations_rideDetails_layout;
    private ConstraintLayout ride_request_progress_layout;
    private ConstraintLayout confirm_pickup_point_layout;
    private ConstraintLayout layout_ride_ongoing;
    private LottieAnimationView set_myloc;
    private GoogleApiClient googleApiClient;
    private Location mlocation;
    private LocationRequest location_Request;
    private GoogleMap mymap;
    private GeoFire geofire;
    private String userid;
    private CardView btn_whereto;
    private FragmentTransaction transaction;
    private SupportMapFragment mapsupp;
    private SlidingUpPanelLayout panel;
    private Button btn_cancel_request;
    private ProgressBar progressBar;
    private boolean ongoing_Ride = false;
    //ride request vars
    private String cust_id;
    private LatLng pickupLocation;
    private LatLng pickup_made_request_latlng = null;
    private LatLng destination_location_latlng;
    private Marker driver_loc_marker;
    private Marker pickup_point_marker;
    private GeoQuery geoQuery1;
    private DatabaseReference driver_loc;
    private ValueEventListener driver_locListener;
    private boolean request_bol = false;
    private String destination_of_ride_request_chosen_name;
    private Button btn_confirm_pickup_point;
    private Button btn_ride_details;
    private String driver_liscense_plate;
    private String driver_name;
    private String driver_phone;
    private String driver_car_type;
    private String driver_car_color;
    private LatLng driver_latlng;
    private EditText confirm_pickup_point_txt;
    private String driver_class;
    private LatLng confirmed_pickup_latlng;
    private Button btn_driver_info;
    private LottieAnimationView search_driver_anim;
    private Button btn_call_help;
    private int routePickupOrpickuppoint = 0;
    private Marker chosen_destination_marker;
    private int back_state = 0;
    private boolean pickup_point_bool = false;
    //notification
    private int notificationId = 20;
    private NotificationCompat.Builder builder;
    private NotificationManagerCompat notificationManager;
    private View v;
    private ValueEventListener listener_pickup;
    private boolean notify_once = false;
    private boolean notify_once3 = false;
    private boolean notify_once2 = false;
    private DatabaseReference driver_ref;
    private ValueEventListener driver_listener;
    private ValueEventListener listener;
    private String average_driver_rating;
    private String image_profileurl;
    private Boolean zoom_first_time = false;
    private boolean bol_zoom_onDriver = false;
    private int radius = 1;
    private boolean FoundDriver = false;
    private String FoundDriver_uid;
    //route drawing vars
    private List<Polyline> polylines;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home_fragment, container, false);
        progressBar = v.findViewById(R.id.home_fragment_user_progressbar);
        polylines = new ArrayList<>();
        cust_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        locations_rideDetails_layout = v.findViewById(R.id.layout_set_locations);
        ride_request_progress_layout = v.findViewById(R.id.layout_ride_request_progress);
        confirm_pickup_point_layout = v.findViewById(R.id.confirm_pickup_point);
        ride_request_progress_layout.setVisibility(View.INVISIBLE);
        confirm_pickup_point_layout.setVisibility(View.INVISIBLE);
        layout_ride_ongoing = v.findViewById(R.id.layout_ride_ongoing);
        map_marker_pickup_point = v.findViewById(R.id.map_marker_pickup_point);
        map_marker_pickup_point.setVisibility(View.INVISIBLE);
        panel = v.findViewById(R.id.home_fragment);
        dest_text = v.findViewById(R.id.text_destination);
        confirm_pickup_point_txt = v.findViewById(R.id.confirm_pickup_point_text);
        ride_request_progress_txt = v.findViewById(R.id.text_ride_request_progress);
        btn_confirm_pickup_point = v.findViewById(R.id.btn_confirm_pickup_point);
        set_myloc = v.findViewById(R.id.locicon);
        set_myloc.setMaxProgress(0.92f);
        btn_whereto = v.findViewById(R.id.where_to_btn);
        btn_ride_details = v.findViewById(R.id.btn_ride_details);
        btn_call_help = v.findViewById(R.id.btn_call_help);
        btn_driver_info = v.findViewById(R.id.btn_driver_details);
        btn_cancel_request = v.findViewById(R.id.btn_cancel_ride_rquest);

        btn_driver_info.setVisibility(View.INVISIBLE);
        //notifications
        notificationManager = NotificationManagerCompat.from((getActivity()));
        createNotificationChannel();


        search_driver_anim = v.findViewById(R.id.anim_driver_search);

        mapsupp = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (ActivityCompat.checkSelfPermission((getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((getActivity()), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapsupp.getMapAsync(this);
        }
        btn_whereto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    Fragment ride_locations = new ride_location_fragment(mlocation);
                    transaction = getChildFragmentManager().beginTransaction();
                    transaction.replace(R.id.slide_up_fragment, ride_locations).commit();
                }
            }
        });
        btn_cancel_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));
                builder.setTitle("Cancel Request");
                builder.setMessage("Are you sure you want to cancel ride request?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                cancel_request();
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
        btn_ride_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_ride_details_set_ride_dataonmap();
            }
        });
        btn_whereto.setClickable(false);

        btn_ride_details.setClickable(false);
        btn_ride_details.setFocusable(false);
        btn_ride_details.setBackgroundColor(Color.parseColor("#7A7979"));
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ride_end_notify();
        return v;
    }

    protected void back_key() {
        if (!first_back) {
            back_key_apply();
            first_back = true;
        }
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    back_key_apply();
                    return true;
                }
                return false;
            }
        });
    }

    private void back_key_apply() {
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else if (back_state == 1) {
            locations_rideDetails_layout.setVisibility(View.VISIBLE);
            confirm_pickup_point_layout.setVisibility(View.INVISIBLE);
            map_marker_pickup_point.setVisibility(View.INVISIBLE);
            pickup_point_bool = false;
            routePickupOrpickuppoint = 0;
            close_ride_location(destination_of_ride_request_chosen_name, destination_location_latlng);
            back_state = 0;
        } else {
            ((getActivity())).moveTaskToBack(true);
        }
        // JToast.makeText((getActivity()), "back key pressed", JToast.LENGTH_SHORT).show();
    }

    private void cancel_request() {
        fn_endpickupProgress();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        location_Request = new LocationRequest();
        location_Request.setInterval(1000);
        location_Request.setFastestInterval(1000);
        location_Request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission((getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((getActivity()), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, location_Request, this);
        check_ongoing_ride();
        check_ongoing_request();
        check_made_Request();


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void close_ride_location(String destination, LatLng adestination_latlng) {
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }

        destination_location_latlng = adestination_latlng;
        destination_of_ride_request_chosen_name = destination;
        if (!destination.equals("")) {
            if (destination.length() > 20)
                destination = destination.substring(0, 20) + "...";
            dest_text.setText(destination);
            drawDestinationonMap();
        } else {
            dest_text.setText("Not Specified");
            eraseRoutePolyLines();
            if (mlocation != null) {
                LatLng L = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
                mymap.moveCamera(CameraUpdateFactory.newLatLng(L));
                mymap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
            }
        }


        btn_ride_details.setClickable(true);
        btn_ride_details.setFocusable(true);
        btn_ride_details.setBackgroundColor(Color.BLACK);
        if (chosen_destination_marker != null)
            chosen_destination_marker.remove();
        chosen_destination_marker = mymap.addMarker(new MarkerOptions().position((adestination_latlng)).title(destination).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_flag_small)));

    }

    private void drawDestinationonMap() {
        getRouteToMarker(destination_location_latlng);
    }

    private void open_ride_details_set_ride_dataonmap() {
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            Fragment ride_details_fragment = new ride_details_fragment();
            transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.slide_up_fragment, ride_details_fragment).commit();
        }
    }

    protected void confirm_ride_details(String drive_class) {
        driver_class = drive_class;
        if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        if (chosen_destination_marker != null)
            chosen_destination_marker.remove();
        eraseRoutePolyLines();
        pickup_point_bool = true;
        getPickupPoint();
        back_state = 1;
    }

    private void getPickupPoint() {
        routePickupOrpickuppoint = 2;
        LatLng L = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
        mymap.moveCamera(CameraUpdateFactory.newLatLng(L));
        mymap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
        locations_rideDetails_layout.setVisibility(View.INVISIBLE);
        confirm_pickup_point_layout.setVisibility(View.VISIBLE);
        map_marker_pickup_point.setVisibility(View.VISIBLE);
        mymap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (pickup_point_bool)
                    map_marker_pickup_point.setProgress(0f);
            }
        });
        mymap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (pickup_point_bool) {
                    map_marker_pickup_point.setMaxProgress(0.16f);
                    map_marker_pickup_point.playAnimation();
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(mymap.getCameraPosition().target);
                    confirmed_pickup_latlng = marker.getPosition();
                    getRouteToMarkerWalking(confirmed_pickup_latlng);
                    Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    try {
                        List<Address> listAddresses = geocoder.getFromLocation(confirmed_pickup_latlng.latitude, confirmed_pickup_latlng.longitude, 1);
                        if (null != listAddresses && listAddresses.size() > 0) {
                            String pickup_location = listAddresses.get(0).getAddressLine(0);
                            confirm_pickup_point_txt.setText(pickup_location);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        btn_confirm_pickup_point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eraseRoutePolyLines();
                mymap.setOnCameraIdleListener(null);
                map_marker_pickup_point.setVisibility(View.INVISIBLE);
                confirm_pickup_point_layout.setVisibility(View.INVISIBLE);
                pickup_point_marker = mymap.addMarker(new MarkerOptions().position((confirmed_pickup_latlng)).title("Pickup Point").icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_flag_small)));
                request_ride(driver_class);
            }
        });
    }

    private void request_ride(String ride_class) {
        if (!request_bol) {
            back_state = 0;
            request_bol = true;
            pickupLocation = confirmed_pickup_latlng;
            pickup_made_request_latlng = confirmed_pickup_latlng;
            ride_request_progress_layout.setVisibility(View.VISIBLE);
            search_driver_anim.setVisibility(View.VISIBLE);
            ride_request_progress_txt.setText("Finding your driver....");

            DatabaseReference ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("CustomerRequest");
            geofire = new GeoFire(ref);
            geofire.setLocation(userid, new GeoLocation(confirmed_pickup_latlng.latitude, confirmed_pickup_latlng.longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Location saved on server successfully!");
                    }
                }
            });
            ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("CurrentRequest");
            HashMap datamap = new HashMap();
            datamap.put("ride_Class", ride_class);
            if (destination_of_ride_request_chosen_name.equals(""))
                datamap.put("destination_name", "no_dest");
            else
                datamap.put("destination_name", destination_of_ride_request_chosen_name);

            datamap.put("destination_lat", destination_location_latlng.latitude);
            datamap.put("destination_lng", destination_location_latlng.longitude);
            ref.updateChildren(datamap);
            findClosestDriver(ride_class, pickupLocation);
        }

    }

    private void findClosestDriver(String ride_class, LatLng pickup_latlng_search) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("AvailableDrivers");
        geofire = new GeoFire(ref);
        geoQuery1 = geofire.queryAtLocation(new GeoLocation(pickup_latlng_search.latitude, pickup_latlng_search.longitude), radius);
        geoQuery1.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!FoundDriver && request_bol) {
                    FoundDriver_uid = key;
                    geoQuery1.removeAllListeners();
                    Query query1 = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(FoundDriver_uid).child("wasalnyClass");
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String found_driverclass = snapshot.getValue(String.class);
                                if (found_driverclass.equals(ride_class)) {
                                    driver_found_action();
                                } else {
                                    FoundDriver_uid = null;
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if (!FoundDriver && radius < 20) {
                    radius++;
                    findClosestDriver(ride_class, pickup_latlng_search);
                    System.out.println("radius " + radius);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void check_ongoing_ride() {
        DatabaseReference ongoing_Req_ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ongoingRide");
        ongoing_Req_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FoundDriver_uid = snapshot.getValue(String.class);
                    locations_rideDetails_layout.setVisibility(View.INVISIBLE);
                    DatabaseReference ongoing_request_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference()
                            .child("Users").child("Drivers").child(FoundDriver_uid).child("customerRequest");
                    ongoing_request_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getChildrenCount() > 1) {
                                Map<String, Object> datamap = (Map<String, Object>) snapshot.getValue();
                                if (datamap.get("CustomerRideId") != null) {
                                    cust_id = datamap.get("CustomerRideId").toString();
                                }
                                if (datamap.get("destination_name") != null) {
                                    destination_of_ride_request_chosen_name = datamap.get("destination_name").toString();
                                }
                                double destination_lat = 0;
                                double destination_lng = 0;
                                if (datamap.get("destination_lat") != null) {
                                    destination_lat = Double.parseDouble(datamap.get("destination_lat").toString());
                                }
                                if (datamap.get("destination_lng") != null) {
                                    destination_lng = Double.parseDouble(datamap.get("destination_lng").toString());
                                }
                                destination_location_latlng = new LatLng(destination_lat, destination_lng);
                                DatabaseReference pickup_point_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("CustomerRequest").child(userid).child("l");
                                pickup_point_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            List<Object> map_loc = (List<Object>) snapshot.getValue();
                                            double pickup_long = 0;
                                            double pickup_lat = 0;
                                            if (map_loc.get(0) != null)
                                                pickup_lat = Double.parseDouble(map_loc.get(0).toString());
                                            if (map_loc.get(1) != null)
                                                pickup_long = Double.parseDouble(map_loc.get(1).toString());
                                            pickupLocation = new LatLng(pickup_lat, pickup_long);
                                            pickup_made_request_latlng = pickupLocation;
                                            zoom_first_time = true;
                                            request_bol = true;

                                            startRide();
                                            getHasRideEnded();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void check_ongoing_request() {
        DatabaseReference ongoing_Req_ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ongoingRequest");
        ongoing_Req_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //JToast.makeText((getActivity()), "ongoing request detected", JToast.LENGTH_SHORT).show();
                    FoundDriver_uid = snapshot.getValue().toString();
                    DatabaseReference ongoing_request_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference()
                            .child("Users").child("Drivers").child(FoundDriver_uid).child("customerRequest");
                    ongoing_request_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getChildrenCount() > 1) {
                                Map<String, Object> datamap = (Map<String, Object>) snapshot.getValue();
                                if (datamap.get("CustomerRideId") != null) {
                                    cust_id = datamap.get("CustomerRideId").toString();
                                }
                                if (datamap.get("destination_name") != null) {
                                    destination_of_ride_request_chosen_name = datamap.get("destination_name").toString();
                                }
                                double destination_lat = 0;
                                double destination_lng = 0;
                                if (datamap.get("destination_lat") != null) {
                                    destination_lat = Double.parseDouble(datamap.get("destination_lat").toString());
                                }
                                if (datamap.get("destination_lng") != null) {
                                    destination_lng = Double.parseDouble(datamap.get("destination_lng").toString());
                                }
                                destination_location_latlng = new LatLng(destination_lat, destination_lng);
                                DatabaseReference pickup_point_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("CustomerRequest").child(userid).child("l");
                                pickup_point_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            List<Object> map_loc = (List<Object>) snapshot.getValue();
                                            double pickup_long = 0;
                                            double pickup_lat = 0;
                                            if (map_loc.get(0) != null)
                                                pickup_lat = Double.parseDouble(map_loc.get(0).toString());
                                            if (map_loc.get(1) != null)
                                                pickup_long = Double.parseDouble(map_loc.get(1).toString());
                                            pickupLocation = new LatLng(pickup_lat, pickup_long);
                                            pickup_made_request_latlng = pickupLocation;
                                            pickup_point_marker = mymap.addMarker(new MarkerOptions().position((pickupLocation)).title("Pickup Point").icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_flag_small)));
                                            getDriverLocation();
                                            getHasRideEnded();
                                            getDriverInfo();
                                            getdriverAskPick(FoundDriver_uid);
                                            request_bol = true;
                                            zoom_first_time = true;
                                            locations_rideDetails_layout.setVisibility(View.INVISIBLE);
                                            ride_request_progress_layout.setVisibility(View.VISIBLE);
                                            btn_driver_info.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void check_made_Request() {
        DatabaseReference made_request_ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("CurrentRequest");
        made_request_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 1) {
                    back_state = 0;
                    request_bol = true;
                    Map<String, Object> datamap = (Map<String, Object>) snapshot.getValue();
                    if (datamap.get("ride_Class") != null) {
                        driver_class = datamap.get("ride_Class").toString();
                    }
                    if (datamap.get("destination_name") != null) {
                        destination_of_ride_request_chosen_name = datamap.get("destination_name").toString();
                    }
                    double destination_lat = 0;
                    double destination_lng = 0;
                    if (datamap.get("destination_lat") != null) {
                        destination_lat = Double.parseDouble(datamap.get("destination_lat").toString());
                    }
                    if (datamap.get("destination_lng") != null) {
                        destination_lng = Double.parseDouble(datamap.get("destination_lng").toString());
                    }
                    destination_location_latlng = new LatLng(destination_lat, destination_lng);

                    DatabaseReference customer_req_ref_loc = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("CustomerRequest").child(userid).child("l");
                    customer_req_ref_loc.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                List<Object> map_loc = (List<Object>) snapshot.getValue();
                                double pickup_long = 0;
                                double pickup_lat = 0;
                                if (map_loc.get(0) != null)
                                    pickup_lat = Double.parseDouble(map_loc.get(0).toString());
                                if (map_loc.get(1) != null)
                                    pickup_long = Double.parseDouble(map_loc.get(1).toString());
                                pickupLocation = new LatLng(pickup_lat, pickup_long);
                                pickup_made_request_latlng = pickupLocation;
                                pickup_point_marker = mymap.addMarker(new MarkerOptions().position((pickupLocation)).title("Pickup Point").icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_flag_small)));
                                locations_rideDetails_layout.setVisibility(View.INVISIBLE);
                                ride_request_progress_layout.setVisibility(View.VISIBLE);
                                search_driver_anim.setVisibility(View.VISIBLE);
                                ride_request_progress_txt.setText("Finding your driver....");
                                findClosestDriver(driver_class, pickup_made_request_latlng);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void driver_found_action() {
        FoundDriver = true;
        if (FoundDriver_uid != null) {

            DatabaseReference driverRef = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference()
                    .child("Users").child("Drivers").child(FoundDriver_uid).child("customerRequest");
            HashMap datamap = new HashMap();
            datamap.put("CustomerRideId", cust_id);
            if (destination_of_ride_request_chosen_name.equals(""))
                datamap.put("destination_name", "no_dest");
            else
                datamap.put("destination_name", destination_of_ride_request_chosen_name);

            datamap.put("destination_lat", destination_location_latlng.latitude);
            datamap.put("destination_lng", destination_location_latlng.longitude);
            driverRef.updateChildren(datamap);
            ride_request_progress_txt.setText("Finding your driver's location");
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("CurrentRequest").setValue(null);
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ongoingRequest").setValue(FoundDriver_uid);
            getDriverLocation();
            getHasRideEnded();
            getDriverInfo();
            getdriverAskPick(FoundDriver_uid);
            search_driver_anim.setVisibility(View.INVISIBLE);
            btn_driver_info.setVisibility(View.VISIBLE);
        }
    }

    private void getdriverAskPick(String driverid) {

        DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("pickRequest").child(driverid).child("pickCustomer");
        if (listener_pickup != null)
            reference.removeEventListener(listener_pickup);
        listener_pickup = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pcikup_string = snapshot.getValue().toString();
                    if (pcikup_string.equals("ask")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        View pickup = LayoutInflater.from(getActivity()).inflate(R.layout.askuserpick_layout_popup, null);
                        builder.setView(pickup);
                        TextView driver_lice = pickup.findViewById(R.id.txt_driverLicense_pickup);
                        Button accept = pickup.findViewById(R.id.btn_accept_ride_pickup);
                        Button reject = pickup.findViewById(R.id.btn_refuse_ride_pickup);
                        AlertDialog dialog = builder.create();
                        driver_lice.setText("Plate: " + driver_liscense_plate);
                        dialog.show();
                        dialog.getWindow().setGravity(Gravity.CENTER);
                        accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                reference.removeEventListener(listener_pickup);
                                reference.setValue("OK");
                                startRide();
                            }
                        });
                        reject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                reference.setValue(null);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startRide() {
        ride_request_progress_layout.setVisibility(View.INVISIBLE);
        layout_ride_ongoing.setVisibility(View.VISIBLE);
        eraseRoutePolyLines();
        if (driver_loc != null && driver_locListener != null)
            driver_loc.removeEventListener(driver_locListener);
        if (pickup_point_marker != null)
            pickup_point_marker.remove();
        if (driver_loc_marker != null)
            driver_loc_marker.remove();
        ongoing_Ride = true;
        routePickupOrpickuppoint = 3;
        if (FoundDriver_uid != null)
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ongoingRide").setValue(FoundDriver_uid);
        FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ongoingRequest").setValue(null);
        if (destination_location_latlng.latitude != 0 && destination_location_latlng.longitude != 0) {
            chosen_destination_marker = mymap.addMarker(new MarkerOptions().position((destination_location_latlng)).title(destination_of_ride_request_chosen_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_flag_small)));
            getRouteToMarker2(destination_location_latlng);
        }
        btn_call_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent call_customer = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:122"));
                startActivity(call_customer);
            }
        });
    }

    private void getDriverLocation() {
        driver_loc = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("DriversWorking").child(FoundDriver_uid).child("l");
        driver_locListener = driver_loc.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && request_bol) {
                    List<Object> map_loc = (List<Object>) snapshot.getValue();
                    double driver_long = 0;
                    double driver_lat = 0;

                    if (map_loc.get(0) != null)
                        driver_lat = Double.parseDouble(map_loc.get(0).toString());
                    if (map_loc.get(1) != null)
                        driver_long = Double.parseDouble(map_loc.get(1).toString());

                    LatLng driver_loc_latlng = new LatLng(driver_lat, driver_long);
                    if (driver_loc_marker != null) {
                        driver_loc_marker.remove();
                    }
                    Location loc_pickup = new Location("");
                    loc_pickup.setLatitude(pickup_made_request_latlng.latitude);
                    loc_pickup.setLongitude(pickup_made_request_latlng.longitude);
                    Location loc_driver = new Location("");
                    loc_driver.setLatitude(driver_loc_latlng.latitude);
                    loc_driver.setLongitude(driver_loc_latlng.longitude);
                    float distance_between = loc_pickup.distanceTo(loc_driver);
                    if (distance_between < 300 && distance_between > 100) {
                        ride_request_progress_txt.setText("Driver is near");
                        notify_driver_near();
                    } else if (distance_between < 100) {
                        ride_request_progress_txt.setText("Driver is at pickup");
                        notify_driver_arrrived();
                    } else {
                        ride_request_progress_txt.setText("Driver Found");
                    }
                    driver_latlng = driver_loc_latlng;
                    driver_loc_marker = mymap.addMarker(new MarkerOptions().position((driver_loc_latlng)).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.car_small)));
                    routePickupOrpickuppoint = 1;
                    getRouteToMarker2(driver_loc_latlng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ASSISTANT";
            String description = "ASSISTANT";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Assistant", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (getActivity()).getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void notify_driver_near() {
        if (!notify_once) {
            notify_once = true;
            builder = new NotificationCompat.Builder((getActivity()), "Assistant")
                    .setSmallIcon(R.drawable.logo_ridetogo)
                    .setContentTitle("Driver is near")
                    .setContentText("Your driver is near to the pickup location.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void notify_ride_end(String price) {
        if (!notify_once3) {
            notify_once3 = true;
            builder = new NotificationCompat.Builder((getActivity()), "Assistant")
                    .setSmallIcon(R.drawable.logo_ridetogo)
                    .setContentTitle("Ridetogo")
                    .setContentText("Ride has ended! your total is " + price + " EGP")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void notify_driver_arrrived() {
        if (!notify_once2) {
            notify_once2 = true;
            builder = new NotificationCompat.Builder((getActivity()), "Assistant")
                    .setSmallIcon(R.drawable.logo_ridetogo)
                    .setContentTitle("Ridetogo")
                    .setContentText("Your driver has arrived!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void getRouteToMarker2(LatLng destination_latlng) {
        if (pickup_made_request_latlng != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(pickup_made_request_latlng, destination_latlng)
                    .key("AIzaSyDhwZjpwoi5aX3XwxkbVyLzdHbyT-6KcOw")
                    .build();
            routing.execute();
        }
    }
private void ride_end_notify(){
    DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Ride_end_notify").child(userid).child("price");
    listener = reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                String price = snapshot.getValue().toString();
                notify_ride_end(price);
                AlertDialog.Builder builder = new AlertDialog.Builder((getActivity()));
                builder.setTitle("Ride end");
                builder.setMessage("Your total is " + price + " EGP")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                reference.setValue(null);
                reference.removeEventListener(listener);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
}
    private void getHasRideEnded() {
        driver_ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(FoundDriver_uid).child("customerRequest");
        driver_listener = driver_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    if (FoundDriver_uid != null) {
                        ride_end_notify();
                        fn_endpickupProgress();
                }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fn_endpickupProgress() {
        if (request_bol) {
            notify_once2 = false;
            notify_once = false;
            notify_once3 = false;
            bol_zoom_onDriver = false;
            routePickupOrpickuppoint = 0;
            request_bol = false;
            ongoing_Ride = false;
            if (geoQuery1 != null)
                geoQuery1.removeAllListeners();
            layout_ride_ongoing.setVisibility(View.INVISIBLE);
            if (chosen_destination_marker != null)
                chosen_destination_marker.remove();
            if (FoundDriver_uid != null) {
                DatabaseReference driver_ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(FoundDriver_uid).child("customerRequest");
                if (driver_ref != null)
                    driver_ref.setValue(null);
                FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("DriversWorking").child(FoundDriver_uid).setValue(null);
                FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("pickRequest").child(FoundDriver_uid).setValue(null);
                FoundDriver_uid = null;
            }
            if (driver_ref != null && driver_listener != null)
                driver_ref.removeEventListener(driver_listener);
            FoundDriver = false;
            radius = 1;
            if (driver_loc_marker != null)
                driver_loc_marker.remove();
            if (pickup_point_marker != null)
                pickup_point_marker.remove();
            DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("CustomerRequest");
            GeoFire geofire = new GeoFire(reference);
            geofire.removeLocation(userid, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                }
            });
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("CurrentRequest").setValue(null);
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ongoingRequest").setValue(null);
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ongoingRide").setValue(null);
            dest_text.setText("Where to?");
            ride_request_progress_layout.setVisibility(View.INVISIBLE);
            map_marker_pickup_point.setVisibility(View.INVISIBLE);
            locations_rideDetails_layout.setVisibility(View.VISIBLE);
            btn_ride_details.setClickable(false);
            btn_ride_details.setFocusable(false);
            btn_ride_details.setBackgroundColor(Color.parseColor("#7A7979"));
            btn_driver_info.setVisibility(View.INVISIBLE);
            eraseRoutePolyLines();
            LatLng L = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
            mymap.moveCamera(CameraUpdateFactory.newLatLng(L));
            mymap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
        }
    }

    private void getDriverInfo() {
        if (FoundDriver_uid != null) {
            DatabaseReference customer_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child("Drivers").child(FoundDriver_uid);
            customer_Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getChildrenCount() > 1) {
                        Map<String, Object> datamap = (Map<String, Object>) snapshot.getValue();
                        if (datamap.get("Name") != null) {
                            driver_name = datamap.get("Name").toString();
                        }
                        if (datamap.get("Phone") != null) {
                            driver_phone = datamap.get("Phone").toString();
                        }
                        if (datamap.get("carModel") != null) {
                            driver_car_type = datamap.get("carModel").toString();
                        }
                        if (datamap.get("carColor") != null) {
                            driver_car_color = datamap.get("carColor").toString();
                        }
                        if (datamap.get("LicensePlate") != null) {
                            driver_liscense_plate = datamap.get("LicensePlate").toString();
                        }
                        if (datamap.get("profile_image") != null) {
                            image_profileurl = datamap.get("profile_image").toString();
                        }
                        int sum = 0;
                        float total_num = 0;
                        for (DataSnapshot rating_child : snapshot.child("rating").getChildren()) {
                            sum += Integer.valueOf(rating_child.getValue().toString());
                            total_num++;
                        }
                        float average;
                        if (sum != 0) {
                            average = sum / total_num;
                            average_driver_rating = average + "";
                        } else average_driver_rating = "Not specified";
                        btn_driver_info.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (panel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                    panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                                    transaction = getChildFragmentManager().beginTransaction();
                                    Fragment driver_info_fragment = new driver_info_fragment(image_profileurl, driver_car_color, driver_car_type, driver_phone, driver_name, average_driver_rating);
                                    transaction.replace(R.id.slide_up_fragment, driver_info_fragment).commit();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mlocation = location;
        if (!zoom_first_time) {
            zoom_first_time = true;
            LatLng L = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
            mymap.moveCamera(CameraUpdateFactory.newLatLng(L));
            mymap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
        }
        btn_whereto.setClickable(true);
    }

    protected synchronized void setGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder((getActivity()))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mymap = googleMap;
        //  Boolean isNight;
        //  Calendar cal = Calendar.getInstance();
        //   int hour = cal.get(Calendar.HOUR_OF_DAY);
        //  if (hour < 6 || hour > 18) {
        //      isNight = true;
        //  } else {
        //      isNight = false;
        //  }
        //  if (isNight) {
        //      googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.night_mode));
        //      map_marker.setAnimation(R.raw.map_marker_night);
        //  } else {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.day_mode));
        //  }

        if (ActivityCompat.checkSelfPermission((getActivity()).getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((getActivity()).getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        setGoogleApiClient();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        set_myloc.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (mlocation != null) {
                    LatLng L = new LatLng(mlocation.getLatitude(), mlocation.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(L));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 1000, null);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getRouteToMarker(LatLng customer_lat_lng) {
        if (mlocation != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mlocation.getLatitude(), mlocation.getLongitude()), customer_lat_lng)
                    .key("AIzaSyDhwZjpwoi5aX3XwxkbVyLzdHbyT-6KcOw")
                    .build();
            routing.execute();
        }
    }

    private void getRouteToMarkerWalking(LatLng customer_lat_lng) {
        if (mlocation != null) {
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mlocation.getLatitude(), mlocation.getLongitude()), customer_lat_lng)
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

            if (routePickupOrpickuppoint == 0) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(mlocation.getLatitude(), mlocation.getLongitude()));
                builder.include(destination_location_latlng);
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width * 0.3);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mymap.animateCamera(cameraUpdate);
                JToast.makeText((getActivity()), "duration: " + route.get(i).getDurationValue() / 60 + " mins", JToast.LENGTH_SHORT).show();
            } else if (routePickupOrpickuppoint == 1 && !bol_zoom_onDriver) {
                bol_zoom_onDriver = true;
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(pickup_made_request_latlng);
                builder.include(driver_latlng);
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width * 0.3);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mymap.animateCamera(cameraUpdate);
            } else if (routePickupOrpickuppoint == 3) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(pickup_made_request_latlng);
                builder.include(destination_location_latlng);
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int padding = (int) (width * 0.3);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mymap.animateCamera(cameraUpdate);
                JToast.makeText((getActivity()), "duration: " + route.get(i).getDurationValue() / 60 + " mins", JToast.LENGTH_SHORT).show();
            } else if (routePickupOrpickuppoint == 2) {
                JToast.makeText((getActivity()), "duration: " + route.get(i).getDurationValue() / 60 + " mins", JToast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void send_music_play_request(String song_url) {
        if (ongoing_Ride && FoundDriver_uid != null) {
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(FoundDriver_uid).child("playsong").setValue(song_url);
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(FoundDriver_uid).child("pausesong").setValue(null);
        }
    }

    protected void public_play_music_Request(String song_url) {
        send_music_play_request(song_url);
    }

    private void send_music_pause_request() {
        if (ongoing_Ride && FoundDriver_uid != null) {
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(FoundDriver_uid).child("playsong").setValue(null);
            FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Drivers").child(FoundDriver_uid).child("pausesong").setValue(true);
        }
    }

    protected void public_pause_music_Request() {
        send_music_pause_request();
    }
}