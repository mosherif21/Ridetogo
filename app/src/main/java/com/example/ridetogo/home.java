package com.example.ridetogo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridetogo.Listeners.network_listener;
import com.google.firebase.auth.FirebaseAuth;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Arrays;

public class home extends AppCompatActivity implements nav_items_adapter.OnItemSelectedListener {

    private static final int LOCATION_REQUEST_CODE = 10;

    //navigation bar variables for items selected
    private static final int frag_home = 1;
    private static final int frag_notifications = 2;
    private static final int frag_ride_history = 3;
    private static final int frag_settings = 4;
    private static final int frag_playMusic = 5;
    private static final int frag_Complain = 7;
    private static final int frag_Contact_us = 8;

    //location and network listeners
    private network_listener network_listener;
    private location_listener location_listener;

    //home fragment instance
    private Home_fragment home = new Home_fragment();

    //fragment manager vars
    private FragmentTransaction transaction;
    private FragmentManager manager;

    //two container views bec home fragment contains google maps which can't be replaced with others in same fragment
    private FragmentContainerView home_container_view;
    private FragmentContainerView other_container_view;

    //ui vars
    private Toolbar toolbar;
    private RecyclerView nav_list;
    private String[] nav_titles;
    private int[] nav_icons;
    private SlidingRootNav navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        //hide action bar
        getSupportActionBar().hide();

        //link ui vars
        com.example.ridetogo.Listeners.network_listener.updateActivity(this, 1);
        home_container_view = findViewById(R.id.home_fragment_container);
        other_container_view = findViewById(R.id.fragment_other_fragments);
        other_container_view.setVisibility(View.INVISIBLE);
        toolbar = findViewById(R.id.home_toolbar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        manager = getSupportFragmentManager();

        //listeners initialize
        location_listener = new location_listener();
        network_listener = new network_listener();

        //check if app has location permission if not request it
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        //navigation var assign
        nav_icons = getnavicons();
        nav_titles = getnavtitle();
        navigation = new SlidingRootNavBuilder(this)
                .withDragDistance(170)
                .withRootViewScale(0.75f)
                .withRootViewElevation(25)
                .withRootViewYTranslation(4)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withToolbarMenuToggle(toolbar)
                .withMenuLayout(R.layout.navigation_menu)
                .inject();

        //initialize navigation bar items
        nav_items_adapter adapter = new nav_items_adapter(Arrays.asList(
                new spaceitem(80),
                nav(frag_home)
                , nav(frag_notifications),
                nav(frag_ride_history),
                nav(frag_settings),
                nav(frag_playMusic),
                new spaceitem(100),
                nav(frag_Complain),
                nav(frag_Contact_us)
        ));
        adapter.setListener(this);
        nav_list = findViewById(R.id.nav_items_list);
        nav_list.setNestedScrollingEnabled(false);
        nav_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        nav_list.setAdapter(adapter);
        adapter.setSelected(frag_home);
    }

    //navigation bar items icons array
    private int[] getnavicons() {
        int[] iconres = new int[9];
        iconres[0] = 0;
        iconres[1] = R.raw.home_icon;
        iconres[2] = R.raw.notification_icon;
        iconres[3] = R.raw.history2_icon;
        iconres[4] = R.raw.settings_icon;
        iconres[5] = R.raw.music_icon;
        iconres[6] = 0;
        iconres[7] = R.raw.supporticon;
        iconres[8] = R.raw.supporticon;
        return iconres;
    }

    //navigation bar items titles array
    private String[] getnavtitle() {
        String[] titles = new String[9];
        titles[0] = "";
        titles[1] = "Home";
        titles[2] = "Notifications";
        titles[3] = "My Trips";
        titles[4] = "Settings";
        titles[5] = "Play music";
        titles[6] = "";
        titles[7] = "Complain";
        titles[8] = "Contact Us";
        return titles;
    }

