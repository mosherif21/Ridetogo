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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class change_email_activity extends AppCompatActivity {
    //firebase variables
    FirebaseUser user;
    DatabaseReference customer_Ref;
    String userid;

    //ui vars
    EditText email_editor;
    EditText pass_editor;
    Button btn_save;
    TextView email_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        getSupportActionBar().hide();
        //link ui vars with corresponding layout
        email_editor = findViewById(R.id.save_newEmail);
        btn_save = findViewById(R.id.btn_save_newEmail);
        email_error = findViewById(R.id.txt_email_change_error);
        pass_editor = findViewById(R.id.password_for_emailChange);

        //get email from previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("user_email");
            email_editor.setText(value);
        }
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email_editor.getText().toString().trim().isEmpty() || pass_editor.getText().toString().trim().isEmpty()) {
                    email_error.setText("email or password can't be empty");
                    email_error.setVisibility(View.VISIBLE);
                } else {
                    email_error.setVisibility(View.INVISIBLE);
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    final String email = user.getEmail();

                    //sign in with old email and password entered if successful update email
                    AuthCredential credential = EmailAuthProvider.getCredential(email, pass_editor.getText().toString().trim());
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user.updateEmail(email_editor.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            JToast.makeText(change_email_activity.this, "error " + email_editor.getText().toString().trim(), JToast.LENGTH_SHORT).show();
                                            email_error.setText("An error happened please try again!");
                                            email_error.setVisibility(View.VISIBLE);
                                        } else {
                                            //email_error.setVisibility(View.INVISIBLE);
                                            userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            customer_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child("Riders").child(userid);
                                            customer_Ref.child("Email").setValue(email_editor.getText().toString().trim());
                                            finish();
                                        }
                                    }
                                });
                            } else {
                                email_error.setText("password is incorrect!");
                                email_error.setVisibility(View.VISIBLE);
                            }
                        }
                    });
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