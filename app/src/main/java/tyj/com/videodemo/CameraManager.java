package tyj.com.videodemo;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class CameraManager {
    private static final String TAG = "CameraManager";
    private Context mContext = null;
    private Camera mCamera;
    private boolean initialized;
    private CameraConfigurationManager cameraConfigurationManager;
    private boolean previewing = false;
    private int mCameraFacing = 0;

    private CameraManager() {
    }

    private void init() {
        mCameraFacing = 0;
        cameraConfigurationManager = CameraConfigurationManager.getInstance().setContext(mContext);
    }

    private static class Holder {
        private static final CameraManager CAMERA_MANAGER = new CameraManager();
    }

    public static CameraManager getInstance() {
        return Holder.CAMERA_MANAGER;
    }

    public void initContext(Context context) {
        mContext = context;
        init();
    }

    public Camera getCamera() {
        return mCamera;
    }

    /**
     * 打开相机硬件驱动程序
     *
     * @param holder
     * @param cameraId 默认传入0，就是打开后置摄像头
     *                 0为后置,1为前置
     */
    public void openDriver(SurfaceHolder holder, int cameraId) {
        if (mCamera == null) {
            //之所以判断非空，是因为有可能界面onPause然后onResume的时候没必要重复赋值
            mCamera = Camera.open(cameraId);
            if (mCamera == null) {
                try {
                    throw new IOException("打开摄像头失败");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "设置previewDisplay失败");
            }

            if (!initialized) {
                initialized = true;
                cameraConfigurationManager.getCameraParams(mCamera);
            }
            cameraConfigurationManager.setDseiredCameraParameters(mCamera);
        }
    }

    /**
     * 控制闪光灯的，在界面关闭的时候强制关闭
     *
     * @param forceClose 是否强制关闭,true代表强制关闭
     */
    public void setFlashLight(boolean forceClose) {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                if (forceClose) {
                    parameters.set("flash-mode", "off");
                } else {
                    String flashMode = parameters.getFlashMode();
                    if (flashMode.equals("off")) {
                        //说明原本就是关闭的，那么现在开启
                        parameters.set("flash-mode", "torch");
                    } else if (flashMode.equals("torch")) {
                        parameters.set("flash-mode", "off");
                    } else {
                        Toast.makeText(mContext, "修改闪光灯状态失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                mCamera.setParameters(parameters);
            } catch (Exception e) {
                Log.e("Camera", e.getMessage());
            }
        }
    }

    public void releaseCamera() {
        stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * 关闭相机硬件驱动
     */
    public void closeDriver() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 开始设置预览界面到手机屏幕上
     */
    public void startPreview() {
        if (mCamera != null && !previewing) {
            mCamera.startPreview();
            previewing = true;
        }
    }

    public void stopPreview() {
        if (mCamera != null && previewing) {
            setFlashLight(true);
            mCamera.stopPreview();
            previewing = false;
        }
    }

    public void requestAutoFocus() {
        if (mCamera != null && previewing) {
            mCamera.autoFocus(null);
        }
    }

    /**
     * 摄像机是否支持前置拍照
     *
     * @return
     */
    public boolean isSupportFrontCamera() {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 缩放
     *
     * @param isZoomIn
     */
    public void handleZoom(boolean isZoomIn) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        if (params == null) {
            return;
        }
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            mCamera.setParameters(params);
        } else {
            Log.e("CameraManager", "不支持缩放");
        }
    }

    /**
     * 局部对焦
     *
     * @param x
     * @param y
     */
    public void handleFocusMetering(float x, float y) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size previewSize = params.getPreviewSize();
        Rect focusRect = calculateTapArea(x, y, 1f, previewSize);
        Rect meteringRect = calculateTapArea(x, y, 1.5f, previewSize);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 1000));
            params.setFocusAreas(focusAreas);
        } else {
            Log.e("TAG", "focus areas not supported");
        }
        if (params.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> meteringAreas = new ArrayList<>();
            meteringAreas.add(new Camera.Area(meteringRect, 1000));
            params.setMeteringAreas(meteringAreas);
        } else {
            Log.e("TAG", "metering areas not supported");
        }
        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        try {
            mCamera.setParameters(params);
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }

    private Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / previewSize.width - 1000);
        int centerY = (int) (y / previewSize.height - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    /**
     * 切换摄像头,默认是后置摄像头打开的
     *
     * @param surfaceHolder
     */
    public void changeCamera(SurfaceHolder surfaceHolder) {
        if (isSupportFrontCamera()) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            //得到摄像头的个数
            int cameraCount = Camera.getNumberOfCameras();
            for (int i = 0; i < cameraCount; i++) {
                //得到每一个摄像头的信息
                Camera.getCameraInfo(i, cameraInfo);
                if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    //现在是后置，变更为前置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        //代表摄像头的方位为前置
                        releaseCamera();
                        try {
                            openDriver(surfaceHolder, Camera.CameraInfo.CAMERA_FACING_BACK);
                            startPreview();
                        } catch (Exception e) {
                            Log.e("TAG", e.getMessage());
                        }
                        mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
                        break;
                    }
                } else {
                    //现在是前置， 变更为后置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        //代表摄像头的方位
                        releaseCamera();
                        try {
                            openDriver(surfaceHolder, Camera.CameraInfo.CAMERA_FACING_FRONT);
                            startPreview();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        mCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        break;
                    }
                }
            }
        } else { //不支持摄像机
            Toast.makeText(mContext, "您的手机不支持前置摄像", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 返回当前是那个摄像头在摄像，camera0就是后置摄像头
     * 后置摄像头拍照就旋转90度来调整，前置摄像头就旋转270度来跳转为竖屏录制
     *
     * @return
     */
    public boolean isCamera0() {
        return mCameraFacing == 0;
    }
}
