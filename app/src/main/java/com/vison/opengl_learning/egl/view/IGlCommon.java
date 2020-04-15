package com.vison.opengl_learning.egl.view;

import com.vison.opengl_learning.egl.filter.type.FilterType;
import com.vison.opengl_learning.egl.filter.type.ScaleType;

import java.nio.ByteBuffer;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/4/12 18:54
 * @Description: 类描述
 */
public interface IGlCommon {

    void setBuffer(ByteBuffer buffer, int width, int height);

    void setRecording(boolean recording);

    void callTakePicture(boolean takePicture, String picturePath);

    void setFilterType(FilterType type);

    void setZoomScale(float scale);
}
