package com.huawei.hmspetstore.ui.center.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.SetMealBean;
import com.huawei.hmspetstore.common.IRecyclerItemListener;

import java.util.List;

public class MemCenterAdapter extends RecyclerView.Adapter<MemCenterAdapter.ItemViewHolder> {

    private List<SetMealBean> mItems;
    private IRecyclerItemListener listener;

    public MemCenterAdapter(List<SetMealBean> mItems) {
        this.mItems = mItems;
    }

    public void setListener(IRecyclerItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memcenter_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        SetMealBean mSetMealBean = mItems.get(position);
        String productId = mSetMealBean.getProductId();
        if ("member02".equals(productId)) {
            // 3 months
            holder.img.setImageResource(R.mipmap.member_3);
        } else if ("member03".equals(productId)) {
            // Lifetime
            holder.img.setImageResource(R.mipmap.member_lifetime);
        } else if ("subscribeMember01".equals(productId)) {
            // Ongoing
            holder.img.setImageResource(R.mipmap.member_o);
        } else if ("member01".equals(productId)) {
            // 1 month
            holder.img.setImageResource(R.mipmap.member_1);
        }
        
        holder.name.setText(mSetMealBean.getName());
        holder.desc.setText(mSetMealBean.getDesc());
        holder.money.setText(mSetMealBean.getMoney());

        if (listener != null) {
            holder.subBtn.setOnClickListener(new View.OnClickListener() {
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
        ImageView img;
        TextView subBtn;
        TextView name;
        TextView desc;
        TextView money;

        ItemViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.memcenter_item_img);
            name = itemView.findViewById(R.id.memcenter_item_name);
            desc = itemView.findViewById(R.id.memcenter_item_desc);
            money = itemView.findViewById(R.id.memcenter_item_money);
            subBtn = itemView.findViewById(R.id.sub_btn);
        }
    }
}