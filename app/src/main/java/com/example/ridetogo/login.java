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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ridetogo.Listeners.network_listener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

public class login extends AppCompatActivity {
    //google sign in vars
    private static int google_sign_in_Requestcode = 4321;
    //ui vars
    private CountryCodePicker country_code;
    private EditText phone_num;
    private Button btn_continue;
    private ProgressBar progressBar;
    private GoogleSignInButton btn_google_signUp;
    //network listener
    private network_listener network_listener = new network_listener();
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleSignInClient;

    //hide keyboard function
    private static void hideSoftKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        //inputMethodManager.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
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
        btn_google_signUp = findViewById(R.id.btn_googleSignup);

        //google sign in vars initialize
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(firebase_google_keys_ids.google_web_client_id)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        //google sign up button listener
        btn_google_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.VISIBLE);
                Intent google_signIn_intent = googleSignInClient.getSignInIntent();
                startActivityForResult(google_signIn_intent, google_sign_in_Requestcode);
            }
        });

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
                            hideSoftKeyboard(login.this);
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
                                                    finish();
                                                } else {
                                                    //if number is not registered then go to otp verification activity
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    Intent intent = new Intent(login.this, otpverifacation.class);
                                                    intent.putExtra("phone_no", full_phone_no);
                                                    intent.putExtra("loginORsignupORother", "signup");
                                                    startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == google_sign_in_Requestcode) {
            Task<GoogleSignInAccount> google_Account_task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount google_account = google_Account_task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(google_account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/");
                                    Query checkuser_exists = database.getReference("Users").child("Drivers").orderByChild("Email").equalTo(email);
                                    checkuser_exists.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            hideSoftKeyboard(login.this);
                                            //if exists go to login activity
                                            if (snapshot.exists()) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Intent intent = new Intent(login.this, driver_MapsActivity.class);
                                                startActivity(intent);

                                            } else {
                                                //check if entered number already used by rider
                                                Query checkuser_exists = database.getReference("Users").child("Riders").orderByChild("Email").equalTo(email);
                                                checkuser_exists.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                        if (snapshot.exists()) {
                                                            //if exists go to login activity
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                            Intent intent = new Intent(login.this, home.class);
                                                            startActivity(intent);
                                                        } else {
                                                            //if number is not registered then go to otp verification activity
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                            Intent intent = new Intent(login.this, phone_otp_Enter.class);
                                                            intent.putExtra("driver", "no");
                                                            intent.putExtra("loginORsignupORother", "google_signup");
                                                            startActivity(intent);
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
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                }
                            }
                        });
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }
}