package com.score.shopz.utils;

import com.score.shopz.pojos.Bill;
import com.score.shopz.pojos.Matm;
import com.score.shopz.pojos.TopUp;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Utility class to handle JSON
 *
 * @author eranga bandara(erangaeb@gmail.com)
 */
public class JSONUtils {

    /**
     * Create JSON string from TopUp
     *
     * @param bill Bill object
     * @return JSON string
     * @throws JSONException
     */
    public static String getBillJson(Bill bill) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("acc", bill.getAccount());
        jsonObject.put("amnt", bill.getAmount());

        return jsonObject.toString();
    }

    /**
     * Create TopUp object by parsing JSON string
     *
     * @param jsonString json string
     * @return TopUp Object
     */
    public static TopUp getTopUp(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        String acc = jsonObject.getString("acc");
        String amnt = jsonObject.getString("amnt");

        return new TopUp(acc, amnt, getCurrentTime());
    }

    /**
     * Create Matm object by parsing JSON string
     *
     * @param jsonString json string
     * @return Matm Object
     */
    public static Matm getMatm(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        String acc = jsonObject.getString("tid");
        String key = jsonObject.getString("key");

        return new Matm(acc, key);
    }

    /**
     * Get current date and time as transaction time
     * format - yyyy/MM/dd HH:mm:ss
     *
     * @return
     */
    private static String getCurrentTime() {
        //date format
        String DATE_FORMAT_NOW = "yyyy/MM/dd HH:mm:ss";

        // generate time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_NOW);

        return simpleDateFormat.format(calendar.getTime());
    }
}
