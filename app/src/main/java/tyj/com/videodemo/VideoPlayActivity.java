package tyj.com.videodemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * @author ChenYe
 *         created by on 2018/2/2 0002. 15:25
 *         暂时定义为竖屏播放
 **/

public class VideoPlayActivity extends Activity {

    private String TEST_VIDEO_PATH = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
    }
}
