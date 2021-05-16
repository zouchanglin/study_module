package cn.tim.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import cn.tim.annotation.ARouter;

// 注解处理器
@AutoService(Processor.class) // 编译期 绑定ARouterProcessor
@SupportedAnnotationTypes({"cn.tim.annotation.ARouter"}) // 指定要处理的注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("myvalue") // 接收外界传进来的值
public class ARouterProcessor extends AbstractProcessor {

    // 操作Element的工具类，类、函数、属性都是Element
    private Elements elementTool;

    // 类信息的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // Messager 用来打印日志相关信息
    private Messager messager; // Gradle日志里输出

    // 文件生成器，生成类或者资源等，都需要由Filer来完成
    private Filer filer;

    // 做初始化相关工作
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // 工具类的初始化
        elementTool = processingEnv.getElementUtils();
        typeTool = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        String myValue = processingEnv.getOptions().get("myvalue");
        // messager.printMessage(myvalue); error
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>> myvalue = " + myValue);
    }

    // 编译期干活的函数
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // ...
        if(annotations.size() == 0){
            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现被@ARouter注解的地方呀");
            return false; // false表示注解处理器根本就没有机会处理，还没有干活
        }

        // 获取被 ARouter注解的 "类节点信息"
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
        for(Element element: elements){
            // 获取类节点，获取包节点（cn.tim.xxx）
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();
            // 获取简单类名，例如：MainActivity
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被@ARetuer注解的类有：" + className);

            // 拿注解
            ARouter aRouter = element.getAnnotation(ARouter.class);

            // 使用JavaPoet生成Java文件【HelloWorld示例】
            //generateHelloWorldClassFunc();
            // 生成Activity对应的XXXRouter.java文件
            generateRouterClassFunc(aRouter.path(), className, element, packageName);
        }

        return true; // 处理完成，后续如果没有变动，service就不会处理了
    }

    /**
     * public class MainActivity$$$$$ARouter {
     *   public static Class findTargetClass(String path) {
     *     return path.equals("app/MainActivity") ? MainActivity.class : null;
     *   }
     * }
     * @param className className
     */
    private void generateRouterClassFunc(String path, String className,
                                         Element element, String packageName) {
        // 定义要给类名动态变化
        String finalClassName = className + "$$$$$$$$$ARouter";

        // 1、方法
        MethodSpec findTargetClass = MethodSpec.methodBuilder("findTargetClass")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Class.class)
                .addParameter(String.class, "path")
                .addStatement("return path.equals($S) ? $T.class : null" ,
                        path,
                        ClassName.get((TypeElement)element)) // element -> MainActivity
                .build();

        // 2、类
        TypeSpec myClass = TypeSpec.classBuilder(finalClassName)
                .addMethod(findTargetClass)
                .addModifiers(Modifier.PUBLIC)
                .build();

        messager.printMessage(Diagnostic.Kind.NOTE, "get package name is " + packageName);
        // 3、包
        JavaFile javaFilePackage = JavaFile.builder(packageName, myClass).build();

        // 开始生成Java文件
        try {
            javaFilePackage.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            // 如果生成失败需要打出日志
            messager.printMessage(Diagnostic.Kind.NOTE,  finalClassName + "创建失败");
        }
    }


    /*
     * 生成一个最简单的HelloWorld代码
     * package com.example.hello_world;
     *
     * public final class HelloWorld {
     *   public static void main(String[] args) {
     *     System.out.println("Hello, JavaPoet!");
     *   }
     * }
     */
    private void generateHelloWorldClassFunc() {
        // 1、方法
        MethodSpec mainMethod = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        // 2、类
        TypeSpec helloWorldClass = TypeSpec.classBuilder("HelloWorld")
                .addMethod(mainMethod)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();

        // 3、包
        JavaFile javaFilePackage = JavaFile.builder("com.example.hello_world",
                helloWorldClass).build();

        // 开始生成文件
        try {
            javaFilePackage.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "HelloWorld.java生成失败...");
        }

        System.out.println(void.class);
        System.out.println(Void.class);
    }
}