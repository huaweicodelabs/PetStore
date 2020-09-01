package com.huawei.hmspetstore.util.fido;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.support.api.fido.fido2.Algorithm;
import com.huawei.hms.support.api.fido.fido2.Attachment;
import com.huawei.hms.support.api.fido.fido2.AttestationConveyancePreference;
import com.huawei.hms.support.api.fido.fido2.AuthenticatorAssertionResponse;
import com.huawei.hms.support.api.fido.fido2.AuthenticatorAttestationResponse;
import com.huawei.hms.support.api.fido.fido2.AuthenticatorSelectionCriteria;
import com.huawei.hms.support.api.fido.fido2.AuthenticatorTransport;
import com.huawei.hms.support.api.fido.fido2.Fido2;
import com.huawei.hms.support.api.fido.fido2.Fido2AuthenticationRequest;
import com.huawei.hms.support.api.fido.fido2.Fido2AuthenticationResponse;
import com.huawei.hms.support.api.fido.fido2.Fido2Client;
import com.huawei.hms.support.api.fido.fido2.Fido2Intent;
import com.huawei.hms.support.api.fido.fido2.Fido2IntentCallback;
import com.huawei.hms.support.api.fido.fido2.Fido2RegistrationRequest;
import com.huawei.hms.support.api.fido.fido2.Fido2RegistrationResponse;
import com.huawei.hms.support.api.fido.fido2.Fido2Response;
import com.huawei.hms.support.api.fido.fido2.NativeFido2AuthenticationOptions;
import com.huawei.hms.support.api.fido.fido2.NativeFido2RegistrationOptions;
import com.huawei.hms.support.api.fido.fido2.PublicKeyCredentialCreationOptions;
import com.huawei.hms.support.api.fido.fido2.PublicKeyCredentialDescriptor;
import com.huawei.hms.support.api.fido.fido2.PublicKeyCredentialParameters;
import com.huawei.hms.support.api.fido.fido2.PublicKeyCredentialRequestOptions;
import com.huawei.hms.support.api.fido.fido2.PublicKeyCredentialRpEntity;
import com.huawei.hms.support.api.fido.fido2.PublicKeyCredentialType;
import com.huawei.hms.support.api.fido.fido2.PublicKeyCredentialUserEntity;
import com.huawei.hms.support.api.fido.fido2.UserVerificationRequirement;
import com.huawei.hmspetstore.common.ICallBack;
import com.huawei.hmspetstore.constant.SPConstants;
import com.huawei.hmspetstore.util.SPUtil;
import com.huawei.hmspetstore.util.fido.server.ByteUtils;
import com.huawei.hmspetstore.util.fido.server.FidoServerSimulator;
import com.huawei.hmspetstore.util.fido.server.IFidoServer;
import com.huawei.hmspetstore.util.fido.server.param.ServerAssertionResultRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerAssertionResultResponseRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerAttestationResultRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerAttestationResultResponseRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerAuthenticatorSelectionCriteria;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialCreationOptionsRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerCreationOptionsResp;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialDescriptor;
import com.huawei.hmspetstore.util.fido.server.param.ServerPublicKeyCredentialParameters;
import com.huawei.hmspetstore.util.fido.server.param.ServerRegDeleteRequest;
import com.huawei.hmspetstore.util.fido.server.param.ServerResponse;
import com.huawei.hmspetstore.util.fido.server.param.ServerStatus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fido2登录相关
 */
public class Fido2Handler {
    private static final String TAG = "Fido2Handler";

    //使用平台认证器指纹或面容。
    private static final String AUTH_ATTACH_MODE = "platform";

    //FIDO2客户端
    private final Fido2Client fido2Client;

    private final Activity activity;

    //FIDO服务器
    private final IFidoServer fidoServerClient = new FidoServerSimulator();

    public Fido2Handler(Activity activity) {
        this.activity = activity;
        //初始化FIDO2客户端
        fido2Client = Fido2.getFido2Client(activity);
    }

