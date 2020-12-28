package com.petrichor.faceme;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;

import java.util.List;

// Add this to the header of your file:


/**
 * This is a very bare sample app to demonstrate the usage of the CameraDetector object from Affectiva.
 * It displays statistics on frames per second, percentage of time a face was detected, and the user's smile score.
 *
 * The app shows off the maneuverability of the SDK by allowing the user to start and stop the SDK and also hide the camera SurfaceView.
 *
 * For use with SDK 2.02
 */
public class MainActivity_cam extends Activity implements Detector.ImageListener, CameraDetector.CameraEventListener {

    final String LOG_TAG = "Affectiva";

    Button startSDKButton;
    //Button surfaceViewVisibilityButton;
    Button shareButton;
    TextView smileTextView;
    //ToggleButton toggleButton;
    Face.EMOJI theEmoji = Face.EMOJI.UNKNOWN;

    SurfaceView cameraPreview;

    boolean isCameraBack = false;
    boolean isSDKStarted = false;
    boolean screenTapped = false;

    RelativeLayout mainLayout;

    CameraDetector detector;

    int previewWidth = 0;
    int previewHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smileTextView = (TextView) findViewById(R.id.smile_textview);

        //toggleButton = (ToggleButton) findViewById(R.id.front_back_toggle_button);
        shareButton = (Button) findViewById(R.id.share_button);
        /*toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCameraBack = isChecked;
                switchCamera(isCameraBack? CameraDetector.CameraType.CAMERA_BACK : CameraDetector.CameraType.CAMERA_FRONT);
            }
        });*/

        /*startSDKButton = (Button) findViewById(R.id.sdk_start_button);
        startSDKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSDKStarted) {
                    isSDKStarted = false;
                    stopDetector();
                    startSDKButton.setText("Start Camera");
                } else {
                    isSDKStarted = true;
                    startDetector();
                    startSDKButton.setText("Stop Camera");
                }
            }
        });
        startSDKButton.setText("Start Camera");*/

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getIntent();

                if (Intent.ACTION_PICK.equals(intent.getAction())) {

                    MessengerThreadParams mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);

                    String metadata = mThreadParams.metadata;
                    List<String> participantIds = mThreadParams.participants;

                    Uri contentUri = Uri.parse("https://raw.githubusercontent.com/Ranks/emojione/master/assets/png_512x512/1f600.png");

                    ShareToMessengerParams shareToMessengerParams =
                            ShareToMessengerParams.newBuilder(contentUri, "image/jpeg")
                                    .setMetaData("{ \"image\" : \"trees\" }")
                                    .build();

                    MessengerUtils.finishShareToMessenger(MainActivity_cam.this, shareToMessengerParams);
                    //mPicking = true;
                    //MessengerThreadParams mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);

                    //String metadata = mThreadParams.metadata;
                    //List<String> participantIds = mThreadParams.participants;
                }
                else {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, theEmoji.getUnicode());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);

                }

            }

        });

        //We create a custom SurfaceView that resizes itself to match the aspect ratio of the incoming camera frames
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        cameraPreview = new SurfaceView(this) {
            @Override
            public void onMeasure(int widthSpec, int heightSpec) {
                int measureWidth = MeasureSpec.getSize(widthSpec);
                int measureHeight = MeasureSpec.getSize(heightSpec);
                int width;
                int height;
                if (previewHeight == 0 || previewWidth == 0) {
                    width = measureWidth;
                    height = measureHeight;
                } else {
                    float viewAspectRatio = (float)measureWidth/measureHeight;
                    float cameraPreviewAspectRatio = (float) previewWidth/previewHeight;

                    if (cameraPreviewAspectRatio > viewAspectRatio) {
                        width = measureWidth;
                        height =(int) (measureWidth / cameraPreviewAspectRatio);
                    } else {
                        width = (int) (measureHeight * cameraPreviewAspectRatio);
                        height = measureHeight;
                    }
                }
                setMeasuredDimension(width,height);
            }
        };
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        cameraPreview.setLayoutParams(params);
        mainLayout.addView(cameraPreview, 0);

        //surfaceViewVisibilityButton = (Button) findViewById(R.id.surfaceview_visibility_button);
       // surfaceViewVisibilityButton.setText("HIDE SURFACE VIEW");
       /** surfaceViewVisibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraPreview.getVisibility() == View.VISIBLE) {
                    cameraPreview.setVisibility(View.INVISIBLE);
                    surfaceViewVisibilityButton.setText("SHOW SURFACE VIEW");
                } else {
                    cameraPreview.setVisibility(View.VISIBLE);
                    surfaceViewVisibilityButton.setText("HIDE SURFACE VIEW");
                }
            }
        });
        */

        detector = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT, cameraPreview);
        detector.setLicensePath("Affdex.license");
        detector.setDetectAllEmojis(true);
        detector.setImageListener(this);
        detector.setOnCameraEventListener(this);
        startDetector();
        isSDKStarted = true;
    }

    @Override
    protected void onResume() {

        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);


        if (isSDKStarted) {
            startDetector();
        }
        else {
            detector.reset();
        }

        screenTapped = false;

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        stopDetector();

    }

    void startDetector() {
        if (!detector.isRunning()) {
            try {
                detector.start();
            } catch (Exception e) {
                Log.e("Affectiva", e.getMessage());
            }
        }
    }

    void stopDetector() {
        if (detector.isRunning()) {
            try {
                detector.stop();
            } catch (Exception e) {
                Log.e("Affectiva",e.getMessage());
            }
        }
    }

    void switchCamera(CameraDetector.CameraType type) {
        try {
            detector.setCameraType(type);
        } catch (Exception e) {
            Log.e("Affectiva", e.getMessage());
        }
    }

    @Override
    public void onImageResults(List<Face> list, Frame frame, float v) {

        if (!screenTapped) {
            String text = "";

            if (list == null)
                return;;
            if (list.size() != 0) {
                Face face = list.get(0);
                theEmoji = face.emojis.getDominantEmoji();
                text = theEmoji.getUnicode();
            }
            smileTextView.setText(text);
        }
    }

    @Override
    public void onCameraSizeSelected(int width, int height, Frame.ROTATE rotate) {
        if (rotate == Frame.ROTATE.BY_90_CCW || rotate == Frame.ROTATE.BY_90_CW) {
            previewWidth = height;
            previewHeight = width;
        } else {
            previewHeight = height;
            previewWidth = width;
        }
        cameraPreview.requestLayout();
    }

    public void screenTapped(View view) {
        screenTapped = !screenTapped;
        //Toast.makeText(getApplicationContext(), "Screen tapped", Toast.LENGTH_SHORT).show();
    }
}
