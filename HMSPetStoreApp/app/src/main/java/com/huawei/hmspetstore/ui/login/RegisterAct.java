package com.huawei.hmspetstore.ui.login;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.UserBean;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.util.UUIDUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 注册
 */
public class RegisterAct extends AppCompatActivity {

    // 用户名
    private AppCompatEditText mEtUserName;
    // 密码
    private AppCompatEditText mEtPassword;
    // 密码Check
    private AppCompatEditText mEtPasswordDouble;

    // 本地用户数据
    private List<UserBean> mLocalData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置屏幕不能截屏和录屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.register_act);
        // 初始化View
        initView();
        // 初始化数据
        initData();
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);
        // 用户名
        mEtUserName = findViewById(R.id.register_username);
        // 密码
        mEtPassword = findViewById(R.id.register_password);
        // 密码Check
        mEtPasswordDouble = findViewById(R.id.register_password_double);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });

        findViewById(R.id.register_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 注册
                onRegister();
            }
        });

        // 富文本设置用户协议和隐私政策
        TextView mTvAgreement = findViewById(R.id.register_agreement);
        String content = getString(R.string.agreement_privacy);
        int length = content.length();
        SpannableString mSpannableString = new SpannableString(content);
        mSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), length - 6, length, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), length - 13, length - 7, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mTvAgreement.setText(mSpannableString);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        String localStr = (String) SPUtil.get(this, SPConstants.KEY_LOCAL_USER, "");
        if (TextUtils.isEmpty(localStr)) {
            mLocalData = null;
            return;
        }
        try {
            Gson gson = new Gson();
            mLocalData = gson.fromJson(localStr, new TypeToken<List<UserBean>>() {
            }.getType());
        } catch (Exception ignored) {

        }
    }

    /**
     * 登录
     */
    private void onRegister() {
        String name = Objects.requireNonNull(mEtUserName.getText()).toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtil.getInstance().showShort(this, R.string.toast_input_username);
            return;
        }

        // 判断是否被注册
        if (checkIsLocalUser(name)) {
            ToastUtil.getInstance().showShort(this, R.string.toast_has_register);
            return;
        }

        String password = Objects.requireNonNull(mEtPassword.getText()).toString().trim();
        if (TextUtils.isEmpty(password)) {
            ToastUtil.getInstance().showShort(this, R.string.toast_input_password);
            return;
        }

        String passwordDouble = Objects.requireNonNull(mEtPasswordDouble.getText()).toString().trim();
        if (TextUtils.isEmpty(passwordDouble)) {
            ToastUtil.getInstance().showShort(this, R.string.toast_input_password_double);
            return;
        }

        if (!password.equals(passwordDouble)) {
            ToastUtil.getInstance().showShort(this, R.string.toast_passwords_different);
            return;
        }


        // 保存用户信息
        UserBean userBean = new UserBean(name, password, UUIDUtil.getUUID());
        if (mLocalData == null || mLocalData.size() == 0) {
            mLocalData = new ArrayList<>();
        }
        mLocalData.add(userBean);
        Gson gson = new Gson();
        String localStr = gson.toJson(mLocalData);

        SPUtil.put(this, SPConstants.KEY_LOGIN, true);
        SPUtil.put(this, SPConstants.KEY_NICK_NAME, name);
        SPUtil.put(this, SPConstants.KEY_LOCAL_USER, localStr);
        finish();
    }

    /**
     * 判断是否为当前用户
     */
    private boolean checkIsLocalUser(String userName) {
        if (TextUtils.isEmpty(userName)) {
            return false;
        }
        if (mLocalData == null || mLocalData.size() == 0) {
            return false;
        }
        boolean isLocalUser = false;
        for (UserBean bean : mLocalData) {
            if (userName.equals(bean.getName())) {
                isLocalUser = true;
                break;
            }
        }
        return isLocalUser;
    }
}