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
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialCreationOptionsRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerCreationOptionsResp;
import com.huawei.hmspetstore.util.fido.server.param.ServerRegDeleteRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerResponse;

/**
 * Fido2 server service
 */
public interface IFidoServer {
    ServerCreationOptionsResp
        getAttestationOptions(ServerPublicKeyCredentialCreationOptionsRequest request);

    ServerResponse getAttestationResult(ServerAttestationResultRequest attestationResultRequest);

    ServerCreationOptionsResp getAssertionOptions(
            ServerPublicKeyCredentialCreationOptionsRequest serverPublicKeyCredentialCreationOptionsRequest);

    ServerResponse getAssertionResult(ServerAssertionResultRequest assertionResultRequest);

    ServerResponse delete(ServerRegDeleteRequest regDeleteRequest);
}