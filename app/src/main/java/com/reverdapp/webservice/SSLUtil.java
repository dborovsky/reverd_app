package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by wojci on 9/7/15.
 */
public class SSLUtil {
    private static final String TAG = LogConfig.genLogTag("SSLUtil");

    public static SSLContext getSSLContext(final Context c) {
        final String KEYSTORE_FN = "reverd.bks";

        try {
            final InputStream is = c.getAssets().open(KEYSTORE_FN);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            String password = "REVERD";
            keystore.load(is, password.toCharArray());

            final String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();

            // Create a TrustManager that trusts the CAs in our KeyStore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keystore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            Log.d(TAG, "Created SSL context");
            return context;
            } catch (IOException e) {
            Log.w(TAG, "getSSLContext", e);
        } catch (CertificateException e) {
            Log.w(TAG, "getSSLContext", e);
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "getSSLContext", e);
        } catch (KeyStoreException e) {
            Log.w(TAG, "getSSLContext", e);
        } catch (KeyManagementException e) {
            Log.w(TAG, "getSSLContext", e);
        }

        return null;
    };
}
