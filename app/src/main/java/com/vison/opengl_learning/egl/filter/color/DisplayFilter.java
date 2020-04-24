package com.vison.opengl_learning.egl.filter.color;

import com.vison.opengl_learning.egl.filter.base.AFilter;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/27 20:08
 * @Description: 原图
 */
public class DisplayFilter extends AFilter {

    public DisplayFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER_2D);
    }

    public DisplayFilter(String vertex, String fragment) {
        super(vertex, fragment);
    }
}
