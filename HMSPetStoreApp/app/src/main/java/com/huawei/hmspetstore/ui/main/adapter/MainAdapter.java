package com.huawei.hmspetstore.ui.main.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.MainItemBean;
import com.huawei.hmspetstore.ui.main.model.IMainItemConstant;
import com.huawei.hmspetstore.ui.main.model.MainStoreViewHolder;
import com.huawei.hmspetstore.ui.main.model.MainVideoViewHolder;
import com.huawei.hmspetstore.ui.main.model.MainViewHolder;

import java.util.List;

/**
 * 功能描述: 首页列表Adapter
 */
public class MainAdapter extends RecyclerView.Adapter<MainViewHolder> {
    private static final String TAG = "ReportTotalAdapter";

    private List<MainItemBean> mItems;

    /**
     * ReportTotalAdapter
     *
     * @param mItems mItems
     */
    public MainAdapter(List<MainItemBean> mItems) {
        this.mItems = mItems;
    }

    @Override
    public int getItemViewType(int position) {
        MainItemBean itemBean = mItems.get(position);
        if (itemBean != null) {
            return itemBean.getViewType();
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MainViewHolder holder;
        View view;
        if (viewType == IMainItemConstant.TYPE_STORE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_store_item, parent, false);
            holder = new MainStoreViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_video_item, parent, false);
            holder = new MainVideoViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        if (null == mItems || mItems.size() == 0) {
            Log.e(TAG, "onBindViewHolder mItems is null");
            return;
        }
        holder.onBindItemViewHolder(holder, mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return (mItems == null) ? 0 : mItems.size();
    }
}