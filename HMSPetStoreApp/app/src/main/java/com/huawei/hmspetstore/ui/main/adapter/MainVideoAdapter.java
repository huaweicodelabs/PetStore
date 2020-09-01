package com.huawei.hmspetstore.ui.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.ImageBean;
import com.huawei.hmspetstore.common.IRecyclerItemListener;

import java.util.List;

/**
 * 功能描述: 首页列表Adapter
 */
public class MainVideoAdapter extends RecyclerView.Adapter<MainVideoAdapter.ItemViewHolder> {

    private List<ImageBean> mItems;

    private IRecyclerItemListener listener;

    public MainVideoAdapter(List<ImageBean> mItems) {
        this.mItems = mItems;
    }

    public void setListener(IRecyclerItemListener listener) {
        this.listener = listener;
    }

    public void setDate(List<ImageBean> data) {
        this.mItems = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
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
        return mItems == null ? 0 : mItems.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        ItemViewHolder(View itemView) {
            super(itemView);
        }
    }
}