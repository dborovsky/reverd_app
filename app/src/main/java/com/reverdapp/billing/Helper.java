package com.reverdapp.billing;

import android.util.Base64;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.checkout.Sku;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by wojci on 9/10/15.
 */
public final class Helper {
    private static final String TAG = LogConfig.genClassLogTag(Helper.class);

    /*
    public static final String toJson(Sku in)
    {
        JSONObject json = new JSONObject();
        try {
            json.put("product", in.product);
            json.put("id", in.id);
            json.put("price", in.price);
            // Price:
            json.put("amount", in.detailedPrice.amount);
            json.put("currency", in.detailedPrice.currency);
            json.put("title", in.title);
            json.put("description", in.description);

        } catch (JSONException e) {
            Log.e(TAG, "Json", e);
            return "";
        }

        return json.toString();
    }
    */

    private final static String KeyPart1 = "ebtHioh8";
    private final static String KeyPart2 = "1p7zU8ex";

    public static String encryptKey(String input)
    {
        byte[] inBytes=input.getBytes();
        String finalString=null;
        try {
            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] keyBytes=md.digest((KeyPart1+KeyPart2).getBytes());
            keyBytes = Arrays.copyOf(keyBytes, 16);
            SecretKey key= new SecretKeySpec(keyBytes,"AES");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
            cipher.init(Cipher.ENCRYPT_MODE,key,ivSpec);
            byte[] outBytes = new byte[cipher.getOutputSize(inBytes.length)];
            //cipher.update(encrypted, 0, encrypted.length, decrypted, 0);
            outBytes=cipher.doFinal(inBytes);
            finalString=new String(Base64.encode(outBytes, 0));
            Log.v(TAG,"Encrypted="+finalString);

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG,"No Such Algorithm",e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG,"No Such Padding",e);
        } catch (InvalidKeyException e) {
            Log.e(TAG,"Invalid Key",e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG,"Invalid Algorithm Parameter",e);
        } catch (IllegalBlockSizeException e) {
        } catch (BadPaddingException e) {}
        return finalString;
    }

    public static String decryptKey(String base64Text)
    {
        byte[] encrypted= Base64.decode(base64Text, 0);
        //encrypted=base64Text.getBytes();
        String decryptedString=null;
        try {
            Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] keyBytes=md.digest((KeyPart1+KeyPart2).getBytes());
            keyBytes = Arrays.copyOf(keyBytes, 16);
            SecretKey key= new SecretKeySpec(keyBytes,"AES");
            IvParameterSpec ivSpec = new IvParameterSpec(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
            cipher.init(Cipher.DECRYPT_MODE,key,ivSpec);
            byte[] decrypted = new byte[cipher.getOutputSize(encrypted.length)];
            //cipher.update(encrypted, 0, encrypted.length, decrypted, 0);
            decrypted=cipher.doFinal(encrypted);
            decryptedString=new String(decrypted);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "missing algorithm", e);
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, "no padding", e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "invalid key", e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "invalid algorithm", e);
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "invalid block size", e);
        } catch (BadPaddingException e) {
            Log.e(TAG, "bad padding", e);
        }
        return decryptedString;
    }
}
