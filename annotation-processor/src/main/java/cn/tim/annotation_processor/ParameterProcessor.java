package cn.tim.annotation_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import cn.tim.annotation.Parameter;
import cn.tim.annotation_processor.config.ProcessorConfig;
import cn.tim.annotation_processor.factory.ParameterFactory;
import cn.tim.annotation_processor.utils.ProcessorUtils;


/**
 * 注解处理器
 */
@AutoService(Processor.class) // 编译期绑定
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE}) // 表示我要处理那个注解
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParameterProcessor extends AbstractProcessor {
    // 操作Element的工具类，函数、类、属性都是Element
    private Elements elementsTool;

    // 类信息的工具类
    private Types typesTool;

    // 编译期打印日志
    private Messager messager;

    // Java文件生成器
    private Filer filer;

    // K-V
    // Key: 类（OrderMainActivity） Value：属性集合
    private final Map<TypeElement, List<Element>> parameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementsTool = processingEnv.getElementUtils();
        typesTool = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(!ProcessorUtils.isEmpty(annotations)){
            Set<? extends Element> elementsSet = roundEnv.getElementsAnnotatedWith(Parameter.class);
            if(!ProcessorUtils.isEmpty(elementsSet)){
                // 找被注释的属性，并且以KV的形式存放
                for(Element element: elementsSet){
                    // 找到父节点，是一个类
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                    if (parameterMap.containsKey(enclosingElement)) {
                        parameterMap.get(enclosingElement).add(element);
                    }else {
                        ArrayList<Element> fieldList = new ArrayList<>();
                        fieldList.add(element);
                        parameterMap.put(enclosingElement, fieldList);
                    }
                }

                // 生成类文件
                if(ProcessorUtils.isEmpty(parameterMap)) return false;
                TypeElement activityType = elementsTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE_NAME);
                TypeElement parameterType = elementsTool.getTypeElement(ProcessorConfig.ROUTER_API_PARAMETER_NAME);

                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_TARGET_NAME).build();
                Set<Map.Entry<TypeElement, List<Element>>> entrySet = parameterMap.entrySet();
                for (Map.Entry<TypeElement, List<Element>> entry : entrySet) {
                    TypeElement typeElement = entry.getKey(); // MainActivity
                    if(!typesTool.isSubtype(typeElement.asType(), activityType.asType())){
                        throw new RuntimeException("@Parameter注解目前只支持Activity");
                    }

                    // MainActivity
                    ClassName className = ClassName.get(typeElement);

                    ParameterFactory parameterFactory = new ParameterFactory.Builder(parameterSpec)
                            .setMessager(messager)
                            .setClassName(className)
                            .build();

                    parameterFactory.addFirstLineCode();

                    // 循环遍历属性，给方法加内容
                    for (Element element : entry.getValue()) {
                        parameterFactory.buildOtherLineCode(element);
                    }

                    // 生成类文件
                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE, "生成的APT参数类文件: " + className.packageName() + "." + finalClassName);

                    try {
                        JavaFile.builder(className.packageName(),
                                TypeSpec.classBuilder(finalClassName)
                                        .addSuperinterface(ClassName.get(parameterType))
                                        .addModifiers(Modifier.PUBLIC)
                                        .addMethod(parameterFactory.build())
                                        .build())
                                .build()
                                .writeTo(filer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
}
