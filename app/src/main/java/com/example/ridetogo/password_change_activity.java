package com.example.ridetogo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class password_change_activity extends AppCompatActivity {
    private FirebaseUser user;

    private Button btn_password_save;
    private EditText old_password;
    private EditText new_password;
    private TextView password_error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);
        getSupportActionBar().hide();
        btn_password_save = findViewById(R.id.btn_save_newPassword);
        old_password = findViewById(R.id.old_password_input);
        new_password = findViewById(R.id.new_password_input);
        password_error = findViewById(R.id.txt_password_change_error);


        btn_password_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new_password.getText().toString().trim().length() < 8) {
                    password_error.setText("New password can't be less than 8 characters");
                    password_error.setVisibility(View.VISIBLE);
                } else {
                    if (old_password.getText().toString().trim().length() < 8) {
                        password_error.setText("old password is incorrect");
                        password_error.setVisibility(View.VISIBLE);
                    } else {
                        password_error.setVisibility(View.INVISIBLE);
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        final String email = user.getEmail();
                        AuthCredential credential = EmailAuthProvider.getCredential(email, old_password.getText().toString().trim());

                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(new_password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {

                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                password_error.setText("An error happened please try again!");
                                                password_error.setVisibility(View.VISIBLE);
                                            } else {
                                                password_error.setVisibility(View.INVISIBLE);
                                                finish();
                                            }
                                        }
                                    });
                                } else {
                                    password_error.setText("Old password is wrong!");
                                    password_error.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_down);
    }
}