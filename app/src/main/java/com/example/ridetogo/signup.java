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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {
    private FirebaseAuth mauth;
    private EditText txt_name;
    private EditText txt_email;
    private EditText txt_pass;
    private TextView email_error_txt;
    private TextView name_error_txt;
    private TextView pass_error_txt;
    private Button txt_btn_signup;
    private boolean email_confirm;
    private boolean pass_confirm;
    private boolean name_confirm;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();
        String phone_no = getIntent().getStringExtra("phone_no");
        mauth = FirebaseAuth.getInstance();
        txt_name = findViewById(R.id.name_signup);
        txt_email = findViewById(R.id.email_signup);
        txt_pass = findViewById(R.id.password_signup);
        txt_btn_signup = findViewById(R.id.btn_signup);
        email_error_txt = findViewById(R.id.txt_email_error_signup);
        pass_error_txt = findViewById(R.id.txt_password_error);
        name_error_txt = findViewById(R.id.txt_name_error);
        progressBar = findViewById(R.id.signup_progressbar);
        txt_btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass_error_txt.setVisibility(View.INVISIBLE);
                email_error_txt.setVisibility(View.INVISIBLE);
                name_error_txt.setVisibility(View.INVISIBLE);
                pass_confirm = true;
                email_confirm = true;
                name_confirm = true;

                String email = txt_email.getText().toString().trim();
                String pass = txt_pass.getText().toString().trim();
                String name = txt_name.getText().toString().trim();
                if (pass.length() < 8) {
                    pass_confirm = false;
                    pass_error_txt.setText(" password can't be less than 8 characters");
                    pass_error_txt.setVisibility(View.VISIBLE);
                }
                if (name.equals("")) {
                    name_confirm = false;
                    name_error_txt.setText("name can't be empty");
                    name_error_txt.setVisibility(View.VISIBLE);
                }
                if (email.equals("")) {
                    email_confirm = false;
                    email_error_txt.setText("email can't be empty");
                    email_error_txt.setVisibility(View.VISIBLE);
                }
                if (name_confirm && pass_confirm && email_confirm) {
                    progressBar.setVisibility(View.VISIBLE);
                    mauth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                DatabaseReference ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("Users").child("Riders")
                                        .child(user.getUid());
                                ref.child("Name").setValue(name);
                                ref.child("Email").setValue(user.getEmail());
                                ref.child("Phone").setValue(phone_no);
                                ref.child("Balance").setValue(0);
                                mauth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Intent intent = new Intent(signup.this, home.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                email_error_txt.setText("Please enter a valid email");
                                email_error_txt.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }


        });


    }
}