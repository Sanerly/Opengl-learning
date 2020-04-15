package com.vison.opengl_learning.egl.view;

import android.content.Context;
import android.graphics.Matrix;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.vison.opengl_learning.egl.filter.base.AFilter;
import com.vison.opengl_learning.egl.filter.base.AFilterGroup;
import com.vison.opengl_learning.egl.filter.core.FilterManager;
import com.vison.opengl_learning.egl.filter.type.FilterType;
import com.vison.opengl_learning.egl.filter.type.ScaleType;
import com.vison.opengl_learning.egl.gles.GlUtils;
import com.vison.opengl_learning.egl.gles.MatrixUtils;
import com.vison.opengl_learning.egl.gles.TakePictureUtils;
import com.vison.opengl_learning.egl.gles.TextureRotationUtils;
import com.vison.opengl_learning.egl.manager.RecordManager;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/18 11:27
 * @Description: 类描述
 */
public class SurfaceRenderer implements GLSurfaceView.Renderer, IGlCommon {

    //创建的纹理
    private int textureId;
    //当前纹理
    private int mCurrentTextureId = 0;
    //录制视频
    private boolean isRecording = false;
    //拍照
    private boolean isTakePicture = false;
    //拍照保存路径
    private String savePicturePath = "";
    //实时滤镜组
    private AFilterGroup mRealTimeFilter;
    //显示输出
    private AFilter mDisplayFilter;
    // 输入流大小
    private int mTextureWidth;
    private int mTextureHeight;
    // 显示大小
    private int mDisplayWidth;
    private int mDisplayHeight;
    //数据buffer
    private ByteBuffer mByteBuffer;
    //滤镜类型
    private FilterType mFilterType = FilterType.NONE;
    //顶点坐标Buffer
    private FloatBuffer mVertexArray;
    //纹理坐标Buffer
    private FloatBuffer mTexCoordArray;
    private float mZoomScale;


    public SurfaceRenderer() {

    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        //渲染滤镜组
        mRealTimeFilter = FilterManager.getFilterGroup();
        //显示输出
        mDisplayFilter = FilterManager.getFilter(FilterType.NONE);

        mVertexArray = GlUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTexCoordArray = GlUtils.createFloatBuffer(TextureRotationUtils.TextureVertices_270);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        onInputSizeChanged(mTextureWidth, mTextureHeight);
        onDisplaySizeChanged(width, height);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        textureId = GlUtils.createTexture(mDisplayFilter.getTextureType());
        texImage2D();

//        // 如果存在滤镜，则绘制滤镜
        if (mRealTimeFilter != null) {
            mRealTimeFilter.changeFilter(mFilterType);
            mRealTimeFilter.changeZoomScale(mZoomScale);
            mCurrentTextureId = mRealTimeFilter.drawFrameBuffer(textureId, mVertexArray, mTexCoordArray);
        }


        // 显示输出，需要调整视口大小
        if (mDisplayFilter != null) {
            GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
            mDisplayFilter.drawFrame(mCurrentTextureId);
        }

        if (isTakePicture) {
            try {
                TakePictureUtils.saveFrame(new File(savePicturePath), mDisplayWidth, mDisplayHeight);
            } catch (IOException e) {
                e.printStackTrace();
            }
            isTakePicture = false;
        }

        if (isRecording) {
            RecordManager.getInstance().setTextureSize(mTextureWidth, mTextureHeight);
            // 设置预览大小
            RecordManager.getInstance().setDisplaySize(mDisplayWidth, mDisplayHeight);
            RecordManager.getInstance().frameAvailable();
            RecordManager.getInstance().drawRecorderFrame(mCurrentTextureId, System.nanoTime());
            RecordManager.getInstance().startRecording(EGL14.eglGetCurrentContext());
        }


    }


    @Override
    public void setBuffer(ByteBuffer buffer, int width, int height) {
        this.mTextureWidth = width;
        this.mTextureHeight = height;
        this.mByteBuffer = buffer;
    }

    @Override
    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    @Override
    public void callTakePicture(boolean takePicture, String picturePath) {
        isTakePicture = takePicture;
        savePicturePath = picturePath;
    }

    @Override
    public void setFilterType(FilterType type) {
        mFilterType = type;
    }

    @Override
    public void setZoomScale(float scale) {
        mZoomScale = scale;
    }

    private void texImage2D() {
        GLES20.glTexImage2D(mRealTimeFilter.getTextureType(), 0, GLES20.GL_RGB, mTextureWidth,
                mTextureHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, mByteBuffer);
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
        if (mRealTimeFilter != null) {
            mRealTimeFilter.onInputSizeChanged(width, height);
        }
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
