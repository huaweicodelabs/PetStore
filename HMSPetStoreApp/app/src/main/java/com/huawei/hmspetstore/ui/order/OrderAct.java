package com.huawei.hmspetstore.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.OrderBean;
import com.huawei.hmspetstore.ui.center.MemberCenterAct;
import com.huawei.hmspetstore.ui.center.MemberRight;
import com.huawei.hmspetstore.ui.center.PurchasesOperation;
import com.huawei.hmspetstore.ui.center.RecordListener;
import com.huawei.hmspetstore.ui.order.adapter.OrderAdapter;
import com.huawei.hmspetstore.util.LoginUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.view.DividerItemDecoration;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 订单
 */
public class OrderAct extends AppCompatActivity {

    private static final String TAG = "OrderAct";

    private static final int REFRESH_CONSUMABLE_DATA = 1;

    private static final int REFRESH_NONCONSUMABLE_DATA = 2;

    private TextView mTvEmpty;
    // 顶部空格
    private View topEmpty;
    // 列表
    private RecyclerView mRecyclerView;

    private Handler refreshHandler = new RefreshHandler(this);

    // 列表数据
    private List<OrderBean> mItemData = new CopyOnWriteArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_act);

        // 初始化View
        initView();
        // 设置列表数据
        initRecyclerData();
        // 初始化RecyclerView
        initRecyclerView();
        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_iap_product);
    }

    /**
     * 初始化View
     */
    private void initView() {
        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);
        mTvEmpty = findViewById(R.id.order_empty);
        mRecyclerView = findViewById(R.id.order_recyclerView);
        topEmpty = findViewById(R.id.order_topEmpty);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });

        mTvEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到会员中心
                if (LoginUtil.loginCheck(OrderAct.this)) {
                    startActivity(new Intent(OrderAct.this, MemberCenterAct.class));
                }
            }
        });

        String content = getString(R.string.order_list_empty);
        SpannableString mSpannableString = getSpannableString(content);
        mTvEmpty.setText(mSpannableString);
    }

    private SpannableString getSpannableString(String content) {
        // 你还没有购买任何套餐，点击此处开始购买
        // Looks like you haven't tried out our packages! Choose the one that works best for you.
        SpannableString mSpannableString = new SpannableString(content);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary));
        if (content.contains("点击")) {
            mSpannableString.setSpan(colorSpan, 11, 13, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (content.contains("one")) {
            mSpannableString.setSpan(colorSpan, 59, 62, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return mSpannableString;
    }


    /**
     * 设置列表数据
     */
    private void initRecyclerData() {
        mItemData.clear();
        if (MemberRight.isVideoAvailableForever(this)) {
            PurchasesOperation.getRecords(this, IapClient.PriceType.IN_APP_NONCONSUMABLE, "", new RecordListener() {
                @Override
                public void onReceive(List<String> inAppPurchaseDataList) {
                    Message message = refreshHandler.obtainMessage(REFRESH_NONCONSUMABLE_DATA, inAppPurchaseDataList);
                    refreshHandler.sendMessage(message);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "load finish");
                }

                @Override
                public void onFail() {
                    Log.i(TAG, "load fail");
                }
            });
        }
        PurchasesOperation.getRecords(this, IapClient.PriceType.IN_APP_CONSUMABLE, "", new RecordListener() {
            @Override
            public void onReceive(List<String> inAppPurchaseDataList) {
                Message message = refreshHandler.obtainMessage(REFRESH_CONSUMABLE_DATA, inAppPurchaseDataList);
                refreshHandler.sendMessage(message);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "load finish");
            }

            @Override
            public void onFail() {
                Log.i(TAG, "load fail");
            }
        });
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        //设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        //设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        //设置Adapter
        OrderAdapter mOrderAdapter = new OrderAdapter(mItemData);
        mRecyclerView.setAdapter(mOrderAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, 20));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private static class RefreshHandler extends Handler {
        private WeakReference<OrderAct> activityHolder;

        public RefreshHandler(OrderAct activity) {
            activityHolder = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            OrderAct activity = activityHolder.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            switch (msg.what) {
                case REFRESH_CONSUMABLE_DATA:
                    List<String> inAppPurchaseDataList = (List<String>) msg.obj;
                    for (String data : inAppPurchaseDataList) {
                        InAppPurchaseData inAppPurchaseData;
                        try {
                            inAppPurchaseData = new InAppPurchaseData(data);
                            if (MemberRight.getCurrentUserId(activity).equals(inAppPurchaseData.getDeveloperPayload())) {
                                activity.mItemData.add(new OrderBean(activity, inAppPurchaseData));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "parse inAppPurchaseData error");
                        }
                    }
                    if (activity.mItemData != null && activity.mItemData.size() > 0) {
                        activity.topEmpty.setVisibility(View.VISIBLE);
                        activity.mTvEmpty.setVisibility(View.GONE);
                    } else {
                        activity.topEmpty.setVisibility(View.GONE);
                        activity.mTvEmpty.setVisibility(View.VISIBLE);
                    }
                    activity.mRecyclerView.getAdapter().notifyDataSetChanged();
                    break;
                case REFRESH_NONCONSUMABLE_DATA:
                    List<String> inAppPurchaseDataList2 = (List<String>) msg.obj;
                    for (String data : inAppPurchaseDataList2) {
                        InAppPurchaseData inAppPurchaseData;
                        try {
                            inAppPurchaseData = new InAppPurchaseData(data);
                            if (MemberRight.getCurrentUserId(activity).equals(inAppPurchaseData.getDeveloperPayload())) {
                                activity.mItemData.add(0, new OrderBean(activity, inAppPurchaseData));
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "parse inAppPurchaseData error");
                        }
                    }
                    if (activity.mItemData != null && activity.mItemData.size() > 0) {
                        activity.topEmpty.setVisibility(View.VISIBLE);
                        activity.mTvEmpty.setVisibility(View.GONE);
                    } else {
                        activity.topEmpty.setVisibility(View.GONE);
                        activity.mTvEmpty.setVisibility(View.VISIBLE);
                    }
                    activity.mRecyclerView.getAdapter().notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }
}