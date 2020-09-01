
package com.huawei.hmspetstore.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hmspetstore.R;

/**
 * 登录加载框
 */
public class LoggingDialog extends LoadingDialog {

    /**
     * 初始化
     */
    public static LoggingDialog newInstance() {
        return new LoggingDialog();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.loading_dialog, container, false);
        final TextView descView = view.findViewById(R.id.loading_desc);
        descView.setText(R.string.logging_desc);
        return view;
    }
}