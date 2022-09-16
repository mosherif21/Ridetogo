package com.example.ridetogo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;

public class phone_otp_Enter extends AppCompatActivity {
    //ui vars
    private CountryCodePicker country_code;
    private EditText phone_num;
    private Button btn_continue;

    //general vars
    private String signORloginORother;
    private String isdriver;

    //hide keyboard function
    private static void hideSoftKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        //inputMethodManager.showSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_otp_enter);
        getSupportActionBar().hide();

        //link ui vars
        country_code = findViewById(R.id.countryCodePicker_phone_otp);
        phone_num = findViewById(R.id.phone_number_phone);
        btn_continue = findViewById(R.id.btn_phone_otp_login);
        btn_continue.setClickable(false);
        country_code.registerCarrierNumberEditText(phone_num);

        //get data from previous activity
        signORloginORother = getIntent().getStringExtra("loginORsignupORother");
        isdriver = getIntent().getStringExtra("driver");

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
                            String full_phone_no = "+" + country_code.getFullNumber();
                            Intent intent = new Intent(phone_otp_Enter.this, otpverifacation.class);
                            intent.putExtra("phone_no", full_phone_no);
                            intent.putExtra("loginORsignupORother", signORloginORother);
                            intent.putExtra("driver", isdriver);
                            startActivity(intent);
                            hideSoftKeyboard(phone_otp_Enter.this);
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
    protected void onResume() {
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}