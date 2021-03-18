package com.vison.opengl_learning.en;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/5/21 10:50
 * @Description: 解码MP3
 *
 * MediaCodec处理输入数据然后产生输出数据，它异步处理数据，使用了一组输入和输入缓存。
 * 你请求到了一个空的输入缓存，将数据填满后，发送给编解码器进行处理。编解码器处理完后，
 * 将处理的结果输出到一个空的输出缓存中，我们能请求到这个输出缓存，并将缓存的数据使用。
 */
public class DecodeMp3Thread extends Thread {

    private final String TAG = "DecodeMp3Thread";
    protected static final int TIMEOUT_USEC = 10000;    // 10[msec]
    private String mPath;
    private String mOutPath;


    public DecodeMp3Thread(String path, String outpath) {
        this.mPath = path;
        this.mOutPath = outpath;
    }

    @Override
    public void run() {
        super.run();
        MediaExtractor extractor = new MediaExtractor();
        int audioTrack = -1;
        boolean hasAudioTrack = false;
        try {
            extractor.setDataSource(mPath);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {
                    audioTrack = i;
                    hasAudioTrack = true;
                    Log.d(TAG, "音频格式：" + format.toString());
                }
            }
            Log.d(TAG, "音轨：" + audioTrack + "/" + hasAudioTrack);
            if (hasAudioTrack) {
                extractor.selectTrack(audioTrack);
                MediaFormat format = extractor.getTrackFormat(audioTrack);
                String mime = format.getString(MediaFormat.KEY_MIME);
                MediaCodec mediaCodec = MediaCodec.createDecoderByType(mime);
                mediaCodec.configure(format, null, null, 0);
                mediaCodec.start();
                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                //用于描述解码得到的byte[]数据的相关信息
                MediaCodec.BufferInfo decodeBufferInfo = new MediaCodec.BufferInfo();

                FileOutputStream fos = new FileOutputStream(new File(mOutPath));
                byte[] chunkPCM;
                boolean isCodeOver=true;
                while (isCodeOver) {
                    //从输入流队列中取数据进行编码操作
                    int inputIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputIndex >= 0) {
                        //取出输入流buffer
                        ByteBuffer inputBuffer = inputBuffers[inputIndex];
                        //将MediaExtractor读取数据到输入流Buffer
                        int sampleSize = extractor.readSampleData(inputBuffer, 0);
                        //小于0 代表所有数据已读取完成
                        if (sampleSize < 0) {
                            //读完所有数据，将状态设置END OF STREAM
                            Log.d(TAG, "END OF STREAM");
                            mediaCodec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            Log.d(TAG, "往解码器写入数据，当前时间戳：" + extractor.getSampleTime());
                            mediaCodec.queueInputBuffer(inputIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }

                    int outputIndex = mediaCodec.dequeueOutputBuffer(decodeBufferInfo, TIMEOUT_USEC);
                    if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        //没有可用的解码器
                        Log.d(TAG, "没有可用的解码器：");
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = mediaCodec.getOutputBuffers();
                    }else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){

                    }else {
                        ByteBuffer outputBuffer = outputBuffers[outputIndex];
                        chunkPCM = new byte[decodeBufferInfo.size];
                        outputBuffer.get(chunkPCM);
                        outputBuffer.clear();
                        fos.write(chunkPCM);
                        fos.flush();
                        Log.d(TAG, "释放输出流缓冲区：" + outputIndex);
                        mediaCodec.releaseOutputBuffer(outputIndex, false);
                    }


                    if ((decodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {//编解码结束
                        fos.close();
                        extractor.release();
                        mediaCodec.stop();
                        mediaCodec.release();
                        isCodeOver=false;
                        Log.d(TAG, "结束：" + isCodeOver);
                    }
                }



            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
