package com.huawei.hmspetstore.ui.main.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.ImageBean;
import com.huawei.hmspetstore.bean.MainItemBean;
import com.huawei.hmspetstore.ui.main.adapter.MainImageAdapter;
import com.huawei.hmspetstore.ui.petstore.PetStoreSearchActivity;
import com.huawei.hmspetstore.ui.push.PushConst;
import com.huawei.hmspetstore.ui.push.PushService;
import com.huawei.hmspetstore.util.LoginUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述: 首页宠物商店ViewHolder
 */
public class MainStoreViewHolder extends MainViewHolder {
    private static final String TAG = "MainStoreViewHolder";
    private Context mContext;

    private MainImageAdapter mainImageAdapter;
    private List<ImageBean> imageBeans = new ArrayList<>();

    public MainStoreViewHolder(@NonNull View itemView) {
        super(itemView);
        Log.e(TAG, "MainStoreViewHolder init");
        mContext = itemView.getContext();
        RecyclerView mRecyclerView = itemView.findViewById(R.id.main_store_item_recyclerView);
        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        //设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        //设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        //设置Adapter
        mainImageAdapter = new MainImageAdapter(imageBeans);
        mainImageAdapter.setListener(this);
        mRecyclerView.setAdapter(mainImageAdapter);
        //设置增加或删除条目的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 宠物商店点击
                if (LoginUtil.loginCheck(mContext)) {
                    // TODO 查看照片等
                    // 宠物商店
                    if (LoginUtil.isLogin(mContext)) {
                        // subscribe petstore topic
                        PushService.subscribe(mContext, PushConst.TOPIC_STORE);
                    }
                    if (LoginUtil.loginCheck(mContext)) {
                        mContext.startActivity(new Intent(mContext, PetStoreSearchActivity.class));
                    }
                }
            }
        });
    }

    @Override
    public void onBindItemViewHolder(MainViewHolder holder, MainItemBean data) {
        if (data == null) {
            return;
        }
        imageBeans.add(new ImageBean(R.mipmap.ic_store_image_1));
        imageBeans.add(new ImageBean(R.mipmap.ic_store_image_2));
        imageBeans.add(new ImageBean(R.mipmap.ic_store_image_3));
        if (mainImageAdapter != null) {
            mainImageAdapter.setDate(imageBeans);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        // 宠物商店点击
        if (LoginUtil.isLogin(mContext)) {
            // subscribe petstore topic
            PushService.subscribe(mContext, PushConst.TOPIC_STORE);
        }
        if (LoginUtil.loginCheck(mContext)) {
            mContext.startActivity(new Intent(mContext, PetStoreSearchActivity.class));
        }
    }
}