    public void onRegistration(String username, ICallBack callBack) {
        if (!fido2Client.isSupported()) {
            Log.e(TAG, "onRegistration: FIDO2 is not supported.");
            callBack.onError("onRegistration: FIDO2 is not supported.");
            return;
        }
        // 组装FIDO服务器请求消息
        ServerPublicKeyCredentialCreationOptionsRequest request =
                getRegServerPublicKeyCredentialCreationOptionsRequest(username);
        if (request == null) {
            callBack.onError("onRegistration: create request fail.");
            return;
        }

        // 从FIDO服务器获取挑战值和相关策略
        ServerCreationOptionsResp response = fidoServerClient.getAttestationOptions(request);
        if (!ServerStatus.OK.equals(response.getStatus())) {
            Log.e(TAG, "re" + response.getErrorMessage());
            callBack.onError("onRegistration: get attestation options fail.");
            return;
        }

        // 组装注册请求消息
        PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions =
                getCreationOptions(response);

        //启动FIDO2客户端注册流程
        reg2Fido2Client(publicKeyCredentialCreationOptions, callBack);
    }

    private void reg2Fido2Client(PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions,
                                 final ICallBack callBack) {
        NativeFido2RegistrationOptions registrationOptions = NativeFido2RegistrationOptions.DEFAULT_OPTIONS;
        Fido2RegistrationRequest registrationRequest =
                new Fido2RegistrationRequest(publicKeyCredentialCreationOptions, null);

        // 调用Fido2Client.getRegistrationIntent获取Fido2Intent，并启动FIDO2客户端注册流程。
        fido2Client.getRegistrationIntent(registrationRequest, registrationOptions, new Fido2IntentCallback() {
            @Override
            public void onSuccess(Fido2Intent fido2Intent) {
                // 通过Fido2Client.REGISTRATION_REQUEST启动FIDO2客户端注册流程。
                Log.d(TAG, "reg2Fido2Client onSuccess.");
                fido2Intent.launchFido2Activity(Fido2Handler.this.activity, Fido2Client.REGISTRATION_REQUEST);
            }

            @Override
            public void onFailure(int errorCode, CharSequence errString) {
                Log.d(TAG, "reg2Fido2Client onFailure: register failed!" + errorCode + "=" + errString);
                callBack.onError("authn fail");
            }
        });
    }

    public void onAuthentication(String username, ICallBack callBack) {
        Boolean fido2Switch =
                (Boolean) SPUtil.get(activity.getApplicationContext(), SPConstants.FINGER_PRINT_LOGIN_SWITCH, false);

        if (!fido2Switch) {
            callBack.onError("fido2 switch off");
            return;
        }
        if (!fido2Client.isSupported()) {
            Log.d(TAG, "onClickAuthentication: FIDO2 is not supported.");
            callBack.onError("onClickAuthentication: FIDO2 is not supported.");
            return;
        }

        // 组装FIDO服务器请求消息
        ServerPublicKeyCredentialCreationOptionsRequest request =
                getAuthnServerPublicKeyCredentialCreationOptionsRequest(username);
        if (request == null) {
            callBack.onError("create request fail");
            return;
        }

        // 从FIDO服务器获取挑战值和相关策略
        ServerCreationOptionsResp response = fidoServerClient.getAssertionOptions(request);
        if (!ServerStatus.OK.equals(response.getStatus())) {
            Log.e(TAG, "authn fail" + response.getErrorMessage());
            callBack.onError("auth fail");
            return;
        }

        // 组装认证请求消息
        PublicKeyCredentialRequestOptions publicKeyCredentialCreationOptions =
                getCredentialRequestOptions(response);

        //启动FIDO2客户端认证流程
        authn2Fido2Client(publicKeyCredentialCreationOptions, callBack);
    }

    private void authn2Fido2Client(PublicKeyCredentialRequestOptions publicKeyCredentialCreationOptions, final ICallBack callBack) {
        NativeFido2AuthenticationOptions authenticationOptions = NativeFido2AuthenticationOptions.DEFAULT_OPTIONS;
        Fido2AuthenticationRequest authenticationRequest =
                new Fido2AuthenticationRequest(publicKeyCredentialCreationOptions, null);

        // 调用Fido2Client.getAuthenticationIntent获取Fido2Intent，并启动FIDO2客户端认证流程。
        fido2Client.getAuthenticationIntent(authenticationRequest, authenticationOptions,
                new Fido2IntentCallback() {
                    @Override
                    public void onSuccess(Fido2Intent fido2Intent) {
                        // 通过Fido2Client.AUTHENTICATION_REQUEST启动FIDO2客户端认证流程。
                        fido2Intent.launchFido2Activity(Fido2Handler.this.activity,
                                Fido2Client.AUTHENTICATION_REQUEST);
                    }

                    @Override
                    public void onFailure(int errorCode, CharSequence errString) {
                        Log.e(TAG, "onFailure: authn fail" + errorCode + "=" + errString);
                        callBack.onError("authn fail");
                    }
                });
    }

