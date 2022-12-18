package chalie.darajaapi.demo;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.Map;

import static chalie.darajaapi.demo.controllers.MpesaController.StkPush;
import static chalie.darajaapi.demo.controllers.MpesaController.StkQuery;
import static spark.Spark.post;

public class Main {
    public static void main(String[] args) {

        post("/stkpush", "application/json", (req, res) -> {
            Gson gson = new Gson();
            Map data = gson.fromJson(req.body(), Map.class);
            return StkPush(data);
        });

        post("/stkquery", "application/json", (req, res) -> {
            JSONObject json = new JSONObject(req.body());
            String data = json.getString("checkoutRequestID");
            return StkQuery(data);
        });
    }
}