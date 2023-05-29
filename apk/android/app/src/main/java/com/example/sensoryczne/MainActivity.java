package com.example.sensoryczne;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "samples.flutter.dev/camera";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            Log.d("test", "test");

                            try {
                                Log.d("test", "Received method call: " + call.method);

                                if (call.method.equals("sendFrame")) {
                                    String imagePath = (String) call.arguments;  // argumentList is List of Object

//                                    if (argumentList != null) {
//                                        byte[] byteArray = new byte[argumentList.size()];
//                                        for (int i = 0; i < argumentList.size(); i++) {
//                                            Integer byteValue = (Integer) argumentList.get(i);
//                                            byteArray[i] = byteValue.byteValue();
//                                        }

                                    Log.d("test", imagePath);

                                } else {
                                    result.notImplemented();
                                }
                            } catch (Exception e) {
                                Log.d("test", "Exception during method call", e);
                            }
                        }
                );
    }
}

