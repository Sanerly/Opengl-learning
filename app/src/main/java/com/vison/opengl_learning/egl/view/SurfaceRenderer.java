package com.vison.opengl_learning.egl.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.vison.opengl_learning.egl.filter.base.AFilter;
import com.vison.opengl_learning.egl.filter.base.AFilterGroup;
import com.vison.opengl_learning.egl.filter.core.FilterManager;
import com.vison.opengl_learning.egl.filter.type.Filter;
import com.vison.opengl_learning.egl.filter.type.FilterType;
import com.vison.opengl_learning.egl.gles.GlUtils;
import com.vison.opengl_learning.egl.manager.RecordManager;

import java.io.ByteArrayOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/18 11:27
 * @Description: 类描述
 */
public class SurfaceRenderer implements GLSurfaceView.Renderer {

//    private AFilter mFilter;
    private int textureId;
    private Context mContext;

    private boolean isRecording = false;
    private EGLContext mEglContext;

//     实时滤镜组
    private AFilterGroup mRealTimeFilter;
//     显示输出
    private AFilter mDisplayFilter;

    // 输入流大小
    private int mTextureWidth;
    private int mTextureHeight;
    // 显示大小
    private int mDisplayWidth;
    private int mDisplayHeight;

    private ByteBuffer mByteBuffer;
    private int mWidth;
    private int mHeight;


    public SurfaceRenderer(Context context) {
        mContext = context;

    }

    /**
     * 初始化滤镜
     */
    private void initFilters() {
         //渲染滤镜组
        mRealTimeFilter = FilterManager.getFilterGroup();
         //显示输出
        mDisplayFilter = FilterManager.getFilter(FilterType.SOURCE);

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        initFilters();
        textureId = GlUtils.createTexture(mDisplayFilter.getTextureType());

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        onInputSizeChanged(mWidth, mHeight);
        onDisplaySizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        texImage2D();
        mDisplayFilter.clearBufferBit();
        mEglContext = EGL14.eglGetCurrentContext();

        // 如果存在滤镜，则绘制滤镜
        if (mRealTimeFilter != null) {
            textureId = mRealTimeFilter.drawFrameBuffer(textureId);
        }

        // 显示输出，需要调整视口大小
        if (mDisplayFilter != null) {
            GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
            mDisplayFilter.drawFrame(textureId);
        }

        if (isRecording) {
            RecordManager.getInstance().setTextureSize(mWidth, mHeight);
            // 设置预览大小
            RecordManager.getInstance().setDisplaySize(mWidth, mHeight);
            RecordManager.getInstance().frameAvailable();
            RecordManager.getInstance().drawRecorderFrame(textureId, System.nanoTime());
            RecordManager.getInstance().startRecording(mEglContext);
        }



    }



    public void setBuffer(ByteBuffer buffer, int width, int height) {
        this.mWidth=width;
        this.mHeight=height;
        this.mByteBuffer = buffer;
    }


    public void setRecording(boolean recording) {
        isRecording = recording;
    }


    public void setFilterType(FilterType type) {
        mRealTimeFilter.changeFilter(type);
    }

    public void texImage2D() {
        GLES20.glTexImage2D(mDisplayFilter.getTextureType(), 0, GLES20.GL_RGB, mWidth,
                mHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, mByteBuffer);
    }


    /**
     * 渲染Texture的大小
     *
     * @param width
     * @param height
     */
    public void onInputSizeChanged(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
//        if (mRealTimeFilter != null) {
//            mRealTimeFilter.onInputSizeChanged(width, height);
//        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(width, height);
        }
    }

    /**
     * Surface显示的大小
     *
     * @param width
     * @param height
     */
    public void onDisplaySizeChanged(int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;
        if (mRealTimeFilter != null) {
            mRealTimeFilter.onDisplayChanged(width, height);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onDisplayChanged(width, height);
        }
    }

}
