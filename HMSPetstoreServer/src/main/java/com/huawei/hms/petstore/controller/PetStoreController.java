/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 */

package com.huawei.hms.petstore.controller;

import com.huawei.hms.petstore.common.log.InterfaceLog;
import com.huawei.hms.petstore.common.log.InterfaceLogUtil;
import com.huawei.hms.petstore.controller.req.JwsVerifyReq;
import com.huawei.hms.petstore.controller.req.OrderVerifyReq;
import com.huawei.hms.petstore.controller.req.SubscriptionModifyReq;
import com.huawei.hms.petstore.controller.req.UserRisksVerifyReq;
import com.huawei.hms.petstore.controller.resp.JwsVerifyResp;
import com.huawei.hms.petstore.controller.resp.QueryPetResp;
import com.huawei.hms.petstore.service.PetService;
import com.huawei.hms.petstore.service.iap.OrderService;
import com.huawei.hms.petstore.service.iap.SubscriptionService;
import com.huawei.hms.petstore.service.iap.notification.AppServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/petstore")
@RestController
public class PetStoreController {

    @Autowired
    private PetService petService;

    /**
     * 代理类
     *
     * @return DelegateRsp
     */
    @ResponseBody
    @RequestMapping(value = "/pet/{id}")
    public QueryPetResp getPet(@PathVariable Long id) {
        InterfaceLog interfLog = new InterfaceLog("/Pet/{id}");
        QueryPetResp rsp = new QueryPetResp();
        rsp.setPetInfo(petService.getPet(id));
        interfLog.setReq(id);
        interfLog.setResp(rsp);
        InterfaceLogUtil.printInterface(interfLog);
        return rsp;
    }

    @ResponseBody
    @RequestMapping(value = "/safetydetect/verifyjws", produces = "application/json", method = RequestMethod.POST)
    public JwsVerifyResp verifyJws(@RequestBody JwsVerifyReq jwsVerifyReq) {
        InterfaceLog interfLog = new InterfaceLog("verifyJws");
        JwsVerifyResp rsp = petService.verifyJws(jwsVerifyReq);
        InterfaceLogUtil.printInterface(interfLog);
        return rsp;
    }

    @ResponseBody
    @RequestMapping(value = "/safetydetect/verifyuserrisks", produces = "application/json", method = RequestMethod.POST)
    public String verifyUserRisks(@RequestBody UserRisksVerifyReq userRisksVerifyReq) {
        InterfaceLog interfLog = new InterfaceLog("verifyUserRisks");
        String rsp = petService.verifyUserRisks(userRisksVerifyReq);
        InterfaceLogUtil.printInterface(interfLog);
        return rsp;
    }

    @RequestMapping(value = "/sub/get", method= RequestMethod.POST)
    public String getSubscription(SubscriptionModifyReq subReq) {
        try {
            return SubscriptionService.getSubscription(subReq.getSubscriptionId(), subReq.getPurchaseToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping(value = "/sub/stop", method= RequestMethod.POST)
    public String stopSubscription(SubscriptionModifyReq subReq) {
        try {
            return SubscriptionService.stopSubscription(subReq.getSubscriptionId(), subReq.getPurchaseToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping(value = "/sub/delay", method= RequestMethod.POST)
    public String delaySubscription(SubscriptionModifyReq subReq) {
        try {
            return SubscriptionService.delaySubscription(subReq.getSubscriptionId(), subReq.getPurchaseToken(),
                    subReq.getCurrentExpirationTime(), subReq.getDesiredExpirationTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping(value = "/sub/returnFee", method= RequestMethod.POST)
    public String returnFeeSubscription(SubscriptionModifyReq subReq) {
        try {
            return SubscriptionService.returnFeeSubscription(subReq.getSubscriptionId(), subReq.getPurchaseToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping(value = "/sub/withdraw", method= RequestMethod.POST)
    public String withdrawSubscription(SubscriptionModifyReq subReq) {
        try {
            return SubscriptionService.withdrawSubscription(subReq.getSubscriptionId(), subReq.getPurchaseToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping(value = "/order/verifyToken", method= RequestMethod.POST)
    public String verifyToken(OrderVerifyReq req) {
        try {
            return OrderService.verifyToken(req.getPurchaseToken(), req.getProductId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    @RequestMapping(value = "/sub/notification", method= RequestMethod.POST)
    public String notification(@RequestBody String req) {
        try {
            return AppServer.dealNotification(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

}
