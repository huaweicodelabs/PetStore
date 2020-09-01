package com.huawei.hmspetstore.ui.center;

import java.util.List;

public interface RecordListener {
    void onReceive(List<String> inAppPurchaseDataList);

    void onFinish();

    void onFail();
}
