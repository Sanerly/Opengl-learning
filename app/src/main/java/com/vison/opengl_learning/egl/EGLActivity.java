package com.vison.opengl_learning.egl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vison.opengl_learning.R;
import com.vison.opengl_learning.egl.encoder.MediaEncoder;
import com.vison.opengl_learning.egl.filter.type.FilterType;
import com.vison.opengl_learning.egl.manager.ParamsManager;
import com.vison.opengl_learning.egl.manager.RecordManager;
import com.vison.opengl_learning.egl.view.GlESView;

import java.io.File;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EGLActivity extends AppCompatActivity {

    @BindView(R.id.gles_view)
    GlESView glesView;
    @BindView(R.id.btn_record)
    Button btnRecord;
    @BindView(R.id.btn_stop)
    Button btnStop;
    @BindView(R.id.btn_1)
    Button btn1;
    @BindView(R.id.btn_2)
    Button btn2;
    @BindView(R.id.btn_3)
    Button btn3;
    @BindView(R.id.btn_4)
    Button btn4;
    @BindView(R.id.btn_photo)
    Button btnPhoto;
    @BindView(R.id.btn_zoom_in)
    Button btnZoomIn;
    @BindView(R.id.btn_zoom_out)
    Button btnZoomOut;
    private Bitmap mBitmap1;
    private Bitmap mBitmap2;
    private boolean isShow = false;
    private ByteBuffer buffer1;
    private ByteBuffer buffer2;
    private String storagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl);
        ButterKnife.bind(this);
        init();
    }

    private void init() {

        ParamsManager.context = this;
        ParamsManager.StoragePath = getExternalCacheDir().getPath();
        storagePath = getExternalCacheDir().getPath() + "/";
        mBitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_trure);
        buffer1 = ByteBuffer.wrap(bitmap2RGB(mBitmap1));
        mBitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_test);
        buffer2 = ByteBuffer.wrap(bitmap2RGB(mBitmap2));

        glesView.setBuffer(buffer1, mBitmap1.getWidth(), mBitmap1.getHeight());
        mHandler.sendEmptyMessageDelayed(1, 1000);


    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                isShow = !isShow;
                if (isShow) {
                    glesView.setBuffer(buffer1, mBitmap1.getWidth(), mBitmap1.getHeight());
                } else {
                    glesView.setBuffer(buffer2, mBitmap2.getWidth(), mBitmap2.getHeight());
                }
                mHandler.sendEmptyMessageDelayed(1, 1000);
            }
            return false;
        }
    });

    @OnClick({R.id.btn_record, R.id.btn_stop, R.id.btn_1,
            R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_photo,
            R.id.btn_zoom_in, R.id.btn_zoom_out})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_record:
                // 初始化录制线程
                RecordManager.getInstance().initThread();
                // 是否允许录音
                RecordManager.getInstance().setEnableAudioRecording(true);
                // 是否允许高清录制
                RecordManager.getInstance().enableHighDefinition(false);


                File folder = new File(storagePath);
                if (!folder.exists() && !folder.mkdirs()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EGLActivity.this, "无法保存视频", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                long time = System.currentTimeMillis();
                final String outputPath = storagePath + time + "_1.mp4";
                RecordManager.getInstance().setOutputPath(outputPath);
                // 初始化录制器
                RecordManager.getInstance().initRecorder(RecordManager.RECORD_WIDTH,
                        RecordManager.RECORD_HEIGHT, new MediaEncoder.MediaEncoderListener() {
                            @Override
                            public void onPrepared(MediaEncoder encoder) {
                                Log.d("EGLActivity", "录制器准备完成");

                                glesView.setRecording(true);
                            }

                            @Override
                            public void onStarted(MediaEncoder encoder) {
                                Log.d("EGLActivity", "录制器正在录制");

                            }

                            @Override
                            public void onStopped(MediaEncoder encoder) {
                                Log.d("EGLActivity", "录制器停止录制");
                            }

                            @Override
                            public void onReleased(MediaEncoder encoder) {
                                Log.d("EGLActivity", "录制器释放");
                            }
                        });

                break;
            case R.id.btn_stop:
                glesView.setRecording(false);
                RecordManager.getInstance().stopRecording();
                break;
            case R.id.btn_photo:

                File folder2 = new File(storagePath);
                if (!folder2.exists() && !folder2.mkdirs()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EGLActivity.this, "无法保存照片", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                final String path = storagePath + System.currentTimeMillis() + "_2.jpeg";
                glesView.callTakePicture(true, path);
                break;
            case R.id.btn_1:
                glesView.setFilterType(FilterType.NONE);
                break;
            case R.id.btn_2:
                glesView.setFilterType(FilterType.BLACKWHITE);
                break;
            case R.id.btn_3:
                glesView.setFilterType(FilterType.SKETCH);
                break;
            case R.id.btn_4:
                glesView.setFilterType(FilterType.SPLITSCREEN);
                break;
            case R.id.btn_zoom_in:
                zoomVal=zoomVal+0.1f;
                glesView.setZoomScale(zoomVal);
                break;
            case R.id.btn_zoom_out:
                zoomVal=zoomVal-0.1f;
                glesView.setZoomScale(zoomVal);
                break;
        }
    }
        float zoomVal=1.0f;

    /**
     * @方法描述 Bitmap转RGB
     */
    public static byte[] bitmap2RGB(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();  //返回可用于储存此位图像素的最小字节数

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //  使用allocate()静态方法创建字节缓冲区
        bitmap.copyPixelsToBuffer(buffer); // 将位图的像素复制到指定的缓冲区

        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4) * 3];

        int count = rgba.length / 4;

        //Bitmap像素点的色彩通道排列顺序是RGBA
        for (int i = 0; i < count; i++) {

            pixels[i * 3] = rgba[i * 4];        //R
            pixels[i * 3 + 1] = rgba[i * 4 + 1];    //G
            pixels[i * 3 + 2] = rgba[i * 4 + 2];       //B

        }
        return pixels;
    }

}
