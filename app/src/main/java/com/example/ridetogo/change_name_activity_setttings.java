package com.example.ridetogo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class change_name_activity_setttings extends AppCompatActivity {
    //firebase vars
    private DatabaseReference customer_Ref;
    private String userid;

    //ui vars
    private EditText name;
    private Button btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name_setttings);
        getSupportActionBar().hide();
        //link ui vars
        name = findViewById(R.id.save_newName);
        btn_save = findViewById(R.id.btn_save_newName);

        //get name from previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("user_name");
            name.setText(value);
        }
        //update name in firebase using user id
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                customer_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child("Riders").child(userid);
                customer_Ref.child("Name").setValue(name.getText().toString().trim());
                finish();
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_down);
    }
}