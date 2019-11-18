package com.example.noob.textdetector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.noob.textdetector.managers.TrieManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONArray;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements  CameraBridgeViewBase.CvCameraViewListener2  {
    Mat mRgba;

    Mat mGray;
    Mat copyMat;
    Scalar CONTOUR_COLOR;
    private int COUNT_DOWN = 5;
    private boolean isProcess = false;
    CameraBridgeViewBase mOpenCvCameraView;
    Handler handler;
    HandlerThread handlerThread;

    TrieManager trieManager;
    LinearLayout signageLayout;
    String locale = "en";
    FrameLayout slidingLayout;
    private boolean collapsed = true;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                   // detector = MSER.create();
                    //mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    private int startGC = 50;
    private String previd = "";

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if(handlerThread != null){
            handlerThread.quitSafely();
        }

        try{
            handlerThread.join();
            handlerThread = null;
            handler       = null;
        }catch (final InterruptedException e){
            Log.d("sss","check it");
        }

        if(trieManager != null){
            trieManager.invalidate();
        }

    }


    private void setupUi(){
//        if(checkTutorialDone()) {
            if (!OpenCVLoader.initDebug()) {
                Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            } else {
                Log.d("OpenCV", "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
            trieManager = new TrieManager("trie", MainActivity.this);

            trieManager.start();

            handlerThread = new HandlerThread("opencv");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());

            slidingLayout = findViewById(R.id.sliding_layout);
            signageLayout = findViewById(R.id.signMain);
//        }else {
//            showTutorial()
//        }



    }

//    private boolean checkTutorialDone(){
//        SharedPreferences pref = getApplicationContext().getSharedPreferences("sharedprefs", 0); // 0 - for private mode
//        return pref.getBoolean("tutorial_done", false);
//    };

//    private void showTutorial(){
//
//    }





    public void onResume()
    {
        super.onResume();
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }else{
                //already permission given
                setupUi();
            }
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    setupUi();
                } else {
                    //not granted
                    Toast.makeText(getApplicationContext(),"Permissions denied",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean checkIfAlreadyhavePermission() {

        int result_write_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result_read_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        int result_wifi_access = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (result_write_storage == PackageManager.PERMISSION_GRANTED && result_read_storage == PackageManager.PERMISSION_GRANTED && result_wifi_access == PackageManager.PERMISSION_GRANTED ) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        String[] permsList = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        ActivityCompat.requestPermissions(this, permsList, 101);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = findViewById(R.id.live_camera_frame);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC3);
        mGray = new Mat();
        copyMat = new Mat(height, width, CvType.CV_8UC3);
        //mByte = new Mat(height, width, CvType.CV_8UC1);
    }



    @Override
    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        mRgba.release();
    }

        @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

            startGC--;
            if (startGC==0) {
                System.gc();
                System.runFinalization();
                startGC=100;
            }

            mRgba = inputFrame.rgba();
            if(!collapsed){
                return mRgba;
            }
            if(!isProcess){
                  isProcess = true;
                  final Mat intermediary = inputFrame.gray();
                  intermediary.copyTo(mGray);
                  mRgba.copyTo(copyMat);
                  final MatOfKeyPoint keypoint = new MatOfKeyPoint();
                  //keypoint.clone();
                  intermediary.release();


                runInBackGround(new Runnable() {
                    @Override
                    public void run() {

                        CONTOUR_COLOR = new Scalar(255);
                            MatOfKeyPoint keypoint = new MatOfKeyPoint();
                            List<KeyPoint> listpoint = new ArrayList<KeyPoint>();
                            KeyPoint kpoint = new KeyPoint();
                            Mat mask = Mat.zeros(mGray.size(), CvType.CV_8UC1);
                            int rectanx1;
                            int rectany1;
                            int rectanx2;
                            int rectany2;

                            //
                            Scalar zeos = new Scalar(0, 0, 0);

                            List<MatOfPoint> contour2 = new ArrayList<MatOfPoint>();
                            Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
                            Mat morbyte = new Mat();
                            Mat hierarchy = new Mat();


                            Rect rectan3 = new Rect();//
                            int imgsize = mRgba.height() * mRgba.width();
                            //


                                long milli = System.currentTimeMillis();
                                isProcess = true;
                                //MSER detector = MSER.create();
                                FeatureDetector detector = FeatureDetector
                                        .create(FeatureDetector.MSER);
                                detector.detect(mGray, keypoint);
                                listpoint = keypoint.toList();
                                //
                                long diff = System.currentTimeMillis() - milli;
                                System.out.print("-------->"+String.valueOf(diff));
                                for (int ind = 0; ind < listpoint.size(); ind++) {
                                    kpoint = listpoint.get(ind);
                                    rectanx1 = (int) (kpoint.pt.x - 0.5 * kpoint.size);
                                    rectany1 = (int) (kpoint.pt.y - 0.5 * kpoint.size);
                                    // rectanx2 = (int) (kpoint.pt.x + 0.5 * kpoint.size);
                                    // rectany2 = (int) (kpoint.pt.y + 0.5 * kpoint.size);
                                    rectanx2 = (int) (kpoint.size);
                                    rectany2 = (int) (kpoint.size);
                                    if (rectanx1 <= 0)
                                        rectanx1 = 1;
                                    if (rectany1 <= 0)
                                        rectany1 = 1;
                                    if ((rectanx1 + rectanx2) > mGray.width())
                                        rectanx2 = mGray.width() - rectanx1;
                                    if ((rectany1 + rectany2) > mGray.height())
                                        rectany2 = mGray.height() - rectany1;
                                    Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
                                    Mat roi = new Mat(mask, rectant);
                                    roi.setTo(CONTOUR_COLOR);
                                    roi.release();

                                }


                                Imgproc.morphologyEx(mask, morbyte, Imgproc.MORPH_DILATE, kernel);
                                Imgproc.findContours(morbyte, contour2, hierarchy,
                                        Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
                                keypoint.release();
                                morbyte.release();
                                mask.release();
                                kernel.release();
                                hierarchy.release();

                                final int[] size = new int[1];
                                final ArrayList<String> resultItems  = new ArrayList<>();
                                size[0]          = contour2.size();
                                if(contour2.size() == 0){
                                    mGray.release();
                                    copyMat.release();
                                    isProcess = false;

                                }
                                for (int ind = 0; ind < contour2.size(); ind++) {
                                    rectan3 = Imgproc.boundingRect(contour2.get(ind));
                                    if (rectan3.area() > 0.5 * imgsize || rectan3.area() < 100
                                            || rectan3.width / rectan3.height < 2) {
                                        size[0]--;
                                        if(size[0] == 0){
                                            mGray.release();
                                            copyMat.release();

                                            for(int k=0;k<contour2.size();k++){
                                                contour2.get(k).release();
                                            }

                                            trieManager.getBoard(resultItems);
                                            isProcess = false;
//
                                        }

                                    } else {
//
                                        final Bitmap result = captureBitmap(new Mat(copyMat, rectan3));
                                        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(result);
                                        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                                                .getOnDeviceTextRecognizer();
                                        textRecognizer.processImage(image)
                                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                                    @Override
                                                    public void onSuccess(FirebaseVisionText result) {
                                                        String resultText = result.getText();

                                                        for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
                                                            String blockText = block.getText();

                                                            for (FirebaseVisionText.Line line: block.getLines()) {
                                                                String lineText = line.getText();

                                                                for (FirebaseVisionText.Element element: line.getElements()) {
                                                                    String elementText = element.getText();
                                                                    String elements[]  = elementText.split(" ");
                                                                    for(String word : elements){
                                                                        if(word.length() > 2){
                                                                            resultItems.add(word);
                                                                        }

                                                                    }

                                                                }
                                                            }
                                                        }

                                                        size[0]--;
                                                        if(size[0] == 0){
                                                            mGray.release();
                                                            copyMat.release();

                                                            for(int k=0;k<contour2.size();k++){
                                                                contour2.get(k).release();
                                                            }

                                                            trieManager.getBoard(resultItems);
                                                            isProcess = false;

//
                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                // Task failed with an exception
                                                                // ...
                                                                e.printStackTrace();
                                                            }
                                                        });

                                                }
                                            }



                    }



                });





            }




            return mRgba;

        }


    private Bitmap captureBitmap(Mat roi){
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(roi.cols(), roi.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(roi, bitmap);
            return bitmap;
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            return null;
        }
    }






    protected synchronized void runInBackGround(final Runnable r){
        if(handler != null){
            handler.post(r);
        }
    }

    public void removeAllViewsAndHide(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(signageLayout.getVisibility() == View.VISIBLE){
                    signageLayout.removeAllViews();
                    signageLayout.setVisibility(View.GONE);
                    previd = "";
                }
            }
        });


    }

    public void displaySignOnBoard(JSONArray board_data, String id){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent netActivity  = new Intent(MainActivity.this, ShowSignActivity.class);
                netActivity.putExtra("data",board_data.toString());
                netActivity.putExtra("locale",locale);
                startActivity(netActivity);
                finish();
            }
        });
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(signageLayout.getVisibility() == View.VISIBLE){
//                    if(!previd.equals(id)){
//                        Toast.makeText(MainActivity.this,"检测到的翻译如下",Toast.LENGTH_SHORT).show();
//                    }
//                }else if(previd.equals("")){
//                    Toast.makeText(MainActivity.this,"检测到的翻译如下",Toast.LENGTH_SHORT).show();
//                }
//                previd = id;
//
//
//                signageLayout.removeAllViews();
//                signageLayout.setVisibility(View.GONE);
//                try {
//                    for (int k = 0; k < board_data.length(); k++) {
//                        JSONArray row_data = board_data.getJSONArray(k);
//                        LinearLayout row_layout = new LinearLayout(MainActivity.this);
//                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                        //params.setMargins(0,(int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,1),0,0);
//                        row_layout.setLayoutParams(params);
//                        row_layout.setOrientation(LinearLayout.HORIZONTAL);
//                        for (int m = 0; m < row_data.length(); m++) {
//                            String sign_text = "";
//                            LinearLayout item_layout = new LinearLayout(MainActivity.this);
//                            LinearLayout.LayoutParams params_item = new LinearLayout.LayoutParams((int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,300), (int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,60));
//                            //params_item.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,1);
//
//                            item_layout.setLayoutParams(params_item);
//                            item_layout.setOrientation(LinearLayout.HORIZONTAL);
//                            if(row_data.getJSONObject(m).getJSONObject("text_multilingual").has(locale)) {
//                                sign_text = row_data.getJSONObject(m).getJSONObject("text_multilingual").getString(locale);
//                            }else{
//                                sign_text = row_data.getJSONObject(m).getJSONObject("text_multilingual").getString("en");
//                            }
//
//                            if(row_data.getJSONObject(m).getString("color").equals("blue")){
//                                item_layout.setBackgroundResource(R.drawable.blue_rounded);
//                            }else if(row_data.getJSONObject(m).getString("color").equals("red")){
//                                item_layout.setBackgroundResource(R.drawable.red_rounded);
//                            }else{
//                                item_layout.setBackgroundResource(R.drawable.blue_rounded);
//                            }
//                            TextView txt            = new TextView(MainActivity.this);
//                            ImageView direction     = new ImageView(MainActivity.this);
//                            ImageView icon          = new ImageView(MainActivity.this);
//                            String direction_name   = row_data.getJSONObject(m).getString("direction_name");
//                            int direction_resid     = getResId(direction_name,R.drawable.class);
//                            if(direction_resid > 0 && !sign_text.equals("")){
//                                LinearLayout.LayoutParams direction_params = new LinearLayout.LayoutParams((int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,36.5f), (int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,36.5f));
//                                direction_params.gravity                    = Gravity.CENTER_VERTICAL;
//                                direction_params.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,7.5f);
//                                //direction_params.bottomMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,10);
//                                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                                direction.setImageResource(direction_resid);
//                                direction.setLayoutParams(direction_params);
//                                item_layout.addView(direction);
//
//                            }
//                            String icon_name        = row_data.getJSONObject(m).getJSONArray("icon_urls").getString(0);
//                            int icon_resid     = getResId(icon_name,R.drawable.class);
//                            if(icon_resid > 0 && !sign_text.equals("")){
//                                LinearLayout.LayoutParams icon_params = new LinearLayout.LayoutParams((int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,35.02f), (int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,35.02f));
//                                icon_params.gravity                    = Gravity.CENTER_VERTICAL;
//                                icon_params.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,7.35f);
//                                //icon_params.bottomMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,10);
//                                icon.setImageResource(icon_resid);
//                                icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                                icon.setLayoutParams(icon_params);
//                                item_layout.addView(icon);
//
//                            }
//
//                            LinearLayout.LayoutParams txt_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                            txt_params.leftMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(MainActivity.this,10.35f);
//                            //txt_params.bottomMargin                 = (int)com.example.noob.textdetector.Utils.getPixelsFromDp(this,10);
//                            txt.setTextSize(19);
//                            txt.setTypeface(null, Typeface.BOLD);
//                            txt.setLayoutParams(txt_params);
//                            txt.setText(sign_text);
//                            txt.setGravity(Gravity.CENTER_VERTICAL);
//                            txt.setTextColor(getResources().getColor(android.R.color.white));
//                            item_layout.addView(txt);
//                            row_layout.addView(item_layout);
//                        }
//                        signageLayout.addView(row_layout);
//                    }
//                }catch (JSONException e){
//                    e.printStackTrace();
//                }
//                signageLayout.setVisibility(View.VISIBLE);
//            }
//        });

    }


    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

//    public void displayProbability(String text){
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                probabilityLog.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        //probabilityLog.setText(String.valueOf(probability));
//                        probabilityLog.setText(text);
//                    }
//                });
//            }
//        });
//    }


}
