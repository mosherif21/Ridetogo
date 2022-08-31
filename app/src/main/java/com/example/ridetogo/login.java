package com.example.ridetogo;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ridetogo.Listeners.network_listener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

public class login extends AppCompatActivity {
    //ui vars
    private CountryCodePicker country_code;
    private EditText phone_num;
    private Button btn_continue;
    private ProgressBar progressBar;

    //network listener
    private network_listener network_listener = new network_listener();

    //hide keyboard function
    private static void hideSoftKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        //network listener change activity
        com.example.ridetogo.Listeners.network_listener.updateActivity(this, 0);

        //link ui vars
        country_code = findViewById(R.id.countryCodePicker);
        phone_num = findViewById(R.id.phone_number);
        btn_continue = findViewById(R.id.btn_login);
        btn_continue.setClickable(false);
        progressBar = findViewById(R.id.login_progressbar);
        country_code.registerCarrierNumberEditText(phone_num);

        //if number is valid allow continue button
        country_code.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                if (isValidNumber) {
                    btn_continue.setBackgroundColor(Color.BLACK);
                    btn_continue.setClickable(true);
                    btn_continue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.VISIBLE);
                            String full_phone_no = "+" + country_code.getFullNumber();
                            //check if entered number already used by driver
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/");
                            Query checkuser_exists = database.getReference("Users").child("Drivers").orderByChild("Phone").equalTo(full_phone_no.trim());
                            checkuser_exists.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    //if exists go to login activity
                                    if (snapshot.exists()) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        Intent intent = new Intent(login.this, perform_login.class);
                                        intent.putExtra("phone_no", full_phone_no);
                                        intent.putExtra("driver", "yes");
                                        startActivity(intent);
                                        finish();
                                        hideSoftKeyboard(login.this);
                                    } else {
                                        //check if entered number already used by rider
                                        Query checkuser_exists = database.getReference("Users").child("Riders").orderByChild("Phone").equalTo(full_phone_no.trim());
                                        checkuser_exists.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                if (snapshot.exists()) {
                                                    //if exists go to login activity
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    Intent intent = new Intent(login.this, perform_login.class);
                                                    intent.putExtra("phone_no", full_phone_no);
                                                    intent.putExtra("driver", "no");
                                                    startActivity(intent);
                                                    hideSoftKeyboard(login.this);
                                                } else {
                                                    //if number is not registered then go to otp verification activity
                                                    Intent intent = new Intent(login.this, otpverifacation.class);
                                                    intent.putExtra("phone_no", full_phone_no);
                                                    startActivity(intent);
                                                    hideSoftKeyboard(login.this);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    Toast.makeText(login.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });

                } else {
                    btn_continue.setBackgroundColor(Color.parseColor("#7A7979"));
                    btn_continue.setClickable(false);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        //register network listener
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(network_listener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        //unregister network listener on stop
        unregisterReceiver(network_listener);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}