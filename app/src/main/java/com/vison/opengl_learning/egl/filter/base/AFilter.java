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

    //  "uniform float uScale;                                      \n" +
    protected static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;                                           \n" +
                    "uniform mat4 uTexMatrix;                                   \n" +
                    "uniform float uScale;                                     \n" +
                    "attribute vec4 aPosition;                                  \n" +
                    "attribute vec4 aCoordinate;                                \n" +
                    "varying vec2 vCoordinate;                                  \n" +
                    "void main() {                                              \n" +
                    "    vec4 position = vec4(aPosition.x * uScale, aPosition.y * uScale, aPosition.zw);              \n" +
                    "    gl_Position = uMVPMatrix * position;                  \n" +
                    "    vCoordinate =(uTexMatrix * aCoordinate).xy;            \n" +
                    "}                                                          \n";

    protected static final String FRAGMENT_SHADER_2D =
            "precision mediump float;                                   \n" +
                    "varying vec2 vCoordinate;                                  \n" +
                    "uniform sampler2D uTexture;                                \n" +
                    "void main() {                                              \n" +
                    "    gl_FragColor = texture2D(uTexture, vCoordinate);       \n" +
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


    protected int mScaleHandle;

    /**
     * 纹理句柄
     */
    private int mTextureHandle;

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

    // --- 视锥体属性 start ---
    //单位矩阵
    private static final float[] OM = MatrixUtils.getOriginalMatrix();
    // 视图矩阵
    protected float[] mViewMatrix = new float[16];
    // 投影矩阵
    protected float[] mProjectionMatrix = new float[16];
    // 模型矩阵
    protected float[] mModelMatrix = new float[16];
    // 变换矩阵
    protected float[] mMVPMatrix = Arrays.copyOf(OM, 16);
    // 缩放矩阵
    protected float[] mTexMatrix = new float[16];

    // 模型矩阵欧拉角的实际角度
    protected float mYawAngle = 0.0f;
    protected float mPitchAngle = 0.0f;
    protected float mRollAngle = 0.0f;
    // --- 视锥体属性 end ---

    protected float mZoomScale = 1.0f;


    private final LinkedList<Runnable> mRunOnDraw;

    public AFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER_2D);
    }


    public AFilter(String vertex, String fragment) {
        mRunOnDraw = new LinkedList<>();
        //创建顶点坐标
        mVertexArray = GlUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        //创建纹理坐标
        mTexCoordArray = GlUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        //创建程序
        mProgram = GlUtils.createProgram(vertex, fragment);
        //获取顶点坐标句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取纹理坐标句柄
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aCoordinate");
        //获取变换矩阵uMVPMatrix成员句柄
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //缩放矩阵句柄
        mTexMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        //缩放顶点坐标句柄
        mScaleHandle = GLES20.glGetUniformLocation(mProgram, "uScale");
        //获取纹理句柄
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");

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
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        runPendingOnDrawTasks();

        calculateMVPMatrix();
        //顶点
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //纹理
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        //指定uMVPMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        //缩放矩阵句柄
        GLES30.glUniformMatrix4fv(mTexMatrixHandle, 1, false, mTexMatrix, 0);

        GLES30.glUniform1f(mScaleHandle, mZoomScale);

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
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glViewport(0, 0, mFrameWidth, mFrameHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffers[0]);

        GLES20.glUseProgram(mProgram);
        runPendingOnDrawTasks();

        calculateMVPMatrix();
        //顶点
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //纹理
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        //指定uMVPMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        //缩放矩阵句柄
        GLES30.glUniformMatrix4fv(mTexMatrixHandle, 1, false, mTexMatrix, 0);

        GLES30.glUniform1f(mScaleHandle, mZoomScale);

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


    ///---------------------- 计算视锥体矩阵变换 ---------------------------------///

    /**
     * 初始化单位矩阵
     */
    private void initMatrix() {
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mProjectionMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mTexMatrix, 0);
    }

    /**
     * 计算视锥体变换矩阵(MVPMatrix)
     */
    public void calculateMVPMatrix() {
        // 模型矩阵变换
        Matrix.setIdentityM(mModelMatrix, 0); // 重置模型矩阵方便计算
        Matrix.rotateM(mModelMatrix, 0, mYawAngle, 1.0f, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, mPitchAngle, 0, 1.0f, 0);
        Matrix.rotateM(mModelMatrix, 0, mRollAngle, 0, 0, 1.0f);
//        Matrix.scaleM(mModelMatrix, 0,  mScale, mScale, mScale);
        // 综合矩阵变换
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    }

    /**
     * 设置投影矩阵
     *
     * @param matrix
     */
    public void setViewMatrix(float[] matrix) {
        if (Arrays.equals(mViewMatrix, matrix)) {
            mViewMatrix = matrix;
        }
    }

    /**
     * 设置投影矩阵
     *
     * @param matrix
     */
    public void setProjectionMatrix(float[] matrix) {
        if (Arrays.equals(mProjectionMatrix, matrix)) {
            mProjectionMatrix = matrix;
        }
    }

    /**
     * 设置模型矩阵
     *
     * @param matrix
     */
    public void setModelMatrix(float[] matrix) {
        if (Arrays.equals(mModelMatrix, matrix)) {
            mModelMatrix = matrix;
        }
    }

    /**
     * 设置变换矩阵
     *
     * @param matrix
     */
    public void setMVPMatrix(float[] matrix) {
        if (!Arrays.equals(mMVPMatrix, matrix)) {
            mMVPMatrix = matrix;
        }
    }

    public float[] getMatrix() {
        return mMVPMatrix;
    }

    /**
     * 设置Texture缩放矩阵
     *
     * @param matrix
     */
    public void setTexMatrix(float[] matrix) {
        mTexMatrix = matrix;
    }

    /**
     * 模型矩阵 X轴旋转角度（0 ~ 360）
     *
     * @param angle
     */
    public void setModelYawAngle(float angle) {
        if (mYawAngle != angle) {
            mYawAngle = angle;
        }
    }

    /**
     * 模型矩阵 Y轴旋转角度(0 ~ 360)
     *
     * @param angle
     */
    public void setModelPitchAngle(float angle) {
        if (mPitchAngle != angle) {
            mPitchAngle = angle;
        }
    }

    /**
     * 模型矩阵 Z轴旋转角度(0 ~ 360)
     *
     * @param angle
     */
    public void setModelRollAngle(float angle) {
        if (mRollAngle != angle) {
            mRollAngle = angle;
        }
    }


    /**
     * 模型矩阵 等比缩放
     *
     * @param scale
     */
    public void setZoomScale(float scale) {
        if (scale <= 0) {
            return;
        }
        mZoomScale = scale;
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
