package cn.tim.annotation_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import cn.tim.annotation.ARouter;
import cn.tim.annotation.bean.RouterBean;
import cn.tim.annotation_processor.config.ProcessorConfig;
import cn.tim.annotation_processor.utils.ProcessorUtils;

/**
 * 注解处理器
 */
@AutoService(Processor.class) // 编译期绑定
@SupportedAnnotationTypes({"cn.tim.annotation.ARouter"}) // 表示我要处理那个注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)
//@SupportedOptions({"myvalue"})
@SupportedOptions({ProcessorConfig.MODULE_NAME, ProcessorConfig.APT_PACKAGE_NAME})
public class ARouterProcessor extends AbstractProcessor {

    // 操作Element的工具类，函数、类、属性都是Element
    private Elements elementsTool;

    // 类信息的工具类
    private Types typesTool;

    // 编译期打印日志
    private Messager messager;

    // Java文件生成器
    private Filer filer;

    private String moduleName;
    private String aptPackageName;
    // Path
    private Map<String, List<RouterBean>> allPathMap = new HashMap<>();
    // Group
    private Map<String, String> allGroupMap = new HashMap<>();

    // 做一些初始化工作
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementsTool = processingEnv.getElementUtils();
        typesTool = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();

        Map<String, String> options = processingEnv.getOptions();
//        String myvalue = options.get("myvalue");
//        messager.printMessage(Diagnostic.Kind.NOTE, "编译参数：myvalue = " + myvalue);

