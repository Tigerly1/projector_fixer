package com.example.sensoryczne;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "samples.flutter.dev/camera";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @Override
    public void onStart(){
        super.onStart();
        OpenCVLoader.initDebug();
    }

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

                                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        // Permission is not granted
                                        ActivityCompat.requestPermissions(this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                                    }

                                    FileInputStream fis = null;
                                    try {
                                        fis = new FileInputStream(new File(imagePath));
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }

                                    // Decode the FileInputStream to a Bitmap
                                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                                    Log.d("test", "Bitmap width: " + bitmap.getWidth());
                                    Log.d("test", "Bitmap height: " + bitmap.getHeight());
                                    Log.d("test", imagePath);

                                    Corrector corrector = new Corrector();
                                    double[] serializedMatrix = corrector
                                            .correct(imagePath)
                                            .getDistortionMatrix();
                                    Log.d("test", "Serialized matrix: " + Arrays.toString(serializedMatrix));
                                    result.success(serializedMatrix);
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

