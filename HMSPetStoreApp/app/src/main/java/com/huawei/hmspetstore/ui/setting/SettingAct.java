package com.huawei.hmspetstore.ui.setting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.fido.fido2.Fido2Client;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.common.ICallBack;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.address.AddAddressManageAct;
import com.huawei.hmspetstore.ui.push.PushConst;
import com.huawei.hmspetstore.ui.push.PushService;
import com.huawei.hmspetstore.ui.push.PushSharedPreferences;
import com.huawei.hmspetstore.util.LoginUtil;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.util.fido.Fido2Handler;
import com.huawei.hmspetstore.view.SignOutDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * 设置
 */
public class SettingAct extends AppCompatActivity {

    private static final String TAG = "SettingAct";
    // 头像
    private ImageView mIvHeadImg;
    // 昵称
    private TextView mTvNickName;

    // FIDO2相关处理
    private Fido2Handler fido2Handler;
    // 指纹登录开关
    private SwitchCompat mSwitchCompatFingerPrintLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_act);
        // 初始化View
        initView();
        // 初始化数据
        initData();
        // 初始化Fido
        initFido();
        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_setting);
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);
        // 头像
        mIvHeadImg = findViewById(R.id.setting_headimg);
        // 昵称
        mTvNickName = findViewById(R.id.setting_nickName);
        // 消息
        SwitchCompat mSwitchCompat = findViewById(R.id.setting_news);
        mSwitchCompatFingerPrintLogin = findViewById(R.id.setting_fingerprint);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });
        findViewById(R.id.setting_address).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 地址管理
                startActivity(new Intent(SettingAct.this, AddAddressManageAct.class));
            }
        });
        findViewById(R.id.setting_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 退出登录
                onLoginCheck();
            }
        });
        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Context context = getApplicationContext();
                PushService.turnOnOff(context, isChecked);
                PushSharedPreferences.saveConfig(context, PushConst.PUSH_MESSAGE_SWITCH, String.valueOf(isChecked));
            }
        });
    }

    /**
     * 初始化用户信息
     */
    private void initData() {
        String nickName = (String) SPUtil.get(this, SPConstants.KEY_NICK_NAME, "");
        if (TextUtils.isEmpty(nickName)) {
            return;
        }
        // 昵称
        mTvNickName.setText(nickName);

        boolean isHuaweiLogin = (boolean) SPUtil.get(this, SPConstants.KEY_HW_LOGIN, false);
        if (!isHuaweiLogin) {
            return;
        }

        String openId = (String) SPUtil.get(this, SPConstants.KEY_HW_OEPNID, "");
        if (TextUtils.isEmpty(openId)) {
            // 无本地保存的个人信息数据
            return;
        }

        String userInfo = (String) SPUtil.get(this, openId, "");
        if (TextUtils.isEmpty(userInfo)) {
            // 无本地保存的个人信息数据
            return;
        }

        // 头像
        String headPhoto = "";
        try {
            JSONObject jsonObject = new JSONObject(userInfo);
            headPhoto = jsonObject.optString(SPConstants.KEY_HEAD_PHOTO, "");
        } catch (JSONException ignored) {

        }
        if (!TextUtils.isEmpty(headPhoto)) {
            Glide.with(this)
                    .load(headPhoto)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mIvHeadImg);
        }
    }

    /**
     * 初始化Fido
     */
    private void initFido() {
        fido2Handler = new Fido2Handler(SettingAct.this);
        boolean loginSwitch = (boolean) SPUtil.get(SettingAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false);
        mSwitchCompatFingerPrintLogin.setChecked(loginSwitch);
        mSwitchCompatFingerPrintLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fido2Handler.onRegistration(mTvNickName.getText().toString(), new ICallBack() {
                        @Override
                        public void onSuccess(Object bean) {
                            // 不做任何处理，待FIDO注册流程完成后，在onActivityResult 中处理
                        }

                        @Override
                        public void onError(String errorMsg) {
                            // 指纹登录打开失败
                            SPUtil.put(SettingAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false);
                            ToastUtil.getInstance().showShort(SettingAct.this,getString(R.string.toast_fingerpring_open_fail));
                            mSwitchCompatFingerPrintLogin.setChecked(false);
                        }
                    });
                } else {
                    boolean ret = fido2Handler.onDeregistration(mTvNickName.getText().toString());
                    if (ret) {
                        SPUtil.put(SettingAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false);
                    }
                }
            }
        });
    }

    /**
     * 登录Check
     */
    private void onLoginCheck() {
        boolean isHuaweiLogin = LoginUtil.isHuaweiLogin(this);
        if (!isHuaweiLogin) {
            onExitLogin();
            return;
        }

        // 显示是否退出华为账号
        FragmentManager manager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) manager.findFragmentByTag("mSignOutDialog");
        if ((fragment != null) && (fragment.getDialog() != null) && (fragment.getDialog().isShowing())) {
            return;
        }
        SignOutDialog mSignOutDialog = SignOutDialog.newInstance();
        mSignOutDialog.setCallBack(new ICallBack<Integer>() {
            @Override
            public void onSuccess(Integer bean) {
                if (bean == 1) {
                    // 退出华为账号
                    huaweiSignOut();
                } else {
                    // 不退出华为账号
                    onExitLogin();
                }
            }

            @Override
            public void onError(String errorMsg) {
            }
        });
        mSignOutDialog.show(getSupportFragmentManager(), "mSignOutDialog");
    }

    /**
     * 退出
     */
    private void onExitLogin() {
        SPUtil.put(this, SPConstants.KEY_LOGIN, false);
        pushClear();
        finish();
    }

    /**
     * 华为帐号退出登录
     */
    private void huaweiSignOut() {
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).createParams();
        HuaweiIdAuthService authService = HuaweiIdAuthManager.getService(SettingAct.this, authParams);
        Task<Void> signOutTask = authService.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "signOut Success");
                // TODO 退出到首页， 不删除华为帐号数据
                SPUtil.put(SettingAct.this, SPConstants.KEY_HW_LOGIN, false);
                onExitLogin();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "signOut fail");
                ToastUtil.getInstance().showShort(SettingAct.this, "signOut fail");
            }
        });
    }

    /**
     * push clear
     */
    private void pushClear() {
        Context context = getApplicationContext();
        Map<String, String> topics = PushSharedPreferences.readTopic(context);
        for (String topic : topics.keySet()) {
            PushService.unsubscribe(context, topic);
        }
        PushSharedPreferences.clearTopic(context);
        PushSharedPreferences.clearMessage(context);
        PushSharedPreferences.saveConfig(context, PushConst.PUSH_MESSAGE_SWITCH, String.valueOf(true));
        PushService.turnOnOff(context, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: not OK!");
            return;
        }
        if (requestCode == Fido2Client.REGISTRATION_REQUEST) {
            // 到FIDO服务器校验注册结果
            final boolean registerResult = fido2Handler.onRegisterToServer(data);
            // 设置开关状态
            mSwitchCompatFingerPrintLogin.setChecked(registerResult);
            SPUtil.put(SettingAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, registerResult);
        }
    }
}