package com.example.sightseer;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Take extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener {
    private static String TAG = "MainActivity";
    JavaCamera2View javaCameraView;
    Mat mRGBA, mRGBAT;
    Mat temp;
    TextView similarita;
    ImageButton photo;
    Boolean a = true;
    Mat img;
    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(Take.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    static {
        if (OpenCVLoader.initDebug()) {
            System.out.println("OpenCV configured correctly!");
        } else {
            System.out.println("OpenCV failed!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_photo);

        javaCameraView = (JavaCamera2View) findViewById(R.id.appcamview);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCvCameraViewListener(Take.this);
        ImageButton photo = (ImageButton) findViewById(R.id.takePhoto);
        photo.setOnClickListener(this);
        similarita = (TextView) findViewById(R.id.textView);
        temp = new Mat(this.getIntent().getExtras().getLong("addr"));
        img = temp.clone();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //javaCameraView.setMaxFrameSize(480, 720);
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        mRGBAT = mRGBA.t();
        Core.flip(mRGBA.t(), mRGBAT, 1);
        Size newsize = mRGBA.size();
        /*newsize.height = 480;
        newsize.width = 640;*/
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());
        /*Imgproc.resize(mRGBAT, testMat, new Size(480, 720),0,0,Imgproc.INTER_CUBIC);*/
        return mRGBAT;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            System.out.println("OpenCV configured correctly!");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            System.out.println("OpenCV failed!");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }

    @Override
    public void onClick(View v) {
        //similarita.setText((int) mRGBA.size().width + " " + (int) mRGBA.size().height);
        //setResolution(480, 1920);
        Compare compare = new Compare(mRGBAT, img);
        double similar = compare.compare();
        similarita.setText("" + similar);
        if (similar >= 0.02) {
            similarita.setText("Ověřeno ✔");
        }
        //similarita.setText(Environment.getExternalStorageState());
        //Imgcodecs.imwrite("openimg.png", mRGBAT);
        /*String imgFileName = "openimg.png";
        boolean imwrite = Imgcodecs.imwrite(imgFileName, mRGBAT);
        if (imwrite) {
            similarita.setText(imgFileName);
        } else {
            similarita.setText("failed");
        }*/
    }
}
