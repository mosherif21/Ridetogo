package com.example.ridetogo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class password_login_Activity extends AppCompatActivity {

    //ui vars
    private Button login;
    private EditText pass;
    private ProgressBar progressBar;

    //general vars
    private String full_phone_no;
    private String isdriver;

    //function to hide keyboard
    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_login);

        //ui vars link
        login = findViewById(R.id.btn_perform_login);
        pass = findViewById(R.id.password_login_txt);
        progressBar = findViewById(R.id.performlogin_progressbar);

        //get user phone number and if he is a driver from previous (login) activity
        full_phone_no = getIntent().getStringExtra("phone_no");
        isdriver = getIntent().getStringExtra("driver");

        //make login button not clickable until data is loaded and the user has entered a valid email and password formats
        login.setClickable(false);

        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pass.getText().toString().length() > 7) {
                    login.setClickable(true);
                    login.setBackgroundColor(Color.BLACK);
                    login.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //make progress bar visible until login is performed or password or email is incorrect
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.VISIBLE);

                        }
                    });
                }
                 else {
                    login.setClickable(false);
                    login.setBackgroundColor(Color.parseColor("#7A7979"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



    }
}