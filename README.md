# 深入浅出Android Gradle
## 前言
讲Gradle的文章和书很多，讲Groovy的文章和书也很多，但是在Android中如何使用Gradle和Groovy，感觉没有一篇文章和书能够讲透，总觉得使用起来模模糊糊，云里雾里，是时候好好研究一下Android Gradle了。

## 写在最前
有些人觉得gradle文件看起来很痛苦，看不懂。其实gradle既是脚本，也是代码。可以像脚本那些执行，而且每一行脚本，都可以理解成执行了相应的对象中的一个方法。但是由于闭包的存在使得有些代码的执行顺序跟定义的顺序不一致。

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