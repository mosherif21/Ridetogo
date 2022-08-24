package com.example.ridetogo.RidesHistoryClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridetogo.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RideHistoryViewHolder> {
    private List<RideHistoryObject> itemList;
    private Context context;
    public  HistoryAdapter(List<RideHistoryObject> itemList,Context context){
        this.itemList=itemList;
        this.context=context;
    }
    @NonNull
    @Override
    public RideHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView= LayoutInflater.from(parent.getContext()).inflate(R.layout.ridehistory_item_layout,null,false);
        RecyclerView.LayoutParams lp=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        RideHistoryViewHolder viewHolder=new RideHistoryViewHolder(layoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RideHistoryViewHolder holder, int position) {
           holder.rideId_txt.setText(itemList.get(position).getRideId());
           holder.ride_date.setText(itemList.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
