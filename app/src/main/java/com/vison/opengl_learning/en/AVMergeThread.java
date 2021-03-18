package com.vison.opengl_learning.en;

import android.app.LoaderManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/5/22 17:38
 * @Description: 合并音频和视频
 */
public class AVMergeThread extends Thread {
    private String mMp4path = "";
    private String mMp3path = "";
    private String mOutMp4path = "";

    public AVMergeThread(String mMp4path, String mMp3path, String mOutMp4path) {
        this.mMp4path = mMp4path;
        this.mMp3path = mMp3path;
        this.mOutMp4path = mOutMp4path;
    }

    @Override
    public void run() {
        super.run();
        try {
            MediaMuxer muxer = new MediaMuxer(mOutMp4path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            //视频音轨
            int videoTrackIndex = -1;
            long vDuration = 0;
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(mMp4path);
            for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
                MediaFormat vFormat = videoExtractor.getTrackFormat(i);
                String mime = vFormat.getString(MediaFormat.KEY_MIME);
                vDuration = vFormat.getLong(MediaFormat.KEY_DURATION);
                if (mime.startsWith("video")) {
                    videoExtractor.selectTrack(i);
                    videoTrackIndex = muxer.addTrack(vFormat);
                }
            }

            Log.d("AVMergeThread", "视频时长"  + vDuration);

            //音频音轨
            int audioTrackIndex = -1;
            long aDuration = 0;
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(mMp3path);
            for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                MediaFormat aFormat = audioExtractor.getTrackFormat(i);
                String mime = aFormat.getString(MediaFormat.KEY_MIME);
                aDuration = aFormat.getLong(MediaFormat.KEY_DURATION);
                Log.d("AVMergeThread", "音频: "  + mime+" MediaFormat: "+aFormat.toString());
                if (mime.startsWith("audio")) {
                    audioExtractor.selectTrack(i);
                    audioTrackIndex = muxer.addTrack(aFormat);
                }
            }
            Log.d("AVMergeThread", "音频时长"  + aDuration);
            //添加完音频和视频，开始合并
            muxer.start();

            //填充视频数据
            if (videoTrackIndex >= 0) {
                MediaCodec.BufferInfo vInfo = new MediaCodec.BufferInfo();
                vInfo.presentationTimeUs = 0;
                ByteBuffer vBuffer = ByteBuffer.allocate(1024 * 1024 * 2);
                while (true) {
                    int sampleSize = videoExtractor.readSampleData(vBuffer, 0);
                    if (sampleSize < 0) {
                        break;
                    } else {

                        if (videoExtractor.getSampleTime()<1000*1000*60*2){
                            Log.d("AVMergeThread", "video write sample "  + sampleSize + ", " + videoExtractor.getSampleTime());
                            videoExtractor.advance();
                            continue;
                        }
                        if (videoExtractor.getSampleTime()>vDuration-(1000*1000*60*1)){
                            videoExtractor.advance();
                            break;
                        }
                        vInfo.offset = 0;
                        vInfo.size = sampleSize;
                        vInfo.flags = videoExtractor.getSampleFlags();
                        vInfo.presentationTimeUs = videoExtractor.getSampleTime();
                        boolean keyframe = (vInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) > 0;
                        Log.d("AVMergeThread", "video write sample " + keyframe + ", " + sampleSize + ", " + vInfo.presentationTimeUs);
                        muxer.writeSampleData(videoTrackIndex, vBuffer, vInfo);
                        videoExtractor.advance();
                    }
                }
            }

            //填充音频数据
            if (audioTrackIndex >= 0) {
                MediaCodec.BufferInfo aInfo = new MediaCodec.BufferInfo();
                aInfo.presentationTimeUs = 0;
                ByteBuffer aBuffer = ByteBuffer.allocate(1024 * 1024 * 2);
                while (true) {
                    int sampleSize = audioExtractor.readSampleData(aBuffer, 0);
                    if (sampleSize < 0) {
                        break;
                    } else {
                        if (audioExtractor.getSampleTime()<1000*1000*60*2){
                            Log.d("AVMergeThread", "audio write sample "  + sampleSize + ", " + audioExtractor.getSampleTime());
                            audioExtractor.advance();
                            continue;
                        }
                        if (audioExtractor.getSampleTime()>aDuration-(1000*1000*60*1)){
                            audioExtractor.advance();
                            break;
                        }
                        aInfo.offset = 0;
                        aInfo.size = sampleSize;
                        aInfo.flags = audioExtractor.getSampleFlags();
                        aInfo.presentationTimeUs = audioExtractor.getSampleTime();
                        boolean keyframe = (aInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) > 0;

                        Log.d("AVMergeThread", "audio write sample " + keyframe + ", " + sampleSize + ", " + aInfo.presentationTimeUs);
                        muxer.writeSampleData(audioTrackIndex, aBuffer, aInfo);
                        audioExtractor.advance();
                    }
                }
            }
            Log.d("AVMergeThread", "success");
            videoExtractor.release();
            audioExtractor.release();

            muxer.stop();
            muxer.release();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