    public boolean onDeregistration(String username) {
        if (username == null) {
            return false;
        }
        ServerRegDeleteRequest request = new ServerRegDeleteRequest();
        request.setUsername(username);

        ServerResponse response = fidoServerClient.delete(request);
        if (!ServerStatus.OK.equals(response.getStatus())) {
            Log.e(TAG, "delete_register_info_fail" + response.getErrorMessage());
            return false;
        }
        return true;
    }

    private ServerPublicKeyCredentialCreationOptionsRequest
    getAuthnServerPublicKeyCredentialCreationOptionsRequest(String userName) {
        ServerPublicKeyCredentialCreationOptionsRequest request =
                new ServerPublicKeyCredentialCreationOptionsRequest();
        if (userName == null) {
            return null;
        }
        request.setUsername(userName);
        request.setDisplayName(userName);

        return request;
    }

    public boolean onRegisterToServer(Intent data) {
        //解析注册结果
        Fido2RegistrationResponse fido2RegistrationResponse = fido2Client.getFido2RegistrationResponse(data);

        //访问FIDO服务区，校验注册结果。
        return reg2Server(fido2RegistrationResponse);
    }

    private boolean reg2Server(Fido2RegistrationResponse fido2RegistrationResponse) {
        if (!fido2RegistrationResponse.isSuccess()) {
            logError("register failed!", fido2RegistrationResponse);
            return false;
        }

        ServerAttestationResultRequest request = convert2ServerAttestationResultRequest(
                fido2RegistrationResponse.getAuthenticatorAttestationResponse());

        ServerResponse response = fidoServerClient.getAttestationResult(request);
        if (!ServerStatus.OK.equals(response.getStatus())) {
            Log.e(TAG, "reg2Server: reg fail" + response.getErrorMessage());
            return false;
        }
        return true;
    }

    public boolean onAuthToServer(Intent data) {
        //解析认证结果
        Fido2AuthenticationResponse fido2AuthenticationResponse = fido2Client.getFido2AuthenticationResponse(data);

        //访问FIDO服务器，校验认证结果。
        return auth2Server(fido2AuthenticationResponse);
    }

    public boolean auth2Server(Fido2AuthenticationResponse fido2AuthenticationResponse) {
        if (!fido2AuthenticationResponse.isSuccess()) {
            logError("auth fail", fido2AuthenticationResponse);
            return false;
        }

        ServerAssertionResultRequest request =
                convert2ServerAssertionResultRequest(fido2AuthenticationResponse.getAuthenticatorAssertionResponse());

        ServerResponse response = fidoServerClient.getAssertionResult(request);
        if (!ServerStatus.OK.equals(response.getStatus())) {
            Log.e(TAG, "Authn failed" + response.getErrorMessage());
            return false;
        }
        return true;
    }

    private void logError(String message, Fido2Response fido2Response) {
        final StringBuilder errMsgBuilder = new StringBuilder();
        errMsgBuilder.append(message)
                .append("Fido2Status: ")
                .append(fido2Response.getFido2Status())
                .append("=")
                .append(fido2Response.getFido2StatusMessage())
                .append(String.format(Locale.getDefault(), "(CtapStatus: 0x%x=%s)", fido2Response.getCtapStatus(),
                        fido2Response.getCtapStatusMessage()));
        Log.i(TAG, "logError: " + errMsgBuilder);
    }

    private ServerPublicKeyCredentialCreationOptionsRequest
    getRegServerPublicKeyCredentialCreationOptionsRequest(String username) {
        ServerPublicKeyCredentialCreationOptionsRequest request =
                new ServerPublicKeyCredentialCreationOptionsRequest();

        if (username == null) {
            return null;
        }
        request.setUsername(username);
        request.setDisplayName(username);
        Boolean residentKey = null;
        ServerAuthenticatorSelectionCriteria selection =
                getAuthenticatorSelectionCriteria(residentKey);
        request.setAuthenticatorSelection(selection);
        request.setAttestation(null);
        return request;
    }

