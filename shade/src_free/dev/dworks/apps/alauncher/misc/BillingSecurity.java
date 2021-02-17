/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.dworks.apps.alauncher.misc;

import android.text.TextUtils;
import android.util.Base64;

import com.android.billingclient.api.Purchase;
import com.android.launcher3.BuildConfig;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class BillingSecurity {

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    //This you will get from Services & API in your application console
    public static String BASE_64_ENCODED_PUBLIC_KEY = BuildConfig.PLAYSTORE_LICENSE_KEY;

    private static final Date DATE_MERCHANT_LIMIT_1; //5th December 2012
    private static final Date DATE_MERCHANT_LIMIT_2; //21st July 2015

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.DECEMBER, 5);
        DATE_MERCHANT_LIMIT_1 = calendar.getTime();
        calendar.set(2015, Calendar.JULY, 21);
        DATE_MERCHANT_LIMIT_2 = calendar.getTime();
    }

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * with a private key. The data also contains the {@link Purchase.PurchaseState}
     * and product ID of the purchase.
     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     */
    public static boolean verifyPurchase(String base64PublicKey, String signedData, String signature) {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey) ||
                TextUtils.isEmpty(signature)) {
            return false;
        }

        PublicKey key = BillingSecurity.generatePublicKey(base64PublicKey);
        return BillingSecurity.verify(key, signedData, signature);
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    private static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param signature server signature
     * @return true if the data and signature match
     */
    private static boolean verify(PublicKey publicKey, String signedData, String signature) {
        byte[] signatureBytes;
        try {
            signatureBytes = Base64.decode(signature, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(signatureBytes)) {
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks merchant's id validity. If purchase was generated by Freedom alike program it doesn't know
     * real merchant id, unless publisher GoogleId was hacked
     * If merchantId was not supplied function checks nothing
     *
     * @param purchase Purchase
     * @return boolean
     */
    public static boolean verifyMerchant(Purchase purchase) {
        long purchaseTimeMillis = purchase.getPurchaseTime();
        Date purchaseDate = purchaseTimeMillis != 0 ? new Date(purchaseTimeMillis) : null;
        if (null != purchaseDate && purchaseDate.before(DATE_MERCHANT_LIMIT_1)) {
            return true;
        }
        if (null != purchaseDate && purchaseDate.after(DATE_MERCHANT_LIMIT_2)) {
            return true;
        }
        String orderId = purchase.getOrderId();
        if (TextUtils.isEmpty(orderId) || orderId.trim().length() == 0) {
            return false;
        }
        int index = orderId.indexOf('.');
        if (index <= 0) {
            return false; //protect on missing merchant id
        }
        //extract merchant id
        String merchantId = orderId.substring(0, index);
        return merchantId.compareTo(BuildConfig.MERCHANT_ID) == 0;
    }
}