package com.sarmale.arduinobtexample_v3;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class CustomizeActivity extends AppCompatActivity {

    private EditText arduinoStringInput;
    private EditText customResponseInput;
    private Button saveButton;

    private CustomResponseManager customResponseManager; // Declare the manager

    private static final String TAG = "FrugalLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);

        arduinoStringInput = findViewById(R.id.arduinoStringInput);
        customResponseInput = findViewById(R.id.customResponseInput);
        saveButton = findViewById(R.id.saveButton);

        // Initialize CustomResponseManager
        customResponseManager = new CustomResponseManager(this);

        // Handle saving the custom response for the Arduino string
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String arduinoString = arduinoStringInput.getText().toString().trim();
                String customResponse = customResponseInput.getText().toString().trim();

                if (arduinoString.isEmpty() || customResponse.isEmpty()) {
                    Log.e(TAG, "Arduino string or custom response cannot be empty.");
                    return; // prevent saving empty inputs
                }

                customResponseManager.saveCustomResponse(arduinoString, customResponse);
                Log.d(TAG, "Saved custom response for Arduino string: " + arduinoString);

                finish(); // Go back to MainActivity
            }
        });
    }
}
