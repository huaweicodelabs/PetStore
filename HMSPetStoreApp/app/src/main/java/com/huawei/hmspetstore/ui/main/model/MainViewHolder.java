package com.huawei.hmspetstore.ui.main.model;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.bean.MainItemBean;
import com.huawei.hmspetstore.common.IRecyclerItemListener;

/**
 * 功能描述: 首页ViewHolder
 */
public abstract class MainViewHolder extends RecyclerView.ViewHolder implements IRecyclerItemListener {

    MainViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * 绑定ViewHolder 界面
     */
    public abstract void onBindItemViewHolder(MainViewHolder holder, MainItemBean data);

    @Override
    public void onItemClick(View view, int position) {

    }
}