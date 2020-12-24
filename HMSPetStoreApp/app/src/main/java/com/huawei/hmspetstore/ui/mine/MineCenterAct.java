package com.huawei.hmspetstore.ui.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.address.AddAddressManageAct;
import com.huawei.hmspetstore.ui.center.MemberCenterAct;
import com.huawei.hmspetstore.ui.center.MemberRight;
import com.huawei.hmspetstore.ui.order.OrderAct;
import com.huawei.hmspetstore.ui.setting.SettingAct;
import com.huawei.hmspetstore.util.LoginUtil;
import com.huawei.hmspetstore.util.SPUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 个人中心
 */
public class MineCenterAct extends AppCompatActivity {
    private static final String TAG = "MineCenterAct";

    // 头像
    private ImageView mIvHeadImg;
    // 成为会员
    private TextView mTvMembers;
    // 会员-时间
    private TextView mTvMembersTime, mTvNickName;
    // 会员卡
    private LinearLayout mLlMemberLayout;
    // 会员卡 - 图标
    private ImageView mIvMemberImg;
    // 会员卡 - 名称
    private TextView mTvMemberName;
    // 会员卡 - 描述
    private TextView mTvMemberDesc;
    // 会员卡 - 立即续费
    private TextView mTvMemberPay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mine_act);
        // 初始化View
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initMemberInfo();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mTvNickName = findViewById(R.id.name_view);
        // 头像
        mIvHeadImg = findViewById(R.id.mine_headImg);
        // 成为会员
        mTvMembers = findViewById(R.id.mine_members);
        // 会员-时间
        mTvMembersTime = findViewById(R.id.mine_members_time);
        // 会员卡
        mLlMemberLayout = findViewById(R.id.mine_member_layout);
        // 会员卡 - 图标
        mIvMemberImg = findViewById(R.id.mine_member_icon);
        // 会员卡 - 名称
        mTvMemberName = findViewById(R.id.mine_member_name);
        // 会员卡 - 描述
        mTvMemberDesc = findViewById(R.id.mine_member_desc);
        // 会员卡 - 立即续费
        mTvMemberPay = findViewById(R.id.mine_member_pay);

        findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });

        findViewById(R.id.title_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置
                startActivity(new Intent(MineCenterAct.this, SettingAct.class));
            }
        });

        findViewById(R.id.mine_orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 我的订单
                startActivity(new Intent(MineCenterAct.this, OrderAct.class));
            }
        });

        findViewById(R.id.mine_addressManage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到地址管理界面
                startActivity(new Intent(MineCenterAct.this, AddAddressManageAct.class));

            }
        });
        findViewById(R.id.sub_manage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String appid = AGConnectServicesConfig.fromContext(MineCenterAct.this).getString("client/app_id");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String url = "pay://com.huawei.hwid.external/subscriptions?package=com.huawei.hmspetstore&appid=" + appid + "&sku=subscribeMember01";
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        mTvMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到会员中心
                if (LoginUtil.loginCheck(MineCenterAct.this)) {
                    startActivity(new Intent(MineCenterAct.this, MemberCenterAct.class));
                }
            }
        });
    }

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
            Log.e(TAG, "initData JSONException");
        }
        if (!TextUtils.isEmpty(headPhoto)) {
            Glide.with(this)
                    .load(headPhoto)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mIvHeadImg);
        }
    }

    /**
     * 设置会员卡信息
     */
    private void initMemberInfo() {
        initData();

        // 会员状态显示
        if (MemberRight.isVideoAvailableForever(this)) {
            mTvMembers.setVisibility(View.GONE);
        } else if (MemberRight.isVideoSubscriptionValid(this)) {
            mTvMembers.setText(getString(R.string.iap_member_valid, new SimpleDateFormat("MM/dd/YYYY", Locale.US).format(MemberRight.getVideoSubscriptionExpireDate(this))));
        } else if (MemberRight.isVideoAvailable(this)) {
            mTvMembers.setText(getString(R.string.iap_member_valid, new SimpleDateFormat("MM/dd/YYYY", Locale.US).format(MemberRight.getNormalVideoExpireDate(this))));
        }

        // 会员状态显示
        if (MemberRight.isVideoAvailableForever(this)) {
            mTvMembersTime.setText(R.string.iap_buy_forever);
            mTvMembers.setVisibility(View.GONE);
        } else if (MemberRight.isVideoSubscriptionValid(this)) {
            mTvMembersTime.setText(getString(R.string.iap_member_valid, new SimpleDateFormat("MM/dd/YYYY", Locale.US).format(MemberRight.getVideoSubscriptionExpireDate(this))));
            mTvMembers.setVisibility(View.GONE);
        } else if (MemberRight.isVideoAvailable(this)) {
            mTvMembers.setVisibility(View.GONE);
            mTvMembersTime.setText(getString(R.string.iap_member_valid, new SimpleDateFormat("MM/dd/YYYY", Locale.US).format(MemberRight.getNormalVideoExpireDate(this))));
        } else {
            mTvMembers.setVisibility(View.VISIBLE);
        }

        // 会员详情展示
        if (MemberRight.isVideoAvailableForever(this)) {
            mLlMemberLayout.setVisibility(View.VISIBLE);
            mTvMemberName.setText(R.string.iap_buy_member_forever);
            mTvMemberDesc.setText(R.string.member_desc);
            mTvMemberPay.setVisibility(View.GONE);
        } else if (System.currentTimeMillis() < MemberRight.getVideoSubscriptionExpireDate(this)) {
            mLlMemberLayout.setVisibility(View.VISIBLE);
            mTvMemberName.setText(R.string.iap_buy_member_subscription);
            mTvMemberDesc.setText(R.string.member_desc);
            mTvMemberPay.setText(R.string.iap_buy_subscription);
        } else if (MemberRight.isVideoAvailable(this)) {
            mLlMemberLayout.setVisibility(View.VISIBLE);
            mTvMemberName.setText(R.string.iap_buy_member);
            mTvMemberDesc.setText(R.string.member_desc);
            mTvMemberPay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MineCenterAct.this, MemberCenterAct.class));
                }
            });
        } else {
            mLlMemberLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isLogin = (boolean) SPUtil.get(this, SPConstants.KEY_LOGIN, false);
        if (!isLogin) {
            finish();
        }
    }
}