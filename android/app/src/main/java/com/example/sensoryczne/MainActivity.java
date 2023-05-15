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

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "samples.flutter.dev/camera";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            Log.d("test", "test");

                            if (call.method.equals("sendFrame")) {
              byte[] frame = call.argument("frame");
              //String s = new String(frame, StandardCharsets.UTF_8);
              //Log.d("flutter", s);
                // Tutaj możemy przetwarzać ramkę, np. używając OpenCV
            } else {
              result.notImplemented();
            }
          }
          );
    }
}

