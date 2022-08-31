package com.example.ridetogo;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class nav_items_adapter extends RecyclerView.Adapter<nav_items_adapter.ViewHolder> {

    private List<navitem> navitems;
    private Map<Class<? extends navitem>, Integer> naviewstypes;
    private SparseArray<navitem> viewholder_factory;
    private OnItemSelectedListener listener;


    protected nav_items_adapter(List<navitem> items) {
        this.navitems = items;
        this.naviewstypes = new HashMap<>();
        this.viewholder_factory = new SparseArray<>();
        processViewTypes();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = viewholder_factory.get(viewType).createViewHolder(parent);
        //
        holder.drawerAdapter = this;
        //
        return holder;
    }

    private void processViewTypes() {
        int i = 0;
        for (navitem item : navitems) {
            if (!naviewstypes.containsKey(item.getClass())) {
                naviewstypes.put(item.getClass(), i);
                viewholder_factory.put(i, item);
                i++;
            }
        }
    }

    protected void setSelected(int position) {
        navitem checked = navitems.get(position);
        if (!checked.isSelectable()) {
            return;
        }

        for (int i = 0; i < navitems.size(); i++) {
            navitem item = navitems.get(i);
            System.out.println(item);

            if (item.isChecked()) {
                item.setChecked(false);
                notifyItemChanged(i);
                break;
            }
        }
        checked.setChecked(true);
        notifyItemChanged(position);
        if (listener != null) {
            listener.onItemSelected(position);
        }
    }

    protected void setListener(OnItemSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        navitems.get(position).bindViewHolder(holder);
    }

    @Override
    public int getItemCount() {
        return navitems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return naviewstypes.get(navitems.get(position).getClass());
    }

    protected interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    protected static abstract class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private nav_items_adapter drawerAdapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            drawerAdapter.setSelected(getAdapterPosition());
        }
    }
}
