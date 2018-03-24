/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.orlov.android.gms.samples.vision.barcodereader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.R;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView barcodeValue;
    Map<String, String> countriesMap = new HashMap<>();

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String PATH_FILE = "countries.csv";
    private static final String UNKNOWN_CODE = "Unknown code...";
    private static final String CSV_DELIMITER = ";";
    private static final String ERROR_READ_FILE = " >>> error on read file...";
    private static final String BARCODE_READ = "Barcode read: ";
    private static final String NO_BARCODE_CAPTURED = "No barcode captured, intent data is null";
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        barcodeValue = (TextView)findViewById(R.id.barcode_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);
        autoFocus.setChecked(true);
        useFlash.setChecked(false);
        autoFocus.setVisibility(View.INVISIBLE);
        useFlash.setVisibility(View.INVISIBLE);
        barcodeValue.setGravity(Gravity.CENTER);
        barcodeValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);

        findViewById(R.id.read_barcode).setOnClickListener(this);

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
        intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
        countriesMap = fileToCollection();
        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    statusMessage.setText(R.string.barcode_success);
                    //barcodeValue.setText(barcode.displayValue);
                    barcodeValue.setText(searchCountry(barcode.displayValue));

                    Log.d(TAG, BARCODE_READ + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, NO_BARCODE_CAPTURED);
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected Map<String, String> fileToCollection(){
        BufferedReader br = null;
        String line;

        try {
            br = new BufferedReader(loadAssetTextAsString(PATH_FILE));
            while ((line = br.readLine()) != null) {
                String[] country = line.split(CSV_DELIMITER);
                countriesMap.put(country[0], country[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return countriesMap;
    }

    protected String searchCountry(String barcode){

        for (Map.Entry<String, String> entry : countriesMap.entrySet()) {
            if (entry.getKey().equals(barcode.substring(0,2))){
                return countriesMap.get(barcode.substring(0,2));
            }
            if (entry.getKey().equals(barcode.substring(0,3))){
                return countriesMap.get(barcode.substring(0,3));
            }
        }
        return UNKNOWN_CODE;
    }

    public BufferedReader loadAssetTextAsString(String name) {
        try {
            InputStreamReader r = new InputStreamReader(getAssets().open(name));
            return new BufferedReader(r);
        } catch (IOException e) {
            Log.w(MainActivity.class.getName(),ERROR_READ_FILE);
        }
        return null;
    }

}
