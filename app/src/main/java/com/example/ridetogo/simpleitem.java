package com.example.ridetogo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;

public class simpleitem extends navitem<simpleitem.ViewHolder> {


    private int selectedtexttint;
    private int regulartexttint;
    private int icon_res;
    private String title;


    public simpleitem(int icon, String title) {
        this.title = title;
        this.icon_res = icon;
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.navitem_design, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void bindViewHolder(ViewHolder holder) {
        holder.title.setText(title);
        holder.icon.setAnimation(icon_res);
        if (icon_res != R.raw.close) {
            if (isChecked) {

                holder.title.setTextColor(selectedtexttint);
                holder.icon.playAnimation();
                holder.icon.loop(true);
            } else {
                holder.title.setTextColor(regulartexttint);
                holder.icon.setProgress(0);
                holder.icon.pauseAnimation();
            }

            //if(icon_res==R.raw.history_ride_icon){
            //    holder.icon.setScale(30f);
            // }
        }

    }

    protected simpleitem applyselectedtexttint(int selectedtexttint) {
        this.selectedtexttint = selectedtexttint;
        return this;
    }

    protected simpleitem texttint(int regulartexttint) {
        this.regulartexttint = regulartexttint;
        return this;
    }


    protected static class ViewHolder extends nav_items_adapter.ViewHolder {
        private LottieAnimationView icon;
        private TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.navitem_lottie_anim);
            title = itemView.findViewById(R.id.navitem_text);
        }
    }
}
