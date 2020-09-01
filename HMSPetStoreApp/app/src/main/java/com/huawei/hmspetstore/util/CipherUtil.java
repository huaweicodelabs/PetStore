package com.huawei.hmspetstore.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.huawei.hmspetstore.R;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CipherUtil {

    private static final String TAG = "CipherUtil";

    public static boolean doCheck(Context context, String content, String sign) {

        if (context == null || TextUtils.isEmpty(content) || TextUtils.isEmpty(sign)) {
            Log.e(TAG, "param is null");
            return false;
        }

        String publicKey = context.getResources().getString(R.string.cipher_key);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(publicKey, Base64.DEFAULT);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = java.security.Signature.getInstance("SHA256WithRSA");

            signature.initVerify(pubKey);
            signature.update(content.getBytes("utf-8"));

            return signature.verify(Base64.decode(sign, Base64.DEFAULT));

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "doCheck NoSuchAlgorithmException");
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, "doCheck InvalidKeySpecException");
        } catch (InvalidKeyException e) {
            Log.e(TAG, "doCheck InvalidKeyException");
        } catch (SignatureException e) {
            Log.e(TAG, "doCheck SignatureException");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "doCheck UnsupportedEncodingException");
        }
        return false;
    }
}