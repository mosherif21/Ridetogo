package com.example.ridetogo.RidesHistoryClasses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridetogo.R;
import com.example.ridetogo.RideHistoryPreview;

public class RideHistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideId_txt;
    public TextView ride_date;
    public RideHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        rideId_txt= itemView.findViewById(R.id.ride_history_rideId);
        ride_date= itemView.findViewById(R.id.txt_ride_history_date);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent(v.getContext(), RideHistoryPreview.class);
        intent.putExtra("rideId",rideId_txt.getText().toString());
        v.getContext().startActivity(intent);

    }
}
