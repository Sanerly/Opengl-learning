package com.vison.opengl_learning.egl.filter.base;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.vison.opengl_learning.egl.gles.TextureRotationUtils;
import com.vison.opengl_learning.egl.gles.GlUtils;
import com.vison.opengl_learning.egl.gles.MatrixUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/18 15:47
 * @Description: 滤镜基类
 */
public class AFilter {

    protected static final String VERTEX_SHADER =
            "uniform mat4 vMatrix;                                      \n" +
            "uniform mat4 vTexMatrix;                                   \n" +
            "attribute vec4 vPosition;                                  \n" +
            "attribute vec4 vCoordinate;                                \n" +
            "varying vec2 aCoordinate;                                  \n" +
            "void main() {                                              \n" +
            "    gl_Position = vMatrix * vPosition;                     \n" +
            "    aCoordinate =(vTexMatrix * vCoordinate).xy;            \n" +
            "}                                                          \n";

    protected static final String FRAGMENT_SHADER_2D =
            "precision mediump float;                                   \n" +
            "varying vec2 aCoordinate;                                  \n" +
            "uniform sampler2D vTexture;                                \n" +
            "void main() {                                              \n" +
            "    gl_FragColor = texture2D(vTexture, aCoordinate);       \n" +
            "}                                                          \n";

    /**
     * 顶点坐标Buffer
     */
    protected FloatBuffer mVertexArray;
    /**
     * 纹理坐标Buffer
     */
    protected FloatBuffer mTexCoordArray;

    /**
     * 程序句柄
     */
    protected int mProgram;

    /**
     * 顶点坐标句柄
     */
    private int mPositionHandle;
    /**
     * 纹理坐标句柄
     */
    private int mTexCoordHandle;

    /**
     * 变换矩阵句柄
     */
    private int mMatrixHandle;

    /**
     * 缩放矩阵句柄
     */
    protected int mTexMatrixHandle;

    /**
     * 纹理句柄
     */
    private int mTextureHandle;

    /**
     * 单位矩阵
     */
    public static final float[] OM = MatrixUtils.getOriginalMatrix();

    /**
     * 默认矩阵
     */
    private float[] mMatrix = Arrays.copyOf(OM, 16);


    /**
     * 缩放矩阵
     */
    protected float[] mTexMatrix = new float[16];
    /**
     * 顶点数量
     */
    protected int mVertexCount = TextureRotationUtils.CubeVertices.length / TextureRotationUtils.CoordsPerVertex;

    // FBO属性
    protected int[] mFramebuffers;
    protected int[] mFramebufferTextures;
    protected int mFrameWidth = -1;
    protected int mFrameHeight = -1;

    // 渲染的Image的宽高
    protected int mImageWidth;
    protected int mImageHeight;
    // 显示输出的宽高
    protected int mDisplayWidth;
    protected int mDisplayHeight;


    private  final LinkedList<Runnable> mRunOnDraw;

