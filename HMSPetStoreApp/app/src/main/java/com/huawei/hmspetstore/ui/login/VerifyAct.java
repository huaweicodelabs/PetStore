package com.huawei.hmspetstore.ui.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.api.CommonStatusCodes;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.sms.ReadSmsManager;
import com.huawei.hms.support.sms.common.ReadSmsConstant;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.util.ToastUtil;

import java.util.Objects;

/**
 * 绑定手机号
 */
public class VerifyAct extends AppCompatActivity {

    private static final String TAG = "VerifyAct";

    // 手机号
    private AppCompatEditText mEtPhone;
    // 验证码
    private AppCompatEditText mEtVerifyCode;
    // 发送验证码
    private TextView mTvVerifyGain;
    // 获取到的验证码
    private String mVerifyCode;
    // 是否已经注册广播
    private boolean isRegisterBroadcast = false;

    private Handler mHandler = new Handler();

    private BroadcastReceiver smsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Status status = bundle.getParcelable(ReadSmsConstant.EXTRA_STATUS);
                if (status != null && status.getStatusCode() == CommonStatusCodes.SUCCESS) {
                    if (bundle.containsKey(ReadSmsConstant.EXTRA_SMS_MESSAGE)) {
                        // 服务读取到了符合要求的短信，服务关闭
                        final String smsMessage = bundle.getString(ReadSmsConstant.EXTRA_SMS_MESSAGE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onGetVerifyCode(smsMessage);
                            }
                        });
                    } else {
                        // 验证码获取失败
                        onVerifyGainError();
                    }
                } else {
                    // 服务已经超时，未读取到符合要求的短信，服务关闭
                    Log.d(TAG, "receive sms failed");
                    // 验证码获取失败
                    onVerifyGainError();
                }
            } else {
                // 验证码获取失败
                onVerifyGainError();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置屏幕不能截屏和录屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.verify_act);
        // 初始化View
        initView();
        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this,R.string.toast_account_verifycode);
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);
        // 用户名
        mEtPhone = findViewById(R.id.verify_phone);
        // 密码
        mEtVerifyCode = findViewById(R.id.verify_code);
        // 发送验证码
        mTvVerifyGain = findViewById(R.id.verify_gain);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });

        mTvVerifyGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取验证码
                onGainVerifyCode();
            }
        });

        findViewById(R.id.verify_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确定设置
                onConfirmSet();
            }
        });
    }

    /**
     * 获取验证码
     */
    private void onGainVerifyCode() {
        // TODO 获取验证码，并自动读取验证码
        String phoneNumber = Objects.requireNonNull(mEtPhone.getText()).toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            ToastUtil.getInstance().showShort(this, getString(R.string.toast_input_phone));
            return;
        }
        mTvVerifyGain.setText(getString(R.string.verify_gain));
        // 自动获取短信验证码
        onGetRandVerifyCode();
        // 开启读取短信的服务
        Task<Void> task = ReadSmsManager.start(this);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "open read sms permission success");
                // 动态注册自动读取短信广播
                if (!isRegisterBroadcast) {
                    IntentFilter intentFilter = new IntentFilter(ReadSmsConstant.READ_SMS_BROADCAST_ACTION);
                    registerReceiver(smsBroadcastReceiver, intentFilter);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "open read sms permission fail");
                // 验证码获取失败
                onVerifyGainError();
            }
        });
    }

    /**
     * 随机获取验证码
     */
    private void onGetRandVerifyCode() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mVerifyCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
                mEtVerifyCode.setText(mVerifyCode);
                mTvVerifyGain.setText(getString(R.string.verify_code));
            }
        }, 1000);
    }

    /**
     * 验证码获取失败
     */
    private void onVerifyGainError() {
        mTvVerifyGain.setText(getString(R.string.verify_code));
        ToastUtil.getInstance().showShort(VerifyAct.this, getString(R.string.toast_verifycode_error));
    }

    /**
     * 获取到验证码
     */
    private void onGetVerifyCode(String smsMessage) {
        Log.e(TAG, "read sms success, sms content is " + smsMessage);

        if (TextUtils.isEmpty(smsMessage)) {
            onVerifyGainError();
            return;
        }
        // 通过应用发送验证码格式，解码  [#] 欢迎登录宠物商店，验证码是200002。 yKaTWEGHzyV
        int indexOf = smsMessage.lastIndexOf("。");
        mVerifyCode = smsMessage.substring(indexOf - 6, indexOf);
        Log.e(TAG, "verifyCode : " + mVerifyCode);
        mEtVerifyCode.setText(mVerifyCode);
    }

    /**
     * 确定设置
     */
    private void onConfirmSet() {
        // TODO 到首页
        String phone = Objects.requireNonNull(mEtPhone.getText()).toString().trim();
        if (TextUtils.isEmpty(phone)) {
            ToastUtil.getInstance().showShort(VerifyAct.this, getString(R.string.toast_input_phone));
            return;
        }

        String verifyCode = Objects.requireNonNull(mEtVerifyCode.getText()).toString().trim();
        if (TextUtils.isEmpty(verifyCode)) {
            ToastUtil.getInstance().showShort(VerifyAct.this, getString(R.string.toast_input_verifycode));
            return;
        }

        if (!verifyCode.equals(mVerifyCode)) {
            ToastUtil.getInstance().showShort(VerifyAct.this, getString(R.string.toast_input_verifycode_correct));
            return;
        }
        // 到主页
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegisterBroadcast) {
            unregisterReceiver(smsBroadcastReceiver);
        }
    }
}