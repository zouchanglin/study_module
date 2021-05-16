package cn.tim.router_api;

/**
 * 参数传递
 */
public interface ParameterLoad {

    /**
     * 目标对象.属性名 = getIntent().属性类型 -》 完成赋值操作
     * @param targetParameter 目标对象，如OrderActivity
     */
    void getParameter(Object targetParameter);
}
