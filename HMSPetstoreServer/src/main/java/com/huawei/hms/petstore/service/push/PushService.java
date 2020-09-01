package com.huawei.hms.petstore.service.push;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hms.petstore.beans.push.Recepit;
import com.huawei.hms.petstore.common.log.LogFactory;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PushService {
    private static final Logger Log = LogFactory.interfaceLog();

    private static final String APP_ID = "";
    private static final String APP_SECRET = "";

    private static final int CLICK_ACTION_OPEN_APP_PAGE = 1;

    private static List<String>  tokens = new ArrayList<>();

    public Recepit processReceipt(String receipt) {
        Log.info("begin process Receipt");
        Log.info(receipt);
        Recepit recepit = new Recepit();
        recepit.setErrno("ok");
        recepit.setErrmsg("process success");
        return recepit;
    }

    public int processNewToken(String newToken) {
        if ((!newToken.isEmpty()) && (!tokens.contains(newToken))) {
            tokens.add(newToken);
            return 200;
        }
        return 201;
    }

    public JSONObject processSendPush(String request) {
        Log.info("begin process push request");
        JSONObject pushMsg = constructPushMsg(request);
        Log.info(JSONObject.toJSONString(pushMsg));
        return HttpsUtil.sendPushMessage(APP_ID, APP_SECRET, pushMsg);
    }

    public JSONObject processSendTopic(String topic) {
        Log.info("begin process push request");
        JSONObject pushMsg = constructTopicMsg(topic);
        Log.info(JSONObject.toJSONString(pushMsg));
        return HttpsUtil.sendPushMessage(APP_ID, APP_SECRET, pushMsg);
    }

    public JSONObject processSendDataMessage(String request) {
        Log.info("begin process push request");
        JSONObject pushMsg = constructDataMsg(request);
        Log.info(JSONObject.toJSONString(pushMsg));
        return HttpsUtil.sendPushMessage(APP_ID, APP_SECRET, pushMsg);
    }

    private JSONObject constructDataMsg(String request) {
        JSONObject message = new JSONObject();
        message.put("data", request);
        message.put("topic", "VIP");

        JSONObject pushMsg = new JSONObject();
        pushMsg.put("validate_only", false);
        pushMsg.put("message", message);
        return pushMsg;
    }

    private JSONObject constructTopicMsg(String topic) {
        JSONObject message = new JSONObject();
        message.put("notification", getNotification("宠物商店上新啦！&快去看看吧！主题消息"));
        message.put("android", getAndroidPart());
        message.put("topic", topic);

        JSONObject pushMsg = new JSONObject();
        pushMsg.put("validate_only", false);
        pushMsg.put("message", message);
        return pushMsg;
    }

    private JSONObject constructPushMsg(String request) {
        JSONObject message = new JSONObject();
        message.put("notification", getNotification(request));
        message.put("android", getAndroidPart());
        message.put("token", tokens);

        JSONObject pushMsg = new JSONObject();
        pushMsg.put("validate_only", false);
        pushMsg.put("message", message);
        return pushMsg;
    }

    private JSONObject getNotification(String request) {
        JSONObject notification = new JSONObject();
        String[] message = request.split("&");
        notification.put("title", message[0]);
        notification.put("body", message[1]);
        return notification;
    }

    private JSONObject getAndroidPart() {
        JSONObject android = new JSONObject();
        android.put("bi_tag","pushReceipt");
        android.put("notification", getAnroidNotification());
        return android;
    }

    private JSONObject getAnroidNotification() {
        JSONObject clickAction = new JSONObject();
        clickAction.put("type", CLICK_ACTION_OPEN_APP_PAGE);
        clickAction.put("action", "com.huawei.hmspetstore.OPEN_PETSTORE");
        JSONObject androidNotification = new JSONObject();
        androidNotification.put("click_action", clickAction);
        return androidNotification;
    }
}
