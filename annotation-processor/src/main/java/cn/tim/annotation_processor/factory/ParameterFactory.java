package cn.tim.annotation_processor.factory;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;


import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import cn.tim.annotation.Parameter;
import cn.tim.annotation_processor.config.ProcessorConfig;
import cn.tim.annotation_processor.utils.ProcessorUtils;


public class ParameterFactory {
    private MethodSpec.Builder method;

    private ClassName className;

    private Messager messager;

    private ParameterFactory(Builder builder){
        this.messager = builder.messager;
        this.className = builder.className;

        method = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }

    public void addFirstLineCode(){
        method.addStatement("$T t = ($T) "+ ProcessorConfig.PARAMETER_TARGET_NAME, className, className);
    }

    // name\age\time
    public void buildOtherLineCode(Element element){
        TypeMirror typeMirror = element.asType();
        int type = typeMirror.getKind().ordinal();
        String fieldName = element.getSimpleName().toString();
        // 获取注解的值
        String value = element.getAnnotation(Parameter.class).name();
        value = ProcessorUtils.isEmpty(value) ? fieldName : value;

        String finalValue = "t." + fieldName;

        String methodContent = finalValue + " = t.getIntent().";

        if(type == TypeKind.INT.ordinal()){
            // t.name = t.getIntent().getStringExtra("name");
            methodContent += "getIntExtra($S, " + finalValue + ")";
        }else if(type == TypeKind.BOOLEAN.ordinal()){
            methodContent += "getBooleanExtra($S, " + finalValue + ")";
        }else {
            if(typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING_PACKAGE_NAME)){
                methodContent += "getStringExtra($S)";
            }
        }

        if(methodContent.endsWith(")")){
            method.addStatement(methodContent, value);
        }else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前仅支持String、int、boolean");
        }
    }

    public MethodSpec build() {
        return method.build();
    }

    public static class Builder {
        private Messager messager;

        private ClassName className;

        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec){
            this.parameterSpec = parameterSpec;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParameterFactory build(){
            if(parameterSpec == null){
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if(className == null){
                throw new IllegalArgumentException("方法中的className为空");
            }

            if(messager == null){
                throw new IllegalArgumentException("message为空，messager用于错误报告，不能为空");
            }

            return new ParameterFactory(this);
        }
    }
}
