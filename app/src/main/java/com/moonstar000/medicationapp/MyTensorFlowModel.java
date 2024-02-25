package com.moonstar000.medicationapp;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class MyTensorFlowModel {

    private Interpreter tfliteInterpreter;

    public MyTensorFlowModel(String modelPath) {
        try {
            // Load the TensorFlow Lite model
            tfliteInterpreter = new Interpreter(loadModelFile(modelPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load model file from assets folder
    private ByteBuffer loadModelFile(String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(modelPath);
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = fileChannel.position();
        long declaredLength = fileChannel.size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] runInference(String[] inputs) {
        // Prepare input tensors
        int inputTensorIndex = 0; // Assuming the first input tensor
        int batchSize = inputs.length; // Number of inputs
        int inputLength = inputs[0].length(); // Length of each input (assuming they have the same length)
        int inputTensorSize = inputLength * Float.SIZE / Byte.SIZE * batchSize;

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputTensorSize);
        inputBuffer.order(ByteOrder.nativeOrder());
        for (String input : inputs) {
            for (int i = 0; i < inputLength; i++) {
                inputBuffer.putFloat(Float.parseFloat(input.substring(i, i + 1)));
            }
        }

        // Prepare output tensors
        int outputTensorIndex = 0; // Assuming the first output tensor
        int outputTensorSize = 90; // Size of the output tensor

        ByteBuffer outputBuffer = ByteBuffer.allocateDirect(outputTensorSize);
        outputBuffer.order(ByteOrder.nativeOrder());

        // Run inference
        tfliteInterpreter.run(inputBuffer, outputBuffer);

        // Process output tensor
        float[] outputArray = new float[outputTensorSize / (Float.SIZE / Byte.SIZE)];
        outputBuffer.rewind();
        outputBuffer.asFloatBuffer().get(outputArray);

        return outputArray;
    }
}
