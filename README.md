# 深入浅出Android Gradle
## 为什么写这个？
讲Gradle的文章和书很多，讲Groovy的文章和书也很多，但是在Android中如何使用Gradle和Groovy，感觉没有一篇文章和书能够讲透，总觉得使用起来模模糊糊，云里雾里。所以，想把平时研究和应用Gradle的一些要点和心得记录下来，既是方便自己，也是方便大家。
- [x] 仓库中的工程是为了方便我经常调试和研究Gradle所搭建，可能有点乱，仅做参考。
- [x] README中记录了我学习和研究Gradle的一些要点和心得，持续更新中...

## Gradle是什么
“Gradle is an open-source build automation system that builds upon the concepts of Apache Ant and Apache Maven and introduces a Groovy-based domain-specific language (DSL) instead of the XML form used by Apache Maven for declaring the project configuration.[1] Gradle uses a directed acyclic graph ("DAG") to determine the order in which tasks can be run.”——维基百科对Gradle的定义。

翻译过来就是：“Gradle是一个基于Apache Ant和Apache Maven概念的项目自动化构建开源工具。它使用一种基于Groovy的特定领域语言(DSL)来声明项目设置，抛弃了基于XML的各种繁琐配置。”

还是很难懂。难怪有些人觉得gradle文件看起来很痛苦，看不懂。我的理解是：gradle既是脚本，也是代码。可以像脚本那些执行，而且每一行脚本，都可以理解成执行了相应的对象中的一个方法。但是由于闭包的存在使得有些代码的执行顺序跟定义的顺序不一致。这样一来，Gradle写起来和读起来都像配置文件，实际上是一系列代码，要以代码的角度来阅读和编写Gradle。这样一来，Gradle就要好理解得多。

## AS中的Gradle Build Script
1. 工程根目录下的setting.gradle和build.gradle
2. 每个Module目录下都有一个build.gradle

## 执行顺序
Gradle执行的时候遵循如下顺序：

1. 首先解析settings.gradle来获取模块信息，这是初始化阶段
2. 然后配置每个模块，配置的时候并不会执行task
3. 配置完了以后，有一个重要的回调project.afterEvaluate，它表示所有的模块都已经配置完了，可以准备执行task了
4. 执行指定的task。

## gradle对象
每个gradle脚本都可以访问gradle对象，比如在setting.gradle下执行：

```
println("gradle name: " + gradle.class.name)
```
会输出：

```
gradle name: org.gradle.invocation.DefaultGradle_Decorated
```
这些gradle对象都是接口Gradle的实现：

```
public interface Gradle extends PluginAware {
    ....
}
```
比如有个叫DefaultGradle的实现：

```
public class DefaultGradle extends AbstractPluginAware implements GradleInternal {
...
}
```
通过gradle对象可以获取Gradle的相关信息和添加一些钩子。


## setting对象
每一个setting.gradle都对应一个setting对象

在setting.gradle中可以访问到setting对象：

```
println("setting.gradle: " + settings)
println("setting.gradle: " + this)
```
输出：

```
setting.gradle: settings 'android-gradle-study'
setting.gradle: settings 'android-gradle-study'
```

## project对象
每一个build.gradle都对应一个project对象。

- [x] 在project的build.gradle中，获取到的project对象是root project
- [x] 在module的build.gradle中，获取到的project对象是module project

比如在root project的build.gradle中和在module的build.gradle中执行如下代码：

```
println("Root build.gradle: " + project)
println("Root build.gradle: " + this)
```
root project输出：

```
Root build.gradle: root project 'android-gradle-study'
Root build.gradle: root project 'android-gradle-study'
```
module project输出：

```
App build.gradle: project ':app'
App build.gradle: project ':app'
```
但是，root project和module project的类型是一样的，都是DefaultProject。

