package com.example.ridetogo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class driver_info_fragment extends Fragment {

    TextView txt_driver_name;
    TextView txt_driver_phone;
    TextView txt_driver_car;
    TextView txt_driver_car_color;
    TextView txt_driver_rating;
    ImageView driver_image;
    String image_profileurl;
    String driver_car_color;
    String driver_car_type;
    String driver_phone;
    String driver_name;
    String driver_rating;
    Button call_driver;

    public driver_info_fragment(String image_profileurl, String driver_car_color, String driver_car_type, String driver_phone, String driver_name,String driver_rating) {
        this.image_profileurl = image_profileurl;
        this.driver_car_color = driver_car_color;
        this.driver_car_type = driver_car_type;
        this.driver_phone = driver_phone;
        this.driver_name = driver_name;
        this.driver_rating=driver_rating;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_driver_info_fragment, container, false);
        txt_driver_name=v.findViewById(R.id.txt_driver_name);
        txt_driver_rating=v.findViewById(R.id.txt_rating_driver_request);
        txt_driver_phone=v.findViewById(R.id.txt_driver_phone);
        txt_driver_car=v.findViewById(R.id.txt_driver_car_model);
        txt_driver_car_color=v.findViewById(R.id.txt_driver_car_color);
        driver_image=v.findViewById(R.id.img_driver_in_ride_rquest);
          call_driver=v.findViewById(R.id.bttn_call_driver_in_deriver_details);
        txt_driver_rating.setText(driver_rating);
        Glide.with(requireActivity()).load(image_profileurl).into(driver_image);
        txt_driver_car_color.setText(driver_car_color);
        txt_driver_car.setText(driver_car_type);
        txt_driver_phone.setText(driver_phone);
        txt_driver_name.setText(driver_name);
        call_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent call_customer=new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+driver_phone));
                startActivity(call_customer);
            }
        });
        return v;
    }
}