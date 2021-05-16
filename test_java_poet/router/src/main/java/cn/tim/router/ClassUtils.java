package cn.tim.router;


import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

public class ClassUtils {
    public static Set<String> getFileNameByPackageName(Application context,
                                                       final String packageName)
            throws PackageManager.NameNotFoundException {
        // 拿到APK当中的dex地址
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);

        if(paths.size() > 0){
            for(String path: paths){
                DexFile dexFile;
                try{
                    // 加载apk中的dex并且遍历获得所有packageName的类
                    dexFile = new DexFile(path);
                    Enumeration<String> dexEntries = dexFile.entries();
                    while(dexEntries.hasMoreElements()){
                        //com.xxx.xxx.xxx 整个APK中所有的类
                        String className = dexEntries.nextElement();
                        if(className.startsWith(packageName)){
                            classNames.add(className);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return classNames;
    }

    private static List<String> getSourcePaths(Application context)
            throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        ApplicationInfo applicationInfo = manager.getApplicationInfo(context.getPackageName(), 0);
        List<String> sourcePaths = new ArrayList<>();
        // 当前应用的APK文件
        sourcePaths.add(applicationInfo.sourceDir);
        // instant run
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if(null != applicationInfo.splitSourceDirs){
                sourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            }
        }
        return sourcePaths;
    }
}

