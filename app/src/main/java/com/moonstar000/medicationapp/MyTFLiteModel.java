package com.moonstar000.medicationapp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MyTFLiteModel {

    private Interpreter tflite;
    private int numClasses;

    MyTFLiteModel(AssetManager assetManager) throws IOException {
        MappedByteBuffer tfliteModel = loadModelFile(assetManager);
        tflite = new Interpreter(tfliteModel);
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd("dish_prediction_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[][] predict(float[][] inputData) {
        float[][] outputData = new float[inputData.length][numClasses];
        tflite.run(inputData, outputData);
        return outputData;
    }
}
