package com.huawei.hmspetstore.ui.center;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.ProductInfo;
import com.huawei.hms.iap.entity.ProductInfoReq;
import com.huawei.hms.iap.entity.ProductInfoResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hmspetstore.R;
import com.huawei.hmspetstore.bean.SetMealBean;
import com.huawei.hmspetstore.common.ICallBack;
import com.huawei.hmspetstore.common.IRecyclerItemListener;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.ui.address.AddressAct;
import com.huawei.hmspetstore.ui.center.adapter.MemCenterAdapter;
import com.huawei.hmspetstore.ui.push.PushConst;
import com.huawei.hmspetstore.ui.push.PushService;
import com.huawei.hmspetstore.util.CipherUtil;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.SafetyDetectUtil;
import com.huawei.hmspetstore.util.ToastUtil;
import com.huawei.hmspetstore.view.IAPDialog;
import com.huawei.hmspetstore.view.LoadingDialog;
import com.huawei.hmspetstore.view.RootTipDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 个人中心
 */
public class MemberCenterAct extends AppCompatActivity {

    private static final String TAG = "MemberCenterAct";

    private static final int REFRESH_NOT_SUPPORT_IAP_WHAT = 1;

    private static final int LOGIN_ACCOUNT_FIRST_WHAT = 2;

    private static final int REFRESH_PRODUCT_LIST_WHAT = 3;

    private static final int REQUEST_FAIL_WHAT = 4;

    private static final int BUY_FAIL_WHAT = 5;

    private static final int BUY_ALREADY_WHAT = 6;

    private static final int PRODUCT_AREA_NOT_SUPPORT = 7;

    private static final int REQ_CODE_LOGIN = 101;

    private static final int REQ_CODE_PAY_CONSUMABLE = 102;

    private static final int REQ_CODE_PAY_NON_CONSUMABLE = 103;

    private static final int REQ_CODE_PAY_SUBSCRIPTION = 104;

    private static final List<String> CONSUMABLE_PRODUCT_LIST = new ArrayList<>();

    private static final List<String> NON_CONSUMABLE_PRODUCT_LIST = new ArrayList<>();

    private static final List<String> SUBSCRIPTION_PRODUCT_LIST = new ArrayList<>();

    /**
     * 商品类型展示顺序
     */
    private static final int[] SHOW_SEQUENCE = {IapClient.PriceType.IN_APP_CONSUMABLE,
            IapClient.PriceType.IN_APP_NONCONSUMABLE, IapClient.PriceType.IN_APP_SUBSCRIPTION};

    static {
        // 在PMS上配置的商品ID
        CONSUMABLE_PRODUCT_LIST.add("member01");
        CONSUMABLE_PRODUCT_LIST.add("member02");
        NON_CONSUMABLE_PRODUCT_LIST.add("member03");
        SUBSCRIPTION_PRODUCT_LIST.add("subscribeMember01");
    }

    // 头像
    private ImageView mIvHeadImg;

    // 列表
    private RecyclerView mRecyclerView;

    // 列表数据
    private List<SetMealBean> mItemData = new ArrayList<>();

    private Handler refreshHandler = new RefreshHandler(this);

    /**
     * 从Status里获取解决问题的方案
     *
     * @param activity 页面activity
     * @param status   result里提取的status
     * @param reqCode  拉起解决页面的请求码
     * @return boolean 是否有解决方案
     */
    private static boolean startResolution(Activity activity, Status status, int reqCode) {
        if (status.hasResolution()) {
            try {
                status.startResolutionForResult(activity, reqCode);
                return true;
            } catch (IntentSender.SendIntentException exp) {
                Log.i(TAG, "startResolution fail");
            }
        } else {
            Log.i(TAG, "startResolution , intent is null");
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.membercenter_act);
        // 初始化View
        initView();
        // 检查环境是否支持IAP
        checkEnv();
        // 测试当前帐号是否支持沙箱
        PurchasesOperation.checkSandbox(this);

        // 提示进入的Kit
        ToastUtil.getInstance().showShort(this, R.string.toast_iap);
    }

