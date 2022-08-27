package com.example.ridetogo;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;


public class onboarding_fragment5 extends Fragment {
    TextView txt_getstarted;
    LottieAnimationView btn_getstarted;
    Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboarding_fragment5, container, false);
        btn_getstarted = v.findViewById(R.id.splash_button_getstarted);
        txt_getstarted = v.findViewById(R.id.frag5_text_getstarted);

        btn_getstarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_getstarted.playAnimation();
                btn_getstarted.setClickable(false);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txt_getstarted.setVisibility(View.GONE);
                    }
                }, 600);
                ((MainActivity) getActivity()).skip_intro(0);
            }
        });


        return v;
    }
}