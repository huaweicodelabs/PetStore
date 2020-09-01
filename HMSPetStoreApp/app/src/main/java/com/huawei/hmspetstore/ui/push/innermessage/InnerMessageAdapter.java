package com.huawei.hmspetstore.ui.push.innermessage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;

import java.util.LinkedList;

/**
 * 功能描述
 */
public class InnerMessageAdapter extends RecyclerView.Adapter<InnerMessageAdapter.ItemViewHolder> {
    private LinkedList<InnerMessage> data;

    public InnerMessageAdapter(LinkedList<InnerMessage> data) {
        this.data = data;
    }

    /**
     * 清理数据
     */
    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_inner_message, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InnerMessage message = data.get(position);
        holder.ivIcon.setImageDrawable(message.getIcon());
        holder.tvTitle.setText(message.getTitle());
        holder.tvContent.setText(message.getContent());
        holder.tvDate.setText(message.getDate());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIcon;
        TextView tvTitle;
        TextView tvContent;
        TextView tvDate;

        ItemViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}