## Root build.gradle
### buildscript
buildscript用于配置插件的classpath，插件跟引用的aar不同，插件不会编译到apk中，插件只是用于构建。设置repositories告诉gradle classpath的仓库地址，dependencies用于配置具体的classpath。
### allprojects
allprojects进行的配置会应用到当前的project以及其所有module，这里配置的repositories会在当前的project以及其所有module都生效。

## Gradle Wrapper
Gradle Wrapper，就是对gradle的一层包装。在AS右侧的Gradle面板直接运行task等同于直接用gradle运行task。但是由于gradle有不同的版本，所以希望使用统一的gradle版本进行构建，避免由于gradle版本不统一带来的问题。

AS的工程下有两个脚本：gradlew和gradlew.bat。包括还有一个文件夹：gradle/wrapper/这个文件夹里面的gradle-wrapper.properties决定了我们使用gradlew的时候调用的gradle版本：

```
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-4.6-all.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```
gradlew下载的gradle一般放在如下的位置：

```
~/.gradle/wrapper/dists
```

## Groovy 闭包
闭包就是一段代码块，以参数的形式传递给其他函数，其他函数可以执行这个闭包。这有点像java里面listener的作用。
示例：

```
task doSomething1 << {
    runEach({
        println(it)
    })
}

def runEach(closure) {
    for (int i in 1..10) {
        closure(i)
    }
}
```
由于Groovy里面方法调用可以省略括号，并且如果方法的最后一个参数是闭包，可以放到方法外面，因此我们经常可以方法调用闭包的写法类似这样：

```
runEach {
    println(it)
}
```

## Task
Gradle里面创建task的方法有很多，我最喜欢的一种是任务名字+闭包配置的方式：

```
task demoTask {
    description 'demo task'
}
```
查看源码可以发现，task都是通过TaskContainer创建的：

```
public Task task(String task) {
    return this.taskContainer.create(task);
}
```
一个task由多个action组成，task真正要完成的任务都放在这些action里面。

在源码AbstractTask中，我们可以找到一个存放action的list：

```
public abstract class AbstractTask implements TaskInternal, DynamicObjectAware {
    private static final Logger BUILD_LOGGER = Logging.getLogger(Task.class);
    private static final ThreadLocal<TaskInfo> NEXT_INSTANCE = new ThreadLocal<TaskInfo>();

    private final ProjectInternal project;

    private final String name;

    private List<ContextAwareTaskAction> actions;

    ...
}
```
我们可以通过doFirst和doLast来添加action，doFirst把action添加到最前面，doLast添加action最后执行：

```
task demoTask {
    description 'demo task'
    doFirst {
        println("this action will run first!")
    }
    doLast {
        println("this action will run last!")
    }
}
```
查看Task的源码可以发现，这两个操作无非是把action添加到action list的队头和队尾：

```
@Override
public Task doFirst(final Closure action) {
    hasCustomActions = true;
    if (action == null) {
        throw new InvalidUserDataException("Action must not be null!");
    }
    taskMutator.mutate("Task.doFirst(Closure)", new Runnable() {
        public void run() {
            getTaskActions().add(0, convertClosureToAction(action, "doFirst {} action"));
        }
    });
    return this;
}

@Override
public Task doLast(final Closure action) {
    hasCustomActions = true;
    if (action == null) {
        throw new InvalidUserDataException("Action must not be null!");
    }
    taskMutator.mutate("Task.doLast(Closure)", new Runnable() {
        public void run() {
            getTaskActions().add(convertClosureToAction(action, "doLast {} action"));
        }
    });
    return this;
}
```

### <<操作符
因为Task的doLast用的很多，所以使用了一种doLast的短标记形式，这就是<<操作符：

```
task doSomething1 << {
    //doLast的action
}
```
<<对应的源码是Task的leftShift方法：