    //change navigation bar items titles color based on what is selected
    private navitem nav(int counter) {
        return new simpleitem(nav_icons[counter], nav_titles[counter])
                .applyselectedtexttint(Color.parseColor("#000000"))
                .texttint(Color.parseColor("#878787"));
    }

    //logout function to be used by settings fragment
    protected void logout() {
        logout_priv();
    }

    private void logout_priv() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(home.this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    //back press override to close the navigation bar if opened or notify home home fragment that back press was triggered
    @Override
    public void onBackPressed() {
        if (navigation.isMenuOpened()) {
            navigation.closeMenu(true);
        } else if (home_container_view.getVisibility() == View.VISIBLE && navigation.isMenuClosed()) {
            home.first_back = false;
            home.back_key();
        } else {
            moveTaskToBack(true);
        }
    }

    //replace fragments in fragment container based on selected item
    @Override
    public void onItemSelected(int position) {
        transaction = manager.beginTransaction();

        if (position == frag_home) {
            String tag = "home";
            Fragment check = manager.findFragmentByTag(tag);
            if (check != null) {
                home_container_view.setVisibility(View.VISIBLE);
                other_container_view.setVisibility(View.INVISIBLE);
            } else {
                transaction.add(R.id.home_fragment_container, home, tag);
                other_container_view.setVisibility(View.INVISIBLE);
            }


        }
        if (position == frag_notifications) {

            other_container_view.setVisibility(View.VISIBLE);
            transaction.replace(R.id.fragment_other_fragments, new Notifications());
            home_container_view.setVisibility(View.INVISIBLE);
        }
        if (position == frag_ride_history) {

            other_container_view.setVisibility(View.VISIBLE);
            transaction.replace(R.id.fragment_other_fragments, new rides_history());
            home_container_view.setVisibility(View.INVISIBLE);
        }
        if (position == frag_settings) {

            other_container_view.setVisibility(View.VISIBLE);
            transaction.replace(R.id.fragment_other_fragments, new settings_fragment());
            home_container_view.setVisibility(View.INVISIBLE);
        }

        if (position == frag_playMusic) {

            other_container_view.setVisibility(View.VISIBLE);
            transaction.replace(R.id.fragment_other_fragments, new playmusic_fragment());
            home_container_view.setVisibility(View.INVISIBLE);
        }

        if (position == frag_Complain) {
            other_container_view.setVisibility(View.VISIBLE);
            transaction.replace(R.id.fragment_other_fragments, new complain_fragment());
            home_container_view.setVisibility(View.INVISIBLE);
        }


        if (position == frag_Contact_us) {

            other_container_view.setVisibility(View.VISIBLE);
            transaction.replace(R.id.fragment_other_fragments, new contactus_fragment());
            home_container_view.setVisibility(View.INVISIBLE);
        }

        navigation.closeMenu();
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //if location services is not turned on launch listener popup
    private void loc() {
        if (!gps_connection.locationTurnedOn(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View no_internet_layout = LayoutInflater.from(this).inflate(R.layout.no_location_dialogue, null);
            builder.setView(no_internet_layout);
            Button turn_location = no_internet_layout.findViewById(R.id.btn_turn_location);

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
            turn_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);

                    Toast.makeText(this, "Please accept locations permission to use the app", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        //register network and location listeners
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(network_listener, filter);
        IntentFilter filter2 = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(location_listener, filter2);
        super.onStart();
    }

    @Override
    protected void onResume() {
        loc();
        super.onResume();
    }

    @Override
    protected void onStop() {
        //unregister listeners on stop of activity
        unregisterReceiver(network_listener);
        unregisterReceiver(location_listener);
        super.onStop();
    }

    //2 following functions to invoke play and pause music from music fragment to home fragment
    protected void play_music_req(String song_url) {
        home.public_play_music_Request(song_url);
    }

    protected void pause_music_req() {
        home.public_pause_music_Request();
    }
}