    /**
     * 检查当前华为帐号的服务地是否支持应用内支付
     */
    private void checkEnv() {
        IapClient mClient = Iap.getIapClient(this);
        mClient.isEnvReady().addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult result) {
                if (result.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                    // 支持应用内支付，则加载商品信息
                    Log.i(TAG, "is support IAP");
                    loadProducts();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException exception = (IapApiException) e;
                    Status status = exception.getStatus();
                    int returnCode = status.getStatusCode();
                    // 帐号未登录，则优先拉起华为帐号登录界面
                    if (OrderStatusCode.ORDER_HWID_NOT_LOGIN == returnCode) {
                        boolean hasResolution = startResolution(MemberCenterAct.this, status, REQ_CODE_LOGIN);
                        if (hasResolution) {
                            return;
                        }
                    }
                }
                refreshHandler.sendEmptyMessage(REFRESH_NOT_SUPPORT_IAP_WHAT);
            }
        });
    }

    private void loadProducts() {
        // 商品查询结果回调监听
        OnUpdateProductListListener updateProductListListener = new OnUpdateProductListListener(3, refreshHandler);

        // 消耗型商品请求
        ProductInfoReq consumeProductInfoReq = new ProductInfoReq();
        consumeProductInfoReq.setPriceType(IapClient.PriceType.IN_APP_CONSUMABLE);
        consumeProductInfoReq.setProductIds(CONSUMABLE_PRODUCT_LIST);

        // 非消耗型商品请求
        ProductInfoReq nonCousumableProductInfoReq = new ProductInfoReq();
        nonCousumableProductInfoReq.setPriceType(IapClient.PriceType.IN_APP_NONCONSUMABLE);
        nonCousumableProductInfoReq.setProductIds(NON_CONSUMABLE_PRODUCT_LIST);

        // 订阅型商品请求
        ProductInfoReq subscriptionProductInfoReq = new ProductInfoReq();
        subscriptionProductInfoReq.setPriceType(IapClient.PriceType.IN_APP_SUBSCRIPTION);
        subscriptionProductInfoReq.setProductIds(SUBSCRIPTION_PRODUCT_LIST);

        // 查询商品信息
        getProducts(consumeProductInfoReq, updateProductListListener);
        getProducts(nonCousumableProductInfoReq, updateProductListListener);
        getProducts(subscriptionProductInfoReq, updateProductListListener);
    }

    private void getProducts(final ProductInfoReq productInfoReq, final OnUpdateProductListListener productListListener) {
        IapClient mClient = Iap.getIapClient(this);
        Task<ProductInfoResult> task = mClient.obtainProductInfo(productInfoReq);
        task.addOnSuccessListener(new OnSuccessListener<ProductInfoResult>() {
            @Override
            public void onSuccess(ProductInfoResult result) {
                // 查询商品成功
                productListListener.onUpdate(productInfoReq.getPriceType(), result);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 查询商品失败
                productListListener.onFail(e);
            }
        });
    }

    /**
     * 提示真实购买
     */
    private void gotoBuy(ProductInfo productInfo) {
        FragmentManager manager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) manager.findFragmentByTag("mIAPDialog");
        if ((fragment != null) && (fragment.getDialog() != null) && (fragment.getDialog().isShowing())) {
            return;
        }
        IAPDialog mIAPDialog = IAPDialog.newInstance();
        mIAPDialog.setCallBack(new ICallBack<Integer>() {
            @Override
            public void onSuccess(Integer bean) {
                if (bean == 1) {
                    // 继续
                    buy(productInfo.getPriceType(), productInfo.getProductId());
                } else {
                    // 取消
                }
            }

            @Override
            public void onError(String errorMsg) {
            }

        });
        mIAPDialog.show(getSupportFragmentManager(), "mIAPDialog");
    }

    private void buy(final int type, String productId) {
        // 构造购买请求
        PurchaseIntentReq req = new PurchaseIntentReq();
        req.setProductId(productId);
        req.setPriceType(type);
        req.setDeveloperPayload(MemberRight.getCurrentUserId(this));

        IapClient mClient = Iap.getIapClient(this);
        Task<PurchaseIntentResult> task = mClient.createPurchaseIntent(req);
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                if (result != null && result.getStatus() != null) {
                    // 拉起应用内支付页面
                    boolean success = startResolution(MemberCenterAct.this, result.getStatus(), getRequestCode(type));
                    if (success) {
                        return;
                    }
                }
                refreshHandler.sendEmptyMessage(REQUEST_FAIL_WHAT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "buy fail, exception: " + e.getMessage());
                refreshHandler.sendEmptyMessage(REQUEST_FAIL_WHAT);
            }
        });
    }

    private int getRequestCode(int priceType) {
        switch (priceType) {
            case IapClient.PriceType.IN_APP_CONSUMABLE:
                return REQ_CODE_PAY_CONSUMABLE;
            case IapClient.PriceType.IN_APP_NONCONSUMABLE:
                return REQ_CODE_PAY_NON_CONSUMABLE;
            case IapClient.PriceType.IN_APP_SUBSCRIPTION:
                return REQ_CODE_PAY_SUBSCRIPTION;
            default:
                return REQ_CODE_PAY_CONSUMABLE;
        }
    }

    private int getPriceType(int requestCode) {
        switch (requestCode) {
            case REQ_CODE_PAY_CONSUMABLE:
                return IapClient.PriceType.IN_APP_CONSUMABLE;
            case REQ_CODE_PAY_NON_CONSUMABLE:
                return IapClient.PriceType.IN_APP_NONCONSUMABLE;
            case REQ_CODE_PAY_SUBSCRIPTION:
                return IapClient.PriceType.IN_APP_SUBSCRIPTION;
            default:
                return IapClient.PriceType.IN_APP_CONSUMABLE;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PurchaseResultInfo buyResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data);
        Log.i(TAG, "confirmOrder, returnCode: " + buyResultInfo.getReturnCode() + " errMsg: " + buyResultInfo.getErrMsg());

        // 用户取消支付
        if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_CANCEL) {
            Log.i(TAG, "cancel buy product");
            return;
        }
        switch (requestCode) {
            case REQ_CODE_LOGIN:
                if (data != null) {
                    int returnCode = data.getIntExtra("returnCode", -1);
                    if (returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                        // 登录成功，重新检查支付环境
                        checkEnv();
                        return;
                    }
                }
                refreshHandler.sendEmptyMessage(LOGIN_ACCOUNT_FIRST_WHAT);
                break;
            case REQ_CODE_PAY_CONSUMABLE:
            case REQ_CODE_PAY_NON_CONSUMABLE:
            case REQ_CODE_PAY_SUBSCRIPTION:
                int priceType = getPriceType(requestCode);
                if (resultCode == RESULT_OK) {
                    // 购买成功
                    if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                        // 先校验数据签名
                        boolean success = CipherUtil.doCheck(this, buyResultInfo.getInAppPurchaseData(), buyResultInfo.getInAppDataSignature());
                        if (success) {
                            // 订阅会员主题
                            PushService.subscribe(getApplicationContext(), PushConst.TOPIC_VIP);
                            PurchasesOperation.deliverProduct(this, buyResultInfo.getInAppPurchaseData(), priceType);
                        } else {
                            //签名校验不通过
                            Log.e(TAG, "check sign fail");
                            return;
                        }

                    } else if (buyResultInfo.getReturnCode() == OrderStatusCode.ORDER_PRODUCT_OWNED) {
                        // 重复购买，需要先消耗
                        PurchasesOperation.replenish(this, "", priceType);
                        refreshHandler.sendEmptyMessageDelayed(BUY_ALREADY_WHAT, 500);
                    } else {
                        Log.e(TAG, "buy fail, returnCode: " + buyResultInfo.getReturnCode() + " errMsg: " + buyResultInfo.getErrMsg());
                        refreshHandler.sendEmptyMessage(BUY_FAIL_WHAT);
                    }
                } else {
                    Log.i(TAG, "cancel pay");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化View
     */
    private void initView() {
        mIvHeadImg = findViewById(R.id.member_headImg);
        // 初始化RecyclerView
        mRecyclerView = findViewById(R.id.membercenter_recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        // 设置布局管理器
        mRecyclerView.setLayoutManager(manager);
        // 设置为垂直布局，这也是默认的
        manager.setOrientation(RecyclerView.VERTICAL);
        // 设置Adapter
        MemCenterAdapter mMemCenterAdapter = new MemCenterAdapter(mItemData);
        mRecyclerView.setAdapter(mMemCenterAdapter);
        mMemCenterAdapter.setListener(new IRecyclerItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                onAdapterItemClick(position);
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // 顶部返回
        ImageView mIvBack = findViewById(R.id.title_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回
                finish();
            }
        });
        findViewById(R.id.add_address_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MemberCenterAct.this, AddressAct.class);
                startActivity(intent);
            }
        });
        initHeadImg();
    }

    private void initHeadImg() {
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
            Log.e(TAG, "initHeadImg JSONException");
        }
        if (!TextUtils.isEmpty(headPhoto)) {
            Glide.with(this)
                    .load(headPhoto)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(mIvHeadImg);
        }
    }

    /**
     * 设置列表数据
     */
    private void updateProductListData(Map<Integer, ProductInfoResult> resultMap) {
        // 刷新数据
        mItemData.clear();
        for (int priceType : SHOW_SEQUENCE) {
            ProductInfoResult productInfoResult = resultMap.get(priceType);
            if (productInfoResult != null) {
                if (productInfoResult.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                    for (ProductInfo productInfo : productInfoResult.getProductInfoList()) {
                        mItemData.add(new SetMealBean(productInfo));
                    }
                }
            }
        }
        // 更新界面
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 列表点击事件
     */
    private void onAdapterItemClick(int position) {
        SetMealBean setMealBean = mItemData.get(position);
        final ProductInfo productInfo = setMealBean.getProductInfo();

        LoadingDialog loadingDialog = LoadingDialog.newInstance();
        loadingDialog.show(getSupportFragmentManager(), "LoggingDialog");

        // 调用系统完整性检测接口以检测支付环境风险
        SafetyDetectUtil.detectSysIntegrity(this, new ICallBack<Boolean>() {
            @Override
            public void onSuccess(Boolean baseIntegrity) {
                loadingDialog.dismiss();
                if (baseIntegrity) {
                    // 系统完整性未遭到破坏
                    gotoBuy(productInfo);
                } else {
                    // 系统完整性遭到破坏, 如果继续进行支付行为，存在风险，弹出提示框，来提醒用户，并让用户选择是否继续
                    showRootTipDialog(productInfo);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "Detect sysIntegrity failed! Detail: " + errorMsg);
                loadingDialog.dismiss();
                gotoBuy(productInfo);
            }
        });
    }

    private void showRootTipDialog(final ProductInfo productInfo) {
        FragmentManager manager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) manager.findFragmentByTag("RootTipDialog");
        if ((fragment != null) && (fragment.getDialog() != null) && (fragment.getDialog().isShowing())) {
            return;
        }
        RootTipDialog mRootTipDialog = RootTipDialog.newInstance();
        mRootTipDialog.setCallBack(new ICallBack<Integer>() {
            @Override
            public void onSuccess(Integer bean) {
                if (bean == 1) {
                    // 继续
                    gotoBuy(productInfo);
                } else {
                    // 返回
                }
            }

            @Override
            public void onError(String errorMsg) {

            }

        });
        mRootTipDialog.show(getSupportFragmentManager(), "RootTipDialog");
    }

    private static class RefreshHandler extends Handler {
        private WeakReference<MemberCenterAct> activityHolder;

        public RefreshHandler(MemberCenterAct activity) {
            activityHolder = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            MemberCenterAct activity = activityHolder.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            switch (msg.what) {
                case REFRESH_NOT_SUPPORT_IAP_WHAT:
                    ToastUtil.getInstance().showShort(activity, R.string.iap_not_support);
                    break;
                case LOGIN_ACCOUNT_FIRST_WHAT:
                    ToastUtil.getInstance().showShort(activity, R.string.iap_pls_login);
                    break;
                case REFRESH_PRODUCT_LIST_WHAT:
                    Map<Integer, ProductInfoResult> data = (Map<Integer, ProductInfoResult>) msg.obj;
                    activity.updateProductListData(data);
                    break;
                case REQUEST_FAIL_WHAT:
                    ToastUtil.getInstance().showShort(activity, R.string.iap_request_fail);
                    break;
                case BUY_FAIL_WHAT:
                    ToastUtil.getInstance().showShort(activity, R.string.iap_buy_fail);
                    break;
                case BUY_ALREADY_WHAT:
                    ToastUtil.getInstance().showShort(activity, R.string.iap_buy_already);
                    break;
                case PRODUCT_AREA_NOT_SUPPORT:
                    ToastUtil.getInstance().showShort(activity, R.string.iap_product_not_support);
                    break;
                default:
                    break;
            }
        }
    }

    private static class OnUpdateProductListListener {
        private AtomicInteger taskCount;

        private Map<Integer, ProductInfoResult> resultMap;

        private Handler refreshHandler;

        public OnUpdateProductListListener(int taskCount, Handler handler) {
            this.taskCount = new AtomicInteger(taskCount);
            this.resultMap = new HashMap<>();
            this.refreshHandler = handler;
        }

        public void onUpdate(int priceType, ProductInfoResult result) {
            Log.i(TAG, "query product success " + taskCount.get());
            resultMap.put(priceType, result);
            onRefreshView();
        }

        public void onFail(Exception e) {
            Log.e(TAG, "query product fail " + taskCount.get());
            onRefreshView();
        }

        private void onRefreshView() {
            // 直到所有商品信息查询完成，才一次性展示出来
            if (taskCount.decrementAndGet() == 0) {
                Message message = refreshHandler.obtainMessage(REFRESH_PRODUCT_LIST_WHAT, resultMap);
                refreshHandler.sendMessage(message);
            }
        }
    }
}