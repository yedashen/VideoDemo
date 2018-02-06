package tyj.com.videodemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.VideoView;

/**
 * @author ChenYe
 *         created by on 2018/2/2 0002. 15:25
 *         暂时定义为竖屏播放
 **/

public class VideoPlayActivity extends Activity {

    private String TEST_VIDEO_PATH = "/storage/emulated/0/recordtest/cy2.mp4";
    private CustomController mCc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mCc = (CustomController) findViewById(R.id.cc);
        VideoView mVv = (VideoView) findViewById(R.id.vv);
        mCc.setVideoView(mVv, TEST_VIDEO_PATH, "测试视频名称", this);
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
