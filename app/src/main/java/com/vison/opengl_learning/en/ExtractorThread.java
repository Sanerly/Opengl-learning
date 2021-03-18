package com.vison.opengl_learning.en;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/5/18 19:05
 * @Description: 类描述
 */
public class ExtractorThread extends Thread {

    private String mPath;
    private String mOutPath;

    public ExtractorThread(String path,String outpath) {
        this.mPath = path;
        this.mOutPath=outpath;
    }

    @Override
    public void run() {
        super.run();
        MediaExtractor extractor = new MediaExtractor();
        MediaMuxer muxer=null;
        try {
            extractor.setDataSource(mPath);
            Log.d("EncoderActivity", "Path:" + mPath);
            int mVideoTrackIndex = -1;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (!mime.startsWith("video")) {
                    Log.d("EncoderActivity","mime not video, continue search");
                    continue;
                }
                extractor.selectTrack(i);
                muxer = new MediaMuxer(mOutPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                mVideoTrackIndex = muxer.addTrack(format);
                muxer.start();
            }



            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024 * 2);
            while (true) {
                int sampleSize = extractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }
                info.offset=0;
                info.flags=extractor.getSampleFlags();
                info.size=sampleSize;
                info.presentationTimeUs = extractor.getSampleTime();
                boolean keyframe = (info.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) > 0;
                Log.d("EncoderActivity","write sample " + keyframe + ", " + sampleSize + ", " + info.presentationTimeUs);
                muxer.writeSampleData(mVideoTrackIndex,buffer,info);
                extractor.advance();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        extractor.release();
        muxer.stop();
        muxer.release();

        Log.d("EncoderActivity","process success !");
    }
}
