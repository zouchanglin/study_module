package cn.tim.router;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Router {
    public static final String packageName = "xxx.xxx.routers";
    private static final Router instance = new Router();

    private Router() {
    }

    public static Router getInstance(){
        return instance;
    }

    // 路由表
    private static final Map<String, Class<? extends Activity>> routersMap = new HashMap<>();

    static boolean registerByPlugin;

    private static void loadRouterMap() {
        registerByPlugin = false;
    }

    public static void init(Application application){
        loadRouterMap();
        if(registerByPlugin){
            return;
        }
        try {
            Set<String> classNames = ClassUtils.getFileNameByPackageName(application, packageName);
            for(String className: classNames){
                Class<?> cls = Class.forName(className);
                // 如果是IRouterLoad的实现类
                if(IRouterLoad.class.isAssignableFrom(cls)){
                    IRouterLoad iRouterLoad = (IRouterLoad) cls.newInstance();
                    iRouterLoad.loadInfo(routersMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 注册
     */
    public void register(String path, Class<? extends Activity> activity){
        routersMap.put(path, activity);
    }

    /**
     * 启动Activity
     */
    public void startActivity(Activity activity, String path){
        Class<? extends Activity> activityClass = routersMap.get(path);
        if(activityClass != null){
            Intent intent = new Intent(activity, activityClass);
            activity.startActivity(intent);
        }
    }
}