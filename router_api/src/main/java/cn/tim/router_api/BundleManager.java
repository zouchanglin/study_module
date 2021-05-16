package cn.tim.router_api;

import android.content.Context;
import android.os.Bundle;

/**
 * 负责数据打包
 */
public class BundleManager {
    private Bundle bundle = new Bundle();

    public Bundle getBundle(){
        return bundle;
    }

    public BundleManager withString(String key, String value){
        bundle.putString(key, value);
        return this;
    }

    public BundleManager withBoolean(String key, boolean value){
        bundle.putBoolean(key, value);
        return this;
    }

    public BundleManager withInt(String key, int value){
        bundle.putInt(key, value);
        return this;
    }

    public BundleManager withBundle(Bundle bundle){
        this.bundle = bundle;
        return this;
    }

    //...添加其他类型

    // 跳转
    public Object navigation(Context context){
        return RouterManager.getInstance().navigation(context, this);
    }
}
