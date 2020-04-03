package com.vison.opengl_learning.egl.filter.base;

import android.opengl.GLES20;

import com.vison.opengl_learning.egl.filter.type.FilterType;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜组基类
 * Created by cain on 17-7-17.
 */
public abstract class AFilterGroup extends AFilter {

    private static int[] mFramebuffers;
    private static int[] mFrameBufferTextures;

    private int mCurrentTextureId;
    protected List<AFilter> mFilters = new ArrayList<AFilter>();

    public AFilterGroup() {

    }

    public AFilterGroup(List<AFilter> filters) {
        mFilters = filters;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mFilters.size() <= 0) {
            return;
        }
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            mFilters.get(i).onInputSizeChanged(width, height);
        }
        // 先销毁原来的Framebuffers
        if(mFramebuffers != null && (mImageWidth != width
                || mImageHeight != height || mFramebuffers.length != size-1)) {
            destroyFramebuffer();
            mImageWidth = width;
            mImageWidth = height;
        }
        initFramebuffer(width, height);
    }

    @Override
    public void onDisplayChanged(int width, int height) {
        super.onDisplayChanged(width, height);
        // 更新显示的的视图大小
        if (mFilters.size() <= 0) {
            return;
        }
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            mFilters.get(i).onDisplayChanged(width, height);
        }
    }

    @Override
    public boolean drawFrame(int textureId) {
        if (mFramebuffers == null || mFrameBufferTextures == null || mFilters.size() <= 0) {
            return false;
        }
        int size = mFilters.size();
        mCurrentTextureId = textureId;
        for (int i = 0; i < size; i++) {
            AFilter filter = mFilters.get(i);
            if (i < size - 1) {
                GLES20.glViewport(0, 0, mImageWidth, mImageHeight);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[i]);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                if (filter.drawFrame(mCurrentTextureId)) {
                    mCurrentTextureId = mFrameBufferTextures[i];
                }
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            } else {
                GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
                filter.drawFrame(mCurrentTextureId);
            }
        }
        return true;
    }

    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mFramebuffers == null || mFrameBufferTextures == null || mFilters.size() <= 0) {
            return false;
        }
        int size = mFilters.size();
        mCurrentTextureId = textureId;
        for (int i = 0; i < size; i++) {
            AFilter filter = mFilters.get(i);
            if (i < size - 1) {
                GLES20.glViewport(0, 0, mImageWidth, mImageHeight);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[i]);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                filter.drawFrame(mCurrentTextureId, vertexBuffer, textureBuffer);
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                mCurrentTextureId = mFrameBufferTextures[i];
            } else {
                GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
                filter.drawFrame(mCurrentTextureId, vertexBuffer, textureBuffer);
            }
        }
        return true;
    }

    @Override
    public int drawFrameBuffer(int textureId) {
        if (mFramebuffers == null || mFrameBufferTextures == null || mFilters.size() <= 0) {
            return textureId;
        }
        int size = mFilters.size();
        mCurrentTextureId = textureId;
        GLES20.glViewport(0, 0, mImageWidth, mImageHeight);
        for (int i = 0; i < size; i++) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[i]);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            if (mFilters.get(i).drawFrame(mCurrentTextureId)) {
                mCurrentTextureId = mFrameBufferTextures[i];
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
        return mCurrentTextureId;
    }

    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mFramebuffers == null || mFrameBufferTextures == null || mFilters.size() <= 0) {
            return textureId;
        }
        int size = mFilters.size();
        mCurrentTextureId = textureId;
        GLES20.glViewport(0, 0, mImageWidth, mImageHeight);
        for (int i = 0; i < size; i++) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[i]);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            if (mFilters.get(i).drawFrame(mCurrentTextureId, vertexBuffer, textureBuffer)) {
                mCurrentTextureId = mFrameBufferTextures[i];
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
        return mCurrentTextureId;
    }

    @Override
    public void release() {
        if (mFilters != null) {
            for (AFilter filter : mFilters) {
                filter.release();
            }
            mFilters.clear();
        }
        destroyFramebuffer();
    }

    /**
     * 初始化framebuffer，这里在调用drawFrame时，会多一个FBO，这里为了方便后面录制视频缩放处理
     */
    public void initFramebuffer(int width, int height) {
        int size = mFilters.size();
        // 创建Framebuffers 和 Textures
        if (mFramebuffers == null) {
            mFramebuffers = new int[size];
            mFrameBufferTextures = new int[size];
            createFramebuffer(0, size);
        }
    }

    /**
     * 创建Framebuffer
     * @param start
     * @param size
     */
    private void createFramebuffer(int start, int size) {
        for (int i = start; i < size; i++) {
            GLES20.glGenFramebuffers(1, mFramebuffers, i);

            GLES20.glGenTextures(1, mFrameBufferTextures, i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[i]);

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    mImageWidth, mImageHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[i]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, mFrameBufferTextures[i], 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    /**
     * 销毁Framebuffers
     */
    public void destroyFramebuffer() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFramebuffers != null) {
            GLES20.glDeleteFramebuffers(mFramebuffers.length, mFramebuffers, 0);
            mFramebuffers = null;
        }
    }

    /**
     * 添加新滤镜
     * @param filters
     */
    public void addFilters(List<AFilter> filters) {
        mFilters.addAll(filters);
        addFrambuffers();
    }

    public abstract void changeFilter(FilterType type);

    /**
     * 添加Framebuffer
     */
    private void addFrambuffers() {
        // 复制和创建新的frambuffer
        int size = mFilters.size();
        int[] framebuffers = new int[size - 1];
        int[] framebufferTextures = new int[size - 1];
        // 复制原来的Framebuffer和Texture
        if (mFramebuffers != null) {
            for (int i = 0; i < mFramebuffers.length; i++) {
                framebuffers[i] = mFramebuffers[i];
                framebufferTextures[i] = mFrameBufferTextures[i];
            }
        }

        int start = 0;
        if (mFramebuffers != null) {
            start = mFramebuffers.length;
        }
        mFramebuffers = framebuffers;
        mFrameBufferTextures = framebufferTextures;
        // 创建新的
        createFramebuffer(start, size - 1);
    }

    /**
     * 替换滤镜组
     * @param filters
     */
    public void replaceWidthFilters(List<AFilter> filters) {
        for (int i = 0; i < mFilters.size(); i++) {
            mFilters.get(i).release();
        }
        mFilters.clear();
        mFilters = filters;
        if (mFramebuffers != null && mFilters.size() < mFramebuffers.length) {
            // 销毁多余的Framebuffers
            int size = mFilters.size() - 1;
            GLES20.glDeleteTextures(mFrameBufferTextures.length - size, mFrameBufferTextures, size);
            GLES20.glDeleteFramebuffers(mFramebuffers.length - size, mFramebuffers, size);
            int[] framebuffers = new int[size];
            int[] framebuffersTextures = new int[size];
            for (int i = 0; i < size; i++) {
                framebuffers[i] = mFramebuffers[i];
                framebuffersTextures[i] = mFrameBufferTextures[i];
            }
            mFramebuffers = framebuffers;
            mFrameBufferTextures = framebuffersTextures;
        } else if (mFramebuffers == null || mFilters.size() > mFramebuffers.length) {
            // 添加Framebuffers
            addFrambuffers();
        }
    }

    /**
     * 获取当前滤镜TextureId
     * @return
     */
    public int getCurrentTextureId() {
        return mCurrentTextureId;
    }
}
