package com.huawei.hmspetstore.ui.petstore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.Place;
import com.huawei.hmspetstore.common.IRecyclerItemListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PetStoreSearchAdapter extends RecyclerView.Adapter<PetStoreSearchAdapter.ItemViewHolder> {

    private List<Place> mPlaces = new ArrayList<>();
    private IRecyclerItemListener listener;

    public PetStoreSearchAdapter() {
    }

    public void replaceAll(List<Place> places) {
        this.mPlaces = places;
        notifyDataSetChanged();
    }

    public Place getItem(int position) {
        return mPlaces.get(position);
    }

    public void setListener(IRecyclerItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.petstore_search_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        Place mPetStoreBean = mPlaces.get(position);
        if (position % 2 == 0) {
            holder.isAuth.setVisibility(View.VISIBLE);
            holder.isPaid.setVisibility(View.VISIBLE);
        } else {
            holder.isAuth.setVisibility(View.GONE);
            holder.isPaid.setVisibility(View.GONE);
        }

        holder.name.setText(mPetStoreBean.getName());
        holder.desc.setText(mPetStoreBean.getFormatAddress());

        double distance = mPetStoreBean.getDistance();
        if (distance < 1000) {
            holder.distance.setText(holder.distance.getContext().getString(R.string.less_than_one));
        } else {
            String distanceStr = String.format(holder.distance.getContext().getString(R.string.search_distance), String.valueOf((int) (distance / 1000)));
            holder.distance.setText(distanceStr);
        }

        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(holder.itemView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPlaces == null ? 0 : mPlaces.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView isAuth;
        private TextView name;
        private TextView isPaid;
        private TextView desc;
        private TextView distance;

        ItemViewHolder(View itemView) {
            super(itemView);
            isAuth = itemView.findViewById(R.id.petstore_item_auth);
            name = itemView.findViewById(R.id.petstore_item_name);
            isPaid = itemView.findViewById(R.id.petstore_item_paid);
            desc = itemView.findViewById(R.id.petstore_item_desc);
            distance = itemView.findViewById(R.id.petstore_item_distance);
        }
    }
}