package com.example.ridetogo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    //ui vars
    private ImageView logo, backimg;
    private LottieAnimationView animation_lot;
    private boarding_page_adapter adapter;
    private SharedPreferences shpref;
    protected static onboard_fragment1 onboard_fragment1_instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //link ui vars
        logo = findViewById(R.id.splash_logo);
        backimg = findViewById(R.id.splash_back);
        animation_lot = findViewById(R.id.splash_animation);
        onboard_fragment1_instance = new onboard_fragment1();

        //logo animation initialize
        Animation anima = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_anim);
        anima.setStartOffset(1000);
        anima.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //check if app intro was skipped or viewed from shared preferences if not start intro
                shpref = getSharedPreferences("MyPrefsFile", 0);
                if (!shpref.getBoolean("skip_intro", true)) {
                    launch();
                } else {
                    ViewPager viewpager;
                    backimg.animate().translationY(2500).setStartDelay(2500).setDuration(500);
                    logo.animate().translationY(2500).setStartDelay(2500).setDuration(500);
                    animation_lot.animate().translationY(2500).setStartDelay(2500).setDuration(500);
                    Animation splash_animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_anim);
                    splash_animation.setStartOffset(2500);
                    adapter = new boarding_page_adapter(getSupportFragmentManager());
                    viewpager = findViewById(R.id.splash_viewpager);
                    viewpager.setAdapter(adapter);
                    viewpager.setAnimation(splash_animation);
                    onboard_fragment1_instance.playAnimation();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        logo.setAnimation(anima);


        //firebase authentication initialize
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());


    }

    //function to skip intro from on boarding fragments
    protected void skip_intro() {
        shpref.edit().putBoolean("skip_intro", false).apply();
        launch();
    }

    private void launch() {
        //check if there is a user logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            //check if the user logged in is a driver or a rider and launch the corresponding activity
            Query checkuser_exists = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").
                    getReference("Users").child("Riders").orderByChild("Email").equalTo(user.getEmail());
            checkuser_exists.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    backimg.animate().translationY(1800);
                    logo.animate().translationY(1800);
                    animation_lot.animate().translationY(1800);
                    if (snapshot.exists()) {
                        Intent intent = new Intent(MainActivity.this, home.class);
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        finish();
                    } else {
                        Intent intent = new Intent(MainActivity.this, driver_MapsActivity.class);
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            //if there is no user logged in go to login activity
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private static class boarding_page_adapter extends FragmentStatePagerAdapter {
        public boarding_page_adapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MainActivity.onboard_fragment1_instance;
                case 1:
                    return new onboarding_fragment2();
                case 2:
                    return new onboarding_fragment3();
                case 3:
                    return new onboarding_fragment4();
                case 4:
                    return new onboarding_fragment5();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}