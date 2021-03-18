package com.vison.opengl_learning.egl.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.vison.opengl_learning.egl.filter.type.FilterType;

import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/1/10 17:05
 * @Description: 类描述
 */
public class GlESView extends GLSurfaceView implements IGlCommon {

    private SurfaceRenderer mFilterRender;
    private Context mContext;

    public GlESView(Context context) {
        this(context, null);
    }

    public GlESView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mFilterRender = new SurfaceRenderer();
        setEGLContextClientVersion(2);
        setRenderer(mFilterRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }


    @Override
    public void setBuffer(ByteBuffer buffer, int width, int height) {
        mFilterRender.setBuffer(buffer, width, height);
        requestRender();
    }

    @Override
    public void setRecording(boolean recording) {
        mFilterRender.setRecording(recording);
    }

    @Override
    public void callTakePicture(boolean takePicture, String picturePath) {
        mFilterRender.callTakePicture(takePicture, picturePath);
    }

    @Override
    public void setFilterType(FilterType type) {
        mFilterRender.setFilterType(type);
        requestRender();
    }

    @Override
    public void setZoomScale(float scale) {
        mFilterRender.setZoomScale(scale);
        requestRender();
    }


    public void setTranslation(float translationX, float translationY) {
        mFilterRender.setTranslation(translationX, translationY);
        requestRender();
    }


    public int getZoomWidth() {
        return mFilterRender.getZoomWidth();
    }

    public int getZoomHeight() {
        return mFilterRender.getZoomHeight();
    }
}