```
@Override
public Task leftShift(final Closure action) {
    DeprecationLogger.nagUserWith("The Task.leftShift(Closure) method has been deprecated and is scheduled to be removed in Gradle 5.0. Please use Task.doLast(Action) instead.");

    hasCustomActions = true;
    if (action == null) {
        throw new InvalidUserDataException("Action must not be null!");
    }
    taskMutator.mutate("Task.leftShift(Closure)", new Runnable() {
        public void run() {
            getTaskActions().add(taskMutator.leftShift(convertClosureToAction(action, "doLast {} action")));
        }
    });
    return this;
}
```
这也进一步说明了doLast和<<操作符是一致的。

## Plugin
### Plugin用来干嘛？
研究一项技术之前如果不弄清楚这项技术用来干嘛，能带来什么好处，那就是为了研究技术而研究技术，没任何卵用。

Plugin说白了就是可以把你之前写在gradle文件中的那些代码，提取出来，放到一个插件中。这个插件可以放到一个仓库中，可以下载下来使用。说白了，就是提高你那段代码的复用性。

### 使用Plugin
apply plugin:[your-plugin]

当你调用这句话的时候，你写在你的自定义Plugin中的apply方法就会执行。就这么简单。

在使用plugin之前，需要在root project的build.gradle中指定classpath和相应的repo仓库地址：

```
buildscript {
    repositories {
        //定义repo仓库地址
    }
    dependencies {
        //定义classpath
    }
}
```
设置buildscript的目的是为了让gradle能知道去哪找到你的插件。

### 自定义Plugin
自定义Plugin很简单，直接参考代码里面的plugin module即可。

自定义的Plugin可以采用Groovy编写，也可以采用java编写。采用Groovy编写可以使用Groovy的一些特性，比如使用闭包。

你甚至可以像示例中的plugin module那样，同时使用groovy和java。并且可以在一个module，或者一个jar中包含多个plugin。

配置一个plugin module可以参考示例代码，其中有几个关键点：
- [x] apply plugin

```
apply plugin: 'groovy'
apply plugin: 'java'
```
- [x] 配置sourceSets

```
sourceSets {
    main {
        groovy {
            srcDir 'src/main/groovy'
        }

        java {
            srcDir 'src/main/java'
        }
    }
}
```
- [x] 配置dependencies

```
dependencies {
    compile gradleApi()
    compile localGroovy()
    compile 'com.android.tools.build:gradle:3.2.1'
}
```
- [x] 配置META-INF
1. 在src/main/下新建目录：resources/META-INF/gradle-plugins/
2. 在这个目录下新建文件:xx.properties，这个xx就是plugin id，也就是用户使用这个插件的时候apply plugin:的那个id
3. 在xx.properties中定义：implementation-class=[class name of your plugin]

### 调试自定义Plugin
如何调试自定义Plugin呢，我一般喜欢用本地repo调试，如何把你的plugin上传到本地repo呢，只需要在你的plugin模块的build.gradle中加入以下代码：

```
apply plugin: 'maven'

group = 'com.xxx.xxx'
version = '0.0.1'

uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: uri('../repo'))
        }
    }
}
```
这个时候AS的gradle面板会多一个uploadArchives任务，执行这个任务便可以把plugin上传到工程根目录的repo目录下。

## android-apt和annotationProcessor
### 作用
APT(Annotation Processing Tool)是一种处理注释的工具,它对源代码文件进行检测找出其中的Annotation，对这些Annotation进行处理。常用的处理方式包括根据这些注解自动生成一些java源文件或者java class文件。
### android-apt
android-apt是annotationProcessor出现之前的apt框架。要使用android-apt需要添加如下的代码：
#### 添加android-apt到Project下的build.gradle中

```
//配置在Project下的build.gradle中
buildscript {
    repositories {
      mavenCentral()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}
```
#### 在Module中build.gradle的配置（以dagger为例）

```
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    apt 'com.squareup.dagger:dagger-compiler:1.1.0'
}
```
### annotationProcessor
annotationProcessor也是一种APT工具，他是google开发的内置框架，不需要引入，可以直接在module的build.gradle文件中使用（以butterknife为例）：

