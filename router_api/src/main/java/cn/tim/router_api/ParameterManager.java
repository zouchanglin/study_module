package cn.tim.router_api;


import android.app.Activity;
import android.util.LruCache;

/**
 * 参数管理器，主要是用于接收参数
 * 1、查找 OrderMainActivity
 * 2、使用 OrderMainActivity
 */
public class ParameterManager {
    private static ParameterManager instance;

    public static ParameterManager getInstance(){
        if(instance == null) {
            synchronized (ParameterManager.class){
                if(instance == null){
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }

    private ParameterManager() {
        cache = new LruCache<>(128);
    }

    // OrderMainActivity -> OrderMainActivity$$Parameter
    private LruCache<String, ParameterLoad> cache;

    private final String SUFFIX_NAME = "$$Parameter";

    public void loadParameter(Activity activity){
        String className = activity.getClass().getName();
        ParameterLoad parameterLoad = cache.get(className);
        if(parameterLoad == null) {
            try {
                Class<?> aClass = Class.forName(className + SUFFIX_NAME);
                parameterLoad = (ParameterLoad) aClass.newInstance();
                cache.put(className, parameterLoad);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterLoad.getParameter(activity);
    }
}
