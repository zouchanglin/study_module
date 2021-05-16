package cn.tim.router_api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;


import cn.tim.annotation.bean.RouterBean;

public class RouterManager {
    private static final String TAG = "RouterManager";
    private static RouterManager instance;

    public static RouterManager getInstance(){
        if(instance == null) {
            synchronized (ParameterManager.class){
                if(instance == null){
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    private RouterManager(){
        pathLruCache = new LruCache<>(128);
        groupLruCache = new LruCache<>(128);
    }

    private String group; // 路由的组名称 order、personal、
    private String path; // 路由的路径 /order/OrderMainActivity

    private LruCache<String, ARouterPath> pathLruCache;
    private LruCache<String, ARouterGroup> groupLruCache;

    String FILE_GROUP_NAME = "ARouter$$Group$$";
    String APT_PACKAGE_NAME = "customrouter_apt";

    /**
     * 跳转（懒加载）
     * @param context context
     * @param bundleManager 参数打包器
     * @return BundleManager
     */
    public Object navigation(Context context, BundleManager bundleManager) {
        // ARouter$$Group$$order
        String groupClassName =  APT_PACKAGE_NAME + "." + FILE_GROUP_NAME + group;
        Log.i(TAG, "navigation: groupClassName = " + groupClassName);
        ARouterGroup routerGroup = groupLruCache.get(group);
        if(routerGroup == null) {
            Class<?> aClass = null;
            try {
                aClass = Class.forName(groupClassName);
                routerGroup = (ARouterGroup) aClass.newInstance();
                groupLruCache.put(group, routerGroup);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ARouterPath routerPath = pathLruCache.get(path);
        if(routerPath == null){
            Class<? extends ARouterPath> aClass = routerGroup.getGroupMap().get(group);
            try {
                routerPath = aClass.newInstance();

                pathLruCache.put(path, routerPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(routerPath.getPathMap().isEmpty()){
            throw new RuntimeException("路由表出错");
        }

        RouterBean routerBean = routerPath.getPathMap().get(path);
        if(routerBean != null){
            switch (routerBean.getTypeEnum()){
                case ACTIVITY:
                    Intent intent = new Intent(context, routerBean.getMyClass());
                    intent.putExtras(bundleManager.getBundle()); // 传参
                    context.startActivity(intent, bundleManager.getBundle());
                    break;
                default:
                    Log.e(TAG, "navigation: 其他类型暂不支持");
                    break;
            }
        }
        return null;
    }

    public BundleManager build(String path){
        if(TextUtils.isEmpty(path) || !path.startsWith("/")){
            throw new IllegalArgumentException("请正确使用path，如/order/OrderMainActivity");
        }

        // "/"
        if(path.lastIndexOf("/") == 0){
            throw new IllegalArgumentException("请正确使用path，如/order/OrderMainActivity");
        }


        String groupString = path.substring(1, path.indexOf("/", 1));
        if(TextUtils.isEmpty(groupString)){
            throw new IllegalArgumentException("请正确使用path，如/order/OrderMainActivity");
        }

        this.path = path;
        this.group = groupString;

        return new BundleManager();
    }
}
