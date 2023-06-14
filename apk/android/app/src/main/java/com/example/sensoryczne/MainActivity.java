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
                                    double[] serializedMatrix = corrector.correct(imagePath).getDistortionMatrix();

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

