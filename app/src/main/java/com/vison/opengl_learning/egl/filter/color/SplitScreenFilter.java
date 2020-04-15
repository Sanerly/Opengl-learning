package com.vison.opengl_learning.egl.filter.color;

import android.util.Log;

import com.vison.opengl_learning.egl.filter.base.AFilter;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/4/13 18:34
 * @Description: 分屏
 */
public class SplitScreenFilter extends AFilter {


    private static final String FRAGMENT_SHADER =
            "precision mediump float;                                       \n" +
                    "varying vec2 vCoordinate;                              \n" +
                    "uniform sampler2D uTexture;                            \n" +
                    "void main() {                                          \n" +
                    "     vec2 uv = vCoordinate.xy;                         \n" +
                    "if (uv.y >= 0.0 && uv.y < 1.0/3.0) {                   \n" +
                    "            uv.y = uv.y + 1.0/3.0;                     \n" +
                    "     } else if(uv.y >2.0/3.0){                         \n" +
                    "uv.y = uv.y - 1.0/3.0;                                 \n" +
                    "     }                                                 \n" +
                    "    gl_FragColor = texture2D(uTexture, uv);            \n" +
                    "}                                                      \n";

    public SplitScreenFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public SplitScreenFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
}
