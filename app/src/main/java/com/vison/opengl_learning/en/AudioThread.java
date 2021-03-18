package com.vison.opengl_learning.en;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/5/13 18:05
 * @Description: 类描述
 */
public class AudioThread extends Thread {

    private AudioRecord mAudioRecord;
    private int recordBufSize = 0; // 声明recoordBufffer的大小字段
    private int RateInHz = 44100;//44.1HZ
    private boolean mRecoding = false;
    private String string;
    private File mFile;
    private final PcmToWavUtil mPcmToWavUtil;

    AudioThread(){
        mPcmToWavUtil = new PcmToWavUtil(RateInHz,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
    }

    @Override
    public void run() {
        super.run();
        recordBufSize = AudioRecord.getMinBufferSize(RateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RateInHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recordBufSize);
        mAudioRecord.startRecording();
        byte[] data = new byte[recordBufSize];


        FileOutputStream os = null;
        try {
            os = new FileOutputStream(mFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (mRecoding) {
            Log.d("EncoderActivity", "正在录制");
            int read = mAudioRecord.read(data, 0, data.length);
            // 如果读取音频数据没有出现错误，就将数据写入到文件
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                try {
                    os.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startRecoding(File file) {
        mFile = file;
        start();
        mRecoding = true;
    }

    public void stopRecoding() {
        mRecoding = false;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public void pcmToWav(String inFilename, String outFilename){
        mPcmToWavUtil.pcmToWav(inFilename,outFilename);
    }
}
