package com.example.ridetogo;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class ride_details_fragment extends Fragment {

Button btn_confirm_rdetails;
RadioButton wasalny;
RadioButton wasalnyplus;
String ride_class;
RadioGroup radioGroup;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_ride_details_fragment, container, false);
        radioGroup=v.findViewById(R.id.radio_group_ride_details);
        btn_confirm_rdetails=v.findViewById(R.id.btn_confirm_ride_details);
        wasalny=v.findViewById(R.id.wasalny_rd);
        btn_confirm_rdetails.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(wasalny.isChecked())
                     ride_class="Wasalny";
                 else ride_class="wasalny+";
                 Home_fragment parentFrag = ((Home_fragment)ride_details_fragment.this.getParentFragment());
                 parentFrag.confirm_ride_details(ride_class);
             }
         });
        btn_confirm_rdetails.setClickable(false);
        btn_confirm_rdetails.setFocusable(false);
        wasalnyplus=v.findViewById(R.id.wasalnyplus_rd);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                btn_confirm_rdetails.setClickable(true);
                btn_confirm_rdetails.setFocusable(true);
                btn_confirm_rdetails.setBackgroundColor(Color.BLACK);
            }
        });
        return v;
    }
}