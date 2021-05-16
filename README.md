# 组件化的意义

1、开发效率高、维护性强

2、高内聚、低耦合

![](https://yqfile.alicdn.com/img_a0d81d6a6c254707004c4da2c17aa632.png)

# Android组件化工程搭建

四个部分：App壳子工程、common组件、order组件【订单页】、personal组件【个人中心】

0、验证Gradle的顺序

1、定义+引入公共配置

```groovy
// 加载项目的gradle的时候，就引入app_config.gradle
apply from : 'app_config.gradle'
```

2、测试公共配置的有效性

3、不同的插件不同的效果

```groovy
if(isRelease){
    apply plugin: 'com.android.library' // 集成化模式
}else {
    apply plugin: 'com.android.application' // 组件化状态
}

// 组件化状态才需要applicationId
if(!isRelease){
    applicationId "cn.tim.order"
}
```

4、Java文件的排除与

```groovy
// 源集 —— 用来设置Java目录或者资源目录
sourceSets {
  main {
    if(!isRelease){
      // 如果是组件化模式，需要单独运行时
      manifest.srcFile 'src/main/debug/AndroidManifest.xml'
    }else {
      // 集成化模式，整个项目打包
      manifest.srcFile 'src/main/AndroidManifest.xml'
      java {
        // release 时 debug 目录下文件不需要合并到主工程
        exclude '**/debug/**'
      }
    }
  }
}
```

> 注意组件化工程的文件命名与打印日志规范：
>
> order -> OrderMainActivity、order_main_activity.xml、order/OrderMainActivity/onCreate..
>
> Common -> BaseApplication、BaseNetManager...

# JavaPoet与APT

APT(Annotation Processing Tool)注解处理器。

ButterKnife、Dagger、EventBus等都用到了APT。



自定义注解-> 代码（1、手动一行一行的写 2、使用JavaPoet通过面向对象的思想进行编码）

## 1、使用JavaPoet生成HelloWorld

注解本身的gradle：

```groovy
// 控制台中文设置UTF-8
tasks.withType(JavaCompile){
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
```

注解处理器引入依赖：

```groovy
plugins {
    id 'java-library'
}

dependencies {
    implementation fileTree(dir: 'libs', includes: ['*.jar'])

    // 编译时期进行注解处理
    annotationProcessor 'com.google.auto.service:auto-service:1.0-rc4'
    compileOnly 'com.google.auto.service:auto-service:1.0-rc4'

    // 帮助我们通过类调用的方式来生成Java代码[JavaPoet]
    implementation 'com.squareup:javapoet:1.10.0'

    // 依赖于注解
    implementation project(':annotation')
}

// 控制台中文设置UTF-8
tasks.withType(JavaCompile){
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
```

## 2、APT实现基于注解的View注入

实现基于注解的View，代替项目中的`findByView`。这里仅仅是学习怎么用APT，如果真的想用DI框架，推荐使用ButterKnife，功能全面。

1、annotation module创建@DIActivity、@DIView注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface DIActivity {
    
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DIView {
    int value() default 0;
}
```

创建DIProcessor方法

```java
@AutoService(Processor.class)
public class DIProcessor extends AbstractProcessor {
    private Elements elementUtils;
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // 规定需要处理的注解
        return Collections.singleton(DIActivity.class.getCanonicalName());
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("DIProcessor");
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(DIActivity.class);
        for (Element element : elements) {
            // 判断是否Class
            TypeElement typeElement = (TypeElement) element;
            List<? extends Element> members = elementUtils.getAllMembers(typeElement);
            MethodSpec.Builder bindViewMethodSpecBuilder = MethodSpec.methodBuilder("bindView")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(TypeName.VOID)
                    .addParameter(ClassName.get(typeElement.asType()), "activity");
            for (Element item : members) {
                DIView diView = item.getAnnotation(DIView.class);
                if (diView == null){
                    continue;
                }
                bindViewMethodSpecBuilder.addStatement(String.format("activity.%s = (%s) activity.findViewById(%s)",item.getSimpleName(),ClassName.get(item.asType()).toString(),diView.value()));
            }
            TypeSpec typeSpec = TypeSpec.classBuilder("DI" + element.getSimpleName())
                    .superclass(TypeName.get(typeElement.asType()))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(bindViewMethodSpecBuilder.build())
                    .build();
            JavaFile javaFile = JavaFile.builder(getPackageName(typeElement), typeSpec).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}
```

使用DIActivity

```java
@DIActivity
public class MainActivity extends Activity {
    @DIView(R.id.text)
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DIMainActivity.bindView(this);
        textView.setText("Hello World!");
    }
}
```

实际上就是通过apt生成以下代码

```java
public final class DIMainActivity extends MainActivity {
 public static void bindView(MainActivity activity) {
   activity.textView = (android.widget.TextView) activity.findViewById(R.id.text);
 }
}
```

## 3、JavaPoet常用方法总结

常用Element子类

1. TypeElement：类
2. ExecutableElement：成员方法
3. VariableElement：成员变量

通过包名和类名获取TypeName

```java
TypeName targetClassName = ClassName.get(“PackageName”, “ClassName”);
```

通过Element获取TypeName

```java
TypeName type = TypeName.get(element.asType());
```

获取TypeElement的包名

```java
String packageName = processingEnv.getElementUtils().getPackageOf(type).getQualifiedName().toString();
```

获取TypeElement的所有成员变量和成员方法

```java
List<? extends Element> members = processingEnv.getElementUtils().getAllMembers(typeElement);
```

## 4、生成我们想要的目标代码

```java
例如：Personal Path：
// 这就是要用 APT 动态生成的代码
public class ARouter$$Path$$personal implements ARouterPath {

    @Override
    public Map<String, RouterBean> getPathMap() {
        Map<String, RouterBean> pathMap = new HashMap<>();

        pathMap.put("/personal/PersonalMainActivity",
                RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
                                  Order_MainActivity.class,
                           "/personal/PersonalMainActivity",
                          "personal"));
        return pathMap;
    }
}

例如：Personal Group：
// 这就是要用 APT 动态生成的代码
public class ARouter$$Group$$personal implements ARouterGroup {

    @Override
    public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        groupMap.put("personal", ARouter$$Path$$personal.class);
        return groupMap;
    }
}
```

## 5、编译器传递参数

```groovy
javaCompileOptions {
  annotationProcessorOptions{
    arguments = [myvalue: 'hello javapoet']
  }
}
```

拿到参数：

```java
......
@SupportedOptions("myvalue")
public class ARouterProcessor extends AbstractProcessor {
    // 做一些初始化工作
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        ......
        Map<String, String> options = processingEnv.getOptions();
        String myvalue = options.get("myvalue");
        messager.printMessage(Diagnostic.Kind.NOTE, "编译参数:myvalue = " + myvalue);
    }
}
```

## 6、补充：公共依赖库的引入

```groovy
dependencies {
    app_dependencies.each {k, v ->
        // api方式引入，其他模块在引入common时才会生效
        api v
        println('引入依赖: ' + k + " -> " + v)
    }
}
```

# 完成参数管理器

1、制定参数传递的标准

2、JavaPoet  + APT生成模板代码

```java
public class OrderMainActivity$$Parameter implements ParameterGet {
  @Override
  public void getParameter(Object targetParameter) {
    Order_MainActivity t = (Order_MainActivity)target;
      
    t.name = t.getIntent().getStringExtra("name");
    t.age = t.getIntent().getIntExtra("age");
    ....
  }
}
```

3.ParameterManager的编写：参数管理器

# 完成路由管理器

发送端

```java
RouterManager.getInstance()
    .build("/order/OrderMainActivity")
    .withString("name", "张三")
    .withString("age", 20)
    .navigation(this);
```

目标端

```java
@Parameter
String name;

@Parameter
int age;

ParameterManager.getInstance().loadParameter(this);
```

