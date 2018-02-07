package tyj.com.videodemo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tyj.com.videodemo.R;
import tyj.com.videodemo.adapter.VideoListAdapter;
import tyj.com.videodemo.model.VideoEntity;

/**
 * @author ChenYe
 *         注意：
 *         （1）录制的过程中，不允许切换摄像头，以为我这里的切换摄像头是重新初始化了camera对象，那么MediaRecord里面的还是之前的camera
 *         对象，会造成录制出来的视频内容有黑屏现象甚至崩溃。
 *         （2）本demo是用原生的代码写的，没有用ffmpeg等框架，所以要是把我这个demo看熟了，有什么问题好商量。
 *         （3）我没写权限申请，所以记得在一开始运行的时候就给予全部权限。
 *         （4）这个视频列表是从特定的文件夹里面去读的，而不是去读取整个手机的内部存储和外部存储的。
 *         功能：
 *         本demo支持在录屏的时候缩放，局部定焦，开启闪光灯等功能。
 *         <p>
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private VideoListAdapter mAdapter;
    private List<VideoEntity> mVideos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.rcv);
        initRecyclerView();
        findViewById(R.id.add_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, TakeVideoActivity.class), 20);
            }
        });
        readVideoList();
    }

    /**
     * 在异步线程里面去读指定文件夹(/storage/emulated/0/recordtest/)的视频列表
     */
    private void readVideoList() {
        File file = new File("/storage/emulated/0/recordtest");
        if (file.exists()) {
            for (File file1 : file.listFiles()) {
                mVideos.add(new VideoEntity(file1.getPath(), "", file1.getName()));
            }
            mAdapter.setList(mVideos);
        }
    }

    /**
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                           int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        System.out.println("w" + bitmap.getWidth());
        System.out.println("h" + bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new VideoListAdapter(this, mVideos);
        mAdapter.setOnItemClickListener(new VideoListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, VideoPlayActivity.class);
                intent.putExtra("path", mAdapter.getItem(position).getVideoPath());
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 20 && data != null) {
            //拿出刚刚添加的那条数据，增加到recyclerView里面去
            Log.e("MainActivity", "onActivityResult()");
            String path = data.getStringExtra("video");
            File file = new File(path);
            VideoEntity entity = new VideoEntity(file.getPath(), "", file.getName());
            mAdapter.addLast(entity);
            mAdapter.notifyItemChanged(mAdapter.getItemCount());
        }
    }
}
