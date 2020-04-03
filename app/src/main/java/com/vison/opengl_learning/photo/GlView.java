package com.vison.opengl_learning.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/1/10 17:05
 * @Description: 类描述
 */
public class GlView extends GLSurfaceView {

    private FilterRender mFilterRender;
    private Context mContext;
    private MediaRecorder mMediaRecorder;

    public GlView(Context context) {
        this(context,null);
    }

    public GlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext=context;
        mFilterRender = new FilterRender(context);
        setEGLContextClientVersion(2);
        setRenderer(mFilterRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

//        mFilterRender.setOnRenderListener(new FilterRender.OnRenderListener() {
//            @Override
//            public void onRender() {
//                Log.d("GlView","执行了");
//                requestRender();
//            }
//        });

        mFilterRender.setOnSurfaceCreateListener(new FilterRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(Surface surface) {
                String path = mContext.getExternalCacheDir().getPath()+ "/";
                long dataTake = System.currentTimeMillis();
                final String jpegName=path+ dataTake +".mp4";

                mMediaRecorder = new MediaRecorder(mContext,jpegName,1080,720,surface);
                Log.d("GlView","执行了"+surface.toString());
            }
        });
    }

    public void start(){
        try {
            mMediaRecorder.start();
            mMediaRecorder.drainEncoder(false);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GlView",e.getMessage());
        }
    };

    public void release(){
        try {
            mMediaRecorder.release();
            mMediaRecorder.drainEncoder(true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GlView",e.getMessage());
        }
    };

    public void setFilter(Filter filter) {
        mFilterRender.setFilter(filter);
        requestRender();
    }

    public void setBitmap(Bitmap bitmap) {
        mFilterRender.setBitmap(bitmap);
        requestRender();
    }

    public ByteBuffer getBuffer() {
        return mFilterRender.getBuffer();
    }
}
