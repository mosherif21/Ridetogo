package com.example.ridetogo;

import android.view.ViewGroup;

public abstract class navitem<T extends nav_items_adapter.ViewHolder> {


    protected boolean isChecked = false;

    protected abstract T createViewHolder(ViewGroup parent);

    protected abstract void bindViewHolder(T holder);

    protected boolean isChecked() {
        return isChecked;
    }

    protected navitem<T> setChecked(boolean ischecked) {
        this.isChecked = ischecked;
        return this;
    }

    protected boolean isSelectable() {
        return true;
    }


}
