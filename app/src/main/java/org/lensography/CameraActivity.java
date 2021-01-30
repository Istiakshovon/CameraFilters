package org.lensography;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.params.TonemapCurve;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "AndroidCameraApi";
    private ImageButton takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();


    ImageButton imgFilter;
    RelativeLayout rl_filter;
    ImageView imgOriginal,imgAqua,imgBlackboard,imgWhiteboard,imgSepia,imgNegative,imgMono;

    int filter;

    AppCompatSeekBar seekbarBrightness,seekbarContrast;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        seekbarBrightness = findViewById(R.id.seekBarBrightness);
        seekbarContrast = findViewById(R.id.seekBarContrast);

        imgFilter = findViewById(R.id.imgFilter);
        rl_filter = findViewById(R.id.rl_filter);
        imgOriginal = findViewById(R.id.imgOriginal);
        imgAqua = findViewById(R.id.imgAqua);
        imgBlackboard = findViewById(R.id.imgBlackBoard);
        imgWhiteboard = findViewById(R.id.imgWhiteBoard);
        imgSepia = findViewById(R.id.imgSepia);
        imgNegative = findViewById(R.id.imgNegative);
        imgMono = findViewById(R.id.imgMono);

        //setup tabHost
        TabHost tabHost = findViewById(R.id.tab_host);
        tabHost.setup();

        //set item 1 in tabhost
        TabHost.TabSpec  Filters= tabHost.newTabSpec("Filters");
        Filters.setContent(R.id.Filters);
        Filters.setIndicator("Filters");
        tabHost.addTab(Filters);

        //set item 2 in tabhost
        TabHost.TabSpec Adjustments= tabHost.newTabSpec("Adjustments");
        Adjustments.setContent(R.id.Adjustments);
        Adjustments.setIndicator("Adjustments");
        tabHost.addTab(Adjustments);



        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabHost.getVisibility()==View.GONE){
                    tabHost.setVisibility(View.VISIBLE);
                }else{
                    tabHost.setVisibility(View.GONE);
                }
            }
        });

        imgOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 0;
                imgOriginal.setBackgroundResource(R.drawable.border);
                imgAqua.setBackgroundResource(0);
                imgBlackboard.setBackgroundResource(0);
                imgWhiteboard.setBackgroundResource(0);
                imgSepia.setBackgroundResource(0);
                imgNegative.setBackgroundResource(0);
                imgMono.setBackgroundResource(0);

                updatePreview();
            }
        });

        imgAqua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 1;
                imgAqua.setBackgroundResource(R.drawable.border);
                imgOriginal.setBackgroundResource(0);
                imgBlackboard.setBackgroundResource(0);
                imgWhiteboard.setBackgroundResource(0);
                imgSepia.setBackgroundResource(0);
                imgNegative.setBackgroundResource(0);
                imgMono.setBackgroundResource(0);

                updatePreview();
            }
        });

        imgBlackboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 2;
                imgBlackboard.setBackgroundResource(R.drawable.border);
                imgAqua.setBackgroundResource(0);
                imgOriginal.setBackgroundResource(0);
                imgWhiteboard.setBackgroundResource(0);
                imgSepia.setBackgroundResource(0);
                imgNegative.setBackgroundResource(0);
                imgMono.setBackgroundResource(0);

                updatePreview();
            }
        });

        imgWhiteboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 3;
                imgWhiteboard.setBackgroundResource(R.drawable.border);
                imgAqua.setBackgroundResource(0);
                imgBlackboard.setBackgroundResource(0);
                imgOriginal.setBackgroundResource(0);
                imgSepia.setBackgroundResource(0);
                imgNegative.setBackgroundResource(0);
                imgMono.setBackgroundResource(0);

                updatePreview();
            }
        });

        imgSepia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 4;
                imgSepia.setBackgroundResource(R.drawable.border);
                imgAqua.setBackgroundResource(0);
                imgBlackboard.setBackgroundResource(0);
                imgWhiteboard.setBackgroundResource(0);
                imgOriginal.setBackgroundResource(0);
                imgNegative.setBackgroundResource(0);
                imgMono.setBackgroundResource(0);

                updatePreview();
            }
        });

        imgNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 5;
                imgNegative.setBackgroundResource(R.drawable.border);

                imgAqua.setBackgroundResource(0);
                imgBlackboard.setBackgroundResource(0);
                imgWhiteboard.setBackgroundResource(0);
                imgSepia.setBackgroundResource(0);
                imgOriginal.setBackgroundResource(0);
                imgMono.setBackgroundResource(0);

                updatePreview();
            }
        });

        imgMono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = 6;
                imgMono.setBackgroundResource(R.drawable.border);
                imgAqua.setBackgroundResource(0);
                imgBlackboard.setBackgroundResource(0);
                imgWhiteboard.setBackgroundResource(0);
                imgSepia.setBackgroundResource(0);
                imgNegative.setBackgroundResource(0);

                updatePreview();
            }
        });

        textureView = (TextureView) findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = (ImageButton) findViewById(R.id.imageButton);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


            if (filter==1){
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_AQUA);
            }else if (filter==2){
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD);
            }else if(filter==3){
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD);
            }else if (filter==4){
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SEPIA);
            }else if (filter==5){
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
            }else if (filter==6){
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
            }else if (filter==0){
                captureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, null);
            }



            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

