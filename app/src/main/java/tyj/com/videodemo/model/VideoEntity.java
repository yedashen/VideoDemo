package tyj.com.videodemo.model;

import java.io.Serializable;

/**
 * @author ChenYe
 *         created by on 2018/2/6 0006. 15:13
 **/

public class VideoEntity implements Serializable {
    private String videoPath;
    private String videoLength;
    private String videoName;
    //视频缩略图地址
    private String videoThumbPath;

    public VideoEntity() {
    }

    public VideoEntity(String videoPath, String videoLength,
                       String videoName, String videoThumbPath) {
        this.videoPath = videoPath;
        this.videoLength = videoLength;
        this.videoName = videoName;
        this.videoThumbPath = videoThumbPath;
    }

    public String getVideoThumbPath() {
        return videoThumbPath;
    }

    public void setVideoThumbPath(String videoThumbPath) {
        this.videoThumbPath = videoThumbPath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(String videoLength) {
        this.videoLength = videoLength;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    @Override
    public String toString() {
        return "VideoEntity{" +
                "videoPath='" + videoPath + '\'' +
                ", videoLength='" + videoLength + '\'' +
                ", videoName='" + videoName + '\'' +
                '}' + "\n";
    }
}
