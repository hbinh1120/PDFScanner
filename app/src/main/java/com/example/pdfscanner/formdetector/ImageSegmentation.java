package com.example.pdfscanner.formdetector;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class ImageSegmentation {

    private GpuDelegate gpuDelegate = null;
    private GpuDelegate.Options delegateOptions;


    private TensorImage inputImageBuffer;

    private ByteBuffer outputImageBuffer;

    //config yolo
    private int imageSizeY;
    private int imageSizeX;

    private int outputShape = 256;
    private final String TAG = "ImageSegmentation";

    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    private static boolean isGPU = false;

    /** The loaded TensorFlow Lite model. */
    private MappedByteBuffer tfliteModel;
    private Interpreter tflite;


    ImageSegmentation(Activity activity) throws IOException{

        String modelPath = "form-detection.tflite";

        try {
            Interpreter.Options options = (new Interpreter.Options());
            CompatibilityList compatList = new CompatibilityList();

            if(isGPU && compatList.isDelegateSupportedOnThisDevice()){
                // if the device has a supported GPU, add the GPU delegate
                delegateOptions = compatList.getBestOptionsForThisDevice();
                gpuDelegate = new GpuDelegate(delegateOptions);
                options.addDelegate(gpuDelegate);
            } else {
                // if the GPU is not supported, run on 4 threads
                options.setNumThreads(NUM_THREADS);
            }

            tfliteModel = loadModelFile(activity, modelPath);
            this.tflite= new Interpreter(tfliteModel, options);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Reads type and shape of input and output tensors, respectively.

        int imageTensorIndex = 0;
        int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
        imageSizeY = imageShape[0];
        imageSizeX = imageShape[1];
        DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
        // Creates the input tensor.
        inputImageBuffer = new TensorImage(imageDataType);

        outputImageBuffer = ByteBuffer.allocateDirect(outputShape * outputShape * Float.SIZE / Byte.SIZE);
        outputImageBuffer.order(ByteOrder.nativeOrder());

    }

    public void close() {
        if (tflite != null) {
            // TODO: Close the interpreter
            tflite.close();
            tflite = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
        tfliteModel = null;
    }



    private TensorImage loadImage(final Bitmap bitmap) {
        // Loads bitmap into a TensorImage.
        inputImageBuffer.load(bitmap);

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .build();
        return imageProcessor.process(inputImageBuffer);
    }

    public Bitmap detectImage(Bitmap bitmap) {
        inputImageBuffer = loadImage(bitmap);
        outputImageBuffer.clear();
        // Runs the inference call.
        // TODO: Run TFLite inference
        tflite.run(inputImageBuffer.getBuffer(), outputImageBuffer);
        return convertOutputBufferToBitmap(outputImageBuffer);
    }

    private Bitmap convertOutputBufferToBitmap(ByteBuffer outImgData) {
        if (outImgData == null) {
            return null;
        }
        outImgData.rewind();
        Bitmap bitmap_out = Bitmap.createBitmap(outputShape , outputShape, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[outputShape * outputShape];
        for (int i = 0; i < outputShape * outputShape; i++) {
            float val = outImgData.getFloat();
            if (val > 0.2) {
                pixels[i] = 0xFFFFFFFF;
            } else {
                pixels[i] = 0xFF000000;
            }
        }
        bitmap_out.setPixels(pixels, 0, outputShape, 0, 0, outputShape, outputShape);
        return bitmap_out;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}