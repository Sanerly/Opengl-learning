package com.vison.opengl_learning.photo;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/8 12:11
 * @Description: 类描述
 */
public enum Filter {

    NONE(0, new float[]{0.0f, 0.0f, 0.0f}),
    GRAY(1, new float[]{0.299f, 0.587f, 0.114f}),
    COOL(2, new float[]{0.2f, 0.0f, 0.0f}),
    WARM(2, new float[]{0.1f, 0.1f, 0.0f}),
    BLUR(2, new float[]{0.0f, 0.0f, 0.5f}),
    MAGN(4, new float[]{0.0f, 0.0f, 0.4f});


    private int vChangeType;
    private float[] data;

    Filter(int vChangeType, float[] data) {
        this.vChangeType = vChangeType;
        this.data = data;
    }

    public int getType() {
        return vChangeType;
    }

    public float[] data() {
        return data;
    }
}