    public AFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER_2D);
    }


    public AFilter( String vertex, String fragment) {
//        mFilter=Filter.NONE;

        mRunOnDraw = new LinkedList<>();
        //创建顶点坐标
        mVertexArray = GlUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        //创建纹理坐标
        mTexCoordArray = GlUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        //创建程序
        mProgram = GlUtils.createProgram(vertex,fragment);
        //获取顶点坐标句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //获取纹理坐标句柄
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //缩放矩阵句柄
        mTexMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vTexMatrix");
        //获取纹理句柄
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture");

        initMatrix();

    }


    /**
     * Surface发生变化时调用
     *
     * @param width
     * @param height
     */
    public void onInputSizeChanged(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * 显示视图发生变化时调用
     *
     * @param width
     * @param height
     */
    public void onDisplayChanged(int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;
    }


    /**
     * 初始化单位矩阵
     */
    public void initMatrix() {
        Matrix.setIdentityM(mMatrix, 0);
        Matrix.setIdentityM(mTexMatrix, 0);
    }

    public void setMatrix(float[] mMatrix) {
        this.mMatrix = mMatrix;
    }

    public float[] getMatrix(){
        return mMatrix;
    }
    /**
     * 绘制Frame
     *
     * @param textureId
     */
    public boolean drawFrame(int textureId) {
        return drawFrame(textureId, mVertexArray, mTexCoordArray);
    }

    /**
     * 绘制Frame
     */
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer,
                              FloatBuffer textureBuffer) {
        if (textureId == GlUtils.GL_NOT_INIT) {
            return false;
        }
        GLES20.glUseProgram(mProgram);
        runPendingOnDrawTasks();
        //顶点
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //纹理
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0);
        //缩放矩阵句柄
        GLES30.glUniformMatrix4fv(mTexMatrixHandle, 1, false, mTexMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureType(), textureId);
        GLES20.glUniform1i(mTextureHandle, 0);
        onDrawArraysBegin();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        onDrawArraysAfter();
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
//        GLES20.glBindTexture(getTextureType(), 0);
        GLES20.glUseProgram(0);
        return true;
    }


    /**
     * 绘制到FBO
     *
     * @param textureId
     * @return FBO绑定的Texture
     */
    public int drawFrameBuffer(int textureId) {
        return drawFrameBuffer(textureId, mVertexArray, mTexCoordArray);
    }

    /**
     * 绘制到FBO
     *
     * @param textureId
     * @param vertexBuffer
     * @param textureBuffer
     * @return FBO绑定的Texture
     */
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mFramebuffers == null) {
            return GlUtils.GL_NOT_INIT;
        }
        GLES20.glViewport(0, 0, mFrameWidth, mFrameHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[0]);

        GLES20.glUseProgram(mProgram);
        runPendingOnDrawTasks();
        //顶点
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //纹理
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0);
        //缩放矩阵句柄
        GLES30.glUniformMatrix4fv(mTexMatrixHandle, 1, false, mTexMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureType(), textureId);
        GLES20.glUniform1i(mTextureHandle, 0);

        onDrawArraysBegin();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        onDrawArraysAfter();

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);

        GLES20.glBindTexture(getTextureType(), 0);
        GLES20.glUseProgram(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        return mFramebufferTextures[0];
    }

    /**
     * 清屏
     */
    public void clearBufferBit() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * 获取Texture类型
     * GLES20.TEXTURE_2D / GLES11Ext.GL_TEXTURE_EXTERNAL_OES等
     */
    public int getTextureType() {
        return GLES20.GL_TEXTURE_2D;
    }

    /**
     * 调用drawArrays之前，方便添加其他属性
     */
    public void onDrawArraysBegin() {

    }

    /**
     * drawArrays调用之后，方便销毁其他属性
     */
    public void onDrawArraysAfter() {

    }

    /**
     * 释放资源
     */
    public void release() {
        GLES20.glDeleteProgram(mProgram);
        mProgram = -1;
        destroyFramebuffer();
    }

    public void initFramebuffer(int width, int height) {
        if (mFramebuffers != null && (mFrameWidth != width || mFrameHeight != height)) {
            destroyFramebuffer();
        }
        if (mFramebuffers == null) {
            mFrameWidth = width;
            mFrameHeight = height;
            mFramebuffers = new int[1];
            mFramebufferTextures = new int[1];
            GlUtils.createSampler2DFrameBuff(mFramebuffers, mFramebufferTextures, width, height);
        }
    }

    public void destroyFramebuffer() {
        if (mFramebufferTextures != null) {
            GLES20.glDeleteTextures(1, mFramebufferTextures, 0);
            mFramebufferTextures = null;
        }

        if (mFramebuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFramebuffers, 0);
            mFramebuffers = null;
        }
        mImageWidth = -1;
        mImageHeight = -1;
    }



    ///------------------ 统一变量(uniform)设置 ------------------------///
    protected void setInteger(final int location, final int intValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, intValue);
            }
        });
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setPoint(final int location, final PointF point) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }
}
