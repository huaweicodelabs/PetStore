
package com.huawei.hms.petstore.service;

import com.huawei.hms.petstore.controller.req.JwsVerifyReq;
import com.huawei.hms.petstore.controller.resp.JwsVerifyResp;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

/**
 * PetServiceTest.java
 *
 * @since 2020-03-16
 */
public class PetServiceTest {
    private PetService petService = new PetService();

    @Test
    public void testVerifyJwsPass() {
        String jws = null;
        try {
            File filepath = ResourceUtils.getFile("classpath:jwses/jws_pass");
            jws = FileUtils.readFileToString(filepath, "utf-8");
        } catch (IOException e) {
            Assert.fail("Expect no exception!");
        }
        JwsVerifyReq jwsVerifyReq = new JwsVerifyReq();
        jwsVerifyReq.setJws(jws);
        JwsVerifyResp jwsVerifyResp = petService.verifyJws(jwsVerifyReq);
        Assert.assertTrue(jwsVerifyResp.getResult());
    }

    @Test
    public void testVerifyJwsFail() {
        String jws = null;
        try {
            File filepath = ResourceUtils.getFile("classpath:jwses/jws_fail");
            jws = FileUtils.readFileToString(filepath, "utf-8");
        } catch (IOException e) {
            Assert.fail("Expect no exception!");
        }
        JwsVerifyReq jwsVerifyReq = new JwsVerifyReq();
        jwsVerifyReq.setJws(jws);
        JwsVerifyResp jwsVerifyResp = petService.verifyJws(jwsVerifyReq);
        Assert.assertFalse(jwsVerifyResp.getResult());
    }
}