package com.example.ridetogo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class playmusic_fragment extends Fragment {

    private Button playBtn, pauseBtn;
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_playmusic_fragment, container, false);
        playBtn = v.findViewById(R.id.BtnPlay);
        pauseBtn = v.findViewById(R.id.BtnPause);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((home) getActivity()).play_music_req("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((home) getActivity()).pause_music_req();
            }
        });
        return v;
    }
}