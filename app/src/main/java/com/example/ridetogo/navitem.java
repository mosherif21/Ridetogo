package com.example.ridetogo;

import android.view.ViewGroup;

public abstract class navitem<T extends nav_items_adapter.ViewHolder> {


    boolean isChecked = false;

    public abstract T createViewHolder(ViewGroup parent);

    public abstract void bindViewHolder(T holder);

    public boolean isChecked() {
        return isChecked;
    }

    public navitem<T> setChecked(boolean ischecked) {
        this.isChecked = ischecked;
        return this;
    }

    public boolean isSelectable() {
        return true;
    }


}
