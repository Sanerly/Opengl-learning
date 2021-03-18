package com.vison.opengl_learning;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.vison.opengl_learning.egl.EGLActivity;
import com.vison.opengl_learning.en.EncoderActivity;
import com.vison.opengl_learning.photo.FilterActivity;
import com.vison.opengl_learning.shape.CubeActivity;
import com.vison.opengl_learning.shape.ShapeActivity;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {


    public static String SAVE_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        new RxPermissions(this).request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    public void startActivity(Class<?> clz) {
        Intent intent = new Intent(this, clz);
        startActivity(intent);
    }

    @OnClick({R.id.btn_shape, R.id.btn_cube, R.id.btn_photo, R.id.btn_av, R.id.btn_encode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_shape:
                startActivity(ShapeActivity.class);
                break;
            case R.id.btn_cube:
                startActivity(CubeActivity.class);
                break;
            case R.id.btn_photo:
                startActivity(FilterActivity.class);
                break;
            case R.id.btn_av:
                startActivity(EGLActivity.class);
                break;
            case R.id.btn_encode:
                startActivity(EncoderActivity.class);
                break;
        }
    }

    ;
}
