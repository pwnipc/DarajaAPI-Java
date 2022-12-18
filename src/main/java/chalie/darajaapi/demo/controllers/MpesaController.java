package chalie.darajaapi.demo.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MpesaController {

    private static Response GenerateAccessTokenRequest() throws IOException {
        Dotenv dotenv = Dotenv.configure().load();
        String URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
        String consumerKey = dotenv.get("MPESA_CONSUMER_KEY");
        String consumerSecret = dotenv.get("MPESA_CONSUMER_SECRET");
        String userPassword = Base64.getEncoder().encodeToString(consumerKey.concat(":").concat(consumerSecret).getBytes());

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(URL)
                .method("GET", null)
                .addHeader("Authorization", "Basic ".concat(userPassword))
                .build();
        return client.newCall(request).execute();
    }

    public static String GetAccessToken() {
        String accessToken = "";
        try {
            Response res = GenerateAccessTokenRequest();
            if(res.code() != 200){
                return  "Unable to generate access token";
            }else {
                ResponseBody body = res.body();
                if (body != null) {
                    String responseString = body.string();
                    try {
                        JSONObject json = new JSONObject(responseString);
                        accessToken = json.getString("access_token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return accessToken ;
    }

    public static JsonObject StkPush(Map data) {
        String phoneNumber = data.get("phoneNumber").toString();
        String amount = data.get("amount").toString();
        String  url = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
        long timestamp_ = System.currentTimeMillis();
        Date date = new Date(timestamp_);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = formatter.format(date);
        Dotenv dotenv = Dotenv.configure().load();
        String business_short_code = dotenv.get("MPESA_SHORT_CODE");
        String password = Base64.getEncoder().encodeToString(business_short_code.concat(dotenv.get("MPESA_PASSKEY")).concat(timestamp).getBytes());
        String callback_url = dotenv.get("CALLBACK_URL");

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        String json = String.format("{" +
                "\"BusinessShortCode\":%s," +
                "\"Password\":\"%s\"," +
                "\"Timestamp\":\"%s\"," +
                "\"TransactionType\":\"CustomerPayBillOnline\"," +
                "\"Amount\":%s," +
                "\"PartyA\":%s," +
                "\"PartyB\":%s," +
                "\"PhoneNumber\":%s," +
                "\"CallBackURL\":\"%s\"," +
                "\"AccountReference\":\"M-Tumbler\"," +
                "\"TransactionDesc\":\"Payment for M-Tumbler\"" +
                "}", business_short_code, password, timestamp, amount, phoneNumber, business_short_code, phoneNumber, callback_url);
        RequestBody body = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(GetAccessToken()))
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(response.body().charStream(), JsonElement.class);

        return jsonElement.getAsJsonObject();
    }

    public static JsonObject StkQuery(String checkoutRequestID){
        String url = "https://sandbox.safaricom.co.ke/mpesa/stkpushquery/v1/query";
        long timestamp_ = System.currentTimeMillis();
        Date date = new Date(timestamp_);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = formatter.format(date);
        Dotenv dotenv = Dotenv.configure().load();
        String business_short_code = dotenv.get("MPESA_SHORT_CODE");
        String password = Base64.getEncoder().encodeToString(business_short_code.concat(dotenv.get("MPESA_PASSKEY")).concat(timestamp).getBytes());
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        String payload = String.format("{" +
                        "\"BusinessShortCode\":%s," +
                        "\"Password\":\"%s\"," +
                        "\"Timestamp\":\"%s\"," +
                        "\"CheckoutRequestID\":\"%s\"}", business_short_code, password, timestamp,checkoutRequestID);
                MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, payload);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(GetAccessToken()))
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson gson = new Gson();
        assert response.body() != null;
        JsonElement jsonElement = gson.fromJson(response.body().charStream(), JsonElement.class);

        return jsonElement.getAsJsonObject();
    }
}
