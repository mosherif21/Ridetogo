package com.example.ridetogo;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ride_location_fragment extends Fragment {
    private EditText pickup_loc;
    private Location user_location;
    private Button skip_destination;

    protected ride_location_fragment(Location user_location) {
        this.user_location = user_location;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ride_location_fragment, container, false);
        pickup_loc = v.findViewById(R.id.edit_text_pickup);
        skip_destination = v.findViewById(R.id.btn_skip_destination);
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyBSMkFBTs_UmxJdV2KQG5YuNYevT1eGStk");
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        Home_fragment parentFrag = ((Home_fragment) ride_location_fragment.this.getParentFragment());
        skip_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFrag.close_ride_location("", new LatLng(0.0, 0.0));
            }
        });
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG));
        //autocompleteFragment.setCountry("EG");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                JToast.makeText((getActivity()), "place.get", JToast.LENGTH_SHORT).show();
                parentFrag.close_ride_location(place.getName(), place.getLatLng());
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(user_location.getLatitude(), user_location.getLongitude(), 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                String _Location = listAddresses.get(0).getAddressLine(0);
                pickup_loc.setText(_Location);
                pickup_loc.setFocusable(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return v;
    }
}