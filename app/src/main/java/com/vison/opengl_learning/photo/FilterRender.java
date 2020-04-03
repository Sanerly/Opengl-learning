package com.vison.opengl_learning.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.vison.opengl_learning.util.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/8 11:41
 * @Description: 滤镜着色器
 */
public class FilterRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private final float[] sPos = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    private final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };


    private int mProgram;
    private int mFProgram;
    private FloatBuffer bPos;
    private FloatBuffer bCoord;
    private Bitmap mBitmap;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int mMatrixHandler;

    private int hChangeType;
    private int hChangeColor;

    //矩阵
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private Filter mFilter = Filter.NONE;
    //    private int[] texture;
    private int textureType = 0;


    private Context mContext;

//    private int mFrameBuffer;
//    private int mRenderBuffer;

    private ByteBuffer mBuffer;

    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[1];


//    private int[] texture = new int[1];

    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private int textureId_mediacodec;
    private OnSurfaceCreateListener mOnSurfaceCreateListener;
    private OnRenderListener onRenderListener;
    public FilterRender(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        ByteBuffer pos = ByteBuffer.allocateDirect(sPos.length * 4);
        pos.order(ByteOrder.nativeOrder());
        bPos = pos.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);

        ByteBuffer coord = ByteBuffer.allocateDirect(sCoord.length * 4);
        coord.order(ByteOrder.nativeOrder());
        bCoord = coord.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);

        mProgram = ShaderUtils.createProgram(mContext.getResources(), "xfilter/default_vertex.sh", "xfilter/default_color_fragment.sh");
        mFProgram = ShaderUtils.createProgram(mContext.getResources(), "xfilter/default_vertex.sh", "xfilter/default_color_fragment.sh");

        //生成纹理
        GLES20.glGenTextures(1, fTexture, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureType);
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0]);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        textureId_mediacodec=fTexture[0];
        surfaceTexture = new SurfaceTexture(textureId_mediacodec);
        surface = new Surface(surfaceTexture);
        surfaceTexture.setOnFrameAvailableListener(this);

        if (mOnSurfaceCreateListener != null) {
            //将Surface回掉出去给MediaCodec绑定渲染
            mOnSurfaceCreateListener.onSurfaceCreate(surface);
        }

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d("GlView","执行了");
//        if (onRenderListener!=null) {
//            onRenderListener.onRender();
//        }
    }
    public void setOnRenderListener(OnRenderListener onRenderListener) {
        this.onRenderListener = onRenderListener;
    }
    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        this.mOnSurfaceCreateListener = onSurfaceCreateListener;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public interface OnSurfaceCreateListener {
        void onSurfaceCreate(Surface surface);
    }

    public interface OnRenderListener {
        void onRender();
    }


    private int mWidth;
    private int mHeight;

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

//        mMVPMatrix = Gl2Utils.flip(Gl2Utils.getOriginalMatrix(), false, true);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        //重新切换到FrameBuffer上。
//
//        fcreateTexture();
//        mBuffer = ByteBuffer.allocate(mBitmap.getWidth() * mBitmap.getHeight() * 4);
//        GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA,
//                GLES20.GL_UNSIGNED_BYTE, mBuffer);
//
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        createTexture();











        GLES20.glDisableVertexAttribArray(glHPosition);
        GLES20.glDisableVertexAttribArray(glHCoordinate);
//        GLES20.glDeleteTextures(1, fTexture, 0);
//        GLES20.glDeleteFramebuffers(1, fFrame, 0);
//        GLES20.glDeleteRenderbuffers(1, fRender, 0);
    }



    private void createTexture() {
//        surfaceTexture.updateTexImage();
        surfaceTexture.updateTexImage();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);

        //获取顶点坐标句柄
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(glHPosition);
        //传入顶点坐标
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos);

        //获取纹理坐标句柄
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord);


        hChangeType = GLES20.glGetUniformLocation(mProgram, "vChangeType");
        GLES20.glUniform1i(hChangeType, 1);
        hChangeColor = GLES20.glGetUniformLocation(mProgram, "vChangeColor");
        GLES20.glUniform3fv(hChangeColor, 1, new float[]{1.0f, 0.0f, 0.0f}, 0);


        //获取纹理句柄
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        GLES20.glUniform1i(glHTexture, textureType);

        //根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);



    }

//    private int createTexture() {
//        if (mBitmap != null && !mBitmap.isRecycled()) {
//
//            //根据以上指定的参数，生成一个2D纹理
//            GlUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
//            return texture[0];
//        }
//        return 0;
//    }


    //生成frameBuffer的时机
    private void fcreateTexture() {

        //申请一个与FrameBuffer绑定的textureId
        GLES20.glGenTextures(1, fTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[0]);

        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // 创建纹理存储.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.getWidth(), mBitmap.getHeight(), 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);


        //创建FrameBuffer Object并且绑定它
        GLES20.glGenFramebuffers(1, fFrame, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);


        // 创建RenderBuffer Object并且绑定它
        GLES20.glGenRenderbuffers(1, fRender, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        //为我们的RenderBuffer申请存储空间
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mBitmap.getWidth(), mBitmap.getHeight());


        // 将renderBuffer挂载到frameBuffer的depth attachment 上。就上面申请了OffScreenId和FrameBuffer相关联
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, fRender[0]);
        // 将text2d挂载到frameBuffer的color attachment上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fTexture[0], 0);

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }
//        // 先不使用FrameBuffer，将其切换掉。到开始绘制的时候，在绑定回来
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mFProgram);
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mFProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);

        //获取顶点坐标句柄
        glHPosition = GLES20.glGetAttribLocation(mFProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(glHPosition);
        //传入顶点坐标
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos);

        //获取纹理坐标句柄
        glHCoordinate = GLES20.glGetAttribLocation(mFProgram, "vCoordinate");
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord);


        hChangeType = GLES20.glGetUniformLocation(mFProgram, "vChangeType");
        GLES20.glUniform1i(hChangeType, mFilter.getType());
        hChangeColor = GLES20.glGetUniformLocation(mFProgram, "vChangeColor");
        GLES20.glUniform3fv(hChangeColor, 1, mFilter.data(), 0);

        //获取纹理句柄
        glHTexture = GLES20.glGetUniformLocation(mFProgram, "vTexture");
        GLES20.glUniform1i(glHTexture, textureType);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);



    }


    public void setFilter(Filter filter) {
        this.mFilter = filter;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;


    }

    public ByteBuffer getBuffer() {
        return mBuffer;
    }


}
