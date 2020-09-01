package com.huawei.hmspetstore.ui.center.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        String memberName = String.format(holder.memberName.getContext().getString(R.string.member_name_desc), numberToChinese(holder.itemView.getContext(), position + 1));
        holder.memberName.setText(memberName);
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
        TextView memberName, subBtn;
        TextView name;
        TextView desc;
        TextView money;

        ItemViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.memcenter_item_name);
            desc = itemView.findViewById(R.id.memcenter_item_desc);
            money = itemView.findViewById(R.id.memcenter_item_money);
            memberName = itemView.findViewById(R.id.member_name);
            subBtn = itemView.findViewById(R.id.sub_btn);
        }
    }

    private static String numberToChinese(Context context, int number) {
        // "零", "一", "二", "三", "四", "五", "六", "七", "八", "九"
        String[] numbers = context.getResources().getStringArray(R.array.numbers);
        // "", "十", "百", "千", "万", "十", "百", "千", "亿", "十"
        String[] units = context.getResources().getStringArray(R.array.units);
        String sign = number < 0 ? context.getResources().getString(R.string.unit_negative) : "";
        if (number < 0) {
            number = -number;
        }
        StringBuilder result = new StringBuilder(sign);
        String string = String.valueOf(number);
        int n = string.length();
        char[] numberCharArray = string.toCharArray();
        for (int i = 0; i < n; i++) {
            int digNum = n - i; // 位数
            int num = numberCharArray[i] - '0';
            if (num != 0) {
                result.append(numbers[num]).append(units[digNum - 1]);
                continue;
            }

            if (result.toString().endsWith(numbers[0])) {
                // 如果是单位所在的位数，则去除上一个0，加上单位
                if (digNum % 4 == 1) {
                    result.deleteCharAt(result.length() - 1);
                    result.append(units[digNum - 1]);
                }
            } else {
                result.append(numbers[0]);
            }
        }
        return result.toString();
    }
}