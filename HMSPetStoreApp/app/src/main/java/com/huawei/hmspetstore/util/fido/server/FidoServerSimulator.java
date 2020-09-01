/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.huawei.hmspetstore.util.fido.server;

import com.huawei.hmspetstore.util.fido.server.param.ServerAssertionResultRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerAttestationResultRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerAuthenticatorSelectionCriteria;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialCreationOptionsRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerCreationOptionsResp;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialDescriptor;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialParameters;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialRpEntity;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialUserEntity;
import com.huawei.hmspetstore.util.fido.server.param.ServerRegDeleteRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerRegInfo;
import com.huawei.hmspetstore.util.fido.server.param.ServerResponse;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulating a Fido Server
 */
public class FidoServerSimulator implements IFidoServer {
    private static List<ServerRegInfo> regInfos = new ArrayList<>();

    @Override
    public ServerCreationOptionsResp
        getAttestationOptions(ServerPublicKeyCredentialCreationOptionsRequest request) {
        final ServerCreationOptionsResp response =
            new ServerCreationOptionsResp();
        response.setAttestation(request.getAttestation());

        ServerAuthenticatorSelectionCriteria selectionCriteria = request.getAuthenticatorSelection();
        if (selectionCriteria != null) {
            response.setAuthenticatorSelection(selectionCriteria);
        }

        response.setChallenge(ByteUtils.byte2base64(getChallege()));

        List<ServerPublicKeyCredentialDescriptor> excludeCredentialList = new ArrayList<>();
        for (ServerRegInfo info : regInfos) {
            ServerPublicKeyCredentialDescriptor desc = new ServerPublicKeyCredentialDescriptor();
            desc.setId(info.getCredentialId());
            desc.setType("public-key");
            excludeCredentialList.add(desc);
        }
        response.setExcludeCredentials(
            excludeCredentialList.toArray(new ServerPublicKeyCredentialDescriptor[excludeCredentialList.size()]));

        List<ServerPublicKeyCredentialParameters> pubKeyCredParamList = new ArrayList<>();
        ServerPublicKeyCredentialParameters cp = new ServerPublicKeyCredentialParameters();
        cp.setAlg(-7);
        cp.setType("public-key");
        pubKeyCredParamList.add(cp);
        cp = new ServerPublicKeyCredentialParameters();
        cp.setAlg(-257);
        cp.setType("public-key");
        pubKeyCredParamList.add(cp);
        response.setPubKeyCredParams(
            pubKeyCredParamList.toArray(new ServerPublicKeyCredentialParameters[pubKeyCredParamList.size()]));

        ServerPublicKeyCredentialRpEntity rpEntity = new ServerPublicKeyCredentialRpEntity();
        rpEntity.setName("www.huawei.fidodemo");
        response.setRp(rpEntity);

        response.setRpId("www.huawei.fidodemo");

        response.setTimeout(60L);
        ServerPublicKeyCredentialUserEntity user = new ServerPublicKeyCredentialUserEntity();
        user.setId(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        response.setUser(user);
        return response;
    }

    @Override
    public ServerResponse getAttestationResult(ServerAttestationResultRequest attestationResultRequest) {
        final ServerResponse response = new ServerResponse();
        ServerRegInfo info = new ServerRegInfo();
        info.setCredentialId(attestationResultRequest.getId());
        regInfos.add(info);
        return response;
    }

    @Override
    public ServerCreationOptionsResp getAssertionOptions(
        ServerPublicKeyCredentialCreationOptionsRequest serverPublicKeyCredentialCreationOptionsRequest) {
        final ServerCreationOptionsResp response =
            new ServerCreationOptionsResp();

        List<ServerPublicKeyCredentialDescriptor> allowCredentials = new ArrayList<>();
        for (ServerRegInfo info : regInfos) {
            ServerPublicKeyCredentialDescriptor desc = new ServerPublicKeyCredentialDescriptor();
            desc.setId(info.getCredentialId());
            desc.setType("public-key");
            allowCredentials.add(desc);
        }
        response.setAllowCredentials(
            allowCredentials.toArray(new ServerPublicKeyCredentialDescriptor[allowCredentials.size()]));

        response.setChallenge(ByteUtils.byte2base64(getChallege()));

        response.setRpId("www.huawei.fidodemo");

        response.setTimeout(60L);

        return response;
    }

    @Override
    public ServerResponse getAssertionResult(ServerAssertionResultRequest assertionResultRequest) {
        final ServerResponse response = new ServerResponse();
        return response;
    }

    @Override
    public ServerResponse delete(ServerRegDeleteRequest regDeleteRequest) {
        final ServerResponse response = new ServerResponse();
        regInfos.clear();
        return response;
    }

    private static byte[] getChallege() {
        return SecureRandom.getSeed(16);
    }
}