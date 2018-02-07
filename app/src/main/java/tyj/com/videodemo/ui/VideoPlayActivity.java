package tyj.com.videodemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.VideoView;

import tyj.com.videodemo.R;
import tyj.com.videodemo.util.camera.CustomController;

/**
 * @author ChenYe
 *         created by on 2018/2/2 0002. 15:25
 *         暂时定义为竖屏播放.
 *         (1)未完成：切换上下、在播放界面显示所有视频列表并支持手动选择。
 **/

public class VideoPlayActivity extends Activity {

    private CustomController mCc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mCc = (CustomController) findViewById(R.id.cc);
        VideoView mVv = (VideoView) findViewById(R.id.vv);
        mCc.setVideoView(mVv, getIntent().getStringExtra("path"), "测试视频名称", this);
        mCc.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCc.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCc.onDestroy();
    }
}
