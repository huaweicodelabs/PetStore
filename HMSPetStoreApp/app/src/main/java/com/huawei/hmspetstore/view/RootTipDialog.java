/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hmspetstore.view;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.common.ICallBack;
import com.huawei.hmspetstore.constant.SPConstants;

/**
 * root 提醒
 */
public class RootTipDialog extends DialogFragment {
    // 回调
    private ICallBack<Integer> mCallback;

    /**
     * 初始化
     */
    public static RootTipDialog newInstance() {
        return new RootTipDialog();
    }

    /**
     * 设置回调
     */
    public void setCallBack(ICallBack<Integer> callBack) {
        this.mCallback = callBack;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置对话框样式
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog);
        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            // 设置DialogFragment宽度为屏幕宽度的75%
            int width = (int) (metrics.widthPixels * 0.85);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.roottip_dialog, container, false);
        // 初始化View
        initView(view);
        return view;
    }

    /**
     * 初始化View
     */
    private void initView(View view) {
        view.findViewById(R.id.roottip_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 继续
                if (mCallback != null) {
                    mCallback.onSuccess(SPConstants.VALUE_1);
                }
                dismiss();
            }
        });

        view.findViewById(R.id.roottip_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                if (mCallback != null) {
                    mCallback.onSuccess(SPConstants.VALUE_2);
                }
                dismiss();
            }
        });
    }
}