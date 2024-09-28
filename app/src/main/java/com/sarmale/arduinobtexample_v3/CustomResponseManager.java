package com.sarmale.arduinobtexample_v3;

import android.content.Context;
import android.content.SharedPreferences;

public class CustomResponseManager {

    private static final String PREF_NAME = "CustomResponses";
    private SharedPreferences sharedPreferences;

    public CustomResponseManager(Context context) {
        // Initialize SharedPreferences
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Function to save custom response
    public void saveCustomResponse(String arduinoString, String customResponse) {
        // Store the custom response in SharedPreferences
        sharedPreferences.edit()
                .putString(arduinoString, customResponse)
                .apply();
    }

    // Function to get custom response
    public String getCustomResponse(String arduinoString) {
        // Retrieve the custom response from SharedPreferences
        return sharedPreferences.getString(arduinoString, null);
    }
}
