package cn.tim.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) // 作用域在属性上
@Retention(RetentionPolicy.SOURCE)
public @interface Parameter {

    // 如果不填写，变量名就是key
    String name() default "";
}
