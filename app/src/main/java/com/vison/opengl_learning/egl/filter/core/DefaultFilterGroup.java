package com.vison.opengl_learning.egl.filter.core;

import com.vison.opengl_learning.egl.filter.base.AFilter;
import com.vison.opengl_learning.egl.filter.base.AFilterGroup;
import com.vison.opengl_learning.egl.filter.type.FilterIndex;
import com.vison.opengl_learning.egl.filter.type.FilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/30 19:02
 * @Description: 默认实时美颜滤镜组
 */
public class DefaultFilterGroup extends AFilterGroup {

    // 颜色层
    private static final int ColorIndex = 0;



    public DefaultFilterGroup() {
        this(initFilters());
    }

    private DefaultFilterGroup(List<AFilter> filters) {
        mFilters = filters;
    }


    private static List<AFilter> initFilters() {
        List<AFilter> filters = new ArrayList<>();
        filters.add(ColorIndex, FilterManager.getFilter(FilterType.SOURCE));
        return filters;
    }

    @Override
    public void changeFilter(FilterType type) {
        FilterIndex index = FilterManager.getIndex(type);
        if (index == FilterIndex.ColorIndex) {
            onChangeColorFilter(type);
        }
    }


    /**
     * 切换颜色滤镜
     *
     * @param type
     */
    private void onChangeColorFilter(FilterType type) {
        if (mFilters != null) {
            mFilters.get(ColorIndex).release();
            mFilters.set(ColorIndex, FilterManager.getFilter(type));
            // 设置宽高
            mFilters.get(ColorIndex).onInputSizeChanged(mImageWidth, mImageHeight);
            mFilters.get(ColorIndex).onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }


}