    private ServerAuthenticatorSelectionCriteria getAuthenticatorSelectionCriteria(Boolean residentKey) {
        ServerAuthenticatorSelectionCriteria selectionCriteria = new ServerAuthenticatorSelectionCriteria();

        if (TextUtils.isEmpty("")) {
            selectionCriteria.setUserVerification(null);
        } else {
            selectionCriteria.setUserVerification("");
        }

        selectionCriteria.setAuthenticatorAttachment(Fido2Handler.AUTH_ATTACH_MODE);

        selectionCriteria.setRequireResidentKey(residentKey);
        return selectionCriteria;
    }

    public static PublicKeyCredentialCreationOptions
    getCreationOptions(ServerCreationOptionsResp resp) {
        PublicKeyCredentialCreationOptions.Builder builder = new PublicKeyCredentialCreationOptions.Builder();
        // 设置PublicKeyCredentialRpEntity
        setPublicKeyCredentialRp(resp, builder);
        // 设置PublicKeyCredentialUserEntity
        setPublicKeyCredentialUser(resp, builder);
        // 设置挑战值
        setChallenge(resp, builder);
        // 设置支持的算法
        setPublicKeyCredParams(resp, builder);
        // 设置排除列表
        setExcludeList(resp, builder);
        // 设置认证器选择标准
        setAuthenticatorSelection(resp, builder);
        // 设置凭据偏好
        setAttestation(resp, builder);
        setExtensions(resp, builder);
        builder.setTimeoutSeconds(resp.getTimeout());
        return builder.build();
    }

    /**
     * 设置挑战值
     */
    private static void setChallenge(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        builder.setChallenge(ByteUtils.base642Byte(response.getChallenge()));
    }

    /**
     * 设置Extensions
     */
    private static void setExtensions(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        if (response.getExtensions() != null) {
            builder.setExtensions(response.getExtensions());
        }
    }

    /**
     * 设置PublicKeyCredentialRpEntity
     */
    private static void setPublicKeyCredentialRp(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        String name = response.getRp().getName();
        PublicKeyCredentialRpEntity entity = new PublicKeyCredentialRpEntity(name, name, null);
        builder.setRp(entity);
    }

    /**
     * 设置PublicKeyCredentialUserEntity
     */
    private static void setPublicKeyCredentialUser(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        String id = response.getUser().getId();
        try {
            builder.setUser(new PublicKeyCredentialUserEntity(id, id.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException");
        }
    }

    /**
     * 设置凭据偏好
     */
    private static void setAttestation(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        if (response.getAttestation() != null) {
            try {
                AttestationConveyancePreference preference =
                        AttestationConveyancePreference.fromValue(response.getAttestation());
                builder.setAttestation(preference);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException");
            }
        }
    }

    /**
     * 设置认证器选择标准
     */
    private static void setAuthenticatorSelection(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        if (response.getAuthenticatorSelection() != null) {
            ServerAuthenticatorSelectionCriteria selectionCriteria = response.getAuthenticatorSelection();

            Attachment attachment = null;
            if (selectionCriteria.getAuthenticatorAttachment() != null) {
                try {
                    attachment = Attachment.fromValue(selectionCriteria.getAuthenticatorAttachment());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException");
                }
            }

            Boolean residentKey = selectionCriteria.isRequireResidentKey();

            UserVerificationRequirement requirement = null;
            if (selectionCriteria.getUserVerification() != null) {
                try {
                    requirement = UserVerificationRequirement.fromValue(selectionCriteria.getUserVerification());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException");
                }
            }

            AuthenticatorSelectionCriteria fido2Selection =
                    new AuthenticatorSelectionCriteria(attachment, residentKey, requirement);
            builder.setAuthenticatorSelection(fido2Selection);
        }
    }

    /**
     * 设置排除列表
     */
    private static void setExcludeList(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        if (response.getExcludeCredentials() != null) {
            List<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();
            ServerPublicKeyCredentialDescriptor[] serverDescriptors = response.getExcludeCredentials();
            for (ServerPublicKeyCredentialDescriptor desc : serverDescriptors) {
                ArrayList<AuthenticatorTransport> transports = new ArrayList<>();
                if (desc.getTransports() != null) {
                    try {
                        transports.add(AuthenticatorTransport.fromValue(desc.getTransports()));
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "IllegalArgumentException");
                    }
                }
                PublicKeyCredentialDescriptor descriptor = new PublicKeyCredentialDescriptor(
                        PublicKeyCredentialType.PUBLIC_KEY, ByteUtils.base642Byte(desc.getId()), transports);
                descriptors.add(descriptor);
            }
            builder.setExcludeList(descriptors);
        }
    }

    /**
     * 设置支持算法
     */
    private static void setPublicKeyCredParams(ServerCreationOptionsResp response, PublicKeyCredentialCreationOptions.Builder builder) {
        // 设置支持算法
        if (response.getPubKeyCredParams() != null) {
            List<PublicKeyCredentialParameters> parameters = new ArrayList<>();
            ServerPublicKeyCredentialParameters[] serverPublicKeyCredentialParameters = response.getPubKeyCredParams();
            for (ServerPublicKeyCredentialParameters param : serverPublicKeyCredentialParameters) {
                try {
                    PublicKeyCredentialParameters parameter = new PublicKeyCredentialParameters(
                            PublicKeyCredentialType.PUBLIC_KEY, Algorithm.fromCode(param.getAlg()));
                    parameters.add(parameter);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "IllegalArgumentException");
                }
            }
            builder.setPubKeyCredParams(parameters);
        }
    }

