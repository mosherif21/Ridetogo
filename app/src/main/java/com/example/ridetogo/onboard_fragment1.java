package com.example.ridetogo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;


public class onboard_fragment1 extends Fragment {

    //on boarding fragment from 1 to 5 is just an intro with animation and each one contains a skip button
    private LottieAnimationView animation;
    private TextView skip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_onboard_fragment1, container, false);
        animation = v.findViewById(R.id.splash_animation1);
        animation.pauseAnimation();

        skip = v.findViewById(R.id.frag1_skip_btn);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).skip_intro();
            }
        });

        return v;
    }

    protected void playAnimation() {
        if (animation != null)
            animation.playAnimation();
    }
}