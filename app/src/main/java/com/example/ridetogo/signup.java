package com.example.ridetogo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {

    //ui vars
    private EditText txt_name;
    private EditText txt_email;
    private EditText txt_pass;
    private TextView email_error_txt;
    private TextView name_error_txt;
    private TextView pass_error_txt;
    private Button txt_btn_signup;

    //firebase and general vars
    private FirebaseAuth mauth;
    private boolean email_confirm;
    private boolean pass_confirm;
    private boolean name_confirm;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();

        //get phone number from previous activity
        String phone_no = getIntent().getStringExtra("phone_no");
        // PhoneAuthCredential credential=(PhoneAuthCredential)getIntent().getParcelableExtra("phoneAuth_Credential");

        //firebase authorization initialize
        mauth = FirebaseAuth.getInstance();

        //link ui vars
        txt_name = findViewById(R.id.name_signup);
        txt_email = findViewById(R.id.email_signup);
        txt_pass = findViewById(R.id.password_signup);
        txt_btn_signup = findViewById(R.id.btn_signup);
        email_error_txt = findViewById(R.id.txt_email_error_signup);
        pass_error_txt = findViewById(R.id.txt_password_error);
        name_error_txt = findViewById(R.id.txt_name_error);
        progressBar = findViewById(R.id.signup_progressbar);

        //sign up button click listener
        txt_btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                pass_error_txt.setVisibility(View.INVISIBLE);
                email_error_txt.setVisibility(View.INVISIBLE);
                name_error_txt.setVisibility(View.INVISIBLE);
                pass_confirm = true;
                email_confirm = true;
                name_confirm = true;

                //get values from edit text
                String email = txt_email.getText().toString().trim();
                String pass = txt_pass.getText().toString().trim();
                String name = txt_name.getText().toString().trim();
                if (pass.length() < 8) {
                    pass_confirm = false;
                    pass_error_txt.setText(" password can't be less than 8 characters");
                    pass_error_txt.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                if (name.equals("")) {
                    name_confirm = false;
                    name_error_txt.setText("name can't be empty");
                    name_error_txt.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                if (!email.isEmpty() && email.length() < 7) {
                    email_confirm = false;
                    email_error_txt.setText("email can't be empty");
                    email_error_txt.setVisibility(View.VISIBLE);
                }
                if (name_confirm && pass_confirm && email_confirm) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders")
                            .child(user.getUid());
                    ref.child("Name").setValue(name);
                    ref.child("Email").setValue(email);
                    ref.child("Phone").setValue(phone_no);
                    ref.child("Balance").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.INVISIBLE);
                                Intent intent = new Intent(signup.this, home.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
            }
        });


    }
}