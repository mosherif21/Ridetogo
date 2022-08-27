package com.example.ridetogo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class spaceitem extends navitem<spaceitem.ViewHolder> {
    private int spacedp;

    public spaceitem(int spacedp) {
        this.spacedp = spacedp;
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        View view = new View(context);
        int height = (int) (context.getResources().getDisplayMetrics().density * spacedp);
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        ));
        return new ViewHolder(view);
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public void bindViewHolder(ViewHolder holder) {

    }

    public class ViewHolder extends nav_items_adapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
