package com.example.ridetogo;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;


public class onboard_fragment1 extends Fragment {

    private LottieAnimationView animation;
    private TextView skip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboard_fragment1, container, false);
        animation = v.findViewById(R.id.splash_animation1);
        animation.pauseAnimation();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animation.playAnimation();
            }
        }, 3000);

        skip = v.findViewById(R.id.frag1_skip_btn);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).skip_intro(1);
            }
        });

        return v;
    }
}