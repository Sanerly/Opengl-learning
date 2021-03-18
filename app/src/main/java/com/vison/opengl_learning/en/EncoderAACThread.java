package com.vison.opengl_learning.en;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/5/21 17:15
 * @Description: PCM编译成ACC 解码
 */
public class EncoderAACThread extends Thread {

    private final String TAG = "EncoderAACThread";
    protected static final int TIMEOUT_USEC = 10000;    // 10[msec]
    private String mPath;
    private String mOutPath;
    private final String mime = "audio/mp4a-latm";


    public EncoderAACThread(String path, String outpath) {
        this.mPath = path;
        this.mOutPath = outpath;
    }

    @Override
    public void run() {
        super.run();
        try {
            FileInputStream inputStream = new FileInputStream(new File(mPath));
            FileOutputStream outputStream = new FileOutputStream(new File(mOutPath));
            byte[] pcm = new byte[1024*4];
            int len = 0;
            MediaCodec mediaCodec = MediaCodec.createEncoderByType(mime);
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME, mime);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024*4);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);

            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();


            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            MediaCodec.BufferInfo outInfo = new MediaCodec.BufferInfo();

            boolean isEncodec = true;
            while (isEncodec) {
                int inputIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputIndex>=0){
                    if (len < 0) {
                        mediaCodec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        len = inputStream.read(pcm);
                        ByteBuffer inputBuffer = inputBuffers[inputIndex];
                        inputBuffer.clear();
                        inputBuffer.put(pcm);
                        inputBuffer.limit(pcm.length);
                        Log.d(TAG, "输入缓冲数据：" + pcm.length);
                        mediaCodec.queueInputBuffer(inputIndex, 0, pcm.length, getPTSUs(), 0);

                    }
                }

                int outputIndex = mediaCodec.dequeueOutputBuffer(outInfo, 0);
                if (outputIndex >=0) {
                    //获取缓存信息的长度
                    int byteBufSize = outInfo.size;
                    //添加ADTS头部后的长度
                    int bytePacketSize = byteBufSize + 7;

                    ByteBuffer buffer = outputBuffers[outputIndex];
                    buffer.position(outInfo.offset);
                    buffer.limit(outInfo.offset + outInfo.size);

                    byte[] targetByte = new byte[bytePacketSize];
                    addADTStoPacket(targetByte, bytePacketSize);

                    buffer.get(targetByte, 7, byteBufSize);
                    buffer.position(outInfo.offset);

                    Log.d(TAG, "输出缓冲数据：" +byteBufSize + "   =   " + targetByte.length);

                    outputStream.write(targetByte);
                    outputStream.flush();

                    mediaCodec.releaseOutputBuffer(outputIndex, false);


                    if ((outInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {//编解码结束
                        outputStream.close();
                        inputStream.close();
                        mediaCodec.stop();
                        mediaCodec.release();
                        isEncodec=false;
                        Log.d(TAG, "结束：" + isEncodec);
                    }
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    outputBuffers = mediaCodec.getOutputBuffers();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 给编码出的aac裸流添加adts头字段
     *
     * @param packet    要空出前7个字节，否则会搞乱数据
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2;  //AAC LC
        int freqIdx = 4;  //44.1KHz
        int chanCfg = 2;  //CPE
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    /**
     * get next encoding presentationTimeUs
     * 获取下一个编码的显示时间，这里需要减去暂停的时间
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime();
        return result / 1000L;
    }

}