    public static PublicKeyCredentialRequestOptions
    getCredentialRequestOptions(ServerCreationOptionsResp response) {
        PublicKeyCredentialRequestOptions.Builder builder = new PublicKeyCredentialRequestOptions.Builder();

        // 设置RP ID
        builder.setRpId(response.getRpId());

        // 设置挑战值
        builder.setChallenge(ByteUtils.base642Byte(response.getChallenge()));

        // 设置允许列表
        ServerPublicKeyCredentialDescriptor[] descriptors = response.getAllowCredentials();
        if (descriptors != null) {
            List<PublicKeyCredentialDescriptor> descriptorList = new ArrayList<>();
            for (ServerPublicKeyCredentialDescriptor descriptor : descriptors) {
                ArrayList<AuthenticatorTransport> transports = new ArrayList<>();
                if (descriptor.getTransports() != null) {
                    try {
                        transports.add(AuthenticatorTransport.fromValue(descriptor.getTransports()));
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "IllegalArgumentException");
                    }
                }
                PublicKeyCredentialDescriptor desc = new PublicKeyCredentialDescriptor(
                        PublicKeyCredentialType.PUBLIC_KEY, ByteUtils.base642Byte(descriptor.getId()), transports);
                descriptorList.add(desc);
            }
            builder.setAllowList(descriptorList);
        }

        // 设置扩展参数
        if (response.getExtensions() != null) {
            builder.setExtensions(response.getExtensions());
        }

        //设置超时时间
        builder.setTimeoutSeconds(response.getTimeout());
        return builder.build();
    }

    public static ServerAttestationResultRequest
    convert2ServerAttestationResultRequest(AuthenticatorAttestationResponse authenticatorAttestationResponse) {
        ServerAttestationResultRequest request = new ServerAttestationResultRequest();
        ServerAttestationResultResponseRequest attestationResponse = new ServerAttestationResultResponseRequest();
        attestationResponse
                .setAttestationObject(ByteUtils.byte2base64(authenticatorAttestationResponse.getAttestationObject()));
        attestationResponse
                .setClientDataJSON(ByteUtils.byte2base64(authenticatorAttestationResponse.getClientDataJson()));
        request.setResponse(attestationResponse);
        request.setId(ByteUtils.byte2base64(authenticatorAttestationResponse.getCredentialId()));
        request.setType("public-key");
        return request;
    }

    public static ServerAssertionResultRequest
    convert2ServerAssertionResultRequest(AuthenticatorAssertionResponse authenticatorAssertation) {
        ServerAssertionResultResponseRequest assertionResultResponse = new ServerAssertionResultResponseRequest();
        assertionResultResponse.setSignature(ByteUtils.byte2base64(authenticatorAssertation.getSignature()));
        assertionResultResponse.setClientDataJSON(ByteUtils.byte2base64(authenticatorAssertation.getClientDataJson()));
        assertionResultResponse
                .setAuthenticatorData(ByteUtils.byte2base64(authenticatorAssertation.getAuthenticatorData()));

        ServerAssertionResultRequest request = new ServerAssertionResultRequest();
        request.setResponse(assertionResultResponse);

        request.setId(ByteUtils.byte2base64(authenticatorAssertation.getCredentialId()));

        request.setType("public-key");
        return request;
    }
}