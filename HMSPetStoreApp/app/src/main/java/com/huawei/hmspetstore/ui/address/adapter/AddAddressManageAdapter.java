package com.huawei.hmspetstore.ui.address.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.AddressBean;
import com.huawei.hmspetstore.common.IRecyclerItemListener;
import java.util.List;

public class AddAddressManageAdapter extends RecyclerView.Adapter<AddAddressManageAdapter.ItemViewHolder> {

    private List<AddressBean> mItems;
    private IRecyclerItemListener listener;

    public AddAddressManageAdapter(List<AddressBean> mItems) {
        this.mItems = mItems;
    }

    public void setListener(IRecyclerItemListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        AddressBean addressBean = mItems.get(position);
        holder.mTvAddress.setText(addressBean.getAddress());
        holder.mTvName.setText(addressBean.getName());
        holder.mTvPhoneNum.setText(addressBean.getPhoneNum());
        holder.mDefault.setText(R.string.default_address_flag);
        if(addressBean.isDefault()){
            holder.mDefault.setVisibility(View.VISIBLE);
        }else{
            holder.mDefault.setVisibility(View.GONE);
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
        return mItems == null ? 0 : mItems.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView mTvName;
        TextView mTvPhoneNum;
        TextView mTvAddress;
        TextView mDefault;

        ItemViewHolder(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvPhoneNum = itemView.findViewById(R.id.tv_phone_num);
            mTvAddress = itemView.findViewById(R.id.tv_address);
            mDefault = itemView.findViewById(R.id.tv_default);
        }
    }
}