package com.vison.opengl_learning.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vison.opengl_learning.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    @BindView(R.id.gl_photo_view)
    GlView glPhotoView;



    //顶点着色器
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 vCoordinate;"+
                    "uniform mat4 vMatrix;"+
                    "varying vec2 aCoordinate;"+
                    "void main() {" +
                    "  gl_Position=vMatrix*vPosition;" +
                    "  aCoordinate=vCoordinate;"+
                    "}";

    //片元着色器
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D vTexture;" +
                    "varying vec2 aCoordinate;" +
                    "void main() {" +
                    "  gl_FragColor=texture2D(vTexture,aCoordinate);" +
                    "}";



    private final float[] sPos={
            -1.0f,1.0f,    //左上角
            -1.0f,-1.0f,   //左下角
            1.0f,1.0f,     //右上角
            1.0f,-1.0f     //右下角
    };

    private final float[] sCoord={
            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,0.0f,
            1.0f,1.0f,
    };

    private int mProgram;
    private FloatBuffer bPos;
    private FloatBuffer bCoord;
    private Bitmap mBitmap;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int mMatrixHandler;

    //矩阵
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);
        init();
    }

    private boolean isShow=false;

    private void init() {
        glPhotoView.setEGLContextClientVersion(2);
        glPhotoView.setRenderer(this);
        glPhotoView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mBitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.icon_trure);
        mHandler.sendEmptyMessageDelayed(1,100);
    }

    Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what==1){
                isShow=!isShow;
                if (isShow){
                    mBitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.icon_trure);
                }else {
                    mBitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.icon_test);
                }

                mHandler.sendEmptyMessageDelayed(1,100);
            }
            return false;
        }
    });

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置背景颜色为黑色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        ByteBuffer pos= ByteBuffer.allocateDirect(sPos.length*4);
        pos.order(ByteOrder.nativeOrder());
        bPos=pos.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);

        ByteBuffer coord=ByteBuffer.allocateDirect(sCoord.length*4);
        coord.order(ByteOrder.nativeOrder());
        bCoord=coord.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);


        //创建顶点着色器
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        //绑定着色器
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);
        //创建片元着色器
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);


        //开始创建Opengl程序
        mProgram = GLES20.glCreateProgram();
        //将着色器方到程序中
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        //开始链接程序
        GLES20.glLinkProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {

        GLES20.glViewport(0,0,width,height);

        int w=mBitmap.getWidth();
        int h=mBitmap.getHeight();
        float sWH=w/(float)h;
        float sWidthHeight=width/(float)height;
        if(width>height){
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight*sWH,sWidthHeight*sWH, -1,1, 3, 7);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight/sWH,sWidthHeight/sWH, -1,1, 3, 7);
            }
        }else{
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1/sWidthHeight*sWH, 1/sWidthHeight*sWH,3, 7);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH/sWidthHeight, sWH/sWidthHeight,3, 7);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler,1,false,mMVPMatrix,0);

        //获取顶点坐标句柄
        glHPosition=GLES20.glGetAttribLocation(mProgram,"vPosition");
        GLES20.glEnableVertexAttribArray(glHPosition);

       //获取纹理坐标句柄
        glHCoordinate=GLES20.glGetAttribLocation(mProgram,"vCoordinate");
        GLES20.glEnableVertexAttribArray(glHCoordinate);

        //获取纹理句柄
        glHTexture=GLES20.glGetUniformLocation(mProgram,"vTexture");
        GLES20.glUniform1i(glHTexture, 0);
        createTexture();

        //传入顶点坐标
        GLES20.glVertexAttribPointer(glHPosition,2,GLES20.GL_FLOAT,false,0,bPos);
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate,2,GLES20.GL_FLOAT,false,0,bCoord);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);

//        glPhotoView.requestRender();
    }


    private int createTexture(){
        int[] texture=new int[1];
        if(mBitmap!=null&&!mBitmap.isRecycled()){
            //生成纹理
            GLES20.glGenTextures(1,texture,0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            return texture[0];
        }
        return 0;
    }
}