//        manually added by me
        startBackgroundThread();

        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


        seekbarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("PROGRESS",progress+" ");
                TextView txt = findViewById(R.id.txtValue1);
                txt.setText(String.valueOf(progress-100));
                int brightness = (int) (-100 + (100 - -100) * (progress-100 / 100f));
                Log.d("BRIGHTNESS", String.valueOf(brightness));
                try {
                    captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, brightness);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekbarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final int minContrast = 0;
                final int maxContrast = 1;

                float[][] channels = new float[0][];

                if (channels == null || progress > 100 || progress < 0) {
                    return;
                }

                float contrast = minContrast + (maxContrast - minContrast) * (progress / 100f);

                float[][] newValues = new float[3][];
                for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
                    float[] array = new float [channels[chanel].length];
                    System.arraycopy(channels[chanel], 0, array, 0, array.length);
                    for (int i = 0; i < array.length; i++) {
                        array[i] *= contrast;
                    }
                    newValues[chanel] = array;
                }
                //set def channels (used for contrast)
                TonemapCurve tc = captureRequestBuilder.get(CaptureRequest.TONEMAP_CURVE);
                if (tc != null) {
                    channels = new float[3][];
                    for (int chanel = TonemapCurve.CHANNEL_RED; chanel <= TonemapCurve.CHANNEL_BLUE; chanel++) {
                        float[] array = new float[tc.getPointCount(chanel) * 2];
                        tc.copyColorCurve(chanel, array, 0);
                        channels[chanel] = array;
                    }
                }
                captureRequestBuilder.set(CaptureRequest.TONEMAP_MODE, CaptureRequest.TONEMAP_MODE_CONTRAST_CURVE);
                TonemapCurve tc1 = new TonemapCurve(newValues[TonemapCurve.CHANNEL_RED], newValues[TonemapCurve.CHANNEL_GREEN], newValues[TonemapCurve.CHANNEL_BLUE]);
                captureRequestBuilder.set(CaptureRequest.TONEMAP_CURVE, tc1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (filter==1){
            captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_AQUA);
        }else if (filter==2){
            captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD);
        }else if(filter==3){
            captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD);
        }else if (filter==4){
            captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SEPIA);
        }else if (filter==5){
            captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
        }else if (filter==6){
            captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
        }else if (filter==0){
            captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, null);
        }

//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_AQUA);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_OFF);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SEPIA);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE);
//        captureRequestBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD);

        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(CameraActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
}