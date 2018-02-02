package tyj.com.videodemo;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author ChenYe
 */

public class CameraConfigurationManager {
    private static final String TAG = "Cy==CameraConfigManager";
    private Context mContext;
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    /**
     * 设置十级缩放级别 27
     */
    private static final int TEN_DESIRED_ZOOM = 20;
    private int previewFormat;
    private String previewFormatString;
    private Point screenResolution;
    private Point cameraResolution;

    private CameraConfigurationManager() {
    }

    public int getPreviewFormat() {
        return previewFormat;
    }

    public String getPreviewFormatString() {
        return previewFormatString;
    }

    private static class Holder {
        private static final CameraConfigurationManager CAMERA_CONFIGURATION_MANAGER = new CameraConfigurationManager();
    }

    public static CameraConfigurationManager getInstance() {
        return Holder.CAMERA_CONFIGURATION_MANAGER;
    }

    public CameraConfigurationManager setContext(Context mContext) {
        this.mContext = mContext;
        return this;
    }

    /**
     * 获取手机的相机和手机屏幕的一些基本参数
     *
     * @param camera
     */
    public void getCameraParams(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        //获取相机预览界面格式
        previewFormat = parameters.getPreviewFormat();
        previewFormatString = parameters.get("preview-format");
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        //获取屏幕的宽高（像素值）设置到一个实体(point)里面
        screenResolution = new Point(display.getWidth(), display.getHeight());
        //尽量获取相机最好的point
        cameraResolution = getCameraResolution(parameters, screenResolution);
    }

    /*设置相机的预览参数（渴望设置成什么样就设置成什么样）*/
    public void setDseiredCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        //设置相机的预览size
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        setFlash(parameters);
        setZoom(parameters);
        setDisplayOrientation(camera, 90);
        camera.setParameters(parameters);
    }

    /**
     * 设置展示角度
     *
     * @param camera
     * @param orientation
     */
    private void setDisplayOrientation(Camera camera, int orientation) {
        Class<? extends Camera> clazz = camera.getClass();
        try {
            Method displayOrientation = clazz.getMethod("setDisplayOrientation", new Class[]{int.class});
            if (displayOrientation != null) {
                displayOrientation.invoke(camera, new Object[]{orientation});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置缩放
     *
     * @param parameters
     */
    private void setZoom(Camera.Parameters parameters) {
        String zoomSupportString = parameters.get("zoom-supported");
        if (zoomSupportString != null && !Boolean.parseBoolean(zoomSupportString)) {
            return;
        }

        int tenDesiredZoom = TEN_DESIRED_ZOOM;

        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null) {
            try {
                int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException e) {
                Log.i(TAG, "获取原本的缩放最大级别转换失败");
            }

            String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
            if (takingPictureZoomMaxString != null) {
                try {
                    int takePicMax = Integer.parseInt(takingPictureZoomMaxString);
                    if (tenDesiredZoom > takePicMax) {
                        tenDesiredZoom = takePicMax;
                    }
                    Log.i(TAG, "拍照缩放级别被最原始级别小");
                } catch (NumberFormatException nfe) {
                    Log.i(TAG, "拍照缩放级别转换失败");
                }
            }

            String motZoomMaxString = parameters.get("mot-zoom-values");
            if (motZoomMaxString != null) {
                tenDesiredZoom = findBestMotZoomValue(motZoomMaxString, tenDesiredZoom);
            }

            String motZoomStepString = parameters.get("mot-zoom-step");
            if (motZoomStepString != null) {
                try {
                    double motZoomStep = Double.parseDouble(motZoomStepString.trim());
                    int tenZoomStep = (int) (10.0 * motZoomStep);
                    if (tenZoomStep > 1) {
                        tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                    }
                } catch (NumberFormatException nfe) {

                }
            }

            if (maxZoomString != null || motZoomMaxString != null) {
                parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
            }

            if (takingPictureZoomMaxString != null) {
                parameters.set("taking-picture-zoom", tenDesiredZoom);
            }
        }
    }

    private static int findBestMotZoomValue(CharSequence stringValues, int tenDesireZoom) {
        int tenBestValue = 0;
        for (String stringValue : COMMA_PATTERN.split(stringValues)) {
            stringValue = stringValue.trim();
            double value;
            try {
                value = Double.parseDouble(stringValue);
            } catch (NumberFormatException nfe) {
                return tenDesireZoom;
            }

            int tenValue = (int) (10.0 * value);
            if (Math.abs(tenDesireZoom - value) < Math.abs(tenDesireZoom - tenBestValue)) {
                tenBestValue = tenValue;
            }
        }
        return tenBestValue;
    }

    /**
     * 设置闪光灯，本来是不用设置的，应该是直接获取，但是根据三星建议说防止黑客攻击，最好设置一下
     *
     * @param parameters
     */
    private void setFlash(Camera.Parameters parameters) {
        if (Build.MODEL.contains("Behold II")) {
            parameters.set("flash-value", 1);
        } else {
            parameters.set("flash-value", 2);
        }
        parameters.set("flash-mode", "off");
    }

    /**
     * 根据相机的参数和手机屏幕参数来重新设置相机的
     * @param parameters
     * @param screenResolution
     * @return
     */
    public Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {
        String previewSizeValueString = parameters.get("preview-size-values");
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        Point cameraResolution = null;

        if (previewSizeValueString != null) {
            cameraResolution = findBestPreviewSizeValue(previewSizeValueString, screenResolution);
        }

        if (cameraResolution == null) {
            //没有找到最优的预览效果,那么至少也要确保相机分辨率是8的倍数(我也不知道为什么)，下面的操作就是防备屏幕不是8的倍数，如果
            //是8的倍数，就会在上面的寻找最优size的时候会找到的
            cameraResolution = new Point(
                    (screenResolution.x >> 3) << 3,
                    (screenResolution.y >> 3) << 3);
        }
        return cameraResolution;
    }

    /*根据预览大小和手机屏幕大小来设一个比较完美的预览效果*/
    public Point findBestPreviewSizeValue(CharSequence previewSizeValueString, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;//差值
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                //这是一个很差的预览大小
                continue;
            }

            int newX;
            int newY;
            try {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                //字符串转换异常，这是自己写的tryCatch，是为了安全起见
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x) + Math.abs(newY - screenResolution.y);

            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }
        }

        if (bestY > 0 && bestX > 0) {
            return new Point(bestX, bestY);
        }

        return null;
    }
}
