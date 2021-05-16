package cn.tim.annotation_processor.utils;

import java.util.Collection;
import java.util.Map;

public class ProcessorUtils {
    public static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }

    public static boolean isEmpty(Collection<?> collection){
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map){
        return map == null || map.isEmpty();
    }
}
