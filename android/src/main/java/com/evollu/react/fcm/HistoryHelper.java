package com.evollu.react.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Date;

public class HistoryHelper {

    private static final String TAG = "HistoryHelper";
    private static final String PREFERENCES_FILE = "HistoryList";
    private static final String HISTORY_KEY= "HistoryNotificationList";

    private Context mContext;
    private SharedPreferences sharedPreferences = null;

    public HistoryHelper(Context context) {
        mContext = context;
        sharedPreferences = (SharedPreferences) mContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public String getHistory(){
        String history = sharedPreferences.getString(HISTORY_KEY, "[]");
        Log.d(TAG,"getHistory :"+history);
        return history;
    }

    public void addToHistory(String jsonString){
        try{

            JsonArray historyList = parseJsonArray(getHistory());

            JsonObject historyObject=(JsonObject) new JsonParser().parse(jsonString);
            historyObject.addProperty("timestamp",new Date().getTime());

            historyList.add(historyObject);

            String historyListString = historyList.toString();

            storeHistory(historyListString);

        }catch(Exception e){
            Log.e(TAG,e.getLocalizedMessage());
        }
    }

    public void clearHistory(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private JsonArray parseJsonArray(String value){
        JsonParser  parser = new JsonParser();
        JsonElement elem   = parser.parse(value);

        return elem.getAsJsonArray();
    }

    private void storeHistory(String historyList) {
        Log.d(TAG,"storeHistory :"+historyList);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORY_KEY, historyList);
        editor.apply();
    }
}
