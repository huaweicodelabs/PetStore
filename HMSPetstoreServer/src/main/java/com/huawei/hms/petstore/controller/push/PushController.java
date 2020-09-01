package com.huawei.hms.petstore.controller.push;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hms.petstore.beans.push.Recepit;
import com.huawei.hms.petstore.common.log.LogFactory;
import com.huawei.hms.petstore.service.push.PushService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PushController {
    private static final Logger Log = LogFactory.runLog();

    @Autowired
    private PushService pushService = new PushService();

    @RequestMapping(value = "/petstore/newToken", method = RequestMethod.POST)
    public int newToken(@RequestBody String newToken) {
        Log.info("receive a new token");
        return pushService.processNewToken(newToken);
    }

    @RequestMapping(value = "/petstore/sendPush", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject sendPush(@RequestBody String pushRequest) {
        Log.info("receive a send push request");
        return pushService.processSendPush(pushRequest);
    }

    @RequestMapping(value = "/petstore/sendTopic", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject sendTopic(@RequestBody String topic) {
        Log.info("receive a send topic request");
        return pushService.processSendTopic(topic);
    }

    @RequestMapping(value = "/petstore/sendDataMessage", method = RequestMethod.POST)
    public @ResponseBody
    JSONObject sendDataMessage(@RequestBody String pushRequest) {
        Log.info("receive a send data message request");
        return pushService.processSendDataMessage(pushRequest);
    }

    @RequestMapping(value = "/petstore/receipt", method = RequestMethod.POST)
    public @ResponseBody
    Recepit receipt(@RequestBody String receiptList) {
        Log.info("receive a receipt");
        return pushService.processReceipt(receiptList);
    }
}
