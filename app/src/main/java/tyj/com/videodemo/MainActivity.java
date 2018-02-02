package tyj.com.videodemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * @author ChenYe
 *         注意：
 *         （1）录制的过程中，不允许切换摄像头，以为我这里的切换摄像头是重新初始化了camera对象，那么MediaRecord里面的还是之前的camera
 *         对象，会造成录制出来的视频内容有黑屏现象甚至崩溃。
 *         （2）本demo是用原生的代码写的，没有用ffmpeg等框架，所以要是把我这个demo看熟了，有什么问题好商量。
 *         （3）我没写权限申请，所以记得在一开始运行的时候就给予全部权限。
 *         功能：
 *         本demo支持在录屏的时候缩放，局部定焦，开启闪光灯等功能。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goTakeVideo(View view) {
        //看我activity的备注，记得给权限。。。
        startActivity(new Intent(this,TakeVideoActivity.class));
    }
}
