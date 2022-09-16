package com.example.ridetogo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class perform_login extends AppCompatActivity {

    //ui vars
    private ProgressBar progressBar;
    private String full_phone_no;
    private String isdriver;
    private Button login_withOtp;
    private Button login_withpassword;

    //firebase vars
    private FirebaseAuth mauth;

    //function to hide keyboard
    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perform_login);
        getSupportActionBar().hide();

        // hideSoftKeyboard(perform_login.this);

        //firebase vars initialize
        mauth = FirebaseAuth.getInstance();

        //ui vars link
        progressBar = findViewById(R.id.performlogin_progressbar);
        progressBar.setVisibility(View.VISIBLE);
        login_withOtp = findViewById(R.id.btn_login_withotp);
        login_withpassword = findViewById(R.id.btn_login_withpassword);

        //get user phone number and if he is a driver from previous (login) activity
        full_phone_no = getIntent().getStringExtra("phone_no");
        isdriver = getIntent().getStringExtra("driver");
        progressBar.setVisibility(View.INVISIBLE);

        //get if user is a rider or driver

        login_withOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(perform_login.this, otpverifacation.class);
                intent.putExtra("phone_no", full_phone_no);
                intent.putExtra("loginORsignupORother", "login");
                intent.putExtra("driver", isdriver);
                startActivity(intent);
                finish();
            }
        });

        login_withpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(perform_login.this, password_login_Activity.class);
                intent.putExtra("phone_no", full_phone_no);
                intent.putExtra("driver", isdriver);
                startActivity(intent);
                finish();
            }
        });


    }
}