        moduleName = options.get(ProcessorConfig.MODULE_NAME);
        aptPackageName = options.get(ProcessorConfig.APT_PACKAGE_NAME);
        if(moduleName == null || aptPackageName == null){
            messager.printMessage(Diagnostic.Kind.ERROR, "APT环境出错");
        }else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT ENV OK, moduleName = " + moduleName + ", aptPackageName = " + aptPackageName);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations.isEmpty()){
            messager.printMessage(Diagnostic.Kind.NOTE, "没有发现被ARouter注解的类");
            return false; // 标注注解处理器没有工作
        }

        TypeElement activityTypeElement = elementsTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE_NAME);
        TypeMirror activityTypeMirror = activityTypeElement.asType();

        // 获取被ARouter注解的类信息
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ARouter.class);
        List<RouterBean> routerBeanList;
        for(Element element: elements){
            // 类名
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被ARouter注解的类有：" + className);
            ARouter aRouter = element.getAnnotation(ARouter.class);

            // 生成一段代码
            // generateHelloWorld();
            RouterBean routerBean = new RouterBean();
            routerBean.setGroup(aRouter.group());
            routerBean.setPath(aRouter.path());
            routerBean.setElement(element);

            // 当前Element如果是Activity
            TypeMirror elementTypeMirror = element.asType();
            if(typesTool.isSubtype(elementTypeMirror, activityTypeMirror)){
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            }else {
                throw new RuntimeException("@ARouter当前不支持此类型");
            }

            // 1、校验path、group
            if(checkRouterPath(routerBean)){
                messager.printMessage(Diagnostic.Kind.NOTE, "checkRouterPath success");

                routerBeanList = allPathMap.get(routerBean.getGroup());
                if(ProcessorUtils.isEmpty(routerBeanList)){
                    routerBeanList = new ArrayList<>();
                    routerBeanList.add(routerBean);

                    allPathMap.put(routerBean.getGroup(), routerBeanList);
                }else {
                    routerBeanList.add(routerBean);
                }
            }else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter参数格式错误");
            }
        }
        TypeElement pathType = elementsTool.getTypeElement(ProcessorConfig.ROUTER_API_PATH);
        TypeElement groupType = elementsTool.getTypeElement(ProcessorConfig.ROUTER_API_GROUP);
        try {
            createPathFile(pathType);
            createGroupFile(groupType, pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "生产代码时发生异常：" + e);
        }
        return true;
    }

    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        if(ProcessorUtils.isEmpty(allGroupMap) || ProcessorUtils.isEmpty(allPathMap)){
            return;
        }

        /*
         * Map<String, Class<? extends ARouterPath>>
         * -> Class<? extends ARouterPath> ????
         */
        ParameterizedTypeName thirdParam = ParameterizedTypeName.get(ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(ClassName.get(pathType)));
        ParameterizedTypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                thirdParam
        );
        // Method
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturn);

        /*
         * Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
         */
        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                thirdParam,
                ProcessorConfig.GROUP_VAR,
                ClassName.get(HashMap.class)
        );

        /*
         * groupMap.put("personal", ARouter$$Path$$personal.class);
         */
        Set<Map.Entry<String, String>> entries = allGroupMap.entrySet();
        for(Map.Entry<String, String> entry: entries){
            methodBuilder.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR,
                    entry.getKey(),
                    ClassName.get(aptPackageName, entry.getValue())
            );
        }

        /*
         * return groupMap;
         */
        methodBuilder.addStatement("return $N", ProcessorConfig.GROUP_VAR);
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + moduleName;
        messager.printMessage(Diagnostic.Kind.NOTE, "Group:最终生成的文件名称是：" + aptPackageName + "." + finalClassName);

        JavaFile.builder(aptPackageName,
                TypeSpec.classBuilder(finalClassName)
                    .addSuperinterface(ClassName.get(groupType))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodBuilder.build())
                    .build())
                .build()
                .writeTo(filer);
    }

    private void createPathFile(TypeElement pathTypeElement) throws IOException {
        if(ProcessorUtils.isEmpty(allPathMap)){
            return;
        }

        /*
         * public Map<String, RouterBean> getPathMap() {
         */
        // Map<String, RouterBean>
        ParameterizedTypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        Set<Map.Entry<String, List<RouterBean>>> entrySet = allPathMap.entrySet();
        for(Map.Entry<String, List<RouterBean>> entry: entrySet){
            /*
             * Map<String, RouterBean> pathMap = new HashMap<>();
             */
            // 方法
            MethodSpec.Builder builder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturn);
            builder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    ProcessorConfig.PATH_VAR,
                    ClassName.get(HashMap.class));
            List<RouterBean> pathList = entry.getValue();
            for(RouterBean routerBean: pathList){
                /*
                 * pathMap.put("/personal/PersonalMainActivity",
                 *                 RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
                 *                                   Order_MainActivity.class,
                 *                            "/personal/PersonalMainActivity",
                 *                           "personal"));
                 */
                builder.addStatement(
                        "$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR,
                        routerBean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.TypeEnum.class),
                        routerBean.getTypeEnum(),
                        ClassName.get((TypeElement) routerBean.getElement()),
                        routerBean.getPath(),
                        routerBean.getGroup()
                );
            }

            // return pathMap;
            builder.addStatement("return $N", ProcessorConfig.PATH_VAR);

            // ARouter$$Path$$personal
            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "Path:最终生成的文件名称是：" + aptPackageName + "." + finalClassName);
            JavaFile.builder(aptPackageName,
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathTypeElement))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(builder.build())
                            .build())
                    .build()
                    .writeTo(filer);
            allGroupMap.put(entry.getKey(), finalClassName);
        }
    }

    private boolean checkRouterPath(RouterBean routerBean) {
        // /app/MainActivity
        String path = routerBean.getPath();
        String group = routerBean.getGroup();
        if(ProcessorUtils.isEmpty(path) || !path.startsWith("/")){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter参数格式错误, path = " + path);
            return false;
        }
        if(0 == path.lastIndexOf("/")){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter参数格式错误, path = " + path);
            return false;
        }
        String groupNameFromPath = path.substring(1, path.indexOf("/", 1));

        // 处理group没写的情况
        if(!ProcessorUtils.isEmpty(group) && !group.equals(moduleName)){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter参数格式错误, path = " + path);
            return false;
        }else{
            routerBean.setGroup(groupNameFromPath);
        }
        return true;
    }

    private void generateHelloWorld() {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}