package com.example.ridetogo;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridetogo.RidesHistoryClasses.HistoryAdapter;
import com.example.ridetogo.RidesHistoryClasses.RideHistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class rides_history extends Fragment {

    //ui vars
    private RecyclerView history_recycler_view;
    private ProgressBar progressBar;
    private TextView txt_previous;

    //ride history rides recyclerview vars
    private ArrayList RideHistory;
    private RecyclerView.Adapter ride_history_adapter;
    private RecyclerView.LayoutManager ride_history_manager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rides_history, container, false);

        //ui vars link
        history_recycler_view = v.findViewById(R.id.ride_history_recycler_view);
        progressBar = v.findViewById(R.id.ride_history_fragment_progressbar);
        txt_previous = v.findViewById(R.id.txt_previous_ride_history_frag);

        //make loading bar visible until recycler view is occupied
        progressBar.setVisibility(View.VISIBLE);

        //recycler view initialize and adapter initialize and set
        history_recycler_view.setNestedScrollingEnabled(false);
        history_recycler_view.setHasFixedSize(true);
        ride_history_manager = new LinearLayoutManager(((home) getActivity()));
        history_recycler_view.setLayoutManager(ride_history_manager);
        RideHistory = new ArrayList<RideHistoryObject>();
        ride_history_adapter = new HistoryAdapter(getDataHistory(), ((home) getActivity()));
        history_recycler_view.setAdapter(ride_history_adapter);

        //history rides get function
        getRideHistory();
        return v;
    }

    private void getRideHistory() {
        //get user id and use it to get if user has ride history entry
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ride_history");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //if snapshot exists fetch rides using ride history key
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot rides : snapshot.getChildren()) {
                        FetchRideInformation(rides.getKey());
                    }
                } else {
                    //else display to user that he has no rides
                    progressBar.setVisibility(View.INVISIBLE);
                    txt_previous.setText("You have no previous rides");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //fetch each ride info using ride history reference key
    private void FetchRideInformation(String key) {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("RidesHistory").child(key);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String rideId = snapshot.getKey();
                    Long timeStamp = 0l;
                    for (DataSnapshot rideData : snapshot.getChildren()) {
                        if (rideData.getKey().equals("timeStamp")) {
                            timeStamp = Long.valueOf(rideData.getValue().toString());
                        }
                    }
                    
                    //occupy ride history object instance and add to ride history arraylist
                    RideHistoryObject obj = new RideHistoryObject(rideId, getDateOfTimeStamp(timeStamp));
                    RideHistory.add(obj);

                    //ride history adapter notify data set changed
                    ride_history_adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //convert timestamp to date function
    private String getDateOfTimeStamp(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timestamp * 1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm", calendar).toString();
        return date;
    }

    //ride history arraylist return function required by ride history recyclerview adapter
    private ArrayList<RideHistoryObject> getDataHistory() {
        return RideHistory;
    }
}