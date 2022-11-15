package com.paymentez.example.sdk;

import com.paymentez.example.model.Customer;
import okhttp3.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mmucito on 18/09/17.
 */
public class Paymentez {

    public static String PAYMENTEZ_DEV_URL = "https://ccapi-stg.paymentez.com";
    public static String PAYMENTEZ_PROD_URL = "https://ccapi.paymentez.com";
    public static final String RESPONSE_HTTP_CODE = "RESPONSE_HTTP_CODE";
    public static final String RESPONSE_JSON = "RESPONSE_JSON";

    public static OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private static String getUniqToken(String auth_timestamp, String app_secret_key) {
        String uniq_token_string = app_secret_key + auth_timestamp;
        return getHash(uniq_token_string);
    }

    public static String getAuthToken(String app_code, String app_secret_key) {
        Long tsLong = System.currentTimeMillis()/1000;
        String auth_timestamp = tsLong.toString();
        String string_auth_token = app_code + ";" + auth_timestamp + ";" + getUniqToken(auth_timestamp, app_secret_key);
        String auth_token = new String(Base64.getEncoder().encode(string_auth_token.getBytes()));
        return auth_token;
    }

    public static String getHash(String message) {
        String sha256hex = new String(Hex.encodeHex(DigestUtils.sha256(message)));
        return sha256hex;
    }

    public static String paymentezDebitJson(Customer customer, String session_id, String token, double amount, String dev_reference, String description) {
        String session_id_row = "";
        if(session_id != null && session_id != ""){
            session_id_row = "\"session_id\": \"" + session_id + "\",";
        }
        return "{" +
                    session_id_row +
                    "\"user\": {" +
                        "\"id\": \"" + customer.getId() + "\"," +
                        "\"email\": \"" + customer.getEmail() + "\"," +
                        "\"ip_address\": \"" + customer.getIpAddress() + "\"" +
                    "}," +
                    "\"order\": {" +
                        "\"code\": \"123\"," +
                        "\"amount\": " + amount + "," +
                        "\"description\": \"" + description + "\"," +
                        "\"dev_reference\": \"" + dev_reference + "\"," +
                        "\"vat\": 0.00" +
                    "}," +
                    "\"card\": {" +
                        "\"token\": \"" + token + "\"" +
                    "}" +
                "}";
    }


    public static String paymentezDeleteJson(String uid, String token) {
        return "{" +
                    "\"card\": {" +
                        "\"token\": \"" +token + "\"" +
                    "}," +
                    "\"user\": {" +
                        "\"id\": \"" + uid + "\"" +
                    "}" +
                "}";
    }

    public static String paymentezVerifyJson(String uid, String transaction_id, String type, String value) {
        return "{" +
                    "\"user\": {" +
                        "\"id\": \"" + uid + "\"" +
                    "}," +
                    "\"transaction\": {" +
                        "\"id\": \"" + transaction_id + "\"" +
                    "}," +
                    "\"type\": \"" + type + "\"," +
                    "\"value\": \"" + value + "\"" +
                    "" +
                "}";
    }

    public static Map<String, String> doPostRequest(String url, String json){
        String jsonResponse = "{}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .header("Auth-Token", Paymentez.getAuthToken(System.getenv("PAYMENTEZ_APP_SERVER_CODE"), System.getenv("PAYMENTEZ_APP_SERVER_KEY")))
                .url(url)
                .post(body)
                .build();

        Response response = null;
        Map<String, String> mapResult = new HashMap<>(2);

        try {
            response = client.newCall(request).execute();
            jsonResponse = response.body().string();
            mapResult.put(RESPONSE_HTTP_CODE, ""+response.code());
            mapResult.put(RESPONSE_JSON, jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapResult;
    }

    public static Map<String, String> doGetRequest(String url){
        String jsonResponse = "{}";
        Request request = new Request.Builder()
                .header("Auth-Token", Paymentez.getAuthToken(System.getenv("PAYMENTEZ_APP_SERVER_CODE"), System.getenv("PAYMENTEZ_APP_SERVER_KEY")))
                .url(url)
                .build();

        Response response = null;
        Map<String, String> mapResult = new HashMap<>(2);

        try {
            response = client.newCall(request).execute();
            jsonResponse = response.body().string();
            mapResult.put(RESPONSE_HTTP_CODE, ""+response.code());
            mapResult.put(RESPONSE_JSON, jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapResult;
    }
}
