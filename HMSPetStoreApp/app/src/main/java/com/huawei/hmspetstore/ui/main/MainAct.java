/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.MainItemBean;
import com.huawei.hmspetstore.ui.center.PurchasesOperation;
import com.huawei.hmspetstore.ui.main.adapter.MainAdapter;
import com.huawei.hmspetstore.ui.mine.MineCenterAct;
import com.huawei.hmspetstore.ui.petstore.PetStoreSearchActivity;
import com.huawei.hmspetstore.ui.petvideo.PetVideoAct;
import com.huawei.hmspetstore.ui.push.PushConst;
import com.huawei.hmspetstore.ui.push.PushService;
import com.huawei.hmspetstore.util.LoginUtil;
import com.huawei.hmspetstore.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页
 */
public class MainAct extends AppCompatActivity {
    private static final String TAG = "MainAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_act);
        // 初始化 View
        initView();
        // 初始化 RecyclerView
        initRecyclerView();
        // Push初始化
        PushService.init(this);
        // IAP在应用启动时的补单机制
        if (LoginUtil.isHuaweiLogin(this)) {
            PurchasesOperation.replenishForLaunch(MainAct.this);
        }
        // 申请权限
        checkPermission();
        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_push_init);
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部右侧消息
        ImageView mIvRight = findViewById(R.id.title_right);
        mIvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 首页消息
                if (LoginUtil.loginCheck(MainAct.this)) {
                    startActivity(new Intent(PushConst.ACTION_INNER_MESSAGE));
                }
            }
        });

        findViewById(R.id.main_petStore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 宠物商店
                if (LoginUtil.isLogin(MainAct.this)) {
                    // subscribe petstore topic
                    PushService.subscribe(MainAct.this, PushConst.TOPIC_STORE);
                }
                if (LoginUtil.loginCheck(MainAct.this)) {
                    startActivity(new Intent(MainAct.this, PetStoreSearchActivity.class));
                }
            }
        });

        findViewById(R.id.main_petVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 宠物视频
                if (LoginUtil.isLogin(MainAct.this)) {
                    // subscribe petvedio topic
                    PushService.subscribe(MainAct.this, PushConst.TOPIC_VEDIO);
                }
                if (LoginUtil.loginCheck(MainAct.this)) {
                    startActivity(new Intent(MainAct.this, PetVideoAct.class));
                }
            }
        });

        findViewById(R.id.main_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 个人中心
                if (LoginUtil.loginCheck(MainAct.this)) {
                    startActivity(new Intent(MainAct.this, MineCenterAct.class));
                }
            }
        });
    }

    /**
     * 初始化 RecyclerView
     */
    private void initRecyclerView() {
        RecyclerView mRecyclerView = findViewById(R.id.main_recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        // 设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        // 设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        // 设置Adapter
        List<MainItemBean> mItems = new ArrayList<>();
        mItems.add(new MainItemBean(0));
        mItems.add(new MainItemBean(1));
        MainAdapter mMainAdapter = new MainAdapter(mItems);
        mRecyclerView.setAdapter(mMainAdapter);
        // 设置增加或删除条目的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void checkPermission() {
        // 动态申请权限(Android 6.0危险权限要求)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "sdk < 28 Q");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions((Activity) this, strings, 1);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                ActivityCompat.requestPermissions((Activity) this, strings, 2);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSSION  failed");
            }
        }

        if (requestCode == 2) {
            if (grantResults.length > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed");
            }
        }
    }
}