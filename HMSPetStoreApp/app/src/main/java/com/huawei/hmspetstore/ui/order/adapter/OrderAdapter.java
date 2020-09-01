package com.huawei.hmspetstore.ui.order.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.OrderBean;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ItemViewHolder> {

    // 单位为分转换成单位为元的除数
    private static final double ONE_HUNDRED = 100.0;

    private List<OrderBean> mItems;

    public OrderAdapter(List<OrderBean> mItems) {
        this.mItems = mItems;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        OrderBean mOrderBean = mItems.get(position);
        holder.time.setText(mOrderBean.getTime());
        holder.status.setText(mOrderBean.getStatus());
        // 图片 holder.img;
        holder.name.setText(mOrderBean.getName());
        long price = mOrderBean.getPrice();
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        df.applyPattern("0.00");
        String priceStr = df.format(price / ONE_HUNDRED);
        holder.price.setText(String.format(holder.price.getContext().getString(R.string.price_unit),priceStr));
        holder.name_p.setText(String.format(holder.price.getContext().getString(R.string.order_unit),priceStr));
        holder.order_price.setText(String.format(holder.price.getContext().getString(R.string.order_total),"1",priceStr));
        holder.price_c.setText("1");
    }


    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView status;
        TextView name;
        TextView price;
        TextView name_p;
        TextView price_c;
        TextView order_price;

        ItemViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.order_item_time);
            status = itemView.findViewById(R.id.order_item_status);
            name = itemView.findViewById(R.id.order_item_name);
            price = itemView.findViewById(R.id.order_item_price);
            name_p = itemView.findViewById(R.id.order_item_p);
            price_c = itemView.findViewById(R.id.order_item_c);
            order_price = itemView.findViewById(R.id.order_price);
        }
    }
}