package com.example.swapnilnaukudkar.fritz_imagesegmentation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.widget.ImageView;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.core.utils.BitmapUtils;
import ai.fritz.fritzvisionsegmentation.FritzVisionMask;
import ai.fritz.fritzvisionsegmentation.FritzVisionSegmentPredictor;
import ai.fritz.fritzvisionsegmentation.FritzVisionSegmentPredictorOptions;
import ai.fritz.fritzvisionsegmentation.FritzVisionSegmentResult;
import ai.fritz.fritzvisionsegmentation.MaskType;
import ai.fritz.peoplesegmentation.FritzVisionPeopleSegmentPredictor;
import ai.fritz.vision.inputs.FritzVisionImage;
import ai.fritz.vision.inputs.FritzVisionOrientation;
import ai.fritz.vision.predictors.FritzVisionCropAndScale;


//import ai.fritz.fritzvisionstylemodel.ArtisticStyle;
//import ai.fritz.fritzvisionstylemodel.FritzStyleResolution;
//import ai.fritz.fritzvisionstylemodel.FritzVisionStylePredictor;
//import ai.fritz.fritzvisionstylemodel.FritzVisionStylePredictorOptions;
//import ai.fritz.fritzvisionstylemodel.FritzVisionStyleTransfer;
//import ai.fritz.vision.predictors.FritzVisionPredictor;

public class MainActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);

    private AtomicBoolean computing = new AtomicBoolean(false);

    private FritzVisionImage styledImage;
    FritzVisionSegmentPredictor segmentPredictor;
    //FritzVisionLabelResult labelResult;
    FritzVisionSegmentResult segmentResult;
    // STEP 1:
    // TODO: Define the predictor variable
    //private FritzVisionStylePredictor predictor;
    // END STEP 1

    private Size cameraViewSize;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fritz
        Fritz.configure(this);
        // Fritz.configure(this, "9c7eeb1663be476c99336ed0d8c642bf");
        // STEP 1: Get the predictor and set the options.
        // ----------------------------------------------
        // TODO: Add the predictor snippet here
        FritzVisionSegmentPredictorOptions options = new FritzVisionSegmentPredictorOptions.Builder()
                .cropAndScaleOption(FritzVisionCropAndScale.SCALE_TO_FIT)
                .build();
        segmentPredictor = new FritzVisionPeopleSegmentPredictor(options);


        // "this" refers to the calling Context (Application, Activity, etc)
        //  segmentPredictor = new FritzVisionPeopleSegmentPredictor();
        //  predictor = FritzVisionStyleTransfer.getPredictor(this, ArtisticStyle.STARRY_NIGHT);
        // ----------------------------------------------
        // END STEP 1
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_stylize;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size previewSize, final Size cameraViewSize, final int rotation) {

        this.cameraViewSize = cameraViewSize;

        // Callback draws a canvas on the OverlayView
        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        // STEP 4: Draw the prediction result
                        // ----------------------------------
                        if (styledImage != null) {
                            // TODO: Draw or show the result here
                            styledImage.drawOnCanvas(canvas);
                        }
                        // ----------------------------------
                        // END STEP 4
                    }
                });
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = reader.acquireLatestImage();

        if (image == null) {
            return;
        }

        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }

        // STEP 2: Create the FritzVisionImage object from media.Image
        // ------------------------------------------------------------------------
        // TODO: Add code for creating FritzVisionImage from a media.Image object
        int rotationFromCamera = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
        final FritzVisionImage fritzImage = FritzVisionImage.fromMediaImage(image, rotationFromCamera);
        // ------------------------------------------------------------------------
        // END STEP 2

        image.close();


        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        // STEP 3: Run predict on the image
                        // ---------------------------------------------------
                        // TODO: Add code for running prediction on the image
                        segmentResult = segmentPredictor.predict(fritzImage);

                        final long startTime = SystemClock.uptimeMillis();
//                         styledImage = predictor.predict(fritzImage);
//                         styledImage.scale(cameraViewSize.getWidth(), cameraViewSize.getHeight());
                        // Log.d(TAG, "INFERENCE TIME:" + (SystemClock.uptimeMillis() - startTime));

                        Log.e(TAG, "run: " + segmentResult.toString());
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.e(TAG, "image frame: ");
                                // Draw the original image
//                                Size targetSize = new Size(2048, 2048);
                                final ImageView img_Overlay = (ImageView) findViewById(R.id.img_overlay);
//// Create a mutable bitmap from the original image
//                                Bitmap originalBitmap = segmentResult.getResultBitmap();
//                               // Bitmap scaledBitmap = BitmapUtils.scale(originalBitmap, getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight());
//                                Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
//
//// Creating a canvas to add the mask to the mutable bitmap.
//                                //Canvas canvas = new Canvas(mutableBitmap);
//
//// Print the mask overlay on the canvas (without the original image)
//                                //segmentResult.drawAllMasks(canvas);
//
//// Set the bitmap for the image view

                                // Create a mutable bitmap from the original image
                                Bitmap originalBitmap = segmentResult.getOriginalImage().getBitmap();
                                Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

// Creating a canvas to add the mask to the mutable bitmap.
                                Canvas canvas = new Canvas(mutableBitmap);

// Draw the original image to the canvas
                                FritzVisionMask personMask = segmentResult.findMask(MaskType.PERSON);

// Draw the specific mask on the canvas with alpha value 100
                                if (personMask != null) {
                                    personMask.drawMask(canvas, 100);
                                }


                                if (img_Overlay != null) {
                                    img_Overlay.setImageBitmap(mutableBitmap);
                                }
                            }
                        });

                        // ----------------------------------------------------
                        // END STEP 3

                        // Fire callback to change the OverlayView
                        //requestRender();
                        computing.set(false);
                    }
                });
    }
}
