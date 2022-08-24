package com.example.ridetogo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.chaos.view.PinView;
import com.example.ridetogo.Listeners.network_listener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class otpverifacation extends AppCompatActivity {
         TextView otp_inst;
         TextView txt_timer;
         TextView txt_wrong_number;
         PinView pin_entered;
         Button btn_verify;
         Button btn_resend;
         ProgressBar progressBar;
    network_listener network_listener=new network_listener();
    private FirebaseAuth mAuth;
    String code_from_system;
    static  CountDownTimer timer1;
    LottieAnimationView timer_icon;
    String phone_no;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverifacation);
        getSupportActionBar().hide();
        com.example.ridetogo.Listeners.network_listener.updateActivity(this,1);
        phone_no=getIntent().getStringExtra("phone_no");
        otp_inst=findViewById(R.id.otp_text_inst);
        txt_timer=findViewById(R.id.txt_timer);
        txt_wrong_number=findViewById(R.id.txt_wrong_number);
        pin_entered=findViewById(R.id.pin_otp_fromuser);
        btn_verify=findViewById(R.id.btn_verify_otp);
        btn_resend=findViewById(R.id.btn_resend_otp);
        btn_resend.setVisibility(View.INVISIBLE);
        timer_icon=findViewById(R.id.timer_anim);
        progressBar=findViewById(R.id.otp_progressbar);
        btn_verify.setClickable(false);
        otp_inst.setText("Please enter one time OTP code sent to\n"+phone_no);
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        mAuth = FirebaseAuth.getInstance();

      //  mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        sendotp_code(phone_no);
        CountDownTimer timer1=new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txt_timer.setText(""+millisUntilFinished/1000+"s");
                if(millisUntilFinished/1000==5){
                    txt_timer.setTextColor(Color.RED);
                }
                if(millisUntilFinished/1000==55){
                    txt_wrong_number.setVisibility(View.VISIBLE);
                  }
            }
            @Override
            public void onFinish() {
                pin_entered.setEnabled(false);
                timer_icon.pauseAnimation();
                timer_icon.setProgress(0f);
                btn_verify.setClickable(false);
                btn_verify.setBackgroundColor(Color.parseColor("#7A7979"));
                btn_resend.setVisibility(View.VISIBLE);
                btn_resend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(otpverifacation.this, otpverifacation.class);
                        intent.putExtra("phone_no",phone_no);
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        finish();
                    }
                });
            }
        }.start();
        pin_entered.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pin= pin_entered.getText().toString();
                if(pin.length()==6){
                    btn_verify.setClickable(true);
                    btn_verify.setBackgroundColor(Color.BLACK);
                    btn_verify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBar.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.VISIBLE);
                            String code=pin_entered.getText().toString();
                            try{
                                verify_otp_code(code);
                            }catch (Exception e){

                            }
                        }
                    });
                }
                else{
                    btn_verify.setClickable(false);
                    btn_verify.setBackgroundColor(Color.parseColor("#7A7979"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

         txt_wrong_number.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 finish();
             }
         });
    }
 private void sendotp_code(String phone_no) {
       PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone_no)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
  private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            code_from_system=s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
          String code=phoneAuthCredential.getSmsCode();
          if(code!=null){
              pin_entered.setText(code);
              verify_otp_code(code);

          }
          else    Toast.makeText(otpverifacation.this, "code is not null", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(otpverifacation.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verify_otp_code(String code) {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(code_from_system,code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            progressBar.setVisibility(View.INVISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            FirebaseAuth mauth=FirebaseAuth.getInstance();
                            mauth.signOut();
                            Intent intent=new Intent(otpverifacation.this, signup.class);
                            intent.putExtra("phone_no",phone_no);
                            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                            finish();
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(otpverifacation.this, "Invalid otp code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
    @Override
    protected void onStart() {
        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(network_listener,filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(network_listener);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
       finish();
    }
}