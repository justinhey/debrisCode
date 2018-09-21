package xdrj.com.module.av;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.io.IOException;
import java.nio.ByteBuffer;

import xdrj.com.debris.BaseActivity;
import xdrj.com.module.R;

/**
 * @description:  使用 MediaExtractor 和 MediaMuxer API 解析和封装 mp4 文件
 * @author:  hwh
 * @date:  2018/9/21 17:20
 */
@Route(path = "/test/mp4_resolve")
public class Mp4ResolveActivity extends BaseActivity {

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private MediaExtractor mMediaExtractor;
    private MediaMuxer mMediaMuxer;
    private ProgressBar mProgressBar;
    private volatile boolean isProcessing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_mp4_resolve);

        mProgressBar = findViewById(R.id.profressbar);
        findViewById(R.id.tv_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (!isProcessing){
                    gogogo();
                }
            }
        });
    }

    private void gogogo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isProcessing = true;
                    process();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isProcessing = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean process() throws IOException {
        // MediaExtractor的作用是把音频和视频的数据进行分离。
        // setDataSource(String path)：即可以设置本地文件又可以设置网络文件
        // getTrackCount()：得到源文件通道数
        // getTrackFormat(int index)：获取指定（index）的通道格式
        // getSampleTime()：返回当前的时间戳
        // readSampleData(ByteBuffer byteBuf, int offset)：把指定通道中的数据按偏移量读取到ByteBuffer中；
        // advance()：读取下一帧数据
        // release(): 读取结束后释放资源

        // MediaMuxer的作用是生成音频或视频文件；还可以把音频与视频混合成一个音视频文件。

        mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(SDCARD_PATH + "/ss.mp4");

        int mVideoTrackIndex = -1;
        int framerate = 0;
        int trackCount = mMediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            // 添加通道；我们更多的是使用MediaCodec.getOutpurForma()或Extractor.getTrackFormat(int index)来获取MediaFormat;也可以自己创建；
            MediaFormat format = mMediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (!mime.startsWith("video/")) {
                continue;
            }
            framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            mMediaExtractor.selectTrack(i);
            mMediaMuxer = new MediaMuxer(SDCARD_PATH + "/out.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mVideoTrackIndex = mMediaMuxer.addTrack(format);
            mMediaMuxer.start();
        }

        if (mMediaMuxer == null) {
            return false;
        }

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        info.presentationTimeUs = 0;
        ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
        int sampleSize = 0;
        while ((sampleSize = mMediaExtractor.readSampleData(buffer,0))>0){
            info.offset = 0;
            info.size = sampleSize;
            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            info.presentationTimeUs += 1000 * 1000 / framerate;
            // 把ByteBuffer中的数据写入到在构造器设置的文件中
            mMediaMuxer.writeSampleData(mVideoTrackIndex, buffer, info);
            mMediaExtractor.advance();
        }

        mMediaExtractor.release();
        mMediaMuxer.stop();
        mMediaMuxer.release();

        return true;
    }

    private void checkPermission() {
        // 获取权限
        int checkWriteExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int checkReadExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkWriteExternalPermission != PackageManager.PERMISSION_GRANTED ||
                checkReadExternalPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }
}
