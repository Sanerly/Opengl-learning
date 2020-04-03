/*
 *
 * ShaderUtils.java
 * 
 * Created by Wuwang on 2016/10/8
 */
package com.vison.opengl_learning.util;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.InputStream;

/**
 * Description:
 *
 *         //创建顶点着色器
 *         int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
 *         //绑定着色器
 *         GLES20.glShaderSource(vertexShader, vertexShaderCode);
 *         GLES20.glCompileShader(vertexShader);
 *         //创建片元着色器
 *         int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
 *         GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
 *         GLES20.glCompileShader(fragmentShader);
 *
 *
 *         //开始创建Opengl程序
 *         mProgram = GLES20.glCreateProgram();
 *         //将着色器方到程序中
 *         GLES20.glAttachShader(mProgram, vertexShader);
 *         GLES20.glAttachShader(mProgram, fragmentShader);
 *         //开始链接程序
 *         GLES20.glLinkProgram(mProgram);
 */
public class ShaderUtils {

    private static final String TAG="ShaderUtils";

    private ShaderUtils(){
    }

    public static void checkGLError(String op){
        Log.e("wuwang",op);
    }

    public static int loadShader(int shaderType, String source){
        int shader= GLES20.glCreateShader(shaderType);
        if(0!=shader){
            GLES20.glShaderSource(shader,source);
            GLES20.glCompileShader(shader);
            int[] compiled=new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS,compiled,0);
            if(compiled[0]==0){
                Log.e(TAG,"Could not compile shader:"+shaderType);
                Log.e(TAG,"GLES20 Error:"+ GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader=0;
            }
        }
        return shader;
    }

    public static int loadShader(Resources res, int shaderType, String resName){
        return loadShader(shaderType,loadFromAssetsFile(resName,res));
    }

    public static int createProgram(String vertexSource, String fragmentSource){
        int vertex=loadShader(GLES20.GL_VERTEX_SHADER,vertexSource);
        if(vertex==0)return 0;
        int fragment=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentSource);
        if(fragment==0)return 0;
        int program= GLES20.glCreateProgram();
        if(program!=0){
            GLES20.glAttachShader(program,vertex);
            checkGLError("Attach Vertex Shader");
            GLES20.glAttachShader(program,fragment);
            checkGLError("Attach Fragment Shader");
            GLES20.glLinkProgram(program);
            int[] linkStatus=new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,linkStatus,0);
            if(linkStatus[0]!= GLES20.GL_TRUE){
                Log.e(TAG,"Could not link program:"+ GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program=0;
            }
        }
        return program;
    }

    public static int createProgram(Resources res, String vertexRes, String fragmentRes){
        return createProgram(loadFromAssetsFile(vertexRes,res),loadFromAssetsFile(fragmentRes,res));
    }

    public static String loadFromAssetsFile(String fname, Resources res){
        StringBuilder result=new StringBuilder();
        try{
            InputStream is=res.getAssets().open(fname);
            int ch;
            byte[] buffer=new byte[1024];
            while (-1!=(ch=is.read(buffer))){
                result.append(new String(buffer,0,ch));
            }
        }catch (Exception e){
            return null;
        }
        return result.toString().replaceAll("\\r\\n","\n");
    }

}
