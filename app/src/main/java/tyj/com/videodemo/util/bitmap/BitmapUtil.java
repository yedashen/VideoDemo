package tyj.com.videodemo.util.bitmap;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author ChenYe
 *         created by on 2018/2/9 0009. 09:17
 **/

public class BitmapUtil {

    /**
     * 把bitmap保存成文件
     *
     * @param bitmap 传进来的是要压缩的图片和要压缩的图片的名字
     */
    public static String saveBitmap(Bitmap bitmap, String path) {
        File file = createOutputMediaThumb(path);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("File", "保存缩略图出错啦" + e.getMessage());
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getPath();
    }

    /**
     * 创建视频缩略图文件夹
     * @param name
     * @return
     */
    public static File createOutputMediaThumb(String name) {
        //下面需要sd卡的读、写权限
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "视频缩略图");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("cameraUtil", "failed to create directory");
                return null;
            }
        }
        File mediaFile = new File(mediaStorageDir.getPath() +
                File.separator + "IMG_" + name + ".jpg");
        return mediaFile;
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
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
}
