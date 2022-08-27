package com.example.ridetogo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class settings_fragment extends Fragment {
    //GET image vars
    final int CHOOSE_PROFILE = 1;
    String userid;
    String name;
    String email;
    String number;
    Button btn_signout;
    Button btn_verify_email;
    TextView name_txt;
    TextView email_txt;
    TextView phone_num_text;
    TextView email_verify_text;
    ImageView user_image;
    DatabaseReference customer_Ref;
    LinearLayout layout_profileImage;
    LinearLayout layout_name;
    LinearLayout layout_email;
    LinearLayout layout_password;
    ProgressBar progressBar;
    FirebaseUser user;
    Uri image_uri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings_fragment, container, false);
        name_txt = v.findViewById(R.id.settings_name_txt);
        email_txt = v.findViewById(R.id.settings_email_txt);
        phone_num_text = v.findViewById(R.id.settings_phone_num_txt);
        user_image = v.findViewById(R.id.settings_user_image);
        progressBar = v.findViewById(R.id.account_Settings_progressbar);
        user = FirebaseAuth.getInstance().getCurrentUser();
        userid = user.getUid();
        customer_Ref = FirebaseDatabase.getInstance("https://ridetogo-dcf8e-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users").child("Riders").child(userid);
        getuserinfo();
        btn_signout = v.findViewById(R.id.btn_signout);
        layout_profileImage = v.findViewById(R.id.settings_profile_picture);
        layout_email = v.findViewById(R.id.settings_email);
        layout_name = v.findViewById(R.id.settings_first_name);
        layout_password = v.findViewById(R.id.settings_password);
        email_verify_text = v.findViewById(R.id.email_verified_text);
        btn_verify_email = v.findViewById(R.id.btn_verify_email);


        btn_verify_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    btn_verify_email.setText("email sent");
                                    btn_verify_email.setClickable(false);
                                }
                            }
                        });
            }

        });
        layout_profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PROFILE);
            }
        });
        layout_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((home) getActivity()), change_name_activity_setttings.class);
                if (!name.isEmpty())
                    intent.putExtra("user_name", name);
                startActivity(intent);
                ((home) getActivity()).overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });
        layout_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((home) getActivity()), change_email_activity.class);
                if (!email.isEmpty())
                    intent.putExtra("user_email", email);
                startActivity(intent);
                ((home) getActivity()).overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });
        layout_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(((home) getActivity()), password_change_activity.class);
                startActivity(intent);
                ((home) getActivity()).overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });
        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(((home) getActivity()));
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to logout")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                ((home) getActivity()).logout();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
        return v;
    }

    private void getverifyEmail() {
        if (user.isEmailVerified()) {
            btn_verify_email.setVisibility(View.INVISIBLE);
            email_verify_text.setText("Verified");
            email_verify_text.setTextColor(Color.parseColor("#4CAF50"));
            //JToast.makeText(((home)getActivity()), "verified", JToast.LENGTH_SHORT).show();
        } else {
            btn_verify_email.setVisibility(View.VISIBLE);
            email_verify_text.setText("Not verified");
            email_verify_text.setTextColor(Color.RED);
            //JToast.makeText(((home)getActivity()), "not verified", JToast.LENGTH_SHORT).show();
        }
    }

    private void getuserinfo() {
        progressBar.setVisibility(View.VISIBLE);
        customer_Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 1) {
                    Map<String, Object> datamap = (Map<String, Object>) snapshot.getValue();
                    if (datamap.get("Name") != null) {
                        name = datamap.get("Name").toString();
                        name_txt.setText(datamap.get("Name").toString());
                    }
                    if (datamap.get("Email") != null) {
                        email = datamap.get("Email").toString();
                        email_txt.setText(datamap.get("Email").toString());
                    }
                    if (datamap.get("Phone") != null) {
                        phone_num_text.setText(datamap.get("Phone").toString());
                    }
                    if (datamap.get("profile_image") != null) {
                        String image_profileurl = datamap.get("profile_image").toString();
                        Glide.with(((home) getActivity())).load(image_profileurl).into(user_image);
                    }

                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getverifyEmail();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PROFILE && resultCode == Activity.RESULT_OK) {
            image_uri = data.getData();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_image").child(userid);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(((home) getActivity()).getApplication().getContentResolver(), image_uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            user_image.setImageBitmap(bitmap);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
            byte[] imagedata = outputStream.toByteArray();
            UploadTask uploadTask = storageRef.putBytes(imagedata);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map modifyImage = new HashMap();
                            modifyImage.put("profile_image", uri.toString());
                            customer_Ref.updateChildren(modifyImage);
                        }
                    });
                }
            });
        }
    }
}