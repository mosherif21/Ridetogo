package com.example.ridetogo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class ride_details_fragment extends Fragment {

    private Button btn_confirm_rdetails;
    private RadioButton wasalny;
    private RadioButton wasalnyplus;
    private String ride_class = "";
    private RadioGroup radioGroup;
    private TextView txt_error;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ride_details_fragment, container, false);
        radioGroup = v.findViewById(R.id.radio_group_ride_details);
        btn_confirm_rdetails = v.findViewById(R.id.btn_confirm_ride_details);
        wasalny = v.findViewById(R.id.wasalny_rd);
        wasalnyplus = v.findViewById(R.id.wasalnyplus_rd);
        txt_error = v.findViewById(R.id.txt_ride_class_error);
        txt_error.setVisibility(View.INVISIBLE);
        btn_confirm_rdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!wasalny.isChecked() && !wasalnyplus.isChecked() || ride_class.isEmpty()) {
                    txt_error.setVisibility(View.VISIBLE);
                } else {
                    txt_error.setVisibility(View.INVISIBLE);
                    Home_fragment parentFrag = ((Home_fragment) ride_details_fragment.this.getParentFragment());
                    parentFrag.confirm_ride_details(ride_class);
                }

            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (wasalny.isChecked())
                    ride_class = "Wasalny";
                else ride_class = "wasalny+";
            }
        });
        return v;
    }
}