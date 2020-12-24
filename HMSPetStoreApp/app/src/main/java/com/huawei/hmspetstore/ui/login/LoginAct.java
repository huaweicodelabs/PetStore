package com.huawei.hmspetstore.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonSyntaxException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.fido.fido2.Fido2Client;
import com.huawei.hms.support.api.safetydetect.SafetyDetect;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.UserBean;
import com.huawei.hmspetstore.common.ICallBack;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.main.MainAct;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.SafetyDetectUtil;
import com.huawei.hmspetstore.util.ToastUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.hmspetstore.util.fido.Fido2Handler;
import com.huawei.hmspetstore.view.LoadingDialog;
import com.huawei.hmspetstore.view.LoggingDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class LoginAct extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_SIGN_IN_LOGIN = 1002;
    private static final String TAG = "LoginAct";

    // 用户名
    private AppCompatEditText mEtUserName;
    // 用户名-删除
    private ImageView mIvUserNameDel;
    // 密码
    private AppCompatEditText mEtPassword;
    // 密码-删除
    private ImageView mIvPasswordDel;

    // 华为账号登录按钮
    private HuaweiIdAuthButton mHuaweiIdAuthButton;

    // 华为账号登录客户端
    private HuaweiIdAuthService mAuthService;

    // 本地用户数据
    private List<UserBean> mLocalData;

    // 登录加载框
    private LoadingDialog loadingDialog;

    // FIDO2相关处理
    private Fido2Handler fido2Handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置屏幕不能截屏和录屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.login_act);
        // 初始化View
        initView();

        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_account);

        fido2Handler = new Fido2Handler(LoginAct.this);

        // 初始化虚假用户检测API
        SafetyDetect.getClient(this).initUserDetect().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                // 初始化行为检测能力成功，您可以在此做一些记录
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 初始化行为检测能力失败，您需要根据具体的错误码进行处理或重新初始化
            }
        });
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);

        mEtUserName = findViewById(R.id.login_username);
        mEtPassword = findViewById(R.id.login_password);
        mIvUserNameDel = findViewById(R.id.login_username_del);
        mIvPasswordDel = findViewById(R.id.login_password_del);
        mIvUserNameDel.setOnClickListener(this);
        mIvPasswordDel.setOnClickListener(this);
        onTextChangedListener();

        mIvBack.setOnClickListener(this);

        findViewById(R.id.login_login).setOnClickListener(this);
        findViewById(R.id.login_register).setOnClickListener(this);

        mHuaweiIdAuthButton = findViewById(R.id.hwid_signin);
        // 注册监听器
        mHuaweiIdAuthButton.setOnClickListener(this);
        // 设置主题
        mHuaweiIdAuthButton.setTheme(HuaweiIdAuthButton.THEME_NO_TITLE);
        // 设置圆角半径
        mHuaweiIdAuthButton.setCornerRadius(HuaweiIdAuthButton.CORNER_RADIUS_LARGE);
        // 设置配色方案
        mHuaweiIdAuthButton.setColorPolicy(HuaweiIdAuthButton.COLOR_POLICY_WHITE);

        // 富文本设置用户协议和隐私政策
        TextView mTvAgreement = findViewById(R.id.login_agreement);
        String content = getString(R.string.agreement_privacy);
        SpannableString mSpannableString = getSpannableString(content);
        mTvAgreement.setText(mSpannableString);
    }

    private SpannableString getSpannableString(String content) {
        // 点注册/登录代表同意宠物市场《用户协议》和《隐私政策》
        // Touching Sign in or Sign up means that you agree with the User Agreement and Privacy Statement of this app.
        SpannableString mSpannableString = new SpannableString(content);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary));
        if (content.contains("隐私政策")) {
            mSpannableString.setSpan(colorSpan, 21, 27, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (content.contains("Privacy Statement")) {
            mSpannableString.setSpan(colorSpan, 77, 94, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary));
        if (content.contains("用户协议")) {

            mSpannableString.setSpan(colorSpan2, 14, 20, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (content.contains("User Agreement")) {
            mSpannableString.setSpan(colorSpan2, 58, 72, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return mSpannableString;
    }

    private void onTextChangedListener() {
        mEtUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (TextUtils.isEmpty(text)) {
                    mIvUserNameDel.setVisibility(View.INVISIBLE);
                } else {
                    mIvUserNameDel.setVisibility(View.VISIBLE);
                }
            }
        });

        mEtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (TextUtils.isEmpty(text)) {
                    mIvPasswordDel.setVisibility(View.INVISIBLE);
                } else {
                    mIvPasswordDel.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 初始化当前用户信息
     */
    private void onInitLocalUser() {
        String localStr = (String) SPUtil.get(this, SPConstants.KEY_LOCAL_USER, "");
        if (TextUtils.isEmpty(localStr)) {
            mLocalData = null;
            return;
        }
        try {
            Gson gson = new Gson();
            mLocalData = gson.fromJson(localStr, new TypeToken<List<UserBean>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "onInitLocalUser json format error");
        }
    }

    /**
     * 登录
     */
    private void onLogin() {
        String localName = (String) SPUtil.get(this, SPConstants.KEY_NICK_NAME, "");
        fido2Handler.onAuthentication(localName, new ICallBack<Object>() {

            @Override
            public void onSuccess(Object bean) {
                // 不做任何处理，待FIDO认证流程完成后，在onActivityResult 中处理
            }

            @Override
            public void onError(String errorMsg) {
                Log.d(TAG, " onLogin#onAuthentication#onError: login with fingerprint fail!" + errorMsg);
                loginUseNameAndPasswordWithUserDetect();
            }
        });
    }

    private void loginUseNameAndPasswordWithUserDetect() {
        final String name = Objects.requireNonNull(mEtUserName.getText()).toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastUtil.getInstance().showShort(this, R.string.toast_input_username);
            return;
        }

        final String password = Objects.requireNonNull(mEtPassword.getText()).toString().trim();
        if (TextUtils.isEmpty(password)) {
            ToastUtil.getInstance().showShort(this, R.string.toast_input_password);
            return;
        }

        loadingDialog = LoggingDialog.newInstance();
        loadingDialog.show(getSupportFragmentManager(), "LoggingDialog");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 调用经过封装后的虚假用户检测接口，此处需要将传入参数替换为当前的Activity或上下文
                // 和一个回调来进行异步处理
                SafetyDetectUtil.callUserDetect(LoginAct.this, new ICallBack<Boolean>() {
                    @Override
                    public void onSuccess(Boolean userVerified) {
                        // 虚假用户检测调用成功
                        loadingDialog.dismiss();
                        loginWithLocalUser(name, password);
                        // 中国区暂不支持虚假用户检测
                        Log.i(TAG, getString(R.string.toast_userdetect_error));
                    }

                    @Override
                    public void onError(String errorMsg) {
                        // 虚假用户检测调用失败
                        loadingDialog.dismiss();
                        ToastUtil.getInstance().showShort(LoginAct.this, R.string.toast_userdetect_error);
                    }
                });
            }
        }).start();
    }

    private void loginWithLocalUser(String name, String password) {
        // 判断当前用户名，密码是否一致
        if (!localUserCheck(name, password)) {
            return;
        }

        SPUtil.put(LoginAct.this, SPConstants.KEY_LOGIN, true);
        SPUtil.put(LoginAct.this, SPConstants.KEY_HW_LOGIN, false);
        SPUtil.put(LoginAct.this, SPConstants.KEY_NICK_NAME, name);

        finish();
    }

    /**
     * 本地用户Check
     */
    private boolean localUserCheck(String userName, String password) {
        // 先注册用户
        if (mLocalData == null || mLocalData.size() == 0) {
            ToastUtil.getInstance().showShort(this, R.string.toast_must_register);
            return false;
        }
        boolean isLocalUser = false;
        boolean pwdRight = true;
        for (UserBean bean : mLocalData) {
            if (userName.equals(bean.getName())) {
                isLocalUser = true;
                if (!password.equals(bean.getPassword())) {
                    pwdRight = false;
                }
                break;
            }
        }
        if (!isLocalUser) {
            ToastUtil.getInstance().showShort(this, R.string.toast_must_register);
            return false;
        }
        if (!pwdRight) {
            ToastUtil.getInstance().showShort(this, R.string.toast_password_error);
            return false;
        }
        return true;
    }

    /**
     * 注册
     */
    private void onRegister() {
        startActivity(new Intent(this, RegisterAct.class));
    }

    /**
     * 华为账号登录
     */
    private void onHuaweiIdLogin() {
        // 构造华为账号登录选项
        HuaweiIdAuthParams authParam = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .createParams();

        mAuthService = HuaweiIdAuthManager.getService(LoginAct.this, authParam);

        // 获取登录授权页面的Intent，并通过startActivityForResult拉起授权页面
        startActivityForResult(mAuthService.getSignInIntent(), REQUEST_SIGN_IN_LOGIN);
    }

    /**
     * 静默登录
     */
    private void silentSignIn() {
        // 配置授权参数
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .createParams();
        // 初始化HuaweiIdAuthService对象
        mAuthService = HuaweiIdAuthManager.getService(LoginAct.this, authParams);
        // 发起静默登录请求
        Task<AuthHuaweiId> task = mAuthService.silentSignIn();
        // 处理授权成功的登录结果
        task.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId authHuaweiId) {
                // 已经授权
                onHuaweiIdLoginSuccess(authHuaweiId, false);
                Log.d(TAG, authHuaweiId.getDisplayName() + " silent signIn success ");
            }
        });
        // 处理授权失败的登录结果
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    if (apiException.getStatusCode() == 2002) {
                        // 未授权，调用onHuaweiIdLogin方法拉起授权界面，让用户授权
                        onHuaweiIdLogin();
                    }
                }
            }
        });
    }

    /**
     * 保存用户信息到SharedPreferences
     */
    private void onHuaweiIdLoginSuccess(AuthHuaweiId huaweiAccount, boolean verifyPhone) {
        // 保存华为账号 openId
        String openId = huaweiAccount.getOpenId();
        SPUtil.put(this, SPConstants.KEY_HW_OEPNID, openId);
        try {
            JSONObject jsonObject = new JSONObject();
            // 保存华为帐号头像
            jsonObject.put(SPConstants.KEY_HEAD_PHOTO, huaweiAccount.getAvatarUriString());
            SPUtil.put(this, openId, jsonObject.toString());
        } catch (JSONException ignored) {
            Log.e(TAG, "onHuaweiIdLoginSuccess json format error");
        }
        // 是否登录
        SPUtil.put(this, SPConstants.KEY_LOGIN, true);
        // 华为登录
        SPUtil.put(this, SPConstants.KEY_HW_LOGIN, true);
        // 保存华为帐号昵称
        SPUtil.put(this, SPConstants.KEY_NICK_NAME, huaweiAccount.getDisplayName());

        if (verifyPhone) {
            // 手机号绑定
            startActivity(new Intent(this, VerifyAct.class));
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String localName = (String) SPUtil.get(this, SPConstants.KEY_NICK_NAME, "");
        if (!TextUtils.isEmpty(localName)) {
            mEtUserName.setText(localName);
        }

        // 初始化当前用户信息
        onInitLocalUser();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 关闭虚假用户检测API
        SafetyDetect.getClient(this).shutdownUserDetect().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void v) {
                // 关闭行为检测能力成功，您可以在此做一些记录
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 关闭行为检测能力失败，您需要根据具体的错误码进行处理或重新关闭
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: not OK!");
            return;
        }

        if (requestCode == Fido2Client.AUTHENTICATION_REQUEST) {
            String localName = (String) SPUtil.get(this, SPConstants.KEY_NICK_NAME, "");
            // 到FIDO服务器校验认证结果
            final boolean authToServerResult = fido2Handler.onAuthToServer(data);
            if (authToServerResult) {
                SPUtil.put(this, SPConstants.KEY_LOGIN, true);
                SPUtil.put(this, SPConstants.KEY_HW_LOGIN, false);
                SPUtil.put(this, SPConstants.KEY_NICK_NAME, localName);

                startActivity(new Intent(this, MainAct.class));
                finish();
            } else {
                loginUseNameAndPasswordWithUserDetect();
            }
        } else if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                // 华为帐号登录成功
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                Log.i(TAG, "signIn success");
                Log.i(TAG, "DisplayName: " + huaweiAccount.getDisplayName());
                Log.i(TAG, "AvatarUriString: " + huaweiAccount.getAvatarUriString());
                //登录成功处理
                onHuaweiIdLoginSuccess(huaweiAccount, true);
            } else {
                // 华为帐号登录失败
                String message = "signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode();
                ToastUtil.getInstance().showShort(this, message);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_back:
                // 返回
                finish();
                break;
            case R.id.login_login:
                // 登录
                onLogin();
                break;
            case R.id.login_register:
                // 登录
                onRegister();
                break;
            case R.id.hwid_signin:
                // 华为账号登录
                silentSignIn();
                break;
            case R.id.login_username_del:
                // 输入账号信息-删除
                mEtUserName.setText("");
                break;
            case R.id.login_password_del:
                // 输入密码信息-删除
                mEtPassword.setText("");
                break;
            default:
                break;
        }
    }
}