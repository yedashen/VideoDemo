package tyj.com.videodemo.util.camera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import tyj.com.videodemo.R;

/**
 * @author ChenYe
 *         (1)当Activity onPause再onResume的时候播放的处理。无论是播放中、暂停中、播放完在onPause的的时候都是
 *         pause(播放完的时候掉这个方法会不会有问题，还有语音那个也是，接的测试一下)，但是当onResume的时候，
 *         点击播放按钮到底是继续播放还是怎么样需要详细思考再进行处理.
 *         注意:
 *         （1）我测试发现当你调用视频暂停的时候,VideoView.isPlaying()返回值是false;
 *         <p>
 *         需求:
 *         (1)能手动切换横竖屏;
 *         (2)最好是做一个目录实现和实现上下切换功能。
 */
public class CustomController extends FrameLayout {

    private VideoView mVideoView;
    private LinearLayout mTopLayout;
    private LinearLayout mBottomLayout;
    boolean isShow = true;
    private TextView mPlayIcon;
    private TextView mTotalTimeTv;
    private String mVideoPath;
    private TimerTask mTask;
    private Timer mTimer;
    private TextView mVideoNameTv;
    private Activity mActivity;
    private TextView mCurrentTimeTv;
    private SeekBar mSeekBar;
    private TextView mExitIv;
    private static final int ANIMATION_TIME_LENGTH = 1000;

    public CustomController(Context context) {
        this(context, null);
    }

    public CustomController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.controller_layout, this);
        mTopLayout = (LinearLayout) findViewById(R.id.top_layout);
        mBottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);
        mPlayIcon = (TextView) findViewById(R.id.play_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.controller_total_time);
        mVideoNameTv = (TextView) findViewById(R.id.controller_time_tv);
        mCurrentTimeTv = (TextView) findViewById(R.id.current_time);
        mSeekBar = (SeekBar) findViewById(R.id.position_sb);
        mExitIv = (TextView) findViewById(R.id.exit_tv);
        initClicks();
    }

    private void initClicks() {
        mPlayIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoView != null) {
                    if (mVideoView.isPlaying()) {
                        mVideoView.pause();
                        mPlayIcon.setText("开始");
                    } else {
                        mVideoView.start();
                        mPlayIcon.setText("暂停");
                    }
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVideoView != null) {
                    mVideoView.seekTo(seekBar.getProgress());
                }
            }
        });

        mExitIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }

    public String timeFormat(long timeLength) {
        return String.format("%02d:%02d", timeLength / 1000 / 60 % 60, timeLength / 1000 % 60);
//        return String.format("%02d:%02d:%02d", timeLength / 1000 / 60 / 60, timeLength / 1000 / 60 % 60, timeLength / 1000 % 60);
    }

    OnClickListener showControlLayoutListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isShow) {
                hideControl();
            } else {
                showControl();
            }
            isShow = !isShow;
        }
    };

    private void hideControl() {
        ObjectAnimator topAnimator = ObjectAnimator.ofFloat(mTopLayout, "translationY", 0, -mTopLayout.getHeight());
        topAnimator.setDuration(ANIMATION_TIME_LENGTH);
        ObjectAnimator bottomAnimator = ObjectAnimator.ofFloat(mBottomLayout, "translationY", 0, mBottomLayout.getHeight());
        bottomAnimator.setDuration(ANIMATION_TIME_LENGTH);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(topAnimator, bottomAnimator);
        animatorSet.start();
    }

    private void showControl() {
        ObjectAnimator topAnimator = ObjectAnimator.ofFloat(mTopLayout, "translationY", -mTopLayout.getHeight(), 0);
        topAnimator.setDuration(ANIMATION_TIME_LENGTH);
        ObjectAnimator bottomAnimator = ObjectAnimator.ofFloat(mBottomLayout, "translationY", mBottomLayout.getHeight(), 0);
        bottomAnimator.setDuration(ANIMATION_TIME_LENGTH);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(topAnimator, bottomAnimator);
        animatorSet.start();
    }

    public void setVideoView(VideoView videoView, String path,
                             String videoName, Activity activity) {
        this.mVideoView = videoView;
        if (mVideoView == null) {
            throw new RuntimeException("你TM设置一个空进来搞事情?");
        }
        this.mVideoPath = path;
        checkPath();
        mVideoNameTv.setText("视频名称: " + videoName);
        this.mActivity = activity;
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                cancelTask();
                mSeekBar.setMax(mVideoView.getDuration());
                mSeekBar.setProgress(mVideoView.getDuration());
                mCurrentTimeTv.setText(timeFormat(mVideoView.getDuration()));
                Log.e("CustomController", "播放完毕了");
            }
        });
    }

    public void start() {
        //在第一次可见的时候调用start，如果是onPause再onResume要调另一个方法
        mVideoView.setVideoURI(Uri.fromFile(new File(mVideoPath)));
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoView.start();
                int duration = mVideoView.getDuration();
                mTotalTimeTv.setText(timeFormat(duration));
                mPlayIcon.setText("暂停");
                beginTask();
            }
        });
    }

    private void beginTask() {
        cancelTask();
        mTask = new TimerTask() {
            @Override
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("CustomController", "跳了一下");
                        mCurrentTimeTv.setText(timeFormat(mVideoView.getCurrentPosition()));
                        mSeekBar.setMax(mVideoView.getDuration());
                        mSeekBar.setProgress(mVideoView.getCurrentPosition());
                    }
                });
            }
        };

        mTimer = new Timer();
        mTimer.schedule(mTask, 100, 500);
    }

    private void cancelTask() {
        if (mTimer != null) {
            mTask.cancel();
            mTimer.cancel();
            mTimer = null;
            mTask = null;
        }
    }

    /**
     * 界面onPause
     */
    public void onPause() {
        cancelTask();
    }

    public void onDestroy() {
        cancelTask();
        mActivity = null;
        mVideoView = null;
    }

    private boolean checkPath() {
        if (mVideoPath == null || mVideoPath.isEmpty() || mVideoPath.replaceAll(" ", "").equals("")) {
            throw new RuntimeException("草，你传进来的视频路径是空的，你在搞事情?");
        }
        return true;
    }
}
