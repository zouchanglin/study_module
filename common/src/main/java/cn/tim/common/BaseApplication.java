package cn.tim.common;

import android.app.Application;
import android.util.Log;

import cn.tim.common.config.BaseConfig;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(BaseConfig.TAG, "common/BaseApplication onCreate: ");
    }
}
