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

    RecyclerView history_recycler_view;
    RecyclerView.Adapter ride_history_adapter;
    RecyclerView.LayoutManager ride_history_manager;
    ArrayList RideHistory = new ArrayList<RideHistoryObject>();
    ProgressBar progressBar;
    TextView txt_previous;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rides_history, container, false);
        history_recycler_view = v.findViewById(R.id.ride_history_recycler_view);
        progressBar = v.findViewById(R.id.ride_history_fragment_progressbar);
        txt_previous = v.findViewById(R.id.txt_previous_ride_history_frag);
        progressBar.setVisibility(View.VISIBLE);
        history_recycler_view.setNestedScrollingEnabled(false);
        history_recycler_view.setHasFixedSize(true);
        ride_history_manager = new LinearLayoutManager(((home) getActivity()));
        history_recycler_view.setLayoutManager(ride_history_manager);
        ride_history_adapter = new HistoryAdapter(getDataHistory(), ((home) getActivity()));
        history_recycler_view.setAdapter(ride_history_adapter);
        getRideHistory();
        return v;

    }

    private void getRideHistory() {
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders").child(userid).child("ride_history");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot rides : snapshot.getChildren()) {
                        FetchRideInformation(rides.getKey());
                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    txt_previous.setText("You have no previous rides");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void FetchRideInformation(String key) {
        // String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    RideHistoryObject obj = new RideHistoryObject(rideId, getDateOfTimeStamp(timeStamp));
                    RideHistory.add(obj);
                    ride_history_adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.INVISIBLE);
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

    ArrayList<RideHistoryObject> getDataHistory() {
        return RideHistory;
    }
}