package com.vison.opengl_learning.egl.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.vison.opengl_learning.egl.filter.type.Filter;
import com.vison.opengl_learning.egl.filter.type.FilterType;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/1/10 17:05
 * @Description: 类描述
 */
public class GlESView extends GLSurfaceView {

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
        mFilterRender = new SurfaceRenderer(context);
        setEGLContextClientVersion(2);
        setRenderer(mFilterRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }


//    public void setFilter(Filter xfilter) {
//        mFilterRender.setFilter(xfilter);
//        requestRender();
//    }

    public void setBuffer(ByteBuffer buffer,int width, int height) {
        mFilterRender.setBuffer(buffer,width,height);
        requestRender();
    }

    public void setRecording(boolean recording) {
        mFilterRender.setRecording(recording);
    }

    public void setFilterType(FilterType type) {
        mFilterRender.setFilterType(type);
        requestRender();
    }
}
