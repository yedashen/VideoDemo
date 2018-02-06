package tyj.com.videodemo;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

/**
 * @author ChenYe
 *         注意：
 *         （1）录制的过程中，不允许切换摄像头，以为我这里的切换摄像头是重新初始化了camera对象，那么MediaRecord里面的还是之前的camera
 *         对象，会造成录制出来的视频内容有黑屏现象甚至崩溃。
 *         （2）本demo是用原生的代码写的，没有用ffmpeg等框架，所以要是把我这个demo看熟了，有什么问题好商量。
 *         （3）我没写权限申请，所以记得在一开始运行的时候就给予全部权限。
 *         （4）仿微信，当录制完毕之后默认就循环播放,如果是正在播放的时候onPause，再进入之后还是继续循环播放。
 *         功能：
 *         本demo支持在录屏的时候缩放，局部定焦，开启闪光灯等功能。
 */

public class TakeVideoActivity extends Activity {

    private static final String TAG = TakeVideoActivity.class.getSimpleName();
    private SurfaceView mSurfaceView;
    /**
     * 判断是否初始化过surfaceView
     */
    private boolean isHasSurface;
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mRecorder;
    private Camera mCamera;
    /**
     * 上一次两指距离
     */
    private float oldDist = 1f;
    private Button mStateTv;
    private Button mCancelBt;
    private Button mSaveBt;
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_video);
        findId();
        //初始化相机配置，在onResume的时候开始预览
        CameraManager.getInstance().initContext(this);
        //设置缩放的
        findViewById(R.id.zoom_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                if (event.getPointerCount() == 1 && action == MotionEvent.ACTION_DOWN) {
                    CameraManager.getInstance().handleFocusMetering(event.getX(), event.getY());
                } else if (event.getPointerCount() >= 2) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_POINTER_DOWN:
                            oldDist = getFingerSpacing(event);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float newDist = getFingerSpacing(event);
                            CameraManager.getInstance().handleZoom(newDist > oldDist);
                            oldDist = newDist;
                            break;
                        default:
                            break;
                    }
                }
                return true;
            }
        });
        initVideoView();
    }

    /**
     * 初始化播放视频，我这里是一开始就初始化，你也可以在录制视频完毕之后再进行初始化视频和播放
     */
    private void initVideoView() {
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("TakeVideo", "视频播放完毕");
                mVideoView.resume();
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e("TakeVideo", "视频准备完毕");
                mVideoView.start();
            }
        });
    }

    private void findId() {
        mSurfaceView = (SurfaceView) findViewById(R.id.sfv);
        mSurfaceHolder = mSurfaceView.getHolder();
        mStateTv = (Button) findViewById(R.id.record_state_tv);
        mCancelBt = (Button) findViewById(R.id.cancel_bt);
        mSaveBt = (Button) findViewById(R.id.save_bt);
        mVideoView = (VideoView) findViewById(R.id.vv);
    }


    /**
     * 计算两点触控距离
     *
     * @param event
     * @return
     */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private SurfaceHolder.Callback mSurfaceCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (!isHasSurface) {
                isHasSurface = true;
                initCamera(holder);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isHasSurface = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //当界面重新可见的时候，看是否已经录制视频完毕了，完毕了就直接播放

        if (isHasSurface) {
            //如果不是第一次进入界面，只需要打开硬件即可，如果是第一进入，那么会在下面的callback里面进行初始化硬件
            initCamera(mSurfaceHolder);
        } else {
            //是第一次进入界面，进行第一次初始化，添加callback
            mSurfaceHolder.addCallback(mSurfaceCallBack);
        }
    }

    private void initCamera(SurfaceHolder holder) {
        try {
            CameraManager.getInstance().openDriver(holder, 0);
        } catch (Exception e) {
            Log.i(TAG, "设置相机失败");
            return;
        }
        CameraManager.getInstance().startPreview();
        CameraManager.getInstance().requestAutoFocus();
        mCamera = CameraManager.getInstance().getCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //当onPause的时候关闭预览界面并且关闭硬件
        CameraManager.getInstance().stopPreview();
        CameraManager.getInstance().closeDriver();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView = null;
    }

    /**
     * 开始录制/结束录制
     *
     * @param view
     */
    public void start(View view) {
        if (mCamera == null) {
            Toast.makeText(this, "初始化相机失败，无法录制", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mRecorder == null) {
            mCamera.unlock();
            mRecorder = new MediaRecorder();
            mRecorder.setCamera(mCamera);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            mRecorder.setProfile(cProfile);
            mRecorder.setVideoSize(640, 480);
            mRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);
            mRecorder.setOrientationHint(CameraManager.getInstance().isCamera0() ? 90 : 270);
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            mRecorder.setOutputFile("/storage/emulated/0/recordtest/cy.mp4");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
            Toast.makeText(this, "开始录制啦", Toast.LENGTH_SHORT).show();
            mStateTv.setText("结束录制");
        } else {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            Toast.makeText(this, "结束录制啦", Toast.LENGTH_SHORT).show();
            mStateTv.setText("开始录制");
            mStateTv.setVisibility(View.GONE);
            mCancelBt.setVisibility(View.VISIBLE);
            mSaveBt.setVisibility(View.VISIBLE);

            //结束录制的时候就不要预览了，然后点击撤销就继续预览，点击播放就播放视频
            CameraManager.getInstance().stopPreview();
            CameraManager.getInstance().closeDriver();

            //开始播放刚刚录制的视频
            mVideoView.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);
            mVideoView.setVideoURI(Uri.fromFile(new File("/storage/emulated/0/recordtest/cy.mp4")));
        }
    }

    /**
     * 撤销刚刚录制的视频
     *
     * @param view
     */
    public void cancel(View view) {
        //删除视频
        File file = new File("/storage/emulated/0/recordtest/cy.mp4");
        if (file.exists()) {
            file.delete();
        }
        Toast.makeText(this, "已撤销", Toast.LENGTH_SHORT).show();
        mStateTv.setVisibility(View.VISIBLE);
        mCancelBt.setVisibility(View.GONE);
        mSaveBt.setVisibility(View.GONE);
        mSurfaceView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.GONE);
        //点击撤销之后就继续显示预览效果
        initCamera(mSurfaceHolder);
    }

    /**
     * 点击返回按钮
     *
     * @param view
     */
    public void clickBack(View view) {
        finish();
    }

    /**
     * 保存
     *
     * @param view
     */
    public void save(View view) {
        //在这里做一些保存的事情或者是继续开启预览，让用户可以继续录制视频。
        finish();
    }

    /**
     * 定焦
     *
     * @param view
     */
    public void focus(View view) {
        CameraManager.getInstance().requestAutoFocus();
    }

    /**
     * 切换前后摄像头
     *
     * @param view
     */
    public void back(View view) {
        CameraManager.getInstance().changeCamera(mSurfaceHolder);
        mCamera = CameraManager.getInstance().getCamera();
    }

    /**
     * 开启/关闭闪光灯
     *
     * @param view
     */
    public void open(View view) {
        CameraManager.getInstance().setFlashLight(true);
    }

    /**
     * 开启/关闭闪光灯
     *
     * @param view
     */
    public void close(View view) {
        CameraManager.getInstance().setFlashLight(false);
    }
}
