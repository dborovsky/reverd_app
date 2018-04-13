package com.reverdapp.webservice;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.reverdapp.database.Database;
import com.reverdapp.ssl.EasySSLSocketFactory;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.ReverdApp;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;


public abstract class WebServiceUtil {
    private static final String TAG = LogConfig.genLogTag("WebServiceUtil");

    public static String postMethod(final Context context,
                                    final String requestURL,
                                    final HashMap<String, String> parameters) throws Exception {
        //return postMethodImplOld(requestURL, parameters);
        return postMethodImplNew(context, requestURL, parameters);
    }

    public static String postMethod(final Context context,
                                    final String requestURL,
                                    final String postData) throws Exception {
        //return postMethodImplOld(requestURL, parameters);
        return postMethodImplNew(context, requestURL, postData);
    }

    public static String postMethodImplNew(final Context context,
                                           final String requestURL,
                                           final HashMap<String, String> parameters) throws Exception {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                //SSLUtil.trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                SSLContext sslContext = SSLUtil.getSSLContext(context);
                https.setSSLSocketFactory(sslContext.getSocketFactory());
                https.setHostnameVerifier(new StrictHostnameVerifier());
                conn = https;
                Log.d(TAG, "SSL enabled");
            } else {
                conn = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "SSL disabled");
            }

            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // Diable gzip which is enabled per default:
            conn.setRequestProperty("Content-Encoding", "identity");
            conn.setRequestProperty("Accept-Encoding", "identity");

            conn.setRequestProperty("Authorization", "abcdefg");

            //final String base64EncodedCredentials = Base64.encodeToString(("abcdefg:abcdefg").getBytes("UTF-8"), Base64.URL_SAFE|Base64.NO_WRAP);
            //conn.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);

            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "UTF-8");
            //conn.setRequestProperty( "charset", "UTF-8");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            //conn.setUseCaches(false);

            Charset charset = Charset.forName("UTF-8");

            final byte[] postData = getEncodeParametersAsString(parameters).getBytes(charset);
            final int postDataLen = postData.length;

            Log.d(TAG, "postDataLen = " + postDataLen);

            //conn.setFixedLengthStreamingMode(postDataLen);

            //conn.setRequestProperty("Content-Length", Integer.toString(postDataLen));

            /*
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();
            */

            DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
            wr.write(postData);
            wr.flush();
            wr.close();

            /*
            try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                wr.write( postData );
            }
            */
            int responseCode=conn.getResponseCode();

            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:
                case HttpsURLConnection.HTTP_CREATED: {
                    Log.e(TAG, "Response OK");

                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                    break;
                }
                default:
                {
                    Log.e(TAG, "responseCode = " + responseCode);
                    response="";

                    //throw new HttpException(responseCode+"");
                    return "";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "performPostCall failed", e);
            throw new Exception( requestURL + "#" + getEncodeParametersAsString(parameters) );
        }

        return response;
    }

    public static String postMethodImplNew(final Context context,
                                           final String requestURL,
                                           final String postData) throws Exception {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = null;

            if (url.getProtocol().toLowerCase().equals("https")) {
                //SSLUtil.trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                SSLContext sslContext = SSLUtil.getSSLContext(context);
                https.setSSLSocketFactory(sslContext.getSocketFactory());
                https.setHostnameVerifier(new StrictHostnameVerifier());
                conn = https;
                Log.d(TAG, "SSL enabled");
            } else {
                conn = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "SSL disabled");
            }

            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // Diable gzip which is enabled per default:
            conn.setRequestProperty("Content-Encoding", "identity");
            conn.setRequestProperty("Accept-Encoding", "identity");

            conn.setRequestProperty("Authorization", "abcdefg");

            //final String base64EncodedCredentials = Base64.encodeToString(("abcdefg:abcdefg").getBytes("UTF-8"), Base64.URL_SAFE|Base64.NO_WRAP);
            //conn.setRequestProperty("Authorization", "Basic " + base64EncodedCredentials);

            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "UTF-8");
            //conn.setRequestProperty( "charset", "UTF-8");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            //conn.setUseCaches(false);

            Charset charset = Charset.forName("UTF-8");

            final int postDataLen = postData.getBytes(charset).length;
            Log.d(TAG, "postDataLen = " + postDataLen);

            //conn.setFixedLengthStreamingMode(postDataLen);

            //conn.setRequestProperty("Content-Length", Integer.toString(postDataLen));

            /*
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(postData);
            writer.flush();
            writer.close();
            os.close();
            */

            DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
            wr.write(postData.getBytes(charset));
            wr.flush();
            wr.close();

            /*
            try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
                wr.write( postData );
            }
            */
            int responseCode=conn.getResponseCode();

            switch (responseCode) {
                case HttpsURLConnection.HTTP_OK:
                case HttpsURLConnection.HTTP_CREATED: {
                    Log.e(TAG, "Response OK");

                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                    break;
                }
                default:
                {
                    Log.e(TAG, "responseCode = " + responseCode);
                    response="";

                    return "";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "performPostCall failed again", e);
            throw new Exception( requestURL + ":" + postData );
        }

        return response;
    }

    private static String getEncodeParametersAsString(HashMap<String, String> params) throws UnsupportedEncodingException {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        Log.d(TAG, "Encoded parameters: " + result.toString());
        return result.toString();
    }

    public static String postMethodImplOld(String url, final HashMap<String, String> parameters) {

        List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            nameValuePairs.add(new BasicNameValuePair(key, value));
        }

         HttpClient httpclient = createHttpClient();
            HttpPost httppost = new HttpPost(url);

            try {
                // Add your data
                
                if (nameValuePairs!=null)
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                httppost.setHeader("Authorization", "abcdefg");
                // Execute HTTP Post Request
                HttpResponse httpResponse = httpclient.execute(httppost);
                String response = convertInputStreamToString(httpResponse);
                Log.d("response", response);
                return response;

            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

    }

    private static DefaultHttpClient createHttpClient() {
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

        final HttpParams params = new BasicHttpParams();
        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
        params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        final ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        final DefaultHttpClient client = new DefaultHttpClient(cm, params);

        client.addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                request.addHeader("User-Agent", "Mozilla/5.0");
            }
        });
        return client;
    }

    private static String convertInputStreamToString(HttpResponse response) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            final StringBuffer stringBuffer = new StringBuffer("");
            String line = "";
            final String LineSeparator = System.getProperty("line.separator");

            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + LineSeparator);
            }
            return stringBuffer.toString();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

}
