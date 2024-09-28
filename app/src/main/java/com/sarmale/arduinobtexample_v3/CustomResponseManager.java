package com.sarmale.arduinobtexample_v3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomResponseManager {

    private static final String PREF_NAME = "CustomResponses";
    private SharedPreferences sharedPreferences;

    private static final String TAG = "FrugalLogs";

    public CustomResponseManager(Context context) {
        // Initialize SharedPreferences
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Function to save custom response in JSON format
    public void saveCustomResponse(String arduinoString, String customResponse) {
        // Retrieve existing responses
        String jsonString = sharedPreferences.getString("responses", "{}"); // Default to an empty JSON object

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            // Add the new response
            jsonObject.put(arduinoString, customResponse);

            // Save the updated JSON object back to SharedPreferences
            sharedPreferences.edit()
                    .putString("responses", jsonObject.toString())
                    .apply();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to save custom response: " + e.getMessage());
        }
    }


    // Function to get custom response
    public String getCustomResponse(String arduinoString) {
        // Retrieve responses
        String jsonString = sharedPreferences.getString("responses", "{}");
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.optString(arduinoString, null); // Return response or null
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
