package tyj.com.videodemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import tyj.com.videodemo.R;
import tyj.com.videodemo.model.VideoEntity;

/**
 * @author ChenYe
 *         created by on 2018/2/6 0006. 15:12
 **/

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoViewHolder> {

    private List<VideoEntity> mVideoList = null;
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private OnItemClickListener mItemClickListener = null;

    public VideoListAdapter(Context context, List<VideoEntity> entities) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mVideoList = entities;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(mInflater.inflate(R.layout.item_video_list, null));
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        holder.updateView(position);
    }

    @Override
    public int getItemCount() {
        return mVideoList == null ? 0 : mVideoList.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout mContentLayout;
        private ImageView mThumbIv;
        private TextView mNameTv;
        private TextView mLengthTv;
        private TextView mDeleteTv;

        public VideoViewHolder(View itemView) {
            super(itemView);
            mContentLayout = (LinearLayout) itemView.findViewById(R.id.content_layout);
            mThumbIv = (ImageView) itemView.findViewById(R.id.thumb_iv);
            mNameTv = (TextView) itemView.findViewById(R.id.name_tv);
            mLengthTv = (TextView) itemView.findViewById(R.id.length_tv);
            mDeleteTv = (TextView) itemView.findViewById(R.id.delete_tv);
        }

        public void updateView(final int position) {
            final VideoEntity entity = mVideoList.get(position);
            mNameTv.setText("视频名称:" + entity.getVideoName());
            mLengthTv.setText("视频长度:" + entity.getVideoLength());
            Glide.with(mContext).load(entity.getVideoThumbPath())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(mThumbIv);
            mDeleteTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = new File(entity.getVideoPath());
                    if (file.exists()) {
                        file.delete();
                    } else {
                        Toast.makeText(mContext, "改文件原本就不存在", Toast.LENGTH_SHORT).show();
                    }
                    //刷新列表
                    mVideoList.remove(position);
                    notifyItemRangeRemoved(position, 1);
                }
            });
            mContentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = new File(entity.getVideoPath());
                    if (file.exists()) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onItemClick(position);
                        }
                    } else {
                        Toast.makeText(mContext, "这个视频不存在，你是不是偷偷删了", Toast.LENGTH_SHORT).show();
                        //刷新列表
                        mVideoList.remove(position);
                        notifyItemRangeRemoved(position, 1);
                    }
                }
            });
        }

        private String convert(long length) {
            return String.format("%02d:%02d", length / 1000 / 60 % 60, length / 1000 % 60);
        }
    }


    public interface OnItemClickListener {

        /**
         * itemClick
         *
         * @param position
         */
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public VideoEntity getItem(int position) {
        if (position > mVideoList.size()) {
            throw new IndexOutOfBoundsException("草，你故意的?");
        }
        return mVideoList.get(position);
    }


    public void addLast(VideoEntity entity) {
        mVideoList.add(entity);
    }

    public void setList(List<VideoEntity> videos) {
        this.mVideoList = videos;
        notifyDataSetChanged();
    }

    public void release() {
        mItemClickListener = null;
        mContext = null;
        mVideoList = null;
    }
}
