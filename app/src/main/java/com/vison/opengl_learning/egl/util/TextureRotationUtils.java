package com.vison.opengl_learning.egl.util;

/**
 * Created by cain on 17-7-26.
 */

public class TextureRotationUtils {

    public static final int CoordsPerVertex = 2;

    //顶点坐标
    public static final float CubeVertices[] = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

//    public static final float CubeVertices[] = {
//            0.0f, 0.0f,    //左上角
//            0.0f, -1.0f,   //左下角
//            1.0f, 0.0f,     //右上角
//            1.0f, -1.0f     //右下角
//    };
    //纹理坐标
    public static final float TextureVertices[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    // x轴反过来
    public static final float TextureVertices_flipx[] = {
            1.0f, 0.0f,     // 0 right bottom
            0.0f, 0.0f,     // 1 left  bottom
            1.0f, 1.0f,     // 2 right top
            0.0f, 1.0f      // 3 left  top
    };

    public static final float TextureVertices_90[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    public static final float TextureVertices_180[] = {
            1.0f, 1.0f, // right top
            0.0f, 1.0f, // left top
            1.0f, 0.0f, // right bottom
            0.0f, 0.0f, // left bottom
    };

    public static final float TextureVertices_270[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    /**
     * 索引，glDrawElements使用
     */
    public static final short[] Indices = {
            0, 1, 2,
            2, 1, 3,
    };


    private TextureRotationUtils() {}

    /**
     * 翻转
     * @param i
     * @return
     */
    private static float flip(final float i) {
        if (i == 0.0f) {
            return 1.0f;
        }
        return 0.0f;
    }
}
