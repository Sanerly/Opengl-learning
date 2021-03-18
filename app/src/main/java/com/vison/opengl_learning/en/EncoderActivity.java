package com.vison.opengl_learning.en;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vison.opengl_learning.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EncoderActivity extends AppCompatActivity {

    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_end)
    Button btnEnd;
    @BindView(R.id.surface_view)
    SurfaceView surfaceView;
    @BindView(R.id.btn_play)
    Button btnPlay;
    private String storagePath;
    private AudioThread audioThread;
    private String pathSrc;
    private String wavPath;
    private AudioTrack mAudioTrack;
    private boolean isPlay;
    private boolean isR = false;
    private Camera mCamera;


    private String outPath;
    private String srcPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encoder);
        ButterKnife.bind(this);

        storagePath = getExternalCacheDir().getPath() + "/";

        surfaceView.getHolder().addCallback(callback2);
        mCamera = Camera.open(2);
        Toast.makeText(this, "摄像头数量 = " + Camera.getNumberOfCameras(), Toast.LENGTH_SHORT).show();
//        Log.d("EncoderActivity", "摄像头数量 = " + Camera.getNumberOfCameras());
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(mPreviewCallback);
    }


    @OnClick({R.id.btn_start, R.id.btn_end, R.id.btn_play,
            R.id.btn_extractor, R.id.btn_encode_mp3,
            R.id.btn_decode_mp3, R.id.btn_marge_av,

    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                isR = !isR;
                if (isR) {
                    File folder = new File(storagePath);
                    if (!folder.exists() && !folder.mkdirs()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EncoderActivity.this, "无法保存文件", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    pathSrc = storagePath + System.currentTimeMillis() + ".pcm";
                    Log.d("EncoderActivity", "录音路径 = " + pathSrc);
                    audioThread = new AudioThread();
                    audioThread.startRecoding(new File(pathSrc));
                    btnStart.setText("停止");
                } else {
                    btnStart.setText("录制");
                    audioThread.stopRecoding();
                }

                break;
            case R.id.btn_end:
                wavPath = storagePath + System.currentTimeMillis() + "_1.wav";
                audioThread.pcmToWav(pathSrc, wavPath);
                break;
            case R.id.btn_play:
                isPlay = !isPlay;
                Log.d("EncoderActivity", wavPath);
                if (isPlay) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            int size = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
                            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO,
                                    AudioFormat.ENCODING_PCM_16BIT, size, AudioTrack.MODE_STREAM);

                            try {
                                FileInputStream inputStream = new FileInputStream(new File(wavPath));
                                byte[] audioData = new byte[size];
                                while (inputStream.read(audioData) != -1) {
                                    mAudioTrack.play();
                                    mAudioTrack.write(audioData, 0, audioData.length);
                                }
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }).start();
                } else {
                    if (mAudioTrack != null) {
                        mAudioTrack.stop();
                        mAudioTrack.release();
                        mAudioTrack = null;
                    }
                }
                break;
            case R.id.btn_extractor:

                File folder = new File(storagePath);
                if (!folder.exists() && !folder.mkdirs()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EncoderActivity.this, "无法保存文件", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                srcPath = storagePath + "jhym.mp4";
                outPath = storagePath + "jhym_no.mp4";

                Log.d("EncoderActivity", "视频路径 = " + srcPath);
                Log.d("EncoderActivity", "分离路径 = " + outPath);


                ExtractorThread thread = new ExtractorThread(srcPath, outPath);
                thread.start();
                break;
            case R.id.btn_marge_av:
                String mMp4path = storagePath + "jhym.mp4";
                String mMp3path = storagePath + "thz.aac";
                String mOutMp4path = storagePath + "jhym_new.mp4";
                AVMergeThread avMergeThread = new AVMergeThread(mMp4path, mMp3path, mOutMp4path);
                avMergeThread.start();
                break;
            case R.id.btn_decode_mp3:
                srcPath = storagePath + "thz.flac";
                outPath = storagePath + "thz.pcm";
//                srcPath = storagePath + "jhym_v3.mp3";
//                outPath = storagePath + "jhym_v3.pcm";
                DecodeMp3Thread decodeMp3Thread = new DecodeMp3Thread(srcPath, outPath);
                decodeMp3Thread.start();
                break;
            case R.id.btn_encode_mp3:
                srcPath = storagePath + "thz.pcm";
                outPath = storagePath + "thz.aac";

//                srcPath = storagePath + "jhym_v3.pcm";
//                outPath = storagePath + "jhym_v3.aac";
                EncoderAACThread encoderACCThread = new EncoderAACThread(srcPath, outPath);
                encoderACCThread.start();
                break;
        }
    }

    SurfaceHolder.Callback2 callback2 = new SurfaceHolder.Callback2() {
        @Override
        public void surfaceRedrawNeeded(SurfaceHolder holder) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.release();
        }
    };


    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
//            Log.d("EncoderActivity", "摄像头采集的数据 = " + data.length);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        surfaceView.getHolder().removeCallback(callback2);
    }
}
