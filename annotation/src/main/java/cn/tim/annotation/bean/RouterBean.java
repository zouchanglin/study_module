package cn.tim.annotation.bean;

import javax.lang.model.element.Element;

public class RouterBean {
    // 方便以后扩展
    public enum TypeEnum{
        ACTIVITY
    }

    private TypeEnum typeEnum;

    private Element element;

    private Class<?> myClass;

    private String path; // 路径

    private String group; // 分组


    public RouterBean() {
    }

    public RouterBean(TypeEnum typeEnum, Element element, Class<?> myClass, String path, String group) {
        this.typeEnum = typeEnum;
        this.element = element;
        this.myClass = myClass;
        this.path = path;
        this.group = group;
    }

    public static RouterBean create(TypeEnum typeEnum, Class<?> clazz, String path, String group){
        return new RouterBean(typeEnum, clazz, path, group);
    }

    public RouterBean(TypeEnum typeEnum, Class<?> myClass, String path, String group) {
        this.typeEnum = typeEnum;
        this.myClass = myClass;
        this.path = path;
        this.group = group;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getMyClass() {
        return myClass;
    }

    public void setMyClass(Class<?> myClass) {
        this.myClass = myClass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "RouterBean{" +
                "path='" + path + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
