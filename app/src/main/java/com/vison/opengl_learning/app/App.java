package com.vison.opengl_learning.app;

import android.app.Application;
import android.content.Context;

/**
 * @Author: Sanerly
 * @CreateDate: 2020/3/18 19:07
 * @Description: 类描述
 */
public class App extends Application {
    public static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
    }
}
