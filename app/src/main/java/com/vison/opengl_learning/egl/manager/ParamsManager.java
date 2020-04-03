package com.vison.opengl_learning.egl.manager;

import android.content.Context;
import android.os.Environment;

/**
 * 管理全局参数和上下文
 * Created by cain.huang on 2017/8/8.
 */
public class ParamsManager {

    private ParamsManager() {}

    // 上下文，方便滤镜使用
    public static Context context;

    // 存储根目录
    public static  String StoragePath ="";

    // 默认相册位置
    public static  String AlbumPath = StoragePath + "/DCIM/Camera/";

    // 图片存放地址
    public static  String ImagePath = StoragePath + "/CainCamera/Image/";

    // 视频存放地址
    public static  String VideoPath = StoragePath + "/SV/Video/";

    // Gif存放地址
    public static  String GifPath = StoragePath + "/CainCamera/Gif/";

}
