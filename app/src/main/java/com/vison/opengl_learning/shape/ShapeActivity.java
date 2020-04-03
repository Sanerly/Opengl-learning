package com.vison.opengl_learning.shape;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.vison.opengl_learning.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * class
 */
public class ShapeActivity extends AppCompatActivity implements GLSurfaceView.Renderer {


    //顶点着色器
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 vMatrix;"+
                    "varying  vec4 vColor;"+
                    "attribute vec4 aColor;"+
                    "void main() {" +
                    "  gl_Position = vMatrix*vPosition;" +
                    "  vColor=aColor;"+
                    "}";

    //片元着色器
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


//    private final String vertexShaderCode =
//            "attribute vec4 vPosition;" +
//                    "uniform mat4 vMatrix;"+
//                    "void main() {" +
//                    "  gl_Position = vMatrix*vPosition;" +
//                    "}";
//
//    private final String fragmentShaderCode =
//            "precision mediump float;" +
//                    "uniform vec4 vColor;" +
//                    "void main() {" +
//                    "  gl_FragColor = vColor;" +
//                    "}";


    //顶点坐标缓冲区
    private FloatBuffer vertexBuffer;

    //顶点坐标
    static float triangleCoords[] = {
            -0.5f,  0.5f, 0.0f, // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f, // bottom right
            0.5f,  0.5f, 0.0f  // top right
    };
    private static final int COORDS_PER_VERTEX = 3;
    //顶点个数
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    //顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节


    //颜色缓冲区
    private FloatBuffer colorBuffer;


    //设置颜色，依次为红绿蓝和透明通道
//    float[] color = {1.0f, 0.0f, 0.0f, 0.5f}; //白色
    float[] color; //白色

//    private  float color[] = {
//            0.0f, 0.3f, 0.0f, 0.2f ,
//            0.5f, 0.5f, 0.0f, 0.1f,
//            0.0f, 0.0f, 0.5f, 0.2f,
//            0.5f, 0.0f, 0.5f, 0.2f
//    };

    //索引
//    private static short index[]={
//            0,1,2,0,2,3
//    };
    private ShortBuffer indexBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMatrixHandler;

    //矩阵
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];



    private float radius=1.0f;
    private int n=360;  //切割份数

    private float[] shapePos; //圆形顶点

    private float height=0.0f;


    private float[]  createPositions(){
        ArrayList<Float> data=new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(height);
        float angDegSpan=360f/n;
        for(float i=0;i<360+angDegSpan;i+=angDegSpan){
            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(height);
        }
        float[] f=new float[data.size()];
        for (int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }
        return f;
    }


    private float[] createPositionColor(){
        List<Float> data=new ArrayList<>();
        for (int i = 0; i < shapePos.length; i++) {
            data.add((float) (0.0f+Math.random()*1.0f));
            data.add((float) (0.0f+Math.random()*1.0f));
            data.add((float) (0.0f+Math.random()*1.0f));
            data.add(1.0f);
        }
        float[] floats=new float[data.size()];
        for (int i=0;i<floats.length;i++){
            floats[i]=data.get(i);
        }
        return floats;
    }





    @BindView(R.id.shape_gl_view)
    ShapeGlView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape);
        ButterKnife.bind(this);
        init();
    }

    private void init() {

        //设置opengl版本
        glView.setEGLContextClientVersion(2);
        //设置渲染器
        glView.setRenderer(this);

        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //1、初始化顶点着色器坐标和片元着色器坐标
        shapePos= createPositions();
        color=createPositionColor();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //将背景设置为灰色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //申请缓冲区空间
//        ByteBuffer buffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
//        buffer.order(ByteOrder.nativeOrder());
//        //将坐标顶点数据转换为FloatBuffer，用以传入给OpenGL ES程序

        ByteBuffer bb=ByteBuffer.allocateDirect(shapePos.length*4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(shapePos);
        vertexBuffer.position(0);

        ByteBuffer dd = ByteBuffer.allocateDirect(color.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer=dd.asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

//        ByteBuffer cc = ByteBuffer.allocateDirect(index.length * 2);
//        cc.order(ByteOrder.nativeOrder());
//        indexBuffer=cc.asShortBuffer();
//        indexBuffer.put(index);
//        indexBuffer.position(0);

        //创建顶点着色器
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);
        //创建片元着色器
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        //创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        //将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        //将片元着色器加入到程序
        GLES20.glAttachShader(mProgram, fragmentShader);
        //连接到着色器程序
        GLES20.glLinkProgram(mProgram);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
//        GLES20.glViewport(0, 0, width, height);

        //计算宽高比
        float ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);

        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);


//        //获取片元着色器的vColor成员的句柄
//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
//        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        //获取顶点着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        //启用句柄
        GLES20.glEnableVertexAttribArray(mColorHandle);
        //准备三角形的颜色数据
        GLES20.glVertexAttribPointer(mColorHandle,4,GLES20.GL_FLOAT,false,0,colorBuffer);
        //绘制三角形
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        //索引法绘制正方形
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES,index.length, GLES20.GL_UNSIGNED_SHORT,indexBuffer);
        //绘制圆形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, shapePos.length/3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }


    @Override
    protected void onResume() {
        super.onResume();
        glView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
    }
}