```
dependencies {
    annotationProcessor 'com.jakewharton:butterknife-compiler:8.4.0'
}
```
### 自定义注解处理器
创建一个java module，编写一个类，继承AbstractProcessor。并且重写其中的process方法：

```java
public class MyProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        ...
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        ...
        return true;
    }
    
    @Override
    public Set getSupportedAnnotationTypes() {
        Set annotataions = new LinkedHashSet();
        annotataions.add(MyAnnotation.class.getCanonicalName());
        return annotataions;
    }
    
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
```
#### Export Processor
有两种方法Export Processor：
##### 手动暴露
1. 在 processors 库的 main 目录下新建 resources 资源文件夹
2. 在 resources文件夹下建立 META-INF/services 目录文件夹
3. 在 META-INF/services 目录文件夹下创建 javax.annotation.processing.Processor 文件
4. 在 javax.annotation.processing.Processor 文件写入注解处理器的全称，包括包路径

##### 使用AutoService
AutoService注解处理器是Google开发的，用来生成 META-INF/services/javax.annotation.processing.Processor 文件的，你只需要在你定义的注解处理器上添加 @AutoService(Processor.class) 就可以了，简直不能再方便了。
- [x] 添加依赖

```
dependencies {
    implementation 'com.google.auto.service:auto-service:1.0-rc2'
}
```
- [x] 用@AutoService注解Processor

```java
@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {
    // ...
}
```

## Transform
### Transform用来干嘛
还是那句话，研究任何一个新技术之前都要先弄明白这个技术是用来干嘛的，不然就毫无意义。

前面介绍了Plugin，但是apply plugin是发生在配置阶段，还没有涉及到真正的构建过程。如果我们想在构建过程中做一些事，比如我们想拿到编译时产生的Class文件，并在生成Dex之前做一些处理。

Transform就是用来应对这种场景的。

### 另外一种处理方式
Transform API 是在1.5.0-beta1版开始使用的。在此之前，如果我们想拿到编译时产生的Class文件，并在生成Dex之前做一些处理，常用的方式是注册project的afterEvaluate方法，在这个方法中拿到一些构建过程中的task，并在这个task中注入一些action来完成：

```
project.afterEvaluate {
    System.out.println(TAG + "execute afterEvaluate: " + project)
    def extension = project.extensions.findByType(AppExtension.class)
    extension.applicationVariants.all { variant ->
        String variantName = capitalize(variant.getName())
        Task mergeJavaResTask = project.tasks.findByName(
                "transformResourcesWithMergeJavaResFor" + variantName)
        System.out.println(TAG + "mergeJavaResTask: " + mergeJavaResTask)
        mergeJavaResTask.doLast {
            System.out.println(TAG + "mergeJavaResTask.doLast execute")
        }
    }
}
```

### 使用Transform
#### 定义Transform
自定义Transform，继承自Transform类：

```
class AgsTransform extends Transform {

    final String TAG = "[AgsTransform]"

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        System.out.println(TAG + "start transform")
        super.transform(transformInvocation)
    }

    @Override
    String getName() {
        return AgsTransform.simpleName
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }
}
```

##### 输入的类型

```
@Override
Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS
}
```
输入类型有两种，CLASSES和RESOURCES，在DefaultContentType指定：

```
enum DefaultContentType implements ContentType {
    /**
     * The content is compiled Java code. This can be in a Jar file or in a folder. If
     * in a folder, it is expected to in sub-folders matching package names.
     */
    CLASSES(0x01),

    /** The content is standard Java resources. */
    RESOURCES(0x02);

    private final int value;

    DefaultContentType(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
```
TransformManager中定义了一系列的类型集合：

