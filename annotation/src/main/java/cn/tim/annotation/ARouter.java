package cn.tim.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // 作用域在类上
@Retention(RetentionPolicy.SOURCE) // 编译期生效
public @interface ARouter {
    String path();

    String group() default ""; // app\order\personal
}