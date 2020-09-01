package com.huawei.hmspetstore.ui.main.model;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.ImageBean;
import com.huawei.hmspetstore.bean.MainItemBean;
import com.huawei.hmspetstore.ui.main.adapter.MainVideoAdapter;
import com.huawei.hmspetstore.ui.petvideo.VideoPlayAct;
import com.huawei.hmspetstore.util.LoginUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述: 首页宠物视频ViewHolder
 */
public class MainVideoViewHolder extends MainViewHolder {

    private Context mContext;
    private MainVideoAdapter mainVideoAdapter;
    // 视频播放信息集合
    private List<ImageBean> imageBeans = new ArrayList<>();

    public MainVideoViewHolder(@NonNull View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        RecyclerView mRecyclerView = itemView.findViewById(R.id.main_video_item_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        mRecyclerView.setLayoutManager(layoutManager);
        //设置Adapter
        mainVideoAdapter = new MainVideoAdapter(imageBeans);
        mainVideoAdapter.setListener(this);
        mRecyclerView.setAdapter(mainVideoAdapter);
        //设置增加或删除条目的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onBindItemViewHolder(MainViewHolder holder, MainItemBean data) {
        if (data == null) {
            return;
        }
        imageBeans.add(new ImageBean(R.mipmap.video_img));
        if (mainVideoAdapter != null) {
            mainVideoAdapter.setDate(imageBeans);
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        // 视频播放
        if (LoginUtil.loginCheck(mContext)) {
            mContext.startActivity(new Intent(mContext, VideoPlayAct.class));
        }
    }
}