```
public static final Set<ContentType> CONTENT_CLASS = ImmutableSet.of(CLASSES);
public static final Set<ContentType> CONTENT_JARS = ImmutableSet.of(CLASSES, RESOURCES);
public static final Set<ContentType> CONTENT_RESOURCES = ImmutableSet.of(RESOURCES);
public static final Set<ContentType> CONTENT_NATIVE_LIBS =
        ImmutableSet.of(NATIVE_LIBS);
public static final Set<ContentType> CONTENT_DEX = ImmutableSet.of(ExtendedContentType.DEX);
public static final Set<ContentType> DATA_BINDING_ARTIFACT =
        ImmutableSet.of(ExtendedContentType.DATA_BINDING);
public static final Set<ContentType> DATA_BINDING_BASE_CLASS_LOG_ARTIFACT =
        ImmutableSet.of(ExtendedContentType.DATA_BINDING_BASE_CLASS_LOG);
```

##### 输入文件所属的范围

```
@Override
Set<? super QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT
}
```
getScopes()用来指明自定的Transform的输入文件所属的范围, 定义在Scope中:

```
enum Scope implements ScopeType {
    /** Only the project content */
    PROJECT(0x01),
    /** Only the sub-projects. */
    SUB_PROJECTS(0x04),
    /** Only the external libraries */
    EXTERNAL_LIBRARIES(0x10),
    /** Code that is being tested by the current variant, including dependencies */
    TESTED_CODE(0x20),
    /** Local or remote dependencies that are provided-only */
    PROVIDED_ONLY(0x40),

    /**
     * Only the project's local dependencies (local jars)
     *
     * @deprecated local dependencies are now processed as {@link #EXTERNAL_LIBRARIES}
     */
    @Deprecated
    PROJECT_LOCAL_DEPS(0x02),
    /**
     * Only the sub-projects's local dependencies (local jars).
     *
     * @deprecated local dependencies are now processed as {@link #EXTERNAL_LIBRARIES}
     */
    @Deprecated
    SUB_PROJECTS_LOCAL_DEPS(0x08);

    private final int value;

    Scope(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
```
同样，TransformManager中定义了一系列的Scope集合：

```
public static final Set<ScopeType> PROJECT_ONLY = ImmutableSet.of(Scope.PROJECT);
public static final Set<Scope> SCOPE_FULL_PROJECT =
        Sets.immutableEnumSet(
                Scope.PROJECT,
                Scope.SUB_PROJECTS,
                Scope.EXTERNAL_LIBRARIES);
public static final Set<ScopeType> SCOPE_FULL_WITH_IR_FOR_DEXING =
        new ImmutableSet.Builder<ScopeType>()
                .addAll(SCOPE_FULL_PROJECT)
                .add(InternalScope.MAIN_SPLIT)
                .build();
public static final Set<ScopeType> SCOPE_FULL_LIBRARY_WITH_LOCAL_JARS =
        ImmutableSet.of(Scope.PROJECT, InternalScope.LOCAL_DEPS);
```

##### 重写transform方法
我们可以通过TransformInvocation来获取输入，也可以获取输出的功能：

```
@Override
void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
    System.out.println(TAG + "start transform")
    super.transform(transformInvocation)
    //处理输入
    System.out.println(TAG + "处理输入")
    for (TransformInput input : transformInvocation.inputs) {
        input.jarInputs.parallelStream().forEach(new Consumer<JarInput>() {
            @Override
            void accept(JarInput jarInput) {
                File file = jarInput.getFile()
                JarFile jarFile = new JarFile(file)
                Enumeration<JarEntry> entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement()
                    System.out.println(TAG + "JarEntry: " + entry)
                }
            }
        })
    }
    //处理输出
    System.out.println(TAG + "处理输出")
    File dest = transformInvocation.outputProvider.getContentLocation(
            "output_name",
            TransformManager.CONTENT_CLASS,
            TransformManager.PROJECT_ONLY,
            Format.DIRECTORY)
}
```

#### 注册Transform
在Plugin中注册：

```
def extension = project.extensions.findByType(AppExtension.class)
System.out.println(TAG + extension)
extension.registerTransform(new AgsTransform())
```
