package com.example.ridetogo;


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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class perform_login extends AppCompatActivity {

    //ui vars
    private Button login;
    private EditText pass;
    private EditText email;
    private ProgressBar progressBar;
    private String full_phone_no;
    private String isdriver;

    //firebase vars
    private String child_ref;
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

        //firebase vars initialize
        mauth = FirebaseAuth.getInstance();

        //ui vars link
        login = findViewById(R.id.btn_perform_login);
        pass = findViewById(R.id.password_login_txt);
        email = findViewById(R.id.email_login_txt);
        progressBar = findViewById(R.id.performlogin_progressbar);

        //make login button not clickable until data is loaded and the user has entered a valid email and password formats
        login.setClickable(false);

        //get user phone number and if he is a driver from previous (login) activity
        full_phone_no = getIntent().getStringExtra("phone_no");
        isdriver = getIntent().getStringExtra("driver");

        //get if user is a rider or driver to check his email
        if (isdriver.equals("yes"))
            child_ref = "Drivers";
        else
            child_ref = "Riders";

        //password edittext change listener to make login button clickable when password is >= 8 characters
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pass.getText().toString().length() > 7 && email.getText().toString().length() > 12) {
                    login.setClickable(true);
                    login.setBackgroundColor(Color.BLACK);
                    login.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //make progress bar visible until login is performed or password or email is incorrect
                            hideSoftKeyboard(perform_login.this);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.VISIBLE);

                            //check if email is correct
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/");
                            Query checkuser_exists = database.getReference("Users").child(child_ref).orderByChild("Email").equalTo(email.getText().toString().trim());
                            checkuser_exists.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    if (snapshot.exists()) {
                                        //if email is correct sign in using email and password
                                        mauth.signInWithEmailAndPassword(email.getText().toString().trim(), pass.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    //if sign in is successful and isdriver is yes then go to driver activity if it's no go to home activity
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Intent intent;
                                                    if (isdriver.equals("yes"))
                                                        intent = new Intent(perform_login.this, driver_MapsActivity.class);

                                                    else
                                                        intent = new Intent(perform_login.this, home.class);

                                                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                                                    finish();
                                                } else {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(perform_login.this, "wrong email or password", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    Toast.makeText(perform_login.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                } else {
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