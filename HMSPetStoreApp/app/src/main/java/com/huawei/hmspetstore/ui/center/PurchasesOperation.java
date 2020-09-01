package com.huawei.hmspetstore.ui.center;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.IapClient;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.IsSandboxActivatedReq;
import com.huawei.hms.iap.entity.IsSandboxActivatedResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hmspetstore.util.CipherUtil;

import org.json.JSONException;

import static com.huawei.hmspetstore.ui.center.MemberRight.ONE_DAY;

public class PurchasesOperation {

    private static final String TAG = "PurchasesOperation";

    /**
     * 消耗已购买的商品
     *
     * @param context       上下文
     * @param purchaseToken 购买token
     */
    public static void consumePurchase(Context context, String purchaseToken) {
        // 构造消耗商品请求
        ConsumeOwnedPurchaseReq req = new ConsumeOwnedPurchaseReq();
        req.setPurchaseToken(purchaseToken);

        IapClient mClient = Iap.getIapClient(context);

        Task<ConsumeOwnedPurchaseResult> task = mClient.consumeOwnedPurchase(req);
        task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult result) {
                if (result != null && result.getStatus() != null) {
                    if (result.getStatus().getStatusCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                        // 成功消耗
                        Log.i(TAG, "consumePurchase success");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 响应异常
                Log.e(TAG, "consumePurchase fail, exception: " + e.getMessage());
            }
        });
    }

    /**
     * 发放权益
     *
     * @param context 上下文
     * @param data    购买数据
     */
    public static void deliverProduct(Context context, String data, int priceType) {
        InAppPurchaseData inAppPurchaseData;
        try {
            inAppPurchaseData = new InAppPurchaseData(data);
        } catch (JSONException e) {
            Log.e(TAG, "parse inAppPurchaseData error");
            return;
        }
        updateMemberRightData(context, inAppPurchaseData);
        if (priceType == IapClient.PriceType.IN_APP_CONSUMABLE) {
            PurchasesOperation.consumePurchase(context, inAppPurchaseData.getPurchaseToken());
        }
    }

    /**
     * 针对不同的商品类型，做不同的权益发放
     *
     * @param context           上下文
     * @param inAppPurchaseData 订单数据
     */
    private static void updateMemberRightData(Context context, InAppPurchaseData inAppPurchaseData) {
        String productId = inAppPurchaseData.getProductId();
        switch (productId) {
            case "member01":
                MemberRight.updateNormalVideoValidDate(context, 30 * ONE_DAY);
                break;
            case "member02":
                MemberRight.updateNormalVideoValidDate(context, 60 * ONE_DAY);
                break;
            case "member03":
                MemberRight.setVideoAvailableForever(context);
                break;
            case "subscribeMember01":
                MemberRight.updateVideoSubscriptionExpireDate(context, inAppPurchaseData);
                break;
            default:
                break;
        }
    }

    /**
     * 应用启动时补单机制
     *
     * @param context 上下文
     */
    public static void replenishForLaunch(final Context context) {
        IapClient mClient = Iap.getIapClient(context);
        mClient.isEnvReady().addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult result) {
                if (result.getReturnCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                    // 支持应用内支付
                    Log.i(TAG, "is support IAP");
                    // 应用补单机制
                    replenish(context, "", IapClient.PriceType.IN_APP_CONSUMABLE);
                    replenish(context, "", IapClient.PriceType.IN_APP_NONCONSUMABLE);
                    replenish(context, "", IapClient.PriceType.IN_APP_SUBSCRIPTION);
                }
            }
        });
    }

    /**
     * 补单机制实现
     *
     * @param context           上下文
     * @param continuationToken 查询已购商品的下一页标识，第一次查询为空值
     */
    public static void replenish(final Context context, String continuationToken, final int priceType) {
        OwnedPurchasesReq req = new OwnedPurchasesReq();
        req.setPriceType(priceType);
        req.setContinuationToken(continuationToken);
        IapClient mClient = Iap.getIapClient(context);

        Task<OwnedPurchasesResult> task = mClient.obtainOwnedPurchases(req);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                if (result != null && result.getStatus() != null) {
                    if (result.getStatus().getStatusCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                        if (result.getInAppPurchaseDataList() != null) {
                            int index = 0;
                            for (String data : result.getInAppPurchaseDataList()) {
                                boolean success = CipherUtil.doCheck(context, data, result.getInAppSignature().get(index));
                                if (success) {
                                    // 补发权益
                                    deliverProduct(context, data, priceType);
                                } else {
                                    //签名校验不通过
                                    Log.e(TAG, "check sign fail");
                                }
                                index++;
                            }
                        }
                        if (!TextUtils.isEmpty(result.getContinuationToken())) {
                            replenish(context, result.getContinuationToken(), priceType);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 请求异常
                Log.e(TAG, "getPurchase exception: " + e.getMessage());
            }
        });
    }

    /**
     * 获取订单记录
     *
     * @param context           上下文
     * @param priceType         商品类型
     * @param continuationToken 继续token
     * @param listener          数据监听
     */
    public static void getRecords(final Context context, final int priceType, String continuationToken, final RecordListener listener) {
        OwnedPurchasesReq req = new OwnedPurchasesReq();
        req.setPriceType(priceType);
        req.setContinuationToken(continuationToken);
        IapClient mClient = Iap.getIapClient(context);

        Task<OwnedPurchasesResult> task = mClient.obtainOwnedPurchaseRecord(req);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                if (result != null && result.getStatus() != null) {
                    if (result.getStatus().getStatusCode() == OrderStatusCode.ORDER_STATE_SUCCESS) {
                        listener.onReceive(result.getInAppPurchaseDataList());
                        // 如果有下一页，则继续查询
                        if (!TextUtils.isEmpty(result.getContinuationToken())) {
                            getRecords(context, priceType, result.getContinuationToken(), listener);
                        } else {
                            // 查询完成
                            listener.onFinish();
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 请求异常
                Log.e(TAG, "getPurchase exception: " + e.getMessage());
                listener.onFail();
            }
        });
    }

    /**
     * 测试是否是沙盒帐号
     */
    public static void checkSandbox(Context context) {
        IapClient mClient = Iap.getIapClient(context);
        Task<IsSandboxActivatedResult> task = mClient.isSandboxActivated(new IsSandboxActivatedReq());
        task.addOnSuccessListener(new OnSuccessListener<IsSandboxActivatedResult>() {
            @Override
            public void onSuccess(IsSandboxActivatedResult result) {
                Log.i(TAG, "isSandboxActivated success");
                String stringBuilder = "errMsg: " + result.getErrMsg() + '\n' +
                        "match version limit : " + result.getIsSandboxApk() + '\n' +
                        "match user limit : " + result.getIsSandboxUser();
                Log.i(TAG, stringBuilder);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "isSandboxActivated fail");
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    int returnCode = apiException.getStatusCode();
                    String errMsg = apiException.getMessage();
                    Log.e(TAG, "returnCode: " + returnCode + ", errMsg: " + errMsg);
                } else {
                    Log.e(TAG, "isSandboxActivated fail, unknown error");
                }
            }
        });
    }
}