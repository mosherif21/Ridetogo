package com.example.ridetogo;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class playmusic_fragment extends Fragment {

    Button playBtn, pauseBtn;
    MediaPlayer mediaPlayer;
    View v;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_playmusic_fragment, container, false);
        playBtn = v.findViewById(R.id.BtnPlay);
        pauseBtn = v.findViewById(R.id.BtnPause);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((home)getActivity()).play_music_req("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((home)getActivity()).pause_music_req();
            }
        });
        return v;
    }

}