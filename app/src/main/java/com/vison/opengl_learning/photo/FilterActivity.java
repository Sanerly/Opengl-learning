package com.vison.opengl_learning.photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vison.opengl_learning.MainActivity;
import com.vison.opengl_learning.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterActivity extends AppCompatActivity  {

    @BindView(R.id.gl_photo_view)
    GlView glView;
    @BindView(R.id.btn_1)
    Button btn1;
    @BindView(R.id.btn_2)
    Button btn2;
    @BindView(R.id.btn_3)
    Button btn3;
    @BindView(R.id.btn_4)
    Button btn4;



    private Bitmap mBitmap;
    public static Bitmap BITMAP;
    private boolean isShow = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        init();
    }


    private void init() {
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_trure);
        glView.setBitmap(mBitmap);
        mHandler.sendEmptyMessageDelayed(1,1000);

    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                isShow = !isShow;
                if (isShow) {
                    mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_trure);
                } else {
                    mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_test);
                }
                glView.setBitmap(mBitmap);
                mHandler.sendEmptyMessageDelayed(1, 1000);
            }
            return false;
        }
    });

    @OnClick({R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_1:
                glView.setFilter(Filter.NONE);
                break;
            case R.id.btn_2:
                glView.setFilter(Filter.BLUR);
                break;
            case R.id.btn_3:
//                glView.setFilter(Filter.COOL);
                glView.start();
                break;
            case R.id.btn_4:
                glView.release();
//                glView.setFilter(Filter.GRAY);
//                Bitmap bitmap=Bitmap.createBitmap(mBitmap.getWidth(),mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//                bitmap.copyPixelsFromBuffer(glView.getBuffer());
//                saveBitmap(bitmap);
//                BITMAP=bitmap;
//                glView.getBuffer().clear();
                break;
        }
    }


    //图片保存
    public void saveBitmap(final Bitmap b){
        String path = getExternalCacheDir().getPath()+ "/";
//        String path = MainActivity.SAVE_PATH;
        Log.e("wuwang","img->"+path);
        File folder=new File(path);
        if(!folder.exists()&&!folder.mkdirs()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FilterActivity.this, "无法保存照片", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName=path+ dataTake +".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FilterActivity.this, "保存成功->"+jpegName, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
